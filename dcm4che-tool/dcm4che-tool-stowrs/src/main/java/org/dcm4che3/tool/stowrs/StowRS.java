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
 * Portions created by the Initial Developer are Copyright (C) 2012
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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONWriter;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */
public class StowRS {
    private static int FF = 0xff;

    private static int SOF = 0xc0;

    private static int DHT = 0xc4;

    private static int DAC = 0xcc;

    private static int SOI = 0xd8;

    private static int SOS = 0xda;

    private static int jpgHeaderLen;
    private static boolean isPDF = false;
    private static byte[] buffer = new byte[8192];
    static final Logger LOG = LoggerFactory.getLogger(StowRS.class);
    private Attributes keys;
    private static Options opts;
    private String URL;
    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.stowrs.messages");

    public StowRS() {
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        opts = new Options();
        opts.addOption(OptionBuilder.hasArgs(2).withArgName("[seq/]attr=value")
                .withValueSeparator().withDescription(rb.getString("metadata"))
                .create("m"));
        opts.addOption("f", "file", true, rb.getString("file"));
        opts.addOption("u", "url", true, rb.getString("url"));
        opts.addOption("t", "metadata-type", true,
                rb.getString("metadata-type"));
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, StowRS.class);
    }

    private static Attributes configureKeys(StowRS main, CommandLine cl) {
        Attributes temp = new Attributes();
        CLIUtils.addAttributes(temp, cl.getOptionValues("m"));
        return temp;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        CommandLine cl = null;
        try {
            Attributes metadata = new Attributes();
            cl = parseComandLine(args);
            StowRS instance = new StowRS();
            instance.keys = configureKeys(instance, cl);
            LOG.info("added keys for coercion: \n" + instance.keys.toString());
            List<String> files = cl.getArgList();
            if (files == null)
                throw new IllegalArgumentException(
                        "No pixel data files or dicom files specified");
            if (!cl.hasOption("u")) {
                throw new IllegalArgumentException("Missing url");
            } else {
                instance.URL = cl.getOptionValue("u");
            }
            File metadataFile = null;
            if (cl.hasOption("t")) {
                Attributes ds = null;
                if (cl.hasOption("f"))
                    metadataFile = new File(cl.getOptionValue("f"));
                else
                    metadataFile = generateMetaData(cl);
                if (cl.getOptionValue("t").contains("JSON")) {
                    try {
                        ds = parseJSON(metadataFile.getPath());
                    } catch (Exception e) {
                        LOG.error("error parsing metadata file" + e);
                        return;
                    }

                } else if (cl.getOptionValue("t").contains("XML")) {

                    try {
                        ds = SAXReader.parse(metadataFile.getPath());
                    } catch (Exception e) {
                        LOG.error("error parsing metadata file");
                        return;
                    }
                }

                else {
                    throw new IllegalArgumentException(
                            "Bad Type specified for metadata, specify either XML or JSON");
                }
                metadata = ds;
                File bulkDataFile = new File(files.get(0));
                     //do pdf check here
                if (isExtension(bulkDataFile, "pdf")) {
                    // set document metadata
                    isPDF = true;
                    metadata.setValue(Tag.EncapsulatedDocument, VR.OB,
                            new BulkData(null, "bulk", false));
                } else {
                    isPDF = false;
                    // images or video
                    metadata.setValue(Tag.PixelData, VR.OB, new BulkData(null,
                            "bulk", false));
                }
                // add URI here
                if (files.size() > 1)
                    throw new IllegalArgumentException(
                            "Only one bulk data file is allowed with one metadata file");

                if (cl.getOptionValue("t").contains("XML")) {

                    sendMetaDataAndBulkDataXML(instance, metadata, bulkDataFile);
                } else {
                    sendMetaDataAndBulkDataJSON(instance, metadata,
                            bulkDataFile);
                }

            }
            if (!cl.hasOption("t")) {
                for (String path : files) {
                    File toSend = new File(path);
                    instance.sendDicomFile(instance, toSend);
                    LOG.info(path + " with size : " + toSend.length());
                }
            }
        } catch (Exception e) {
            if (!cl.hasOption("u")) {
                LOG.error("stowrs: missing required option -u");
                LOG.error("Try 'stowrs --help' for more information.");
                System.exit(2);
            } else {
                LOG.error("Error: \n");
                e.printStackTrace();
            }

        }
    }

    private static void coerceattributes(Attributes metadata, StowRS instance) {
        if (instance.keys.tags().length > 0)
            LOG.info("Coercing the following keys from specified attributes to metadata:");
        metadata.update(instance.keys, new Attributes());
        LOG.info(instance.keys.toString());
    }

    private static void sendMetaDataAndBulkDataJSON(StowRS instance,
            Attributes metadata, File bulkDataFile) throws IOException {

        String contentTypeBulkData = null;
        String bulkDataTransferSyntax = null;
        URL newUrl = null;
        try {
            newUrl = new URL(instance.URL);
        } catch (MalformedURLException e2) {
            LOG.error("malformed url" + e2.getStackTrace().toString());
        }
        String boundary = "-------gc0p4Jq0M2Yt08jU534c0p";
        HttpURLConnection connection = (HttpURLConnection) newUrl
                .openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        if (isPDF) {
            connection.setRequestProperty("Content-Type",
                    "multipart/related; type=application/json; boundary="
                            + boundary);
            bulkDataTransferSyntax = "transfer-syntax=1.2.840.10008.1.2.1";
            contentTypeBulkData = "application/pdf";
        } else {
            if (isExtension(bulkDataFile, "mpeg")) {
                connection.setRequestProperty("Content-Type",
                        "multipart/related; type=application/json; boundary="
                                + boundary);
                bulkDataTransferSyntax = "transfer-syntax=1.2.840.10008.1.2.4.100";
                contentTypeBulkData = "video/mpeg";
            } else if (isExtension(bulkDataFile, "jpeg")) {
                connection.setRequestProperty("Content-Type",
                        "multipart/related; type=application/json; boundary="
                                + boundary);
                bulkDataTransferSyntax = "transfer-syntax=1.2.840.10008.1.2.4.50";
                contentTypeBulkData = "image/dicom+jpeg";
            } else if (isExtension(bulkDataFile, "mp4")) {
                connection.setRequestProperty("Content-Type",
                        "multipart/related; type=application/json; boundary="
                                + boundary);
                bulkDataTransferSyntax = "transfer-syntax=1.2.840.10008.1.2.4.102";
                contentTypeBulkData = "video/mp4";
            } else {
                throw new IllegalArgumentException(
                        "Unsupported bulkdata (not MPEG2, MPEG4 or JPEG baseline)");
            }
        }

        connection.setRequestProperty("Accept", "application/dicom+xml");
        connection.setRequestProperty("charset", "utf-8");
        connection.setUseCaches(false);

        DataOutputStream wr;

        wr = new DataOutputStream(connection.getOutputStream());
        try {
            wr.writeBytes("\r\n--" + boundary + "\r\n");
            // write metadata
            wr.writeBytes("Content-Type: application/json; transfer-syntax="
                    + bulkDataTransferSyntax + " \r\n");

            wr.writeBytes("\r\n");
        } catch (IOException e1) {
            LOG.error("Error writing metadata");
        }
        // here set pixel or document data attributes
        if (!isPDF)
            setPixelAttributes(bulkDataFile, metadata);
        else
            setPDFAttributes(bulkDataFile, metadata);
        // coerce here before sending metadata
        coerceattributes(metadata, instance);
        JsonGenerator gen = Json.createGenerator(wr);
        JSONWriter writer = new JSONWriter(gen);
        writer.write(metadata);
        gen.flush();
        try {
            wr.writeBytes("\r\n--" + boundary + "\r\n");
            // write bulkdata
            wr.writeBytes("Content-Type: " + contentTypeBulkData + " \r\n");
            wr.writeBytes("Content-Location: " + "bulk" + " \r\n");
            wr.writeBytes("\r\n");
            byte[] bytes = getBytesFromFile(bulkDataFile);
            wr.write(bytes);
            wr.writeBytes("\r\n--" + boundary + "--\r\n");
            wr.flush();
            wr.close();
            String response = connection.getResponseMessage();
            LOG.info("response: " + response);
            connection.disconnect();

        } catch (IOException e) {
            LOG.error("Error writing bulk data");
        }
    }

    private static void setPDFAttributes(File bulkDataFile, Attributes metadata) {
        metadata.setString(Tag.SOPClassUID, VR.UI, UID.EncapsulatedPDFStorage);
        metadata.setInt(Tag.InstanceNumber, VR.IS, 1);
        metadata.setString(Tag.ContentDate, VR.DA,
                DateUtils.formatDA(null, new Date(bulkDataFile.lastModified())));
        metadata.setString(Tag.ContentTime, VR.TM,
                DateUtils.formatTM(null, new Date(bulkDataFile.lastModified())));
        metadata.setString(Tag.AcquisitionDateTime, VR.DT,
                DateUtils.formatTM(null, new Date(bulkDataFile.lastModified())));
        metadata.setString(Tag.BurnedInAnnotation, VR.CS, "YES");
        metadata.setNull(Tag.DocumentTitle, VR.ST);
        metadata.setNull(Tag.ConceptNameCodeSequence, VR.SQ);
        metadata.setString(Tag.MIMETypeOfEncapsulatedDocument, VR.LO,
                "application/pdf");

    }

    private static boolean isExtension(File bulkDataFile, String string)
            throws IOException {
        if (string.compareToIgnoreCase("mpeg") == 0) {
            if ((Files.probeContentType(bulkDataFile.toPath()) != null && ((Files
                    .probeContentType(bulkDataFile.toPath())
                    .compareToIgnoreCase("mpeg") == 0)
                    || (Files.probeContentType(bulkDataFile.toPath())
                            .compareToIgnoreCase("mpg") == 0)
                    || (Files.probeContentType(bulkDataFile.toPath())
                            .compareToIgnoreCase("m2v") == 0) || (Files
                    .probeContentType(bulkDataFile.toPath())
                    .compareToIgnoreCase("mpv") == 0)))
                    || bulkDataFile.getName().toLowerCase()
                            .endsWith("mpeg".toLowerCase())
                    || bulkDataFile.getName().toLowerCase()
                            .endsWith("mpg".toLowerCase())
                    || bulkDataFile.getName().toLowerCase()
                            .endsWith("mpv".toLowerCase())
                    || bulkDataFile.getName().toLowerCase()
                            .endsWith("m2v".toLowerCase())) {
                return true;
            }
        }

        else if (string.compareToIgnoreCase("jpeg") == 0) {
            if ((Files.probeContentType(bulkDataFile.toPath()) != null && ((Files
                    .probeContentType(bulkDataFile.toPath())
                    .compareToIgnoreCase("jpeg") == 0)
                    || (Files.probeContentType(bulkDataFile.toPath())
                            .compareToIgnoreCase("jpg") == 0)
                    || (Files.probeContentType(bulkDataFile.toPath())
                            .compareToIgnoreCase("jfif") == 0) || (Files
                    .probeContentType(bulkDataFile.toPath())
                    .compareToIgnoreCase("jif") == 0)))
                    || bulkDataFile.getName().toLowerCase()
                            .endsWith("jpeg".toLowerCase())
                    || bulkDataFile.getName().toLowerCase()
                            .endsWith("jpg".toLowerCase())
                    || bulkDataFile.getName().toLowerCase()
                            .endsWith("jfif".toLowerCase())
                    || bulkDataFile.getName().toLowerCase()
                            .endsWith("jif".toLowerCase())) {
                return true;
            }
        } else if (string.compareToIgnoreCase("mp4") == 0) {
            if ((Files.probeContentType(bulkDataFile.toPath()) != null && ((Files
                    .probeContentType(bulkDataFile.toPath())
                    .compareToIgnoreCase("mp4") == 0)
                    || (Files.probeContentType(bulkDataFile.toPath())
                            .compareToIgnoreCase("m4a") == 0) || (Files
                    .probeContentType(bulkDataFile.toPath())
                    .compareToIgnoreCase("m4v") == 0)))
                    || bulkDataFile.getName().toLowerCase()
                            .endsWith("mp4".toLowerCase())
                    || bulkDataFile.getName().toLowerCase()
                            .endsWith("m4a".toLowerCase())
                    || bulkDataFile.getName().toLowerCase()
                            .endsWith("m4v".toLowerCase())) {
                return true;
            }

        } else if (string.compareToIgnoreCase("pdf") == 0) {
            if (Files.probeContentType(bulkDataFile.toPath()) != null
                    && (Files.probeContentType(bulkDataFile.toPath())
                            .compareToIgnoreCase("pdf") == 0)
                    || bulkDataFile.getName().toLowerCase()
                            .endsWith("pdf".toLowerCase())) {
                return true;
            }
        } else
            throw new IllegalArgumentException(
                    "not a recognized bulk file type");

        return false;

    }

    private static void sendMetaDataAndBulkDataXML(StowRS instance,
            Attributes metadata, File bulkDataFile) throws IOException {

        String contentTypeBulkData;
        String bulkDataTransferSyntax;
        URL newUrl = null;
        try {
            newUrl = new URL(instance.URL);
        } catch (MalformedURLException e2) {
            LOG.error("malformed url" + e2.getStackTrace().toString());
        }
        String boundary = "-------gc0p4Jq0M2Yt08jU534c0p";
        HttpURLConnection connection = (HttpURLConnection) newUrl
                .openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        if (isPDF) {
            connection.setRequestProperty("Content-Type",
                    "multipart/related; type=application/dicom+xml; boundary="
                            + boundary);
            bulkDataTransferSyntax = "transfer-syntax=1.2.840.10008.1.2.1";
            contentTypeBulkData = "application/pdf";
        } else {
            if (isExtension(bulkDataFile, "mpeg")) {
                connection.setRequestProperty("Content-Type",
                        "multipart/related; type=application/dicom+xml; boundary="
                                + boundary);
                bulkDataTransferSyntax = "transfer-syntax=1.2.840.10008.1.2.4.100";
                contentTypeBulkData = "video/mpeg";
            } else if (isExtension(bulkDataFile, "jpeg")) {
                connection.setRequestProperty("Content-Type",
                        "multipart/related; type=application/dicom+xml; boundary="
                                + boundary);
                bulkDataTransferSyntax = "transfer-syntax=1.2.840.10008.1.2.4.50";
                contentTypeBulkData = "image/dicom+jpeg";
            } else if (isExtension(bulkDataFile, "mp4")) {
                connection.setRequestProperty("Content-Type",
                        "multipart/related; type=application/dicom+xml; boundary="
                                + boundary);
                bulkDataTransferSyntax = "transfer-syntax=1.2.840.10008.1.2.4.102";
                contentTypeBulkData = "video/mp4";
            } else {
                throw new IllegalArgumentException(
                        "Unsupported bulkdata (not MPEG2, MPEG4 or JPEG baseline)");
            }
        }

        connection.setRequestProperty("Accept", "application/dicom+xml");
        connection.setRequestProperty("charset", "utf-8");
        connection.setUseCaches(false);

        DataOutputStream wr;

        wr = new DataOutputStream(connection.getOutputStream());
        try {
            wr.writeBytes("\r\n--" + boundary + "\r\n");
            // write metadata
            wr.writeBytes("Content-Type: application/dicom+xml; transfer-syntax="
                    + bulkDataTransferSyntax + " \r\n");

            wr.writeBytes("\r\n");
        } catch (IOException e1) {
            LOG.error("Error writing metadata");
        }
        // here set pixel or document data attributes
        if (!isPDF)
            setPixelAttributes(bulkDataFile, metadata);
        else
            setPDFAttributes(bulkDataFile, metadata);
        // coerce here before sending metadata
        coerceattributes(metadata, instance);
        try {
            SAXTransformer.getSAXWriter(new StreamResult(wr)).write(metadata);
        } catch (TransformerConfigurationException e) {
            LOG.error("Error transforming xml" + e.getStackTrace().toString());
        } catch (SAXException e) {
            // exception wrapper
        }
        try {
            wr.writeBytes("\r\n--" + boundary + "\r\n");
            // write bulkdata
            wr.writeBytes("Content-Type: " + contentTypeBulkData + " \r\n");
            wr.writeBytes("Content-Location: " + "bulk" + " \r\n");
            wr.writeBytes("\r\n");
            byte[] bytes = getBytesFromFile(bulkDataFile);
            wr.write(bytes);
            wr.writeBytes("\r\n--" + boundary + "--\r\n");
            wr.flush();
            wr.close();
            String response = connection.getResponseMessage();
            LOG.info("response: " + response);
            connection.disconnect();

        } catch (IOException e) {
            LOG.error("Error writing bulk data");
        }

    }

    private static void setPixelAttributes(File bulkDataFile,
            Attributes metadata) {
        metadata.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
        metadata.setInt(Tag.NumberOfFrames, VR.IS, 1);
        metadata.setInt(Tag.InstanceNumber, VR.IS, 1);
        metadata.setNull(Tag.PurposeOfReferenceCodeSequence, VR.SQ);
        jpgHeaderLen = 0;
        DataInputStream jpgInput;
        try {
            jpgInput = new DataInputStream(new BufferedInputStream(
                    new FileInputStream(bulkDataFile)));

            readHeader(metadata, jpgInput);
        } catch (FileNotFoundException e) {
            LOG.error("Error reading the image file "
                    + e.getStackTrace().toString());
        } catch (IOException e) {
            LOG.error("Error parsing jpeg header (malformed header) "
                    + e.getStackTrace().toString());
        }
        ensureUS(metadata, Tag.BitsAllocated, 8);
        ensureUS(metadata, Tag.BitsStored, metadata.getInt(Tag.BitsAllocated,
                (buffer[jpgHeaderLen] & 0xff) > 8 ? 16 : 8));
        ensureUS(
                metadata,
                Tag.HighBit,
                metadata.getInt(Tag.BitsStored, (buffer[jpgHeaderLen] & 0xff)) - 1);
        ensureUS(metadata, Tag.PixelRepresentation, 0);
        ensureUID(metadata, Tag.StudyInstanceUID);
        ensureUID(metadata, Tag.SeriesInstanceUID);
        ensureUID(metadata, Tag.SOPInstanceUID);
        Date now = new Date();
        metadata.setDate(Tag.InstanceCreationDate, VR.DA, now);
        metadata.setDate(Tag.InstanceCreationTime, VR.TM, now);
    }

    private static void readHeader(Attributes attrs, DataInputStream jpgInput)
            throws IOException {
        if (jpgInput.read() != FF || jpgInput.read() != SOI
                || jpgInput.read() != FF) {
            throw new IOException("JPEG stream does not start with FF D8 FF");
        }
        int marker = jpgInput.read();
        int segmLen;
        boolean seenSOF = false;
        buffer[0] = (byte) FF;
        buffer[1] = (byte) SOI;
        buffer[2] = (byte) FF;
        buffer[3] = (byte) marker;
        jpgHeaderLen = 4;
        while (marker != SOS) {
            segmLen = jpgInput.readUnsignedShort();
            if (buffer.length < jpgHeaderLen + segmLen + 2) {
                growBuffer(jpgHeaderLen + segmLen + 2);
            }
            buffer[jpgHeaderLen++] = (byte) (segmLen >>> 8);
            buffer[jpgHeaderLen++] = (byte) segmLen;
            jpgInput.readFully(buffer, jpgHeaderLen, segmLen - 2);
            if ((marker & 0xf0) == SOF && marker != DHT && marker != DAC) {
                seenSOF = true;
                int p = buffer[jpgHeaderLen] & 0xff;
                int y = ((buffer[jpgHeaderLen + 1] & 0xff) << 8)
                        | (buffer[jpgHeaderLen + 2] & 0xff);
                int x = ((buffer[jpgHeaderLen + 3] & 0xff) << 8)
                        | (buffer[jpgHeaderLen + 4] & 0xff);
                int nf = buffer[jpgHeaderLen + 5] & 0xff;
                attrs.setInt(Tag.SamplesPerPixel, VR.US, nf);
                if (nf == 3) {
                    attrs.setString(Tag.PhotometricInterpretation, VR.CS,
                            "YBR_FULL_422");
                    attrs.setInt(Tag.PlanarConfiguration, VR.US, 0);
                } else {
                    attrs.setString(Tag.PhotometricInterpretation, VR.CS,
                            "MONOCHROME2");
                }
                attrs.setInt(Tag.Rows, VR.US, y);
                attrs.setInt(Tag.Columns, VR.US, x);
                attrs.setInt(Tag.BitsAllocated, VR.US, p > 8 ? 16 : 8);
                attrs.setInt(Tag.BitsStored, VR.US, p);
                attrs.setInt(Tag.HighBit, VR.US, p - 1);
                attrs.setInt(Tag.PixelRepresentation, VR.US, 0);
            }
            if (jpgInput.read() != 0xff) {
                throw new IOException("Missing SOS segment in JPEG stream");
            }
            marker = jpgInput.read();
            buffer[jpgHeaderLen++] = (byte) 0xff;
            buffer[jpgHeaderLen++] = (byte) marker;
        }
        if (!seenSOF) {
            throw new IOException("Missing SOF segment in JPEG stream");
        }
    }

    private static void growBuffer(int minSize) {
        int newSize = buffer.length << 1;
        while (newSize < minSize) {
            newSize <<= 1;
        }
        byte[] tmp = new byte[newSize];
        System.arraycopy(buffer, 0, tmp, 0, jpgHeaderLen);
        buffer = tmp;
    }

    private static byte[] getBytesFromFile(File file) {
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        try {
            fis = new FileInputStream(file);
            bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            try {
                for (int readNum; (readNum = fis.read(buf)) != -1;) {
                    bos.write(buf, 0, readNum);
                }
            } catch (IOException e) {
                LOG.error(e.getStackTrace().toString());
            }
        } catch (FileNotFoundException e1) {
            LOG.error("File not found" + e1.getStackTrace().toString());
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    LOG.error("Error closing file {} , Exception: {}",
                            file.getName(), e.getStackTrace().toString());
                }
        }

        return bos.toByteArray();
    }

    private static File generateMetaData(CommandLine cl) {
        
        if (cl.getOptionValue("t").contains("JSON")){
            LOG.info("No metadata file specified, using etc/stowrs/metadata.json");
            return new File("../etc/stowrs/metadata.json");
        }
        else{
            LOG.info("No metadata file specified, using etc/stowrs/metadata.xml");
            return new File("../etc/stowrs/metadata.xml");
        }
    }

    public void sendDicomFile(StowRS instance, File f) throws IOException,
            InterruptedException {
        doRequestDICOM(instance.URL, f);
    }

    private void doRequestDICOM(String url, File f) {
        try {
            URL newUrl = new URL(url);
            String boundary = "-------gc0p4Jq0M2Yt08jU534c0p";
            HttpURLConnection connection = (HttpURLConnection) newUrl
                    .openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "multipart/related; type=application/dicom; boundary="
                            + boundary);
            connection.setRequestProperty("Accept", "application/dicom+xml");
            connection.setRequestProperty("charset", "utf-8");
            connection.setUseCaches(false);

            DataOutputStream wr;
            wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes("\r\n--" + boundary + "\r\n");
            wr.writeBytes("Content-Disposition: inline; name=\"file[]\"; filename=\""
                    + f.getName() + "\"\r\n");
            wr.writeBytes("Content-Type: application/dicom \r\n");
            wr.writeBytes("\r\n");
            FileInputStream fis = new FileInputStream(f);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            try {
                for (int readNum; (readNum = fis.read(buf)) != -1;) {
                    bos.write(buf, 0, readNum);
                }
            } catch (IOException e) {
                LOG.error(e.getStackTrace().toString());
            }
            byte[] bytes = bos.toByteArray();
            wr.write(bytes);
            wr.writeBytes("\r\n--" + boundary + "--\r\n");
            wr.flush();
            wr.close();
            String response = connection.getResponseMessage();
            LOG.info("response: " + response);
            connection.disconnect();
        } catch (IOException e) {
            LOG.error("error writing to http data output stream"
                    + e.getStackTrace().toString());
        }
    }

    private static void ensureUID(Attributes attrs, int tag) {
        if (!attrs.containsValue(tag)) {
            attrs.setString(tag, VR.UI, UIDUtils.createUID());
        }
    }

    private static void ensureUS(Attributes attrs, int tag, int val) {
        if (!attrs.containsValue(tag)) {
            attrs.setInt(tag, VR.US, val);
        }
    }

    public static Attributes parseJSON(String fname) throws Exception {
        Attributes attrs = new Attributes();
        parseJSON(fname, attrs);
        return attrs;
    }

    private static JSONReader parseJSON(String fname, Attributes attrs)
            throws IOException {
        @SuppressWarnings("resource")
        InputStream in = fname.equals("-") ? System.in : new FileInputStream(
                fname);
        try {
            JSONReader reader = new JSONReader(
                    Json.createParser(new InputStreamReader(in, "UTF-8")));
            reader.readDataset(attrs);
            return reader;
        } finally {
            if (in != System.in)
                SafeClose.close(in);
        }
    }
}
