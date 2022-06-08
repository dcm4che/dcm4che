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
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2017
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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.*;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.io.SAXWriter;
import org.dcm4che3.net.*;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.ExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.SafeClose;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 *
 */
public class FindSCU {

    public static enum InformationModel {
        PatientRoot(UID.PatientRootQueryRetrieveInformationModelFind, "STUDY"),
        StudyRoot(UID.StudyRootQueryRetrieveInformationModelFind, "STUDY"),
        PatientStudyOnly(UID.PatientStudyOnlyQueryRetrieveInformationModelFind, "STUDY"),
        MWL(UID.ModalityWorklistInformationModelFind, null),
        UPSPull(UID.UnifiedProcedureStepPull, null),
        UPSWatch(UID.UnifiedProcedureStepWatch, null),
        UPSQuery(UID.UnifiedProcedureStepQuery, null),
        HangingProtocol(UID.HangingProtocolInformationModelFind, null),
        ColorPalette(UID.ColorPaletteQueryRetrieveInformationModelFind, null);

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
    }

    private static ResourceBundle rb =
        ResourceBundle.getBundle("org.dcm4che3.tool.findscu.messages");
    private static SAXTransformerFactory saxtf;

    private final Device device = new Device("findscu");
    private final ApplicationEntity ae = new ApplicationEntity("FINDSCU");
    private final Connection conn = new Connection();
    private final Connection remote = new Connection();
    private final AAssociateRQ rq = new AAssociateRQ();
    private int priority;
    private int cancelAfter;
    private InformationModel model;

    private File outDir;
    private DecimalFormat outFileFormat;
    private int[] inFilter;
    private Attributes keys = new Attributes();

    private boolean catOut = false;
    private boolean xml = false;
    private boolean xmlIndent = false;
    private boolean xmlIncludeKeyword = true;
    private boolean xmlIncludeNamespaceDeclaration = false;
    private File xsltFile;
    private Templates xsltTpls;
    private OutputStream out;

    private Association as;
    private AtomicInteger totNumMatches = new AtomicInteger();

    public FindSCU() throws IOException {
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.addConnection(conn);
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
            CLIUtils.addBindClientOption(opts, "FINDSCU");
            CLIUtils.addAEOptions(opts);
            CLIUtils.addSendTimeoutOption(opts);
            CLIUtils.addResponseTimeoutOption(opts);
            CLIUtils.addPriorityOption(opts);
            CLIUtils.addCommonOptions(opts);
            return CLIUtils.parseComandLine(args, opts, rb, FindSCU.class);
    }

    @SuppressWarnings("static-access")
    private static void addServiceClassOptions(Options opts) {
        opts.addOption(Option.builder("M")
                .hasArg()
                .argName("name")
                .desc(rb.getString("model"))
                .build());
        CLIUtils.addTransferSyntaxOptions(opts);
        opts.addOption(null, "relational", false, rb.getString("relational"));
        opts.addOption(null, "datetime", false, rb.getString("datetime"));
        opts.addOption(null, "fuzzy", false, rb.getString("fuzzy"));
        opts.addOption(null, "timezone", false, rb.getString("timezone"));
    }

    @SuppressWarnings("static-access")
    private static void addQueryLevelOption(Options opts) {
        opts.addOption(Option.builder("L")
                .hasArg()
                .argName("PATIENT|STUDY|SERIES|IMAGE")
                .desc(rb.getString("level"))
                .build());
   }

    @SuppressWarnings("static-access")
    private static void addCancelOption(Options opts) {
        opts.addOption(Option.builder()
                .longOpt("cancel")
                .hasArg()
                .argName("num-matches")
                .desc(rb.getString("cancel"))
                .build());
    }

    @SuppressWarnings("static-access")
    private static void addKeyOptions(Options opts) {
        opts.addOption(Option.builder("m")
                .hasArgs()
                .argName("[seq.]attr=value")
                .desc(rb.getString("match"))
                .build());
        opts.addOption(Option.builder("r")
                .hasArgs()
                .argName("[seq.]attr")
                .desc(rb.getString("return"))
                .build());
        opts.addOption(Option.builder("i")
                .hasArgs()
                .argName("attr")
                .desc(rb.getString("in-attr"))
                .build());
    }

    @SuppressWarnings("static-access")
    private static void addOutputOptions(Options opts) {
        opts.addOption(Option.builder()
                .longOpt("out-dir")
                .hasArg()
                .argName("directory")
                .desc(rb.getString("out-dir"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("out-file")
                .hasArg()
                .argName("name")
                .desc(rb.getString("out-file"))
                .build());
        opts.addOption("X", "xml", false, rb.getString("xml"));
        opts.addOption(Option.builder("x")
                .longOpt("xsl")
                .hasArg()
                .argName("xsl-file")
                .desc(rb.getString("xsl"))
                .build());
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
                main.open();
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
        as = ae.connect(conn, remote, rq);
    }

    public void close() throws IOException, InterruptedException {
        if (as != null && as.isReadyForDataTransfer()) {
            as.waitForOutstandingRSP();
            as.release();
        }
        SafeClose.close(out);
        out = null;
    }

    public void query(File f) throws Exception {
        Attributes attrs;
        String filePath = f.getPath();
        String fileExt = filePath.substring(filePath.lastIndexOf(".")+1).toLowerCase();
        DicomInputStream dis = null;
        try {
            attrs = fileExt.equals("xml")
                    ? SAXReader.parse(filePath)
                    : new DicomInputStream(f).readDataset();
            if (inFilter != null) {
                attrs = new Attributes(inFilter.length + 1);
                attrs.addSelected(attrs, inFilter);
            }
        } finally {
            SafeClose.close(dis);
        }
        mergeKeys(attrs, keys);
        query(attrs);
    }

    private static class MergeNested implements Attributes.Visitor {
        private final Attributes keys;

        MergeNested(Attributes keys) {
            this.keys = keys;
        }

        @Override
        public boolean visit(Attributes attrs, int tag, VR vr, Object val) {
            if (isNotEmptySequence(val)) {
                Object o = keys.remove(tag);
                if (isNotEmptySequence(o))
                    ((Sequence) val).get(0).addAll(((Sequence) o).get(0));
            }
            return true;
        }

        private static boolean isNotEmptySequence(Object val) {
            return val instanceof Sequence && !((Sequence) val).isEmpty();
        }
    }

    static void mergeKeys(Attributes attrs, Attributes keys) {
        try {
            attrs.accept(new MergeNested(keys), false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        attrs.addAll(keys);
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
        as.cfind(model.cuid, priority, keys, null, rspHandler);
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
        if (tpls == null);
            xsltTpls = tpls = tf.newTemplates(new StreamSource(xsltFile));

        return tf.newTransformerHandler(tpls);
    }


}
