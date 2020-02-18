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
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2017-2019
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

package org.dcm4che3.tool.stowrs;

import java.io.*;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.cli.*;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.imageio.codec.XPEGParser;
import org.dcm4che3.imageio.codec.jpeg.JPEG;
import org.dcm4che3.imageio.codec.jpeg.JPEGParser;
import org.dcm4che3.imageio.codec.mp4.MP4Parser;
import org.dcm4che3.imageio.codec.mpeg.MPEG2Parser;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.json.JSONWriter;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.*;
import org.dcm4che3.util.Base64;
import org.dcm4che3.ws.rs.MediaTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Apr 2017
 */
public class StowRS {
    private static final Logger LOG = LoggerFactory.getLogger(StowRS.class);
    private static ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.stowrs.messages");
    private static String fileContentType;
    private static String[] keys;
    private String url;
    private String user;
    private String bearer;
    private boolean noApp;
    private boolean pixelHeader;
    private static boolean vlPhotographicImage;
    private static boolean videoPhotographicImage;
    private static String requestAccept;
    private static String requestContentType;
    private static String metadataFile;
    private boolean tsuid;
    private static final String boundary = "myboundary";
    private static final String APPLN_DICOM = "application/dicom";
    private static final String APPLN_DICOM_XML = "application/dicom+xml";
    private static FileType fileType;
    private Map<String, StowRSBulkdata> contentLocBulkdata = new HashMap<>();

    private static final int[] IUIDS_TAGS = {
            Tag.StudyInstanceUID,
            Tag.SeriesInstanceUID
    };

    private static final int[] TYPE2_TAGS = {
            Tag.ContentDate,
            Tag.ContentTime
    };

    private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();

    @SuppressWarnings("static-access")
    private static CommandLine parseCommandLine(String[] args)
            throws ParseException {
        Options opts = new Options();
        opts.addOption(Option.builder("m")
                .numberOfArgs(2).argName("[seq/]attr=value")
                .valueSeparator()
                .desc(rb.getString("metadata"))
                .build());
        opts.addOption(Option.builder("f")
                .hasArg()
                .argName("file")
                .longOpt("file")
                .desc(rb.getString("file"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("url")
                .longOpt("url")
                .desc(rb.getString("url"))
                .build());
        opts.addOption(Option.builder("t")
                .hasArg()
                .argName("type")
                .longOpt("type")
                .desc(rb.getString("type"))
                .build());
        opts.addOption(Option.builder()
                .argName("tsuid")
                .longOpt("tsuid")
                .desc(rb.getString("tsuid"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("pixel-header")
                .desc(rb.getString("pixel-header"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("no-app")
                .desc(rb.getString("no-app"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("xc")
                .desc(rb.getString("xc"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("video")
                .desc(rb.getString("video"))
                .build());
        opts.addOption(Option.builder("a")
                .longOpt("accept")
                .hasArg()
                .desc(rb.getString("accept"))
                .build());
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("u")
                .hasArg()
                .argName("user:password")
                .longOpt("user")
                .desc(rb.getString("user"))
                .build());
        group.addOption(Option.builder()
                .hasArg()
                .argName("bearer")
                .longOpt("bearer")
                .desc(rb.getString("bearer"))
                .build());
        opts.addOptionGroup(group);
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, StowRS.class);
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseCommandLine(args);
            List<String> files = cl.getArgList();
            StowRS stowRS = new StowRS();
            stowRS.doNecessaryChecks(cl, files);
            LOG.info("Storing objects.");
            stowRS.stow(files);
        } catch (ParseException e) {
            System.err.println("stowrs: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            LOG.error("Error: \n", e);
            e.printStackTrace();
            System.exit(2);
        }
    }

    private void doNecessaryChecks(CommandLine cl, List<String> files)
            throws Exception {
        if (files.isEmpty())
            throw new MissingArgumentException("No pixel data files or dicom files specified");
        if ((url = cl.getOptionValue("url")) == null)
            throw new MissingOptionException("Missing url.");
        if (cl.hasOption("f")
                && !Files.probeContentType(Paths.get((metadataFile = cl.getOptionValue("f")))).endsWith("xml"))
            throw new IllegalArgumentException("Metadata file extension not supported. Read -f option in stowrs help");

        tsuid = cl.hasOption("tsuid");
        pixelHeader = cl.hasOption("pixel-header");
        noApp = cl.hasOption("no-appn");
        keys = cl.getOptionValues("m");
        user = cl.getOptionValue("u");
        bearer = cl.getOptionValue("bearer");
        vlPhotographicImage = cl.hasOption("xc");
        videoPhotographicImage = cl.hasOption("video");
        processFileType(files);
        setContentAndAcceptType(cl);
    }

    enum FileType {
        PDF(UID.EncapsulatedPDFStorage, Tag.EncapsulatedDocument, MediaTypes.APPLICATION_PDF,
                "encapsulatedPDFMetadata.xml"),
        XML(UID.EncapsulatedCDAStorage, Tag.EncapsulatedDocument, MediaType.TEXT_XML,
                "encapsulatedCDAMetadata.xml"),
        SLA(UID.EncapsulatedSTLStorage, Tag.EncapsulatedDocument, MediaTypes.MODEL_STL,
                "encapsulatedSTLMetadata.xml"),
        MTL(UID.EncapsulatedMTLStorage, Tag.EncapsulatedDocument, MediaTypes.MODEL_MTL,
                "encapsulatedMTLMetadata.xml"),
        OBJ(UID.EncapsulatedOBJStorage, Tag.EncapsulatedDocument, MediaTypes.MODEL_OBJ,
                "encapsulatedOBJMetadata.xml"),
        JPEG(vlPhotographicImage ? UID.VLPhotographicImageStorage : UID.SecondaryCaptureImageStorage, 
                Tag.PixelData, 
                MediaTypes.IMAGE_JPEG,
                vlPhotographicImage ? "vlPhotographicImageMetadata.xml" : "secondaryCaptureImageMetadata.xml"),
        JP2(vlPhotographicImage ? UID.VLPhotographicImageStorage : UID.SecondaryCaptureImageStorage, 
                Tag.PixelData, 
                MediaTypes.IMAGE_JP2,
                vlPhotographicImage ? "vlPhotographicImageMetadata.xml" : "secondaryCaptureImageMetadata.xml"),
        PNG(vlPhotographicImage ? UID.VLPhotographicImageStorage : UID.SecondaryCaptureImageStorage,
                Tag.PixelData,
                MediaTypes.IMAGE_PNG,
                vlPhotographicImage ? "vlPhotographicImageMetadata.xml" : "secondaryCaptureImageMetadata.xml"),
        GIF(videoPhotographicImage
                ? UID.VideoPhotographicImageStorage
                : vlPhotographicImage
                    ? UID.VLPhotographicImageStorage : UID.SecondaryCaptureImageStorage,
                Tag.PixelData,
                MediaTypes.IMAGE_GIF,
                vlPhotographicImage || videoPhotographicImage
                        ? "vlPhotographicImageMetadata.xml" : "secondaryCaptureImageMetadata.xml"),
        MPEG(UID.VideoPhotographicImageStorage, Tag.PixelData, MediaTypes.VIDEO_MPEG,
                "vlPhotographicImageMetadata.xml"),
        MP4(UID.VideoPhotographicImageStorage, Tag.PixelData, MediaTypes.VIDEO_MP4,
                "vlPhotographicImageMetadata.xml"),
        QUICKTIME(UID.VideoPhotographicImageStorage, Tag.PixelData, MediaTypes.VIDEO_QUICKTIME,
                "vlPhotographicImageMetadata.xml");

        private String cuid;
        private int bulkdataTypeTag;
        private String sampleMetadataFile;
        private String mediaType;

        public String getSOPClassUID() {
            return cuid;
        }

        public String getSampleMetadataResourceURL() {
           return "resource:" + sampleMetadataFile;
        }

        public int getBulkdataTypeTag() {
            return bulkdataTypeTag;
        }

        public String getMediaType() {
            return mediaType;
        }

        FileType(String cuid, int bulkdataTypeTag, String mediaType, String sampleMetadataFile) {
            this.cuid = cuid;
            this.bulkdataTypeTag = bulkdataTypeTag;
            this.sampleMetadataFile = sampleMetadataFile;
            this.mediaType = mediaType;
        }

        static FileType valueOf() {
            try {
                return valueOf(fileContentType.substring(fileContentType.indexOf("/") + 1).toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        MessageFormat.format(rb.getString("bulkdata-file-not-supported"), fileContentType));
            }
        }
    }

    private void processFileType(List<String> files) throws IOException {
        for (String file : files) 
            applyFunctionToFile(file, new StowRSFileFunction<Path>() {
                @Override
                public void apply(Path path) throws IOException {
                    checkFileContentType(path);
                }
            });

        if (fileContentType.equals(APPLN_DICOM))
            return;
        
        fileType = FileType.valueOf();
    }

    private static void checkFileContentType(Path path) throws IOException {
        String contentType = Files.probeContentType(path);
        if (contentType == null)
            contentType = mtlOrObj(path);

        if (fileContentType == null) {
            if ((fileContentType = contentType) == null)
                if (isDICOM(path))
                    fileContentType = APPLN_DICOM;
        } else {
            if (contentType == null)
                isDICOM(path);
            else if (!fileContentType.equals(contentType))
                throw new IllegalArgumentException("Uploading multiple files of different content types not supported.");
        }
    }

    private static String mtlOrObj(Path path) {
        String fileName = path.toFile().getName();
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1);
        if (ext.equalsIgnoreCase("obj")) {
            fileType = FileType.OBJ;
            fileContentType = "model/obj";
        } else if (ext.equalsIgnoreCase("mtl")) {
            fileType = FileType.MTL;
            fileContentType = "model/mtl";
        }
        return fileContentType;
    }

    private static boolean isDICOM(Path path) {
        DicomInputStream dis = null;
        try {
            dis = new DicomInputStream(path.toFile());
            return true;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unrecognized file content type.");
        } finally {
            SafeClose.close(dis);
        }
    }

    private static void setContentAndAcceptType(CommandLine cl) {
        if (fileContentType.equals(APPLN_DICOM)) {
            requestContentType = APPLN_DICOM;
            requestAccept = APPLN_DICOM_XML;
            return;
        }

        requestContentType = cl.hasOption("t") ? getOptionValue("t", cl) : APPLN_DICOM_XML;
        requestAccept = cl.hasOption("a") ? getOptionValue("a", cl) : requestContentType;
    }

    private static String getOptionValue(String option, CommandLine cl) {
        String optionValue = cl.getOptionValue(option);
        if (optionValue.equalsIgnoreCase("xml") || optionValue.equalsIgnoreCase("json"))
            return "application/dicom+" + optionValue;

        throw new IllegalArgumentException("Unsupported type. Read -" + option + " option in stowrs help");
    }

    private Attributes createMetadata(Path bulkdataFilePath, Attributes staticMetadata) throws IOException {
        LOG.info("Supplementing file specific metadata.");
        String contentLoc = "bulk" + UIDUtils.createUID();
        Attributes metadata = new Attributes(staticMetadata);
        supplementMissingUID(metadata, Tag.SOPInstanceUID);
        supplementType2Tags(metadata);
        metadata.setValue(fileType.getBulkdataTypeTag(), VR.OB, new BulkData(null, contentLoc, false));
        switch (fileType) {
            case PDF:
            case XML:
            case SLA:
            case MTL:
            case OBJ:
                supplementEncapsulatedDocAttrs(metadata);
                contentLocBulkdata.put(contentLoc, new StowRSBulkdata(bulkdataFilePath));
                break;
            case JPEG:
            case JP2:
            case PNG:
            case GIF:
            case MPEG:
            case MP4:
            case QUICKTIME:
                pixelMetadata(contentLoc, bulkdataFilePath.toFile(), metadata);
                break;
        }
        return metadata;
    }

    private void pixelMetadata(String contentLoc, File bulkdataFile, Attributes metadata) throws IOException {
        StowRSBulkdata stowRSBulkdata = new StowRSBulkdata(bulkdataFile.toPath());
        if (pixelHeader || tsuid || noApp) {
            CompressedPixelData compressedPixelData = CompressedPixelData.valueOf();
            try(FileInputStream fis = new FileInputStream(bulkdataFile)) {
                compressedPixelData.parse(fis.getChannel());
                XPEGParser parser = compressedPixelData.getParser();
                if (pixelHeader)
                    parser.getAttributes(metadata);
                stowRSBulkdata.setParser(parser);
            }
        }
        contentLocBulkdata.put(contentLoc, stowRSBulkdata);
    }

    private static Attributes createStaticMetadata()
            throws Exception {
        LOG.info("Creating static metadata. Set defaults, if essential attributes are not present.");
        Attributes metadata = SAXReader.parse(StreamUtils.openFileOrURL(fileType.getSampleMetadataResourceURL()));
        addAttributesFromFile(metadata);
        CLIUtils.addAttributes(metadata, keys);
        supplementSOPClass(metadata, fileType.getSOPClassUID());
        supplementMissingUIDs(metadata);
        return metadata;
    }

    private static void addAttributesFromFile(Attributes metadata) throws Exception {
        if (metadataFile == null)
            return;

        metadata.addAll(SAXReader.parse(Paths.get(metadataFile).toString(), metadata));
    }

    private static void supplementMissingUIDs(Attributes metadata) {
        for (int tag : IUIDS_TAGS)
            if (!metadata.containsValue(tag))
                metadata.setString(tag, VR.UI, UIDUtils.createUID());
    }

    private static void supplementMissingUID(Attributes metadata, int tag) {
        if (!metadata.containsValue(tag))
            metadata.setString(tag, VR.UI, UIDUtils.createUID());
    }

    private static void supplementSOPClass(Attributes metadata, String value) {
        if (!metadata.containsValue(Tag.SOPClassUID))
            metadata.setString(Tag.SOPClassUID, VR.UI, value);
    }

    private static void supplementType2Tags(Attributes metadata) {
        for (int tag : TYPE2_TAGS)
            if (!metadata.contains(tag))
                metadata.setNull(tag, DICT.vrOf(tag));
    }

    private static void supplementEncapsulatedDocAttrs(Attributes metadata) {
        if (fileType == FileType.SLA || fileType == FileType.OBJ)
            supplementMissingUID(metadata, Tag.FrameOfReferenceUID);
        if (!metadata.contains(Tag.AcquisitionDateTime))
            metadata.setNull(Tag.AcquisitionDateTime, VR.DT);
    }

    private enum CompressedPixelData {
        JPEG {
            @Override
            void parse(SeekableByteChannel channel) throws IOException {
                setParser(new JPEGParser(channel));
            }
        },
        MPEG {
            @Override
            void parse(SeekableByteChannel channel) throws IOException {
                setParser(new MPEG2Parser(channel));
            }
        },
        MP4 {
            @Override
            void parse(SeekableByteChannel channel) throws IOException {
                setParser(new MP4Parser(channel));
            }
        };

        abstract void parse(SeekableByteChannel channel) throws IOException;

        private XPEGParser parser;

        public XPEGParser getParser() {
            return parser;
        }

        void setParser(XPEGParser parser) {
            this.parser = parser;
        }

        static CompressedPixelData valueOf() {
            return fileType == FileType.JP2
                    ? JPEG
                    : fileType == FileType.QUICKTIME
                        ? MP4 : valueOf(fileType.name());
        }
    }

    private void stow(List<String> files) throws Exception {
        URL newUrl = new URL(url);
        final HttpURLConnection connection = (HttpURLConnection) newUrl.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                "multipart/related; type=\"" + requestContentType + "\"; boundary=" + boundary);
        connection.setRequestProperty("Accept", requestAccept);
        logOutgoing(connection);
        authorize(connection);
        try (OutputStream out = connection.getOutputStream()) {
            writeData(files, out);
            out.write(("\r\n--" + boundary + "--\r\n").getBytes());
            out.flush();
            logIncoming(connection);
            connection.disconnect();
            LOG.info("STOW successful!");
        } catch (Exception e) {
            LOG.error("Exception : " + e.getMessage());
        }
    }

    private void authorize(HttpURLConnection connection) {
        if (user == null && bearer == null)
            return;

        String authorization = user != null ? basicAuth() : "Bearer " + bearer;
        LOG.info("> Authorization: " + authorization);
        connection.setRequestProperty("Authorization", authorization);
    }

    private String basicAuth() {
        byte[] userPswdBytes = user.getBytes();
        int len = (userPswdBytes.length * 4 / 3 + 3) & ~3;
        char[] ch = new char[len];
        Base64.encode(userPswdBytes, 0, userPswdBytes.length, ch, 0);
        return "Basic " + new String(ch);
    }

    private void logOutgoing(HttpURLConnection connection) {
        LOG.info("> " + connection.getRequestMethod() + " " + connection.getURL());
        LOG.info("> Content-Type: " + connection.getRequestProperty("Content-Type"));
        LOG.info("> Accept: " + connection.getRequestProperty("Accept"));
    }

    private void logIncoming(HttpURLConnection connection) throws Exception {
        LOG.info("< Content-Length: " + connection.getContentLength());
        LOG.info("< HTTP/1.1 Response: " + connection.getResponseCode() + " " + connection.getResponseMessage());
        LOG.info("< Transfer-Encoding: " + connection.getContentEncoding());
        LOG.info("< Content-Type: " + connection.getContentType());
        LOG.info("< Date: " + connection.getLastModified());
        LOG.info("< Response Content: ");
        try (InputStream is = connection.getInputStream()) {
            LOG.debug(readFullyAsString(is));
        }
    }

    private static String readFullyAsString(InputStream inputStream)
            throws IOException {
        return readFully(inputStream).toString("UTF-8");
    }

    private static ByteArrayOutputStream readFully(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[16384];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            return baos;
        }
    }

    private void writeData(List<String> files, final OutputStream out) throws Exception {
        if (!requestContentType.equals(APPLN_DICOM)) {
            writeMetadataAndBulkData(out, files, createStaticMetadata());
            return;
        }

        for (String file : files) 
            applyFunctionToFile(file, new StowRSFileFunction<Path>() {
                @Override
                public void apply(Path path) throws IOException {
                    writeDicomFile(out, path);
                }
            });
    }

    private static void writeDicomFile(OutputStream out, Path path) throws IOException {
        writePartHeaders(out, requestContentType, null);
        Files.copy(path, out);
    }

    private void writeMetadataAndBulkData(OutputStream out, List<String> files, final Attributes staticMetadata)
            throws Exception {
        if (requestContentType.equals(APPLN_DICOM_XML))
            writeXMLMetadataAndBulkdata(out, files, staticMetadata);
        else {
            try (ByteArrayOutputStream bOut = new ByteArrayOutputStream()) {
                try (JsonGenerator gen = Json.createGenerator(bOut)) {
                    gen.writeStartArray();
                    for (String file : files)
                        applyFunctionToFile(file, new StowRSFileFunction<Path>() {
                            @Override
                            public void apply(Path path) throws IOException {
                                new JSONWriter(gen).write(createMetadata(path, staticMetadata));
                            }
                        });
                    gen.writeEnd();
                    gen.flush();
                }
                writeMetadata(out, bOut);

                for (String contentLocation : contentLocBulkdata.keySet())
                    writeFile(contentLocation, out);
            }
        }
        contentLocBulkdata.clear();
    }

    private void writeXMLMetadataAndBulkdata(final OutputStream out, List<String> files, final Attributes staticMetadata)
            throws Exception {
        for (String file : files) 
            applyFunctionToFile(file, new StowRSFileFunction<Path>() {
                @Override
                public void apply(Path path) {
                    writeXMLMetadataAndBulkdataForEach(out, staticMetadata, path);
                }
            });
    }

    private void writeXMLMetadataAndBulkdataForEach(OutputStream out, Attributes staticMetadata, Path bulkdataFilePath) {
        try {
            Attributes metadata = createMetadata(bulkdataFilePath, staticMetadata);
            try (ByteArrayOutputStream bOut = new ByteArrayOutputStream()) {
                SAXTransformer.getSAXWriter(new StreamResult(bOut)).write(metadata);
                writeMetadata(out, bOut);
            }
            writeFile(((BulkData) metadata.getValue(fileType.getBulkdataTypeTag())).getURI(), out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeMetadata(OutputStream out, ByteArrayOutputStream bOut)
            throws IOException {
        LOG.info("> Metadata Content Type: " + requestContentType);
        writePartHeaders(out, requestContentType, null);
        LOG.debug("Metadata being sent is : " + bOut.toString());
        out.write(bOut.toByteArray());
    }

    private static void writePartHeaders(OutputStream out, String contentType, String contentLocation) throws IOException {
        out.write(("\r\n--" + boundary + "\r\n").getBytes());
        out.write(("Content-Type: " + contentType + "\r\n").getBytes());
        if (contentLocation != null)
            out.write(("Content-Location: " + contentLocation + "\r\n").getBytes());
        out.write("\r\n".getBytes());
    }

    private void writeFile(String contentLocation, OutputStream out) throws Exception {
        String bulkdataContentType = fileType.getMediaType();
        StowRSBulkdata stowRSBulkdata = contentLocBulkdata.get(contentLocation);
        XPEGParser parser = stowRSBulkdata.getParser();
        if (fileType.getBulkdataTypeTag() == Tag.PixelData && tsuid)
            bulkdataContentType = fileType.getMediaType()
                    + "; transfer-syntax="
                    + parser.getTransferSyntaxUID();
        LOG.info("> Bulkdata Content Type: " + bulkdataContentType);
        writePartHeaders(out, bulkdataContentType, contentLocation);

        int offset = 0;
        int length = (int) stowRSBulkdata.getBulkdataFilePath().toFile().length();
        long positionAfterAPPSegments = parser != null ? parser.getPositionAfterAPPSegments() : -1L;
        if (noApp && positionAfterAPPSegments != -1L) {
            offset = (int) positionAfterAPPSegments;
            out.write(-1);
            out.write((byte) JPEG.SOI);
        }
        length -= offset;
        out.write(Files.readAllBytes(stowRSBulkdata.getBulkdataFilePath()), offset, length);
    }

    static class StowRSBulkdata {
        Path bulkdataFilePath;
        XPEGParser parser;

        StowRSBulkdata(Path bulkdataFilePath) {
            this.bulkdataFilePath = bulkdataFilePath;
        }

        Path getBulkdataFilePath() {
            return bulkdataFilePath;
        }

        XPEGParser getParser() {
            return parser;
        }

        void setParser(XPEGParser parser) {
            this.parser = parser;
        }
    }

    private void applyFunctionToFile(String file, final StowRSFileFunction<Path> function) throws IOException {
        Path path = Paths.get(file);
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new StowRSFileVisitor(new StowRSFileConsumer<Path>() {
                @Override
                public void accept(Path path) throws IOException {
                    function.apply(path);
                }
            }));
        } else
            function.apply(path);
    }

    static class StowRSFileVisitor extends SimpleFileVisitor<Path> {
        private StowRSFileConsumer<Path> consumer;

        StowRSFileVisitor(StowRSFileConsumer<Path> consumer){
            this.consumer = consumer;
        }

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
            consumer.accept(path);
            return FileVisitResult.CONTINUE;
        }
    }

    interface StowRSFileConsumer<Path> {
        void accept(Path path) throws IOException;
    }

    interface StowRSFileFunction<Path> {
        void apply(Path path) throws IOException;
    }
}
