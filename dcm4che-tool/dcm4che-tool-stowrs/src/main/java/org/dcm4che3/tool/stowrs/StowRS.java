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
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
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
import org.dcm4che3.imageio.codec.jpeg.JPEGHeader;
import org.dcm4che3.imageio.codec.mpeg.MPEGHeader;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.json.JSONWriter;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.service.DicomServiceException;
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
    static final Logger LOG = LoggerFactory.getLogger(StowRS.class);
    private Attributes keys;
    private static Options opts;
    private String URL;
    private String user;
    private boolean noAppn;
    private boolean pixelHeader;
    private String sampleMetadataResourceURL;
    private boolean isSecondaryCapture;
    private JPEGHeader jpegHeader;
    private String accept;
    private String contentType;
    private String metadataFile;
    private String sopCUID;
    private String contentLoc;
    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.stowrs.messages");
    private static final String boundary = "myboundary";
    private static final String APPLN_DICOM = "application/dicom";
    private static final String APPLN_DICOM_XML = "application/dicom+xml";
    private FileType fileType;
    private List<Attributes> metadataList = new ArrayList<>();
    private Map<String, Object> contentLocBulkData = new HashMap<>();

    private static final int INIT_BUFFER_SIZE = 8192;
    private static final int MAX_BUFFER_SIZE = 10485768;
    private static final String patName = "STOW-RS-PatientName";

    private static final int[] IUIDS_TAGS = {
            Tag.StudyInstanceUID,
            Tag.SeriesInstanceUID,
            Tag.SOPInstanceUID
    };

    private static final int[] IMAGE_PIXEL_TAGS = {
            Tag.SamplesPerPixel,
            Tag.PhotometricInterpretation,
            Tag.Rows,
            Tag.Columns,
            Tag.BitsAllocated,
            Tag.BitsStored,
            Tag.HighBit,
            Tag.PixelRepresentation
    };
    private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();


    public StowRS() {
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseCommandLine(String[] args)
            throws ParseException {
        opts = new Options();
        opts.addOption(Option.builder("m").numberOfArgs(2).argName("[seq/]attr=value")
                .valueSeparator().desc(rb.getString("metadata"))
                .build());
        opts.addOption("f", "file", true, rb.getString("file"));
        opts.addOption(Option.builder().hasArg().argName("url").longOpt("url")
                .desc(rb.getString("url")).build());
        opts.addOption(Option.builder("u").hasArg().argName("user:password").longOpt("user")
                .desc(rb.getString("user")).build());
        opts.addOption("t", "type", true, rb.getString("type"));
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
        opts.addOption("a","accept", true, rb.getString("accept"));
        OptionGroup sampleMetadataOG = new OptionGroup();
        sampleMetadataOG.addOption(Option.builder()
                .longOpt("sc")
                .hasArg(false)
                .desc(rb.getString("sc"))
                .build());
        sampleMetadataOG.addOption(Option.builder()
                .longOpt("xc")
                .hasArg(false)
                .desc(rb.getString("xc"))
                .build());
        sampleMetadataOG.addOption(Option.builder()
                .longOpt("pdf")
                .hasArg(false)
                .desc(rb.getString("pdf"))
                .build());
        opts.addOptionGroup(sampleMetadataOG);
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, StowRS.class);
    }

    private static Attributes configureKeys(CommandLine cl) {
        Attributes temp = new Attributes();
        CLIUtils.addAttributes(temp, cl.getOptionValues("m"));
        return temp;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            CommandLine cl = parseCommandLine(args);
            StowRS instance = new StowRS();
            instance.keys = configureKeys(cl);
            LOG.info("added keys for coercion: \n" + instance.keys.toString());
            List<String> files = cl.getArgList();
            doNecessaryChecks(cl, instance, files);
            LOG.info("Storing objects.");
            stow(instance, files);
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

    private static void doNecessaryChecks(CommandLine cl, StowRS instance, List<String> files)
            throws Exception {
        if (files.isEmpty())
            throw new MissingArgumentException("No pixel data files or dicom files specified");
        if ((instance.URL = cl.getOptionValue("url")) == null)
            throw new MissingOptionException("Missing url.");
        checkFileType(files, instance);
        instance.user = cl.getOptionValue("u");
        instance.metadataFile = cl.getOptionValue("f");
        setContentAndAcceptType(instance, cl);
        instance.pixelHeader = cl.hasOption("pixel-header");
        instance.noAppn = cl.hasOption("no-appn");
        instance.isSecondaryCapture = cl.hasOption("sc");
        instance.sampleMetadataResourceURL = cl.hasOption("sc")
                ? "resource:secondaryCaptureImageMetadata.xml"
                : cl.hasOption("xc")
                    ? "resource:vlPhotographicImageMetadata.xml"
                    : cl.hasOption("pdf")
                        ? "resource:encapsulatedPDFMetadata.xml" : null;
    }

    enum FileType {
        PDF("application/pdf"),
        IMAGE("image/jpeg"),
        VIDEO("video/mpeg"),
        NOT_BULKDATA("");

        String bulkdataType;

        FileType(String bulkdataType) {
            this.bulkdataType = bulkdataType;
        }
    }

    enum Extension {
        pdf(FileType.PDF),
        mpg(FileType.VIDEO),
        mpg2(FileType.VIDEO),
        mpeg(FileType.VIDEO),
        jpeg(FileType.IMAGE),
        jpg(FileType.IMAGE),
        notBulkdata(FileType.NOT_BULKDATA);

        final FileType fileType;

        Extension(FileType fileType) {
            this.fileType = fileType;
        }
    }

    private static void checkFileType(List<String> files, StowRS instance) {
        FileType fileType = getExt(files.get(0)).fileType;
        for (int i = 1; i < files.size(); i++)
            if (fileType != getExt(files.get(i)).fileType)
                throw new IllegalArgumentException("Uploading multiple bulkdata files of different file types not supported.");

        instance.fileType = fileType;
        if (fileType == FileType.NOT_BULKDATA)
            return;

        instance.sopCUID = fileType == FileType.IMAGE
                ? instance.isSecondaryCapture
                    ? UID.SecondaryCaptureImageStorage : UID.VLPhotographicImageStorage
                : fileType == FileType.VIDEO
                    ? UID.VideoPhotographicImageStorage
                    : UID.EncapsulatedPDFStorage;
    }

    private static Extension getExt(String file) {
        String fileExt = file.substring(file.lastIndexOf(".")+1).toLowerCase();
        for (Extension ext : Extension.values())
            if (ext.name().equals(fileExt))
                return ext;
        return Extension.notBulkdata;
    }

    private static void setContentAndAcceptType(StowRS instance, CommandLine cl) {
        String contentType = getOptionValue("t", cl);
        instance.contentType = contentType == null
                                ? instance.fileType == FileType.NOT_BULKDATA
                                    ? APPLN_DICOM : APPLN_DICOM_XML
                                : contentType;
        String acceptType = getOptionValue("a", cl);
        instance.accept = acceptType == null
                            ? instance.contentType.equals(APPLN_DICOM)
                                ? APPLN_DICOM_XML : contentType
                            : acceptType;
    }

    private static String getOptionValue(String option, CommandLine cl) {
        String optionValue = cl.getOptionValue(option);
        if (optionValue == null)
            return null;

        optionValue = optionValue.toLowerCase();
        if (!(optionValue.equals("xml") || optionValue.equals("json")))
            throw new IllegalArgumentException("Unsupported type. Read -" + option + " option in stowrs help");

        return "application/dicom+" + optionValue;
    }

    private static Attributes createMetadata(StowRS instance, File bulkdataFile, Attributes staticMetadata) throws Exception {
        LOG.info("Supplementing file specific metadata.");
        Attributes metadata = new Attributes(staticMetadata);
        switch (instance.fileType) {
            case PDF:
                pdfMetadata(instance, metadata, bulkdataFile);
                break;
            case IMAGE:
            case VIDEO:
                pixelMetadata(instance, metadata, bulkdataFile);
                break;
            default:
                throw new IllegalArgumentException("Unsupported bulkdata type. Read stowrs help.");
        }
        return metadata;
    }

    private static void pdfMetadata(StowRS instance, Attributes metadata, File bulkdataFile) {
        setPDFAttributes(bulkdataFile, metadata);
        supplementBulkdata(metadata, Tag.EncapsulatedDocument, instance);
        instance.contentLocBulkData.put(instance.contentLoc, bulkdataFile);
        supplementUIDs(metadata);
        supplementDateTime(metadata, bulkdataFile);
    }

    private static Attributes createStaticMetadata(StowRS instance)
            throws Exception {
        LOG.info("Creating static metadata. Set defaults, if essential attributes are not present.");
        Attributes metadata = createSampleMetadata(instance);
        String metadataFile = instance.metadataFile;
        if (metadataFile != null) {
            String metadataFileExt = metadataFile.substring(metadataFile.lastIndexOf(".")+1).toLowerCase();
            if (!metadataFileExt.equals("xml"))
                throw new IllegalArgumentException("Metadata file extension not supported. Read -f option in stowrs help");

            metadata = SAXReader.parse(Paths.get(metadataFile).toString(), metadata);
        }

        metadata.update(Attributes.UpdatePolicy.OVERWRITE, instance.keys, new Attributes());
        supplementDefaultValue(metadata, Tag.PatientName, VR.PN, patName);
        supplementDefaultValue(metadata, Tag.SOPClassUID, VR.UI, instance.sopCUID);
        return metadata;
    }

    private static Attributes createSampleMetadata(StowRS instance) throws Exception {
        return (instance.fileType == FileType.IMAGE || instance.fileType == FileType.PDF)
                && instance.sampleMetadataResourceURL != null
                ? SAXReader.parse(StreamUtils.openFileOrURL(instance.sampleMetadataResourceURL))
                : new Attributes();
    }

    private static void supplementUIDs(Attributes metadata) {
        for (int tag : IUIDS_TAGS)
            if (!metadata.containsValue(tag))
                metadata.setString(tag, VR.UI, UIDUtils.createUID());
    }

    private static void supplementDefaultValue(Attributes metadata, int tag, VR vr, String value) {
        if (!metadata.containsValue(tag))
            metadata.setString(tag, vr, value);
    }

    private static void supplementDateTime(Attributes metadata, File bulkdataFile) {
        Date date = new Date(bulkdataFile.lastModified());
        metadata.setString(Tag.ContentDate, VR.DA, DateUtils.formatDA(null, date));
        metadata.setString(Tag.ContentTime, VR.TM, DateUtils.formatTM(null, date));
    }

    private static void supplementBulkdata(Attributes metadata, int tag, StowRS instance) {
        if (!metadata.containsValue(tag)) {
            instance.contentLoc = "bulk" + UIDUtils.createUID();
            metadata.setValue(tag, VR.OB, new BulkData(null, instance.contentLoc, false));
        }
    }

    private static void pixelMetadata(StowRS instance, Attributes metadata, File pixelDataFile)
            throws Exception {
        supplementBulkdata(metadata, Tag.PixelData, instance);
        supplementUIDs(metadata);
        supplementDateTime(metadata, pixelDataFile);
        CompressedPixelData compressedPixelData = CompressedPixelData.valueOf(instance);
        if (instance.pixelHeader)
            readFile(instance, metadata, compressedPixelData, pixelDataFile);
        else
            instance.contentLocBulkData.put(instance.contentLoc, pixelDataFile);
    }

    private static void readFile(StowRS instance, Attributes metadata, CompressedPixelData compressedPixelData, File file) throws IOException {
        boolean result = false;
        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] btemp = ByteUtils.EMPTY_BYTES;
            int rl = 0;
            int grow = INIT_BUFFER_SIZE;
            int fileLen = (int) file.length();
            while (rl == btemp.length && rl < fileLen) {
                int newLen = grow += rl;
                btemp = Arrays.copyOf(btemp, newLen);
                rl += StreamUtils.readAvailable(bis, btemp, rl, btemp.length - rl);

                if (!result && compressedPixelData != null && instance.pixelHeader) {
                    byte[] header = Arrays.copyOf(btemp, newLen);
                    if (compressedPixelData.parseHeader(instance, header, metadata, file)) {
                        result = true;
                        verifyImagePixelModule(metadata);
                        if (fileLen > MAX_BUFFER_SIZE) {
                            instance.contentLocBulkData.put(instance.contentLoc, file);
                            return;
                        }
                    }
                }
            }
            int off = 0;
            if (instance.noAppn && instance.jpegHeader != null)
                off = instance.jpegHeader.offsetAfterAPP();
            instance.contentLocBulkData.put(instance.contentLoc, Arrays.copyOfRange(btemp, off, fileLen));
        }
    }

    private enum CompressedPixelData {
        JPEG {
            @Override
            boolean parseHeader(StowRS instance, byte[] header, Attributes metadata, File file) {
                instance.jpegHeader = new JPEGHeader(header, org.dcm4che3.imageio.codec.jpeg.JPEG.SOS);
                return instance.jpegHeader.toAttributes(metadata) != null;
            }
        },
        MPEG {
            @Override
            boolean parseHeader(StowRS instance, byte[] header, Attributes metadata, File file) {
                return new MPEGHeader(header).toAttributes(metadata, file.length()) != null;
            }
        };

        abstract boolean parseHeader(StowRS instance, byte[] header, Attributes metadata, File file);

        static CompressedPixelData valueOf(StowRS instance) {
            return instance.fileType == FileType.IMAGE ? JPEG : MPEG;
        }
    }

    private static void verifyImagePixelModule(Attributes metadata) throws DicomServiceException {
        for (int tag : IMAGE_PIXEL_TAGS)
            if (!metadata.containsValue(tag))
                throw missingAttribute(tag);
        if (metadata.getInt(Tag.SamplesPerPixel, 1) > 1 && !metadata.containsValue(Tag.PlanarConfiguration))
            throw missingAttribute(Tag.PlanarConfiguration);
    }

    private static DicomServiceException missingAttribute(int tag) {
        return new DicomServiceException(Status.IdentifierDoesNotMatchSOPClass,
                "Missing " + DICT.keywordOf(tag) + " " + TagUtils.toString(tag));
    }

    private static void stow(StowRS instance, List<String> files)
            throws Exception {
        URL newUrl = new URL(instance.URL);
        final HttpURLConnection connection = (HttpURLConnection) newUrl.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                "multipart/related; type=\"" + instance.contentType + "\"; boundary=" + boundary);
        connection.setRequestProperty("Accept", instance.accept);
        logOutgoing(connection);
        if (instance.user != null) {
            String basicAuth = basicAuth(instance.user);
            LOG.info("> Authorization: " + basicAuth);
            connection.setRequestProperty("Authorization", basicAuth);
        }
        try (OutputStream out = connection.getOutputStream()) {
            writeData(instance, files, out);
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
        LOG.info("< HTTP/1.1 Response: " + String.valueOf(connection.getResponseCode()) + " " + connection.getResponseMessage());
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

    private static void writeData(StowRS instance, List<String> files,
                                  OutputStream out) throws Exception {
        if (!instance.contentType.equals("application/dicom")) {
            writeMetadataAndBulkData(instance, out, files);
            return;
        }
        for (String file : files) {
            out.write(("\r\n--" + boundary + "\r\n").getBytes());
            out.write(("Content-Type: " + instance.contentType + "\r\n").getBytes());
            out.write("\r\n".getBytes());
            Files.copy(Paths.get(file), out);
        }
    }

    private static void writeMetadataAndBulkData(StowRS instance, OutputStream out, List<String> files)
            throws Exception {
        try (ByteArrayOutputStream bOut = new ByteArrayOutputStream()) {
            boolean isXmlContentType = instance.contentType.equals(APPLN_DICOM_XML);
            Attributes staticMetadata = createStaticMetadata(instance);
            for (String file : files) {
                instance.metadataList.add(createMetadata(instance, Paths.get(file).toFile(), staticMetadata));
                if (isXmlContentType)
                    break;
            }

            if (isXmlContentType)
                SAXTransformer.getSAXWriter(new StreamResult(bOut)).write(instance.metadataList.get(0));
            else
                try (JsonGenerator gen = Json.createGenerator(bOut)) {
                    gen.writeStartArray();
                    for (Attributes metadata : instance.metadataList)
                        new JSONWriter(gen).write(metadata);
                    gen.writeEnd();
                    gen.flush();
                }
            LOG.debug("Metadata being sent is : " + bOut.toString());
            out.write(("\r\n--" + boundary + "\r\n").getBytes());
            out.write(("Content-Type: " + metadataContentType(instance, isXmlContentType) + "\r\n").getBytes());
            out.write("\r\n".getBytes());
            out.write(bOut.toByteArray());

            for (Map.Entry<String, Object> entry : instance.contentLocBulkData.entrySet()) {
                out.write(("\r\n--" + boundary + "\r\n").getBytes());
                out.write(("Content-Type: " + instance.fileType.bulkdataType + "\r\n").getBytes());
                String contentLoc = entry.getKey();
                out.write(("Content-Location: " + contentLoc + "\r\n").getBytes());
                out.write("\r\n".getBytes());
                Object value = instance.contentLocBulkData.get(contentLoc);
                if (instance.fileType == FileType.PDF || !instance.pixelHeader) {
                    writeFile((File) value, out);
                    continue;
                }
                if (instance.jpegHeader != null && instance.noAppn) {
                    out.write(-1);
                    out.write((byte) JPEG.SOI);
                    out.write(-1);
                }
                if (value instanceof File) {
                    writeFile((File) value, out);
                    continue;
                }
                out.write((byte[]) value);

                if (isXmlContentType)
                    break;
            }
            instance.contentLocBulkData.clear();
            instance.metadataList.clear();
        }
    }

    private static String metadataContentType(StowRS instance, boolean isXMLContentType) {
        return isXMLContentType
                ? instance.contentType
                : instance.fileType == FileType.IMAGE
                    ? instance.contentType + "; transfer-syntax=" + UID.JPEGBaseline1
                    : instance.contentType + "; transfer-syntax=" + UID.ExplicitVRLittleEndian;
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

    private static void setPDFAttributes(File bulkDataFile, Attributes metadata) {
        metadata.setInt(Tag.SeriesNumber, VR.IS, 1);
        metadata.setInt(Tag.InstanceNumber, VR.IS, 1);
        metadata.setString(Tag.AcquisitionDateTime, VR.DT,
                DateUtils.formatTM(null, new Date(bulkDataFile.lastModified())));
        metadata.setString(Tag.BurnedInAnnotation, VR.CS, "YES");
        metadata.setNull(Tag.DocumentTitle, VR.ST);
        metadata.setNull(Tag.ConceptNameCodeSequence, VR.SQ);
        metadata.setString(Tag.MIMETypeOfEncapsulatedDocument, VR.LO,
                "application/pdf");
    }
}
