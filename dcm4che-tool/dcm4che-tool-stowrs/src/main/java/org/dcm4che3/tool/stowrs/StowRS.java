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

package org.dcm4che3.tool.stowrs;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.cli.*;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.imageio.codec.jpeg.JPEG;
import org.dcm4che3.imageio.codec.jpeg.JPEGParser;
import org.dcm4che3.imageio.codec.mp4.MP4Parser;
import org.dcm4che3.imageio.codec.mpeg.MPEG2Parser;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.json.JSONWriter;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.*;
import org.dcm4che3.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Apr 2017
 */
public class StowRS {
    private static final Logger LOG = LoggerFactory.getLogger(StowRS.class);
    private static String bulkdataContentType;
    private static String[] keys;
    private static String url;
    private static String user;
    private static boolean noAppn;
    private static boolean pixelHeader;
    private static boolean isVLPhotographicImage;
    private static String requestAccept;
    private static String requestContentType;
    private static String metadataFile;
    private static String tsuid;
    private static CompressedPixelData compressedPixelData;
    private static ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.stowrs.messages");
    private static final String boundary = "myboundary";
    private static final String APPLN_DICOM = "application/dicom";
    private static final String APPLN_DICOM_XML = "application/dicom+xml";
    private static FileType fileType;
    private static Map<String, File> contentLocBulkData = new HashMap<>();

    private static final String patName = "STOW-RS-PatientName";

    private static final int[] IUIDS_TAGS = {
            Tag.StudyInstanceUID,
            Tag.SeriesInstanceUID,
            Tag.SOPInstanceUID
    };

    private static final long[] DA_TM_TAGS = {
            Tag.ContentDateAndTime,
            Tag.AcquisitionDateTime
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
        opts.addOption(Option.builder("u")
                .hasArg()
                .argName("user:password")
                .longOpt("user")
                .desc(rb.getString("user"))
                .build());
        opts.addOption(Option.builder("t")
                .hasArg()
                .argName("type")
                .longOpt("type")
                .desc(rb.getString("type"))
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("tsuid")
                .longOpt("tsuid")
                .desc(rb.getString("tsuid"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("pixel-header")
                .hasArg(false)
                .desc(rb.getString("pixel-header"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("no-appn")
                .hasArg(false)
                .desc(rb.getString("no-appn"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("xc")
                .hasArg(false)
                .desc(rb.getString("xc"))
                .build());
        opts.addOption("a","accept", true, rb.getString("accept"));
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, StowRS.class);
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseCommandLine(args);
            List<String> files = cl.getArgList();
            doNecessaryChecks(cl, files);
            LOG.info("Storing objects.");
            stow(files);
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

    private static void doNecessaryChecks(CommandLine cl, List<String> files)
            throws Exception {
        if (files.isEmpty())
            throw new MissingArgumentException("No pixel data files or dicom files specified");
        if ((url = cl.getOptionValue("url")) == null)
            throw new MissingOptionException("Missing url.");
        if (cl.hasOption("f")
                && !Files.probeContentType(Paths.get((metadataFile = cl.getOptionValue("f")))).endsWith("xml"))
            throw new IllegalArgumentException("Metadata file extension not supported. Read -f option in stowrs help");

        checkFileType(files);
        keys = cl.getOptionValues("m");
        user = cl.getOptionValue("u");
        tsuid = cl.getOptionValue("tsuid");
        setContentAndAcceptType(cl);
        pixelHeader = cl.hasOption("pixel-header");
        noAppn = cl.hasOption("no-appn");
        isVLPhotographicImage = cl.hasOption("xc");
    }

    enum FileType {
        PDF(UID.EncapsulatedPDFStorage, Tag.EncapsulatedDocument, "encapsulatedPDFMetadata.xml"),
        XML(UID.EncapsulatedCDAStorage, Tag.EncapsulatedDocument, "encapsulatedCDAMetadata.xml"),
        SLA(UID.EncapsulatedSTLStorage, Tag.EncapsulatedDocument, "encapsulatedSTLMetadata.xml"),
        JPEG(UID.SecondaryCaptureImageStorage, Tag.PixelData, "secondaryCaptureImageMetadata.xml"),
        VLJPEG(UID.VLPhotographicImageStorage, Tag.PixelData, "vlPhotographicImageMetadata.xml"),
        MPEG(UID.VideoPhotographicImageStorage, Tag.PixelData, "vlPhotographicImageMetadata.xml"),
        MP4(UID.VideoPhotographicImageStorage, Tag.PixelData, "vlPhotographicImageMetadata.xml");

        private String cuid;
        private int bulkdataTypeTag;
        private String sampleMetadataFile;

        public String getSOPClassUID() {
            return cuid;
        }

        public String getSampleMetadataResourceURL() {
            return "resource:" + sampleMetadataFile;
        }

        public int getBulkdataTypeTag() {
            return bulkdataTypeTag;
        }

        FileType(String cuid, int bulkdataTypeTag, String sampleMetadataFile) {
            this.cuid = cuid;
            this.bulkdataTypeTag = bulkdataTypeTag;
            this.sampleMetadataFile = sampleMetadataFile;
        }
    }

    private static void checkFileType(List<String> files) throws IOException {
        Path path = Paths.get(files.get(0));
        bulkdataContentType = Files.probeContentType(path);
        for (int i = 1; i < files.size(); i++)
            if (!bulkdataContentType.equals(Files.probeContentType(Paths.get(files.get(i)))))
                throw new IllegalArgumentException("Uploading multiple files of different content types not supported.");

        if (bulkdataContentType.equals(APPLN_DICOM))
            return;

        try {
            fileType = FileType.valueOf(bulkdataContentType.substring(bulkdataContentType.indexOf("/") + 1).toUpperCase());
            if (isVLPhotographicImage && fileType == FileType.JPEG)
                fileType = FileType.VLJPEG;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    MessageFormat.format(rb.getString("bulkdata-file-not-supported"), bulkdataContentType, path));
        }

    }

    private static void setContentAndAcceptType(CommandLine cl) {
        if (bulkdataContentType.equals(APPLN_DICOM)) {
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

    private static Attributes createMetadata(File bulkdataFile, Attributes staticMetadata) throws Exception {
        LOG.info("Supplementing file specific metadata.");
        Attributes metadata = new Attributes(staticMetadata);
        supplementMissingUIDs(metadata);
        supplementMissingDateTime(metadata, bulkdataFile);
        supplementBulkdata(metadata, fileType.getBulkdataTypeTag(), bulkdataFile);
        switch (fileType) {
            case PDF:
            case XML:
            case SLA:
                break;
            case JPEG:
            case VLJPEG:
            case MPEG:
            case MP4:
                pixelMetadata(bulkdataFile, metadata);
                break;
        }
        return metadata;
    }

    private static void pixelMetadata(File bulkdataFile, Attributes metadata) throws Exception {
        compressedPixelData = CompressedPixelData.valueOf();
        if (pixelHeader)
            compressedPixelData.getAttributes(new FileInputStream(bulkdataFile).getChannel(), metadata);
    }

    private static Attributes createStaticMetadata()
            throws Exception {
        LOG.info("Creating static metadata. Set defaults, if essential attributes are not present.");
        Attributes metadata = SAXReader.parse(StreamUtils.openFileOrURL(fileType.getSampleMetadataResourceURL()));
        supplementDefaultValue(metadata, Tag.PatientName, patName);
        addAttributesFromFile(metadata);
        CLIUtils.addAttributes(metadata, keys);
        supplementDefaultValue(metadata, Tag.SOPClassUID, fileType.getSOPClassUID());
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

    private static void supplementDefaultValue(Attributes metadata, int tag, String value) {
        if (!metadata.containsValue(tag))
            metadata.setString(tag, DICT.vrOf(tag), value);
    }

    private static void supplementMissingDateTime(Attributes metadata, File bulkdataFile) {
        Date date = new Date(bulkdataFile.lastModified());
        for (long tag : DA_TM_TAGS)
            if (!metadata.containsValue((int) (tag >>> 32)))
                metadata.setDate(tag, date);
    }

    private static void supplementBulkdata(Attributes metadata, int tag, File bulkdataFile) {
        if (!metadata.containsValue(tag)) {
            String contentLoc = "bulk" + UIDUtils.createUID();
            metadata.setValue(tag, VR.OB, new BulkData(null, contentLoc, false));
            contentLocBulkData.put(contentLoc, bulkdataFile);
        }
    }

    private enum CompressedPixelData {
        JPEG {
            @Override
            Attributes getAttributes(SeekableByteChannel channel, Attributes attrs) throws IOException {
                JPEGParser jpegParser = new JPEGParser(channel);
                return jpegParser.getAttributes(attrs);
            }

            @Override
            public String getTransferSyntaxUID() {
                return UID.JPEGBaseline1;
            }
        },
        MPEG {
            @Override
            Attributes getAttributes(SeekableByteChannel channel, Attributes attrs) throws IOException {
                return new MPEG2Parser(channel).getAttributes(attrs);
            }

            @Override
            public String getTransferSyntaxUID() {
                return UID.MPEG2;
            }
        },
        MP4 {
            @Override
            Attributes getAttributes(SeekableByteChannel channel, Attributes attrs) throws IOException {
                MP4Parser mp4Parser = new MP4Parser(channel);
                setTransferSyntaxUID(mp4Parser.getTransferSyntaxUID());
                return mp4Parser.getAttributes(attrs);
            }
        };

        abstract Attributes getAttributes(SeekableByteChannel channel, Attributes attrs) throws IOException;

        private String tsuid;

        public String getTransferSyntaxUID() {
            return tsuid;
        }

        public void setTransferSyntaxUID(String tsuid) {
            this.tsuid = tsuid;
        }

        static CompressedPixelData valueOf() {
            return fileType == FileType.VLJPEG ? JPEG : valueOf(fileType.name());
        }
    }

    private static void stow(List<String> files) throws Exception {
        URL newUrl = new URL(url);
        final HttpURLConnection connection = (HttpURLConnection) newUrl.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                "multipart/related; type=\"" + requestContentType + "\"; boundary=" + boundary);
        connection.setRequestProperty("Accept", requestAccept);
        logOutgoing(connection);
        if (user != null) {
            String basicAuth = basicAuth(user);
            LOG.info("> Authorization: " + basicAuth);
            connection.setRequestProperty("Authorization", basicAuth);
        }
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

    private static String basicAuth(String userPswd) {
        byte[] userPswdBytes = userPswd.getBytes();
        int len = (userPswdBytes.length * 4 / 3 + 3) & ~3;
        char[] ch = new char[len];
        Base64.encode(userPswdBytes, 0, userPswdBytes.length, ch, 0);
        return "Basic " + new String(ch);
    }

    private static void logOutgoing(HttpURLConnection connection) {
        LOG.info("> " + connection.getRequestMethod() + " " + connection.getURL());
        LOG.info("> Content-Type: " + connection.getRequestProperty("Content-Type"));
        LOG.info("> Accept: " + connection.getRequestProperty("Accept"));
    }

    private static void logIncoming(HttpURLConnection connection) throws Exception {
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

    private static void writeData(List<String> files, OutputStream out) throws Exception {
        if (!requestContentType.equals("application/dicom")) {
            writeMetadataAndBulkData(out, files, createStaticMetadata());
            return;
        }
        for (String file : files) {
            out.write(("\r\n--" + boundary + "\r\n").getBytes());
            out.write(("Content-Type: " + requestContentType + "\r\n").getBytes());
            out.write("\r\n".getBytes());
            Files.copy(Paths.get(file), out);
        }
    }

    private static void writeMetadataAndBulkData(OutputStream out, List<String> files, Attributes staticMetadata)
            throws Exception {
        if (requestContentType.equals(APPLN_DICOM_XML)) {
            writeXMLMetadataAndBulkdata(out, files, staticMetadata);
            return;
        }

        try (ByteArrayOutputStream bOut = new ByteArrayOutputStream()) {
            try (JsonGenerator gen = Json.createGenerator(bOut)) {
                gen.writeStartArray();
                for (String file : files)
                    new JSONWriter(gen).write(createMetadata(Paths.get(file).toFile(), staticMetadata));
                gen.writeEnd();
                gen.flush();
            }
            write(out, bOut);

            for (Map.Entry<String, File> entry : contentLocBulkData.entrySet()) {
                out.write(("\r\n--" + boundary + "\r\n").getBytes());
                out.write(("Content-Type: " + bulkdataContentType + "\r\n").getBytes());
                out.write(("Content-Location: " + entry.getKey() + "\r\n").getBytes());
                out.write("\r\n".getBytes());
                if (compressedPixelData == CompressedPixelData.JPEG && noAppn) {
                    out.write(-1);
                    out.write((byte) JPEG.SOI);
                    out.write(-1);
                }
                writeFile(contentLocBulkData.get(entry.getKey()), out);
            }
            contentLocBulkData.clear();
        }
    }

    private static void write(OutputStream out, ByteArrayOutputStream bOut)
            throws IOException {
        String metadataContentType = requestContentType;
        if (compressedPixelData != null && (pixelHeader || compressedPixelData != CompressedPixelData.MP4))
            metadataContentType = requestContentType
                                    + "; transfer-syntax="
                                    + (tsuid != null ? tsuid : compressedPixelData.getTransferSyntaxUID());

        out.write(("\r\n--" + boundary + "\r\n").getBytes());
        LOG.info("> Metadata Content Type: " + metadataContentType);
        out.write(("Content-Type: " + metadataContentType + "\r\n").getBytes());
        out.write("\r\n".getBytes());
        LOG.debug("Metadata being sent is : " + bOut.toString());
        out.write(bOut.toByteArray());
    }

    private static void writeXMLMetadataAndBulkdata(OutputStream out, List<String> files, Attributes staticMetadata)
            throws Exception {
        for (String file : files) {
            try (ByteArrayOutputStream bOut = new ByteArrayOutputStream()) {
                File bulkdataFile = Paths.get(file).toFile();
                Attributes metadata = createMetadata(bulkdataFile, staticMetadata);
                SAXTransformer.getSAXWriter(new StreamResult(bOut)).write(metadata);
                write(out, bOut);

                out.write(("\r\n--" + boundary + "\r\n").getBytes());
                out.write(("Content-Type: " + bulkdataContentType + "\r\n").getBytes());
                out.write(("Content-Location: "
                        + ((BulkData) metadata.getValue(fileType.getBulkdataTypeTag())).getURI()
                        + "\r\n")
                        .getBytes());
                out.write("\r\n".getBytes());
                if (compressedPixelData == CompressedPixelData.JPEG && noAppn) {
                    out.write(-1);
                    out.write((byte) JPEG.SOI);
                    out.write(-1);
                }
                writeFile(bulkdataFile, out);
            }
        }
    }

    private static void writeFile(File file, OutputStream out) throws Exception {
        byte[] buf = new byte[8192];
        try (InputStream is = new FileInputStream(file)) {
            int c;
            while ((c = is.read(buf, 0, buf.length)) > 0) {
                out.write(buf, 0, c);
                out.flush();
            }
        }
    }
}
