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

import javax.ws.rs.core.MediaType;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.ContentHandlerAdapter;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONWriter;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.UIDUtils;
import org.dcm4che3.ws.rs.MediaTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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

    private boolean generatedMetadata = false;
    private String singleFrameMultipleFragmentsURI;
    private static int jpgHeaderLen;
    private static boolean isPDF = false;
    private static byte[] buffer = new byte[8192];
    static final Logger LOG = LoggerFactory.getLogger(StowRS.class);
    private Attributes keys = new Attributes();
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

    public void overrideTag(int tag, String value) throws Exception {
        VR vr = ElementDictionary.vrOf(tag, null);
        keys.setString(tag, vr, value);
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        CommandLine cl = null;
        try {
            Attributes metadata = new Attributes();
            Attributes fmi = new Attributes();
            cl = parseComandLine(args);
            StowRS instance = new StowRS();
            instance.keys = configureKeys(instance, cl);
            LOG.info("added keys for coercion: \n" + instance.keys.toString());
            if (!cl.hasOption("u")) {
                throw new IllegalArgumentException("Missing url");
            } else {
                instance.URL = cl.getOptionValue("u");
            }
            File metadataFile = null;
            if (cl.hasOption("t")) {
                if (cl.hasOption("f"))
                    metadataFile = new File(cl.getOptionValue("f"));
                else {
                    metadataFile = generateMetaData(cl);
                    instance.generatedMetadata = true;
                }
                if (cl.getOptionValue("t").contains("JSON")) {
                    try {
                        metadata = parseJSON(metadataFile.getPath());
                    } catch (Exception e) {
                        LOG.error("error parsing metadata file" + e);
                        return;
                    }

                } else if (cl.getOptionValue("t").contains("XML")) {

                    try {
                        ContentHandlerAdapter ch = new ContentHandlerAdapter(metadata);
                        SAXParserFactory.newInstance().newSAXParser().parse(metadataFile.getPath(),ch);
                        fmi = ch.getFileMetaInformation();
                        metadata.addAll(fmi);
                        ArrayList<BulkDataChunk> files = new ArrayList<BulkDataChunk>();
                        Document doc = loadXml(metadataFile);
                        if(isMultiFrame(metadata)) {
                            //multiply frames
                            if(doc.getElementsByTagName("DataFragment").getLength() > 1) {
                                //multiple fragment - reject
                                LOG.error("Compressed multiframe multi fragment"
                                        + " is not supported by stow in the DICOM standard");
                                return;
                            }
                            else {
                                //no fragments, just bulkdatauri
                                files = extractBlkDataFiles(doc);
                                sendMetaDataAndBulkDataXML(instance, metadata, files, false);
                            }
                        }
                        else {
                            //single frame
                            instance.singleFrameMultipleFragmentsURI = UUID.randomUUID().toString();
                            files = extractBlkDataFiles(doc);
                            replaceFragmentsByUri(doc,instance.singleFrameMultipleFragmentsURI);
                            LOG.info("Single frame multiple fragments, combining fragments");
                            sendMetaDataAndBulkDataXML(instance, metadata, files, true);
                        }

                    } catch (Exception e) {
                        LOG.error("error parsing metadata file");
                        return;
                    }
                }

                else {
                    throw new IllegalArgumentException(
                            "Bad Type specified for metadata, specify either XML or JSON");
                }


            }
            if (!cl.hasOption("t")) {
                List<String> files = cl.getArgList();
                if (files == null)
                    throw new IllegalArgumentException(
                            "No dicom files specified");
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

    private static void replaceFragmentsByUri(Document doc, String uri) {
        NodeList list = doc.getElementsByTagName("DicomAttribute");
        for(int i=0 ; i < list.getLength(); i++) {
            if(list.item(i).hasAttributes())
                if(list.item(i).getAttributes().getNamedItem("keyword").getNodeValue().equalsIgnoreCase("PixelData")) {
                    list.item(i).setNodeValue("<BulkData uri="+uri+"/>");
                }
        }
    }

    private static ArrayList<BulkDataChunk> extractBlkDataFiles(Document doc) throws URISyntaxException {
        ArrayList<BulkDataChunk> files = new ArrayList<BulkDataChunk>();
        NodeList list = doc.getElementsByTagName("BulkData");
        for(int i = 0 ; i<list.getLength();i++) {
            String fullUri = list.item(i).getAttributes().item(0).getNodeValue();
            String offset = fullUri.replaceAll(".*\\?", "").replaceAll("\\&.*", "").replaceAll("offset=", "");
            String length = fullUri.replaceAll(".*\\?", "").replaceAll(".*\\&", "").replaceAll("length=", "");
            String uri = fullUri.split("\\?")[0];
            files.add(new BulkDataChunk(fullUri, uri, offset, length, childOfPixelData(list.item(i))));
        }
        return files;
    }

    private static boolean childOfPixelData(Node item) {
        if(item.getParentNode().getNodeName().equalsIgnoreCase("DataFragment"))
        return childOfPixelData(item.getParentNode());
        return item.getParentNode().getAttributes().getNamedItem("keyword").getNodeValue().equalsIgnoreCase("PixelData")? true : false;
    }

    private static Document loadXml(File metadataFile) {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(metadataFile);
            doc.getDocumentElement().normalize();
            return doc;
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Error parsing the XML", e);
        } catch (SAXException e) {
            throw new IllegalStateException("Error parsing the XML", e);
        } catch (IOException e) {
            throw new IllegalStateException("Error accessing the XML", e);
        }
    }

    private static boolean isMultiFrame(Attributes metadata) {
        return metadata.contains(Tag.NumberOfFrames) && metadata.getInt(Tag.NumberOfFrames,1) > 1;
    }

    private static void coerceattributes(Attributes metadata, StowRS instance) {
        if (instance.keys.tags().length > 0)
            LOG.info("Coercing the following keys from specified attributes to metadata:");
        metadata.update(instance.keys, new Attributes());
        LOG.info(instance.keys.toString());
    }

//    private static void sendMetaDataAndBulkDataJSON(StowRS instance,
//            Attributes metadata, File bulkDataFile) throws IOException {
//
//        String contentTypeBulkData = null;
//        String bulkDataTransferSyntax = null;
//        URL newUrl = null;
//        try {
//            newUrl = new URL(instance.URL);
//        } catch (MalformedURLException e2) {
//            LOG.error("malformed url" + e2.getStackTrace().toString());
//        }
//        String boundary = "-------gc0p4Jq0M2Yt08jU534c0p";
//        HttpURLConnection connection = (HttpURLConnection) newUrl
//                .openConnection();
//        connection.setDoOutput(true);
//        connection.setDoInput(true);
//        connection.setInstanceFollowRedirects(false);
//        connection.setRequestMethod("POST");
//        if (isPDF) {
//            connection.setRequestProperty("Content-Type",
//                    "multipart/related; type=application/json; boundary="
//                            + boundary);
//            bulkDataTransferSyntax = "transfer-syntax=1.2.840.10008.1.2.1";
//            contentTypeBulkData = "application/pdf";
//        } else {
//            if (isExtension(bulkDataFile, "mpeg")) {
//                connection.setRequestProperty("Content-Type",
//                        "multipart/related; type=application/json; boundary="
//                                + boundary);
//                bulkDataTransferSyntax = "transfer-syntax=1.2.840.10008.1.2.4.100";
//                contentTypeBulkData = "video/mpeg";
//            } else if (isExtension(bulkDataFile, "jpeg")) {
//                connection.setRequestProperty("Content-Type",
//                        "multipart/related; type=application/json; boundary="
//                                + boundary);
//                bulkDataTransferSyntax = "transfer-syntax=1.2.840.10008.1.2.4.50";
//                contentTypeBulkData = "image/dicom+jpeg";
//            } else if (isExtension(bulkDataFile, "mp4")) {
//                connection.setRequestProperty("Content-Type",
//                        "multipart/related; type=application/json; boundary="
//                                + boundary);
//                bulkDataTransferSyntax = "transfer-syntax=1.2.840.10008.1.2.4.102";
//                contentTypeBulkData = "video/mp4";
//            } else {
//                throw new IllegalArgumentException(
//                        "Unsupported bulkdata (not MPEG2, MPEG4 or JPEG baseline)");
//            }
//        }
//
//        connection.setRequestProperty("Accept", "application/dicom+xml");
//        connection.setRequestProperty("charset", "utf-8");
//        connection.setUseCaches(false);
//
//        DataOutputStream wr;
//
//        wr = new DataOutputStream(connection.getOutputStream());
//        try {
//            wr.writeBytes("\r\n--" + boundary + "\r\n");
//            // write metadata
//            wr.writeBytes("Content-Type: application/json; transfer-syntax="
//                    + bulkDataTransferSyntax + " \r\n");
//
//            wr.writeBytes("\r\n");
//        } catch (IOException e1) {
//            LOG.error("Error writing metadata");
//        }
//        // here set pixel or document data attributes
//        if (!isPDF)
//            setPixelAttributes(bulkDataFile, metadata);
//        else
//            setPDFAttributes(bulkDataFile, metadata);
//        // coerce here before sending metadata
//        coerceattributes(metadata, instance);
//        JsonGenerator gen = Json.createGenerator(wr);
//        JSONWriter writer = new JSONWriter(gen);
//        writer.write(metadata);
//        gen.flush();
//        try {
//            wr.writeBytes("\r\n--" + boundary + "\r\n");
//            // write bulkdata
//            wr.writeBytes("Content-Type: " + contentTypeBulkData + " \r\n");
//            wr.writeBytes("Content-Location: " + "bulk" + " \r\n");
//            wr.writeBytes("\r\n");
//            byte[] bytes = getBytesFromFile(bulkDataFile);
//            wr.write(bytes);
//            wr.writeBytes("\r\n--" + boundary + "--\r\n");
//            wr.flush();
//            wr.close();
//            String response = connection.getResponseMessage();
//            LOG.info("response: " + response);
//            connection.disconnect();
//
//        } catch (IOException e) {
//            LOG.error("Error writing bulk data");
//        }
//    }

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
            Attributes metadata,  ArrayList<BulkDataChunk> files, boolean combined) throws IOException {

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
        connection.setChunkedStreamingMode(2048);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                    "multipart/related; type=application/dicom+xml; boundary="
                            + boundary);
            bulkDataTransferSyntax = "transfer-syntax="+metadata.getString(Tag.TransferSyntaxUID);
            MediaType type = getBulkDataMediaType(metadata);
            contentTypeBulkData = type.getSubtype()!=null? type.getType()+"/"+type.getSubtype():type.getType();

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
        
        coerceattributes(metadata, instance);
        try {
            SAXTransformer.getSAXWriter(new StreamResult(wr)).write(metadata);
        } catch (TransformerConfigurationException e) {
            LOG.error("Error transforming xml" + e.getStackTrace().toString());
        } catch (SAXException e) {
            // exception wrapper
        }
        
        if(!combined)
        for(BulkDataChunk chunk : files) {
        try {
            wr.writeBytes("\r\n--" + boundary + "\r\n");
            // write bulkdata
            wr.writeBytes("Content-Type: " + contentTypeBulkData + " \r\n");
            wr.writeBytes("Content-Location: " + chunk.getBulkDataUri() + " \r\n");
            wr.writeBytes("\r\n");
            getByteandWrite(new File(chunk.getFileUrl().getPath()), chunk.getOffset(), chunk.getLength(), wr);
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
        else {
            ArrayList<BulkDataChunk> onlyPixelData = new ArrayList<BulkDataChunk>();
            for(Iterator<BulkDataChunk> iter = files.iterator();iter.hasNext();) {
                BulkDataChunk chunk = iter.next();
                if(chunk.isPixelData()) {
                    onlyPixelData.add(chunk);
                    iter.remove();
                }
            }
            if(!files.isEmpty()) {
                for(BulkDataChunk chunk : files) {
                    try {
                        wr.writeBytes("\r\n--" + boundary + "\r\n");
                        // write bulkdata
                        wr.writeBytes("Content-Type: " + contentTypeBulkData + " \r\n");
                        wr.writeBytes("Content-Location: " + chunk.getBulkDataUri() + " \r\n");
                        wr.writeBytes("\r\n");
                        getByteandWrite(new File(chunk.getFileUrl().getPath()), chunk.getOffset(), chunk.getLength(), wr);
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
            }

            try {
                wr.writeBytes("\r\n--" + boundary + "\r\n");
                // write bulkdata
                wr.writeBytes("Content-Type: " + contentTypeBulkData + " \r\n");
                wr.writeBytes("Content-Location: " + "<BulkData uri="+instance.singleFrameMultipleFragmentsURI+"/>" + " \r\n");
                wr.writeBytes("\r\n");
                for(BulkDataChunk chunk : onlyPixelData) {
                    FileInputStream fis = new FileInputStream(chunk.getFileUrl().getPath());
                    fis.skip(chunk.getOffset());
                    byte[] bytes = new byte[chunk.getLength()];
                    fis.read(bytes, 0,chunk.getLength());
                    fis.close();
                    wr.write(bytes);
                    wr.flush();
                }
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
    }

    private static MediaType getBulkDataMediaType(Attributes metadata) {
        return forTransferSyntax(metadata.getString(Tag.TransferSyntaxUID));
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

    private static boolean getByteandWrite(File file, int offset, int length, DataOutputStream wr) throws IOException {
        FileInputStream fis = null;
        byte[] buffer = new byte[4096];
        try {
            fis = new FileInputStream(file);
            try {
                fis.skip(offset);
                int cnt = 0;
                offset = 0;
                wr.flush();
                while ((cnt = fis.read(buffer)) >= 0 && offset < length) {
                    wr.write(buffer,0,cnt);
                     offset+=cnt;
                 }
                LOG.info("wrote " + (offset) + " bytes of data " +
                 "was supposed to write " + length);
            } catch (Exception e) {
               e.printStackTrace();
               return false;
            }
        } catch (FileNotFoundException e1) {
            LOG.error("File not found" + e1.getStackTrace().toString());
            return false;
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    LOG.error("Error closing file {} , Exception: {}",
                            file.getName(), e.getStackTrace().toString());
                    return false;
                }
        }
        return true;
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
    public static MediaType forTransferSyntax(String ts) {
        if (UID.ExplicitVRLittleEndian.equals(ts)
                || UID.ImplicitVRLittleEndian.equals(ts))
            return MediaType.APPLICATION_OCTET_STREAM_TYPE;

        if (UID.JPEGLossless.equals(ts))
            return MediaTypes.IMAGE_DICOM_JPEG_TYPE;

        if (UID.JPEGLSLossless.equals(ts))
            return MediaTypes.IMAGE_DICOM_JPEG_LS_TYPE;

        if (UID.JPEG2000LosslessOnly.equals(ts))
            return MediaTypes.IMAGE_DICOM_JPEG_JP2_TYPE;

        if (UID.JPEG2000Part2MultiComponentLosslessOnly.equals(ts))
            return MediaTypes.IMAGE_DICOM_JPEG_JPX_TYPE;

        if (UID.RLELossless.equals(ts))
            return MediaTypes.IMAGE_DICOM_RLE_TYPE;


        if (UID.JPEGBaseline1.equals(ts)
                || UID.JPEGExtended24.equals(ts)
                || UID.JPEGLosslessNonHierarchical14.equals(ts))
            return MediaTypes.IMAGE_DICOM_JPEG_TYPE;
        else if (UID.JPEGLSLossyNearLossless.equals(ts))
            return MediaTypes.IMAGE_DICOM_JPEG_LS_TYPE;
        else if (UID.JPEG2000.equals(ts))
            return MediaTypes.IMAGE_DICOM_JPEG_JP2_TYPE;
        else if (UID.JPEG2000Part2MultiComponent.equals(ts))
            return MediaTypes.IMAGE_DICOM_JPEG_JP2_TYPE;
        else if (UID.MPEG2.equals(ts)
                || UID.MPEG2MainProfileHighLevel.equals(ts))
            return MediaTypes.VIDEO_MPEG_TYPE;
        else if (UID.MPEG4AVCH264HighProfileLevel41.equals(ts)
                || UID.MPEG4AVCH264BDCompatibleHighProfileLevel41.equals(ts))
            return MediaTypes.VIDEO_MP4_TYPE;
        else
            throw new IllegalArgumentException("ts: " + ts);
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
class BulkDataChunk {
    private int offset;
    private int length;
    private URI fileUrl;
    private String bulkDataURI;
    private boolean isPixelData = false;
    public BulkDataChunk(String bulkDataURI, String uri, String offset, String length, boolean isPixelData) throws URISyntaxException {
        this.offset = Integer.valueOf(offset);
        this.length = Integer.valueOf(length);
        this.fileUrl = new URI(uri);
        this.bulkDataURI = bulkDataURI;
        this.isPixelData = isPixelData;
    }
    public int getOffset() {
        return offset;
    }
    public int getLength() {
        return length;
    }
    public URI getFileUrl() {
        return fileUrl;
    }
    public String getBulkDataUri() {
        return bulkDataURI;
    }
    public boolean isPixelData() {
        return isPixelData;
    }
}
