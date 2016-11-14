/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.tool.findscu;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.SAXWriter;
import org.dcm4che3.net.*;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.ExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StringUtils;

/**
 * The findscu application implements a Service Class User (SCU) for the
 * Query/Retrieve, the Modality Worklist Management, the Unified Worklist and
 * Procedure Step, the Hanging Protocol Query/Retrieve and the Color Palette
 * Query/Retrieve Service Class. findscu only supports query functionality using
 * the C-FIND message. It sends query keys to an Service Class Provider (SCP)
 * and waits for responses.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class FindSCU {

    public static enum InformationModel {
        PatientRoot(UID.PatientRootQueryRetrieveInformationModelFIND, "STUDY"),
        StudyRoot(UID.StudyRootQueryRetrieveInformationModelFIND, "STUDY"),
        PatientStudyOnly(UID.PatientStudyOnlyQueryRetrieveInformationModelFINDRetired, "STUDY"),
        MWL(UID.ModalityWorklistInformationModelFIND, null),
        UPSPull(UID.UnifiedProcedureStepPullSOPClass, null),
        UPSWatch(UID.UnifiedProcedureStepWatchSOPClass, null),
        HangingProtocol(UID.HangingProtocolInformationModelFIND, null),
        ColorPalette(UID.ColorPaletteQueryRetrieveInformationModelFIND, null);

        final String cuid;
        final String level;

        InformationModel(String cuid, String level) {
            this.cuid = cuid;
            this.level = level;
       }

        public void adjustQueryOptions(EnumSet<QueryOption> queryOptions) {
            if (level == null) {
                queryOptions.add(QueryOption.RELATIONAL);
                queryOptions.add(QueryOption.DATETIME);
            }
        }

        public String getCuid() {
            return cuid;
        }
    }

    private static ResourceBundle rb =
        ResourceBundle.getBundle("org.dcm4che3.tool.findscu.messages");
    private static SAXTransformerFactory saxtf;

    private Device device = new Device("findscu");
    private ApplicationEntity ae = new ApplicationEntity("FINDSCU");
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private int priority;
    private int cancelAfter;
    private InformationModel model;
    private static String[] modelUIDandTS;
    
    private File outDir;
    private DecimalFormat outFileFormat;
    private int[] inFilter;
    private final Attributes keys = new Attributes();

    private boolean catOut = false;
    private boolean xml = false;
    private boolean xmlIndent = false;
    private boolean xmlIncludeKeyword = true;
    private boolean xmlIncludeNamespaceDeclaration = false;
    private File xsltFile;
    private Templates xsltTpls;
    private OutputStream out;

    private Association as;
    private final AtomicInteger totNumMatches = new AtomicInteger();
    
    private long tStartCFind;

    public FindSCU() throws IOException {
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
    }
    public FindSCU(ApplicationEntity appEntity) throws IOException {
        this.ae = appEntity;
        this.device = this.ae.getDevice();

    }
    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public final void setInformationModel(InformationModel model, String[] tss,
            EnumSet<QueryOption> queryOptions) {
       this.model = model;
       rq.addPresentationContext(new PresentationContext(1, model.cuid, tss));
       if (!queryOptions.isEmpty()) {
           model.adjustQueryOptions(queryOptions);
           rq.addExtendedNegotiation(new ExtendedNegotiation(model.cuid, 
                   QueryOption.toExtendedNegotiationInformation(queryOptions)));
       }
       if (model.level != null)
           addLevel(model.level);
    }

    public void addLevel(String s) {
        keys.setString(Tag.QueryRetrieveLevel, VR.CS, s);
    }

    public final void setCancelAfter(int cancelAfter) {
        this.cancelAfter = cancelAfter;
    }

    public final void setOutputDirectory(File outDir) {
        outDir.mkdirs();
        this.outDir = outDir;
    }

    public final void setOutputFileFormat(String outFileFormat) {
        this.outFileFormat = new DecimalFormat(outFileFormat);
    }

    public final void setXSLT(File xsltFile) {
        this.xsltFile = xsltFile;
    }

    public final void setXML(boolean xml) {
        this.xml = xml;
    }

    public final void setXMLIndent(boolean indent) {
        this.xmlIndent = indent;
    }

    public final void setXMLIncludeKeyword(boolean includeKeyword) {
        this.xmlIncludeKeyword = includeKeyword;
    }

    public final void setXMLIncludeNamespaceDeclaration(
            boolean includeNamespaceDeclaration) {
        this.xmlIncludeNamespaceDeclaration = includeNamespaceDeclaration;
    }

    public final void setConcatenateOutputFiles(boolean catOut) {
        this.catOut = catOut;
    }

    public final void setInputFilter(int[] inFilter) {
        this.inFilter = inFilter;
    }

    private static CommandLine parseComandLine(String[] args)
                throws ParseException {
            Options opts = new Options();
            addServiceClassOptions(opts);
            addKeyOptions(opts);
            addOutputOptions(opts);
            addQueryLevelOption(opts);
            addCancelOption(opts);
            CLIUtils.addConnectOption(opts);
            CLIUtils.addBindOption(opts, "FINDSCU");
            CLIUtils.addAEOptions(opts);
            CLIUtils.addResponseTimeoutOption(opts);
            CLIUtils.addPriorityOption(opts);
            CLIUtils.addCommonOptions(opts);
            return CLIUtils.parseComandLine(args, opts, rb, FindSCU.class);
    }

    @SuppressWarnings("static-access")
    private static void addServiceClassOptions(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("name")
                .withDescription(rb.getString("model"))
                .create("M"));
        CLIUtils.addTransferSyntaxOptions(opts);
        opts.addOption(null, "model-uid", true, rb.getString("model-uid"));
        opts.addOption(null, "relational", false, rb.getString("relational"));
        opts.addOption(null, "datetime", false, rb.getString("datetime"));
        opts.addOption(null, "fuzzy", false, rb.getString("fuzzy"));
        opts.addOption(null, "timezone", false, rb.getString("timezone"));
    }

    @SuppressWarnings("static-access")
    private static void addQueryLevelOption(Options opts) {
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("PATIENT|STUDY|SERIES|IMAGE")
                .withDescription(rb.getString("level"))
                .create("L"));
   }

    @SuppressWarnings("static-access")
    private static void addCancelOption(Options opts) {
        opts.addOption(OptionBuilder
                .withLongOpt("cancel")
                .hasArg()
                .withArgName("num-matches")
                .withDescription(rb.getString("cancel"))
                .create());
    }

    @SuppressWarnings("static-access")
    private static void addKeyOptions(Options opts) {
        opts.addOption(OptionBuilder
                .hasArgs()
                .withArgName("[seq/]attr=value")
                .withValueSeparator('=')
                .withDescription(rb.getString("match"))
                .create("m"));
        opts.addOption(OptionBuilder
                .hasArgs()
                .withArgName("[seq/]attr")
                .withDescription(rb.getString("return"))
                .create("r"));
        opts.addOption(OptionBuilder
                .hasArgs()
                .withArgName("attr")
                .withDescription(rb.getString("in-attr"))
                .create("i"));
    }

    @SuppressWarnings("static-access")
    private static void addOutputOptions(Options opts) {
        opts.addOption(OptionBuilder
                .withLongOpt("out-dir")
                .hasArg()
                .withArgName("directory")
                .withDescription(rb.getString("out-dir"))
                .create());
        opts.addOption(OptionBuilder
                .withLongOpt("out-file")
                .hasArg()
                .withArgName("name")
                .withDescription(rb.getString("out-file"))
                .create());
        opts.addOption("X", "xml", false, rb.getString("xml"));
        opts.addOption(OptionBuilder
                .withLongOpt("xsl")
                .hasArg()
                .withArgName("xsl-file")
                .withDescription(rb.getString("xsl"))
                .create("x"));
        opts.addOption("I", "indent", false, rb.getString("indent"));
        opts.addOption("K", "no-keyword", false, rb.getString("no-keyword"));
        opts.addOption(null, "xmlns", false, rb.getString("xmlns"));
        opts.addOption(null, "out-cat", false, rb.getString("out-cat"));
    }
    
    public ApplicationEntity getApplicationEntity() {
        return ae;
    }

    public Connection getRemoteConnection() {
        return remote;
    }
    
    public AAssociateRQ getAAssociateRQ() {
        return rq;
    }
    
    public Association getAssociation() {
        return as;
    }

    public Device getDevice() {
        return device;
    }    
    
    public Attributes getKeys() {
        return keys;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            FindSCU main = new FindSCU();
            CLIUtils.configureConnect(main.remote, main.rq, cl);
            CLIUtils.configureBind(main.conn, main.ae, cl);
            CLIUtils.configure(main.conn, cl);
            main.remote.setTlsProtocols(main.conn.getTlsProtocols());
            main.remote.setTlsCipherSuites(main.conn.getTlsCipherSuites());
            configureServiceClass(main, cl);
            configureKeys(main, cl);
            configureOutput(main, cl);
            configureCancel(main, cl);
            main.setPriority(CLIUtils.priorityOf(cl));
            ExecutorService executorService =
                    Executors.newSingleThreadExecutor();
            ScheduledExecutorService scheduledExecutorService =
                    Executors.newSingleThreadScheduledExecutor();
            main.device.setExecutor(executorService);
            main.device.setScheduledExecutor(scheduledExecutorService);
            try {
                long t1 = System.currentTimeMillis();
                main.open();
                long t2 = System.currentTimeMillis();
                System.out.println("Association opened in "+(t2-t1)+"ms");
                List<String> argList = cl.getArgList();
                if (argList.isEmpty())
                    main.query();
                else
                    for (String arg : argList)
                        main.query(new File(arg));
            } finally {
                main.close();
                executorService.shutdown();
                scheduledExecutorService.shutdown();
            }
       } catch (ParseException e) {
            System.err.println("findscu: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("findscu: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static EnumSet<QueryOption> queryOptionsOf(FindSCU main, CommandLine cl) {
        EnumSet<QueryOption> queryOptions = EnumSet.noneOf(QueryOption.class);
        if (cl.hasOption("relational"))
            queryOptions.add(QueryOption.RELATIONAL);
        if (cl.hasOption("datetime"))
            queryOptions.add(QueryOption.DATETIME);
        if (cl.hasOption("fuzzy"))
            queryOptions.add(QueryOption.FUZZY);
        if (cl.hasOption("timezone"))
            queryOptions.add(QueryOption.TIMEZONE);
        return queryOptions;
    }

    private static void configureOutput(FindSCU main, CommandLine cl) {
        if (cl.hasOption("out-dir"))
            main.setOutputDirectory(new File(cl.getOptionValue("out-dir")));
        main.setOutputFileFormat(cl.getOptionValue("out-file", "000'.dcm'"));
        main.setConcatenateOutputFiles(cl.hasOption("out-cat"));
        main.setXML(cl.hasOption("X"));
        if (cl.hasOption("x")) {
            main.setXML(true);
            main.setXSLT(new File(cl.getOptionValue("x")));
        }
        main.setXMLIndent(cl.hasOption("I"));
        main.setXMLIncludeKeyword(!cl.hasOption("K"));
        main.setXMLIncludeNamespaceDeclaration(cl.hasOption("xmlns"));
    }

    private static void configureCancel(FindSCU main, CommandLine cl) {
        if (cl.hasOption("cancel"))
            main.setCancelAfter(Integer.parseInt(cl.getOptionValue("cancel")));
    }

    private static void configureKeys(FindSCU main, CommandLine cl) {
        CLIUtils.addEmptyAttributes(main.keys, cl.getOptionValues("r"));
        CLIUtils.addAttributes(main.keys, cl.getOptionValues("m"));
        if (cl.hasOption("L"))
            main.addLevel(cl.getOptionValue("L"));
        if (cl.hasOption("i"))
            main.setInputFilter(CLIUtils.toTags(cl.getOptionValues("i")));
    }

    private static void configureServiceClass(FindSCU main, CommandLine cl) throws ParseException {
        main.setInformationModel(informationModelOf(cl), 
                CLIUtils.transferSyntaxesOf(cl), queryOptionsOf(main, cl));
        if (cl.hasOption("model-uid")) {
            String cuidAndTS = cl.getOptionValue("model-uid");
            modelUIDandTS = StringUtils.split(cuidAndTS, '|');
            main.rq.addPresentationContext(new PresentationContext(3, modelUIDandTS[0], UID.ImplicitVRLittleEndian));
            main.rq.addPresentationContext(new PresentationContext(5, modelUIDandTS[0], UID.ExplicitVRLittleEndian));
            for (int i = 1, pcid = 7 ; i < modelUIDandTS.length ; i++) {
                main.rq.addPresentationContext(new PresentationContext(pcid, modelUIDandTS[0], modelUIDandTS[i]));
                pcid += 2;
            }
        }
    }

    private static InformationModel informationModelOf(CommandLine cl) throws ParseException {
        try {
            return cl.hasOption("M")
                    ? InformationModel.valueOf(cl.getOptionValue("M"))
                    : InformationModel.StudyRoot;
        } catch(IllegalArgumentException e) {
            throw new ParseException(
                    MessageFormat.format(
                            rb.getString("invalid-model-name"),
                            cl.getOptionValue("M")));
        }
    }

    public void open() throws IOException, InterruptedException,
            IncompatibleConnectionException, GeneralSecurityException {
        as = ae.connect(remote, rq);
    }

    public void close() throws IOException, InterruptedException {
        if (as != null && as.isReadyForDataTransfer()) {
            as.waitForOutstandingRSP();
            as.release();
        }
        SafeClose.close(out);
        out = null;
    }

    public void query(File f) throws IOException, InterruptedException {
        Attributes attrs;
        DicomInputStream dis = null;
        try {
            dis = new DicomInputStream(f);
            attrs = dis.readDataset(-1, -1);
            if (inFilter != null) {
                attrs = new Attributes(inFilter.length + 1);
                attrs.addSelected(attrs, inFilter);
            }
        } finally {
            SafeClose.close(dis);
        }
        attrs.addAll(keys);
        query(attrs);
    }

    
   public void query() throws IOException, InterruptedException {
        query(keys);
    }
   
    private void query(Attributes keys) throws IOException, InterruptedException {
         DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {

            int cancelAfter = FindSCU.this.cancelAfter;
            int numMatches;

            @Override
            public void onDimseRSP(Association as, Attributes cmd,
                    Attributes data) {
                System.out.println("####### DimesRSP received after "+(System.currentTimeMillis()-tStartCFind)+"ms");
                super.onDimseRSP(as, cmd, data);
                int status = cmd.getInt(Tag.Status, -1);
                if (Status.isPending(status)) {
                    FindSCU.this.onResult(data);
                    ++numMatches;
                    if (cancelAfter != 0 && numMatches >= cancelAfter)
                        try {
                            cancel(as);
                            cancelAfter = 0;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
        };

        query(keys, rspHandler);
    }

    public void query( DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        query(keys, rspHandler);
    }
    
    private void query(Attributes keys, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        String cuid = model.cuid;
        if (modelUIDandTS != null) {
            Set<String> ts = as.getTransferSyntaxesFor(modelUIDandTS[0]);
            if (ts.size() > 0) {
                cuid = modelUIDandTS[0];
            }
        }
        tStartCFind = System.currentTimeMillis();
        as.cfind(cuid, priority, keys, null, rspHandler);
        long t2 = System.currentTimeMillis();
        System.out.println("C-FIND Request done in "+(t2-tStartCFind)+"ms!");
    }
    
    private void onResult(Attributes data) {
        int numMatches = totNumMatches.incrementAndGet();
        if (outDir == null) 
            return;

        try {
            if (out == null) {
                File f = new File(outDir, fname(numMatches));
                out = new BufferedOutputStream(
                        new FileOutputStream(f));
            }
            if (xml) {
                writeAsXML(data, out);
            } else {
                DicomOutputStream dos = 
                        new DicomOutputStream(out, UID.ImplicitVRLittleEndian);
                dos.writeDataset(null, data);
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            SafeClose.close(out);
            out = null;
        } finally {
            if (!catOut) {
                SafeClose.close(out);
                out = null;
            }
        }
    }

    private String fname(int i) {
        synchronized (outFileFormat) {
            return outFileFormat.format(i);
        }
    }

    private void writeAsXML(Attributes attrs, OutputStream out) throws Exception {
        TransformerHandler th = getTransformerHandler();
        th.getTransformer().setOutputProperty(OutputKeys.INDENT,
                xmlIndent ? "yes" : "no");
        th.setResult(new StreamResult(out));
        SAXWriter saxWriter = new SAXWriter(th);
        saxWriter.setIncludeKeyword(xmlIncludeKeyword);
        saxWriter.setIncludeNamespaceDeclaration(xmlIncludeNamespaceDeclaration);
        saxWriter.write(attrs);
    }

    private TransformerHandler getTransformerHandler() throws Exception {
        SAXTransformerFactory tf = saxtf;
        if (tf == null)
            saxtf = tf = (SAXTransformerFactory) TransformerFactory
                .newInstance();
        if (xsltFile == null)
            return tf.newTransformerHandler();

        Templates tpls = xsltTpls;
        if (tpls == null)
            xsltTpls = tpls = tf.newTemplates(new StreamSource(xsltFile));

        return tf.newTransformerHandler(tpls);
    }


}
