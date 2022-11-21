/*
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  J4Care.
 *  Portions created by the Initial Developer are Copyright (C) 2015-2018
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 */

package org.dcm4che3.tool.wadows;

import jakarta.activation.DataHandler;
import jakarta.xml.ws.soap.AddressingFeature;
import jakarta.xml.ws.soap.MTOMFeature;
import org.apache.commons.cli.*;
import org.dcm4che3.data.*;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.xdsi.*;
import org.dcm4che3.xdsi.RetrieveDocumentSetRequestType.DocumentRequest;
import org.dcm4che3.xdsi.RetrieveImagingDocumentSetRequestType.TransferSyntaxUIDList;
import org.dcm4che3.xdsi.RetrieveRenderedImagingDocumentSetRequestType.StudyRequest.SeriesRequest.RenderedDocumentRequest.ContentTypeList;
import org.dcm4che3.xdsi.RetrieveDocumentSetResponseType.DocumentResponse;
import org.dcm4che3.xdsi.RetrieveRenderedImagingDocumentSetResponseType.RenderedDocumentResponse;
import org.dcm4che3.xdsi.RetrieveRenderedImagingDocumentSetRequestType.StudyRequest.SeriesRequest.RenderedDocumentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since June 2018
 */

public class WadoWS {
    private static final Logger LOG = LoggerFactory.getLogger(WadoWS.class);
    private static final ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.wadows.messages");
    private static final String DEFAULT_CONTENT_TYPE = "image/jpeg";
    private static final String DEFAULT_TRANSFER_SYNTAX_UID = UID.ExplicitVRLittleEndian;
    private static final String DEFAULT_REPOSITORY_UNIQUE_ID = "1.3.6.1.4.1.21367.13.80.110"; //change this later by looking into what is used for XDS tools
    private static ImagingDocumentSource service;
    private static String url;
    private static boolean rendered;
    private static String rows;
    private static String columns;
    private static String windowWidth;
    private static String windowCenter;
    private static String imageQuality;
    private static String frameNo;
    private static Attributes kosAttr;
    private static String[] tsuids = { DEFAULT_TRANSFER_SYNTAX_UID };
    private static String[] contentTypes = { DEFAULT_CONTENT_TYPE };
    private static String repositoryUniqueID;
    private static File outDir;
    private static int count;

    public WadoWS() {}

    public static void main(String[] args) {
        try {
            WadoWS wadoWS = new WadoWS();
            init(parseComandLine(args), wadoWS);
            wadoWS.wado();
        } catch (ParseException e) {
            System.err.println("wadows: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (StringIndexOutOfBoundsException e) {
            System.err.println("wadows: " + e.getMessage());
            System.err.println(rb.getString("study"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("wadows: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private void setOutputDirectory(File dir) {
        dir.mkdirs();
        outDir = dir;
    }

    private static CommandLine parseComandLine(String[] args) throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        opts.addOption(Option.builder()
                .longOpt("out-dir")
                .hasArg()
                .argName("directory")
                .desc(rb.getString("out-dir"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("url")
                .longOpt("url")
                .desc(rb.getString("url"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("uid")
                .longOpt("repository-unique-id")
                .desc(rb.getString("repository-unique-id"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("rendered")
                .hasArg(false)
                .desc(rb.getString("rendered"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .longOpt("rows")
                .desc(rb.getString("rows"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .longOpt("columns")
                .desc(rb.getString("columns"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .longOpt("window-width")
                .desc(rb.getString("window-width"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .longOpt("window-center")
                .desc(rb.getString("window-center"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .longOpt("image-quality")
                .desc(rb.getString("image-quality"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .longOpt("frame-number")
                .desc(rb.getString("frame-number"))
                .build());
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder()
                .longOpt("tsuid")
                .hasArgs()
                .desc(rb.getString("tsuid"))
                .build());
        group.addOption(Option.builder("t")
                .longOpt("contentType")
                .hasArgs()
                .desc(rb.getString("contentType"))
                .build());
        opts.addOptionGroup(group);
        OptionGroup reqGroup = new OptionGroup();
        reqGroup.addOption(Option.builder("f")
                .hasArg()
                .longOpt("file")
                .argName("file")
                .desc(rb.getString("file"))
                .build());
        reqGroup.addOption(Option.builder()
                .hasArg()
                .longOpt("study")
                .desc(rb.getString("study"))
                .build());
        opts.addOptionGroup(reqGroup);
        return CLIUtils.parseComandLine(args, opts, rb, WadoWS.class);
    }

    private static void init(CommandLine cl, WadoWS wadoWS) throws Exception {
        if ((url = cl.getOptionValue("url")) == null)
            throw new MissingOptionException("Missing url.");
        if (!cl.hasOption("study") && !cl.hasOption("f"))
            throw new MissingOptionException("Specify either -f or --study option");
        rendered = cl.hasOption("rendered");
        service = new ImagingDocumentSource();
        rows = cl.getOptionValue("rows");
        columns = cl.getOptionValue("columns");
        windowWidth = cl.getOptionValue("window-width");
        windowCenter = cl.getOptionValue("window-center");
        imageQuality = cl.getOptionValue("image-quality");
        frameNo = cl.getOptionValue("frame-number");
        if (cl.hasOption("tsuid"))
            tsuids = cl.getOptionValues("tsuid");
        if (cl.hasOption("t"))
            contentTypes = cl.getOptionValues("t");
        repositoryUniqueID = cl.hasOption("repositoryUniqueID")
                ? cl.getOptionValue("repositoryUniqueID") : DEFAULT_REPOSITORY_UNIQUE_ID;
        if (cl.hasOption("out-dir"))
            wadoWS.setOutputDirectory(new File(cl.getOptionValue("out-dir")));
        kosAttr = cl.hasOption("f") 
                ? new DicomInputStream(new FileInputStream(new File(cl.getOptionValue("f")))).readDataset()
                : toAttributes(cl.getOptionValue("study"));
    }

    private void wado() throws Exception {
        ImagingDocumentSourcePortType port = port();
        if (rendered) {
            RetrieveRenderedImagingDocumentSetResponseType retrieveRenderedImagingDocumentSetResponseType
                    = port.imagingDocumentSourceRetrieveRenderedImagingDocumentSet(
                            createRetrieveRenderedImagingDocumentSetRequest());
            List<RenderedDocumentResponse> renderedDocumentResponse = retrieveRenderedImagingDocumentSetResponseType
                                                                        .getRenderedDocumentResponse();
            LOG.info("<< RetrieveRenderedImagingDocumentSetResponse:");
            for (RenderedDocumentResponse rsp : renderedDocumentResponse) {
                logIncoming(rsp);
                write(rsp.getDocument().getInputStream());
            }
            return;
        }

        RetrieveDocumentSetResponseType retrieveDocumentSetResponseType
                = port.imagingDocumentSourceRetrieveImagingDocumentSet(createRetrieveImagingDocumentSetRequest());
        List<DocumentResponse> documentResponse = retrieveDocumentSetResponseType.getDocumentResponse();
        LOG.info("<< RetrieveDocumentSetResponse:");
        for (DocumentResponse rsp : documentResponse) {
            logIncoming(rsp);
            write(rsp.getDocument().getInputStream());
        }
    }

    private static void write(InputStream in) throws IOException {
        Path path = outDir != null
                ? new File(outDir.toPath().toString(), "part" + count).toPath()
                : Paths.get("part" + count);
        try (OutputStream out = Files.newOutputStream(path)) {
            StreamUtils.copy(in, out);
        }
        System.out.println(MessageFormat.format(rb.getString("unpacked"), path));
        count++;
    }

    private ImagingDocumentSourcePortType port() throws Exception {
        ImagingDocumentSourcePortType port = service.getImagingDocumentSourcePortSoap12(
                new AddressingFeature(true, true),
                new MTOMFeature());
        XDSUtils.ensureMustUnderstandHandler(port);
        XDSUtils.setEndpointAddress(port, url);
        return port;
    }

    private RetrieveImagingDocumentSetRequestType createRetrieveImagingDocumentSetRequest() {
        RetrieveImagingDocumentSetRequestType req = new RetrieveImagingDocumentSetRequestType();
        for (Attributes refStudy : kosAttr.getSequence(Tag.CurrentRequestedProcedureEvidenceSequence))
            req.getStudyRequest().add(createStudyReq(refStudy));

        TransferSyntaxUIDList tsuidList = new TransferSyntaxUIDList();
        for (String tsuid : tsuids)
            tsuidList.getTransferSyntaxUID().add(tsuid);

        req.setTransferSyntaxUIDList(tsuidList);
        logOutgoing(req);
        return req;
    }

    private void logOutgoing(RetrieveImagingDocumentSetRequestType req) {
        LOG.info(">> RetrieveImagingDocumentSetRequest:");
        for (RetrieveImagingDocumentSetRequestType.StudyRequest studyReq : req.getStudyRequest()) {
            LOG.info("  Study[uid=" + studyReq.getStudyInstanceUID() + "]");
            for (RetrieveImagingDocumentSetRequestType.StudyRequest.SeriesRequest seriesReq : studyReq.getSeriesRequest()) {
                LOG.info("   Series[uid=" + seriesReq.getSeriesInstanceUID() + "]");
                for (DocumentRequest docReq : seriesReq.getDocumentRequest()) {
                    LOG.info("    Document[uid=" + docReq.getDocumentUniqueId() + "]");
                    LOG.info("    Repository[uid=" + docReq.getRepositoryUniqueId() + "]");
                }
            }
        }
        for (String tsuid : req.getTransferSyntaxUIDList().getTransferSyntaxUID())
            LOG.info("  Transfer Syntax[uid=" + tsuid + "]");
    }

    private void logOutgoing(RetrieveRenderedImagingDocumentSetRequestType req) {
        LOG.info(">> RetrieveRenderedImagingDocumentSetRequest:");
        for (RetrieveRenderedImagingDocumentSetRequestType.StudyRequest studyReq : req.getStudyRequest()) {
            LOG.info("  Study[uid=" + studyReq.getStudyInstanceUID() + "]");
            for (RetrieveRenderedImagingDocumentSetRequestType.StudyRequest.SeriesRequest seriesReq : studyReq.getSeriesRequest()) {
                LOG.info("   Series[uid=" + seriesReq.getSeriesInstanceUID() + "]");
                for (RenderedDocumentRequest docReq : seriesReq.getRenderedDocumentRequest()) {
                    LOG.info("    Document[uid=" + docReq.getDocumentUniqueId() + "]");
                    LOG.info("    Repository[uid=" + docReq.getRepositoryUniqueId() + "]");
                }
            }
        }
    }

    private void logIncoming(DocumentResponse rsp) {
        DataHandler document = rsp.getDocument();
        LOG.info("< Document[uid="
                + rsp.getDocumentUniqueId()
                + ", name=" + document.getName()
                + ", contentType=" + document.getContentType()
                + "]");
        LOG.info("< Home Community ID: " + rsp.getHomeCommunityId());
        LOG.info("< Mime Type: " + rsp.getMimeType());
        LOG.info("< Repository Unique ID: " + rsp.getRepositoryUniqueId());
    }

    private void logIncoming(RenderedDocumentResponse rsp) {
        DataHandler document = rsp.getDocument();
        LOG.info("< Document[uid="
                + rsp.getSourceDocumentUniqueId()
                + ", Name=" + document.getName()
                + ", ContentType=" + document.getContentType()
                + ", Rows=" + rsp.getRows()
                + ", Columns=" + rsp.getColumns()
                + ", Region=" + rsp.getRegion()
                + ", WindowWidth=" + rsp.getWindowWidth()
                + ", WindowCenter=" + rsp.getWindowCenter()
                + ", ImageQuality=" + rsp.getImageQuality()
                + ", PresentationUID=" + rsp.getPresentationUID()
                + ", PresentationSeriesUID=" + rsp.getPresentationSeriesUID()
                + ", Annotation=" + rsp.getAnnotation()
                + ", Anonymize=" + rsp.getAnonymize()
                + ", FrameNumber=" + rsp.getFrameNumber()
                + "]");
        LOG.info("< Home Community ID: " + rsp.getHomeCommunityId());
        LOG.info("< Mime Type: " + rsp.getMimeType());
        LOG.info("< Repository Unique ID: " + rsp.getRepositoryUniqueId());
    }

    private RetrieveImagingDocumentSetRequestType.StudyRequest createStudyReq(Attributes refStudy) {
        RetrieveImagingDocumentSetRequestType.StudyRequest studyReq = new RetrieveImagingDocumentSetRequestType.StudyRequest();
        studyReq.setStudyInstanceUID(refStudy.getString(Tag.StudyInstanceUID));
        for (Attributes refSeries : refStudy.getSequence(Tag.ReferencedSeriesSequence))
            studyReq.getSeriesRequest().add(createSeriesReq(refSeries));

        return studyReq;
    }

    private RetrieveImagingDocumentSetRequestType.StudyRequest.SeriesRequest createSeriesReq(Attributes refSeries) {
        RetrieveImagingDocumentSetRequestType.StudyRequest.SeriesRequest seriesReq =
                new RetrieveImagingDocumentSetRequestType.StudyRequest.SeriesRequest();
        seriesReq.setSeriesInstanceUID(refSeries.getString(Tag.SeriesInstanceUID));
        for (Attributes refSOP : refSeries.getSequence(Tag.ReferencedSOPSequence))
            seriesReq.getDocumentRequest().add(createInstanceReq(refSOP));

        return seriesReq;
    }

    private RetrieveImagingDocumentSetRequestType.StudyRequest.SeriesRequest.DocumentRequest createInstanceReq(Attributes refSOP) {
        DocumentRequest sopReq = new DocumentRequest();
        sopReq.setDocumentUniqueId(refSOP.getString(Tag.ReferencedSOPInstanceUID));
        sopReq.setRepositoryUniqueId(repositoryUniqueID);
        return sopReq;
    }

    private RetrieveRenderedImagingDocumentSetRequestType createRetrieveRenderedImagingDocumentSetRequest() {
        RetrieveRenderedImagingDocumentSetRequestType req = new RetrieveRenderedImagingDocumentSetRequestType();
        for (Attributes refStudy : kosAttr.getSequence(Tag.CurrentRequestedProcedureEvidenceSequence))
            req.getStudyRequest().add(createRenderedStudyReq(refStudy));

        logOutgoing(req);
        return req;
    }

    private RetrieveRenderedImagingDocumentSetRequestType.StudyRequest createRenderedStudyReq(Attributes refStudy) {
        RetrieveRenderedImagingDocumentSetRequestType.StudyRequest studyReq =
                new RetrieveRenderedImagingDocumentSetRequestType.StudyRequest();
        studyReq.setStudyInstanceUID(refStudy.getString(Tag.StudyInstanceUID));
        for (Attributes refSeries : refStudy.getSequence(Tag.ReferencedSeriesSequence))
            studyReq.getSeriesRequest().add(createRenderedSeriesReq(refSeries));

        return studyReq;
    }

    private RetrieveRenderedImagingDocumentSetRequestType.StudyRequest.SeriesRequest createRenderedSeriesReq(Attributes refSeries) {
        RetrieveRenderedImagingDocumentSetRequestType.StudyRequest.SeriesRequest seriesReq =
                new RetrieveRenderedImagingDocumentSetRequestType.StudyRequest.SeriesRequest();
        seriesReq.setSeriesInstanceUID(refSeries.getString(Tag.SeriesInstanceUID));
        for (Attributes refSOP : refSeries.getSequence(Tag.ReferencedSOPSequence))
            seriesReq.getRenderedDocumentRequest().add(createRenderedDocReq(refSOP));

        return seriesReq;
    }

    private RenderedDocumentRequest createRenderedDocReq(Attributes refSOP) {
        RenderedDocumentRequest sopReq = new RenderedDocumentRequest();
        sopReq.setDocumentUniqueId(refSOP.getString(Tag.ReferencedSOPInstanceUID));
        sopReq.setRepositoryUniqueId(repositoryUniqueID);
        if (rows != null)
            sopReq.setRows(rows);
        if (columns != null)
            sopReq.setColumns(columns);
        if (windowWidth != null)
            sopReq.setWindowWidth(windowWidth);
        if (windowCenter != null)
            sopReq.setWindowCenter(windowCenter);
        if (imageQuality != null)
            sopReq.setImageQuality(imageQuality);
        if (frameNo != null)
            sopReq.setFrameNumber(frameNo);
        ContentTypeList contentTypeList = new ContentTypeList();
        for (String contentType : contentTypes)
            contentTypeList.getContentType().add(contentType);

        sopReq.setContentTypeList(contentTypeList);

        return sopReq;
    }

    private static Attributes toAttributes(String study) {
        String instanceSeparator = ",";
        char beginReferencedObj = '[';
        String seriesSplitterRegex = "(],)";
        
        Attributes attrs = new Attributes();
        Attributes refStudy = new Attributes();
        int seriesListStart = study.indexOf(beginReferencedObj);
        refStudy.setString(Tag.StudyInstanceUID, VR.UI, study.substring(0, seriesListStart));
        String[] seriesList = study.substring(seriesListStart+1, study.length() -1).split(seriesSplitterRegex);
        Sequence refSeriesSequence = refStudy.newSequence(Tag.ReferencedSeriesSequence, seriesList.length);
        for (String series : seriesList) {
            int instanceListStart = series.indexOf(beginReferencedObj);
            Attributes refSeries = new Attributes();
            refSeries.setString(Tag.SeriesInstanceUID, VR.UI, series.substring(0, instanceListStart));
            String[] instances = series.substring(instanceListStart + 1).replace(']', ' ').split(instanceSeparator);
            Sequence refSopSequence = refSeries.newSequence(Tag.ReferencedSOPSequence, instances.length);
            for (String instance : instances) {
                Attributes refSop = new Attributes();
                refSop.setString(Tag.ReferencedSOPInstanceUID, VR.UI, instance);
                refSopSequence.add(refSop);
            }
            refSeriesSequence.add(refSeries);
        }
        attrs.newSequence(Tag.CurrentRequestedProcedureEvidenceSequence, 1).add(refStudy);
        return attrs;
    }
}
