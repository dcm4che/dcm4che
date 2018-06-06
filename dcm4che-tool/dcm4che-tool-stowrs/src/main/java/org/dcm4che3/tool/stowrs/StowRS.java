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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
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
    private JPEGHeader jpegHeader;
    private String accept;
    private String contentType;
    private String metadataFile;
    private String bulkdataType;
    private String sopCUID;
    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.stowrs.messages");
    private BulkData defaultBulkdata = new BulkData(null, "bulk", false);
    private static final String boundary = "myboundary";
    private byte[] pixelData;
    private File pixelDataFile;

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
        opts.addOption(Option.builder().hasArg().argName("pixel-header").longOpt("pixel-header")
                .desc(rb.getString("pixel-header")).build());
        opts.addOption(Option.builder().hasArg().argName("no-appn").longOpt("no-appn")
                .desc(rb.getString("no-appn")).build());
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
            if (instance.contentType.equals("application/dicom")) {
                LOG.info("Storing DICOM objects");
                stow(instance, null, files);
                return;
            }
            stowMetadataAndBulkdata(instance, files);
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
        LOG.info("Check extension of first file only to determine whether STOW is for dicom or non dicom type of objects.");
        instance.user = cl.getOptionValue("u");
        instance.metadataFile = cl.getOptionValue("f");
        setContentAndAcceptType(instance,  cl, files.get(0));
        instance.pixelHeader = Boolean.valueOf(cl.getOptionValue("pixel-header"));
        instance.noAppn = Boolean.valueOf(cl.getOptionValue("no-appn"));
        instance.sampleMetadataResourceURL = cl.hasOption("sc")
                                                ? "resource:secondaryCaptureImageMetadata.xml"
                                                : cl.hasOption("xc")
                                                    ? "resource:vlPhotographicImageMetadata.xml"
                                                    : cl.hasOption("pdf")
                                                        ? "resource:encapsulatedPDFMetadata.xml" : null;
    }

    enum Extension {
        pdf, mpg, mpg2, mpeg, jpeg, jpg, notBulkdata
    }

    private static Extension getExt(String file) {
        String fileExt = file.substring(file.lastIndexOf(".")+1).toLowerCase();
        for (Extension ext : Extension.values())
            if (ext.name().equals(fileExt))
                return ext;
        return Extension.notBulkdata;
    }

    private static void setContentAndAcceptType(StowRS instance, CommandLine cl, String firstFile) {
        String contentType = getOptionValue("t", cl);
        String acceptType = getOptionValue("a", cl);
        instance.contentType = contentType == null
                                ? getExt(firstFile) == Extension.notBulkdata 
                                    ? "application/dicom" : "application/dicom+xml"
                                : contentType;
        instance.accept = acceptType == null
                            ? instance.contentType.equals("application/dicom")
                                ? "application/dicom+xml" : contentType
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
    
    private static void stowMetadataAndBulkdata(StowRS instance, List<String> files) throws Exception {
        LOG.info("Storing metadata and bulkdata.");
        Path bulkdataFile = Paths.get(files.get(0));
        Extension ext = getExt(files.get(0));
        Attributes metadata = createMetadata(instance, ext, bulkdataFile);
        switch (ext) {
            case pdf:
                readPDF(instance, metadata, bulkdataFile);
                break;
            case mpg:
            case mpg2:
            case mpeg:
            case jpeg:
            case jpg:
                readPixelHeader(instance, metadata, bulkdataFile, ext);
                break;
            case notBulkdata:
                throw new IllegalArgumentException("Unsupported bulkdata type. Read stowrs help.");
        }
        stow(instance, metadata, files);
    }

    private static void readPDF(StowRS instance, Attributes metadata, Path bulkdataFile) throws Exception {
        instance.bulkdataType = "application/pdf";
        instance.sopCUID = UID.EncapsulatedPDFStorage;
        setPDFAttributes(bulkdataFile, metadata);
        supplementBulkdata(metadata, Tag.EncapsulatedDocument, instance);
        readFile(instance, null, null, bulkdataFile.toFile());
    }

    private static Attributes createMetadata(StowRS instance, Extension ext, Path bulkdataFile)
            throws Exception {
        LOG.info("Set defaults, if required attributes are not present.");
        Attributes metadata = createSampleMetadata(instance, ext);
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
        supplementUIDs(metadata);
        supplementDateTime(metadata, bulkdataFile);
        return metadata;
    }

    private static Attributes createSampleMetadata(StowRS instance, Extension ext) throws Exception {
        return createSampleMetadata(ext) && instance.sampleMetadataResourceURL != null
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

    private static void supplementDateTime(Attributes metadata, Path bulkdataFile) {
        Date date = new Date(bulkdataFile.toFile().lastModified());
        metadata.setString(Tag.ContentDate, VR.DA, DateUtils.formatDA(null, date));
        metadata.setString(Tag.ContentTime, VR.TM, DateUtils.formatTM(null, date));
    }

    private static void supplementBulkdata(Attributes metadata, int tag, StowRS instance) {
        if (!metadata.containsValue(tag))
            metadata.setValue(tag, VR.OB, instance.defaultBulkdata);
    }

    private static void readPixelHeader(StowRS instance, Attributes metadata, Path pixelDataFile, Extension ext)
            throws Exception {
        CompressedPixelData compressedPixelData = CompressedPixelData.valueOf(instance, ext);
        File file = pixelDataFile.toFile();
        supplementBulkdata(metadata, Tag.PixelData, instance);
        readFile(instance, metadata, compressedPixelData, file);
    }

    private static void readFile(StowRS instance, Attributes metadata, CompressedPixelData compressedPixelData, File file) throws IOException {
        boolean result = false;
        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            instance.pixelDataFile = file;
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
                        if (instance.pixelDataFile.length() > MAX_BUFFER_SIZE)
                            return;
                    }
                }
            }
            int off = 0;
            if (instance.noAppn)
                off = instance.jpegHeader.offsetAfterAPP();
            instance.pixelData = Arrays.copyOfRange(btemp, off, fileLen);
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

        static CompressedPixelData valueOf(StowRS instance, Extension ext) {
            if (ext == Extension.jpeg || ext == Extension.jpg) {
                instance.bulkdataType = "image/jpeg; transfer-syntax: " + UID.JPEGBaseline1;
                instance.sopCUID = UID.SecondaryCaptureImageStorage;
                return JPEG;
            }
            else {
                instance.bulkdataType = "video/mpeg";
                instance.sopCUID = UID.VideoPhotographicImageStorage;
                return MPEG;
            }
        }
    }

    private static boolean createSampleMetadata(Extension ext) {
        return ext == Extension.jpeg || ext == Extension.jpg || ext == Extension.pdf;
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

    private static void stow(StowRS instance, Attributes metadata, List<String> files)
            throws Exception {
        OutputStream out = null;
        try {
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
            out = connection.getOutputStream();
            out.write(("\r\n--" + boundary + "\r\n").getBytes());
            out.write(("Content-Type: " + instance.contentType + "\r\n").getBytes());
            out.write("\r\n".getBytes());
            writeData(instance, metadata, files, out);
            out.write(("\r\n--" + boundary + "--\r\n").getBytes());
            out.flush();
            logIncoming(connection);
            connection.disconnect();
            LOG.info("STOW successful!");
        } catch (Exception e) {
            LOG.error("Exception : " + e.getMessage());
        } finally {
            if (out != null)
                out.close();
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

    private static void writeData(StowRS instance, Attributes metadata, List<String> files,
                                  OutputStream out) throws Exception {
        if (!instance.contentType.equals("application/dicom")) {
            writeMetadataAndBulkData(instance, metadata, out);
            return;
        }
        Files.copy(Paths.get(files.get(0)), out);
        for (int i = 1; i < files.size(); i++) {
            out.write(("\r\n--"+boundary+"\r\n").getBytes());
            out.write(("Content-Type: " + instance.contentType + "\r\n").getBytes());
            out.write("\r\n".getBytes());
            Files.copy(Paths.get(files.get(i)), out);
        }
    }

    private static void writeMetadataAndBulkData(StowRS instance,
                                                Attributes metadata, OutputStream out)
            throws Exception {
        try (ByteArrayOutputStream bOut = new ByteArrayOutputStream()) {
            if (instance.contentType.equals("application/dicom+xml"))
                SAXTransformer.getSAXWriter(new StreamResult(bOut)).write(metadata);
            else
                try (JsonGenerator gen = Json.createGenerator(bOut)) {
                    gen.writeStartArray();
                    new JSONWriter(gen).write(metadata);
                    gen.writeEnd();
                }
            LOG.debug("Metadata being sent is : " + bOut.toString());
            out.write(bOut.toByteArray());
            out.write(("\r\n--" + boundary + "\r\n").getBytes());
            out.write(("Content-Type: " + instance.bulkdataType + "\r\n").getBytes());
            String contentLoc = instance.bulkdataType.equals("application/pdf")
                    ? ((BulkData) metadata.getValue(Tag.EncapsulatedDocument)).getURI()
                    : ((BulkData) metadata.getValue(Tag.PixelData)).getURI();
            out.write(("Content-Location: " + contentLoc + "\r\n").getBytes());
            out.write("\r\n".getBytes());
            if (instance.jpegHeader != null && instance.noAppn) {
                out.write(-1);
                out.write((byte) JPEG.SOI);
                out.write(-1);
            }
            if (instance.pixelDataFile.length() > MAX_BUFFER_SIZE) {
                writeLargeFile(instance, out);
                return;
            }
            for (byte b : instance.pixelData)
                out.write(b);
            instance.pixelData = new byte[0];
        }
    }

    private static void writeLargeFile(StowRS instance, OutputStream out) throws Exception {
        byte[] buf = new byte[8192];
        try (InputStream is = new FileInputStream(instance.pixelDataFile)) {
            int c;
            while ((c = is.read(buf, 0, buf.length)) > 0) {
                out.write(buf, 0, c);
                out.flush();
            }
        }
    }

    private static void setPDFAttributes(Path bulkDataFile, Attributes metadata) {
        metadata.setInt(Tag.SeriesNumber, VR.IS, 1);
        metadata.setInt(Tag.InstanceNumber, VR.IS, 1);
        metadata.setString(Tag.AcquisitionDateTime, VR.DT,
                DateUtils.formatTM(null, new Date(bulkDataFile.toFile().lastModified())));
        metadata.setString(Tag.BurnedInAnnotation, VR.CS, "YES");
        metadata.setNull(Tag.DocumentTitle, VR.ST);
        metadata.setNull(Tag.ConceptNameCodeSequence, VR.SQ);
        metadata.setString(Tag.MIMETypeOfEncapsulatedDocument, VR.LO,
                "application/pdf");
    }
}
