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

import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.jpeg.JPEG;
import org.dcm4che3.imageio.codec.jpeg.JPEGHeader;
import org.dcm4che3.imageio.codec.mpeg.MPEGHeader;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.json.JSONWriter;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

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
    private boolean noAppn;
    private boolean pixelHeader;
    private JPEGHeader jpegHeader;
    private String accept;
    private String contentType;
    private String metadataFile;
    private String bulkdataType;
    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.stowrs.messages");
    private BulkData defaultBulkdata = new BulkData(null, "bulk", false);
    private static final String boundary = "myboundary";
    private byte[] pixelData;

    public StowRS() {
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseCommandLine(String[] args)
            throws ParseException {
        opts = new Options();
        opts.addOption(OptionBuilder.hasArgs(2).withArgName("[seq/]attr=value")
                .withValueSeparator().withDescription(rb.getString("metadata"))
                .create("m"));
        opts.addOption("f", "file", true, rb.getString("file"));
        opts.addOption("u", "url", true, rb.getString("url"));
        opts.addOption("t", "type", true, rb.getString("type"));
        opts.addOption("ph", "pixel-header", true, rb.getString("pixel-header"));
        opts.addOption("na","no-appn", true, rb.getString("no-appn"));
        opts.addOption("a","accept", true, rb.getString("accept"));
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
        CommandLine cl = null;
        try {
            cl = parseCommandLine(args);
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
        if ((instance.URL = cl.getOptionValue("u")) == null)
            throw new MissingOptionException("Missing url.");
        LOG.info("Check extension of first file only to determine whether STOW is for dicom or non dicom type of objects.");
        instance.contentType = getContentType(cl.getOptionValue("t"), files.get(0));
        instance.metadataFile = cl.getOptionValue("f");
        instance.accept = getAccept(cl.getOptionValue("a"), instance.contentType);
        instance.pixelHeader = Boolean.valueOf(cl.getOptionValue("pixel-header"));
        instance.noAppn = Boolean.valueOf(cl.getOptionValue("no-appn"));
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

    private static String getContentType(String value, String firstFile) {
        if (value != null) {
            value = value.toLowerCase();
            if (!(value.equals("xml") || value.equals("json")))
                throw new IllegalArgumentException("Unsupported content type. Read -t option in stowrs help");
            else
                return "application/dicom+"+value;
        }
        return getExt(firstFile) == Extension.notBulkdata ? "application/dicom" : "application/dicom+xml";
    }

    private static String getAccept(String value, String contentType) {
        if (value != null) {
            if (!(value.equals("xml") || value.equals("json")))
                throw new IllegalArgumentException("Unsupported accept type. Read -a option in stowrs help");
            else
                return "application/dicom+"+value;
        }
        return contentType.equals("application/dicom") ? "application/dicom+xml" : contentType;
    }

    private static void stowMetadataAndBulkdata(StowRS instance, List<String> files) throws Exception {
        LOG.info("Storing metadata and bulkdata.");
        Attributes metadata = getMetadata(instance);
        File bulkdataFile = new File(files.get(0));
        Extension ext = getExt(files.get(0));
        switch (ext) {
            case pdf:
                instance.bulkdataType = "application/pdf";
                setPDFAttributes(bulkdataFile, metadata);
                if (metadata.getValue(Tag.EncapsulatedDocument) == null)
                    metadata.setValue(Tag.EncapsulatedDocument, VR.OB, instance.defaultBulkdata);
                readAllDataFromFile(instance, bulkdataFile);
                break;
            case mpg:
            case mpg2:
            case mpeg:
                instance.bulkdataType = "video/mpeg";
                readPixelHeader(instance, metadata, bulkdataFile, true);
                if (metadata.getString(Tag.SOPClassUID) == null)
                    metadata.setString(Tag.SOPClassUID, VR.UI, UID.VideoPhotographicImageStorage);
                break;
            case jpeg:
            case jpg:
                instance.bulkdataType = "image/jpeg; transfer-syntax: " + UID.JPEGBaseline8Bit;
                readPixelHeader(instance, metadata, bulkdataFile, false);
                if (metadata.getString(Tag.SOPClassUID) == null)
                    metadata.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
                break;
            case notBulkdata:
                throw new IllegalArgumentException("Unsupported bulkdata type. Read stowrs help.");
        }
        stow(instance, metadata, files);
    }

    private static Attributes getMetadata(StowRS instance)
            throws Exception {
        Attributes metadata = defaultMetadata();
        String metadataFile = instance.metadataFile;
        if (metadataFile != null) {
            String metadataFileExt = metadataFile.substring(metadataFile.lastIndexOf(".")+1).toLowerCase();
            if (!metadataFileExt.equals("xml"))
                throw new IllegalArgumentException("Metadata file extension not supported. Read -f option in stowrs help");
            File filePath = new File(metadataFile);
            metadata = SAXReader.parse(filePath.toString());
        }
        coerceAttributes(metadata, instance);
        return metadata;
    }

    private static Attributes defaultMetadata() throws ParserConfigurationException, SAXException, IOException {
        LOG.info("Always first set default metadata in the event of required attributes not set/sent by user.");
        Attributes metadata = new Attributes();
        metadata.setString(Tag.PatientName, VR.PN, "STOW-RS-PatientName");
        metadata.setString(Tag.StudyInstanceUID, VR.UI, UIDUtils.createUID());
        metadata.setString(Tag.SeriesInstanceUID, VR.UI, UIDUtils.createUID());
        metadata.setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());
        return metadata;
    }

    private static void readAllDataFromFile(StowRS instance, File bulkdataFile) throws Exception {
        int fileLen = (int) bulkdataFile.length();
        BufferedInputStream bis = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(bulkdataFile);
            bis = new BufferedInputStream(fis);
            instance.pixelData = new byte[fileLen];
            StreamUtils.readFully(bis, instance.pixelData, 0, fileLen);
        } finally {
            if (bis != null)
                bis.close();
            if (fis != null)
                fis.close();
        }
    }

    private static void readPixelHeader(StowRS instance, Attributes metadata, File pixelDataFile, boolean isMpeg)
            throws Exception {
        if (metadata.getValue(Tag.PixelData) == null)
            metadata.setValue(Tag.PixelData, VR.OB, instance.defaultBulkdata);
        if (!instance.pixelHeader) {
            readAllDataFromFile(instance, pixelDataFile);
            return;
        }
        BufferedInputStream bis = null;
        FileInputStream fis = null;
        int fileLen = (int) pixelDataFile.length();
        try {
            fis = new FileInputStream(pixelDataFile);
            bis = new BufferedInputStream(fis);
            byte[] b16384 = new byte[16384];
            StreamUtils.readFully(bis, b16384, 0, 16384);
            instance.pixelData = new byte[fileLen];
            if (isMpeg) {
                MPEGHeader mpegHeader = new MPEGHeader(b16384);
                mpegHeader.toAttributes(metadata, pixelDataFile.length());
                instance.pixelData = Arrays.copyOf(b16384, fileLen);
                StreamUtils.readFully(bis, instance.pixelData, 16384, fileLen-16384);
            } else {
                instance.jpegHeader = new JPEGHeader(b16384, JPEG.SOS);
                instance.jpegHeader.toAttributes(metadata);
                if (instance.noAppn) {
                    int off = instance.jpegHeader.offsetAfterAPP();
                    byte[] bTemp = Arrays.copyOf(b16384, fileLen);
                    StreamUtils.readFully(bis, bTemp, 16384, fileLen - 16384);
                    instance.pixelData = Arrays.copyOfRange(bTemp, off, fileLen);
                } else {
                    instance.pixelData = Arrays.copyOf(b16384, fileLen);
                    StreamUtils.readFully(bis, instance.pixelData, 16384, fileLen - 16384);
                }
            }
        } finally {
            if (fis != null)
                fis.close();
            if (bis != null)
                bis.close();
        }
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
                    "multipart/related; type=" + instance.contentType + "; boundary=" + boundary);
            connection.setRequestProperty("Accept", instance.accept);
            out = connection.getOutputStream();
            out.write(("\r\n--" + boundary + "\r\n").getBytes());
            out.write(("Content-Type: " + instance.contentType + "\r\n").getBytes());
            out.write("\r\n".getBytes());
            logOutgoing(connection);
            writeData(instance, metadata, files, out);
            out.write(("\r\n--" + boundary + "--\r\n").getBytes());
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
        LOG.debug(readFullyAsString(connection));
    }

    private static String readFullyAsString(HttpURLConnection connection)
            throws IOException {
        return readFully(connection).toString("UTF-8");
    }

    private static ByteArrayOutputStream readFully(HttpURLConnection connection) throws IOException {
        ByteArrayOutputStream baos = null;
        InputStream inputStream = null;
        try {
            inputStream = connection.getInputStream();
            baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[16384];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            return baos;
        } finally {
            if (baos != null)
                baos.close();
            if (inputStream != null)
                inputStream.close();
        }
    }

    private static void writeData(StowRS instance, Attributes metadata, List<String> files,
                                  OutputStream out) throws Exception {
        if (!instance.contentType.equals("application/dicom")) {
            writeMetdataAndBulkData(instance, metadata, out);
            return;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(files.get(0)));
            StreamUtils.copy(fis, out);
            for (int i = 1; i < files.size(); i++) {
                fis.close();
                out.write(("\r\n--" + boundary + "\r\n").getBytes());
                out.write(("Content-Type: " + instance.contentType + "\r\n").getBytes());
                out.write("\r\n".getBytes());
                fis = new FileInputStream(new File(files.get(i)));
                StreamUtils.copy(fis, out);
            }
        } finally {
            if (fis != null)
                fis.close();
        }
    }

    private static void writeMetdataAndBulkData(StowRS instance,
                                                Attributes metadata, OutputStream out)
            throws Exception {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        try {
            if (instance.contentType.equals("application/dicom+xml"))
                SAXTransformer.getSAXWriter(new StreamResult(bOut)).write(metadata);
            else {
                JsonGenerator gen = null;
                try {
                    gen = Json.createGenerator(bOut);
                    new JSONWriter(gen).write(metadata);
                } finally {
                    if (gen != null)
                        gen.close();
                }
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
                out.write(instance.pixelData);
            } else
                out.write(instance.pixelData);
        } finally {
            bOut.close();
        }
    }

    private static void coerceAttributes(Attributes metadata, StowRS instance) {
        if (instance.keys.tags().length > 0)
            LOG.info("Coercing the following keys from specified attributes to metadata:");
        metadata.addAll(instance.keys);
        LOG.info(instance.keys.toString());
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
}
