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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.ContentHandlerAdapter;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONWriter;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.tool.stowrs.test.StowRSResponse;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.ws.rs.MediaTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public class StowRS {
    private String singleFrameMultipleFragmentsURI;
    static final Logger LOG = LoggerFactory.getLogger(StowRS.class);
    private Attributes keys = new Attributes();
    private static Options opts;
    private String URL;
    private final List<StowRSResponse> responses = new ArrayList<StowRSResponse>();
    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.stowrs.messages");

    private boolean multipleFragments;
    private String mediaType;
    private String transferSyntax;
    private List<File> files = new ArrayList<File>();
    public StowRS() {
    }

    public StowRS(Attributes overrideAttrs, String mediaType, List<File> files, String url, String ts) {
        this.URL = url;
        this.keys = overrideAttrs;
        transferSyntax = ts;
        if (!mediaType.equalsIgnoreCase("NO_METADATA_DICOM"))
            this.mediaType = mediaType;
        this.files = files;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        CommandLine cl = null;
        try {

            cl = parseComandLine(args);
            StowRS instance = new StowRS();
            if(cl.hasOption("m"))
            instance.keys = configureKeys(instance, cl);
            if (!cl.hasOption("u")) {
                throw new IllegalArgumentException("Missing url");
            } else {
                instance.URL = cl.getOptionValue("u");
            }
            if (cl.hasOption("t")) {
                if(!cl.hasOption("ts")) {
                    throw new MissingArgumentException("Missing option required option ts when sending metadata");
                }
                else {
                    instance.setTransferSyntax(cl.getOptionValue("ts"));
                }
                instance.mediaType = cl.getOptionValue("t");
                
                for(Iterator<String> iter = cl.getArgList().iterator(); iter.hasNext();) {
                    instance.files.add(new File(iter.next()));
                }
                
                
                if (instance.files == null)
                    throw new IllegalArgumentException("Missing metadata files");
                }
            instance.stow();
            
            if (instance.mediaType == null) {
                for(Iterator<String> iter = cl.getArgList().iterator(); iter.hasNext();) {
                    instance.files.add(new File(iter.next()));
                }
                if (instance.files == null)
                    throw new IllegalArgumentException(
                            "No dicom files specified");
                for (File metadataFile : instance.files) {
                    instance.addResponse(sendDicomFile(instance, metadataFile));
                    LOG.info(metadataFile.getPath() + " with size : " + metadataFile.length());
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

    public void stow() throws IOException, InterruptedException {
        Attributes metadata = new Attributes();
        Attributes fmi;
        for (File metadataFile : files)
            if (mediaType == null) {
                if (files == null)
                    throw new IllegalArgumentException(
                            "No dicom files specified");
                addResponse(sendDicomFile(this, metadataFile));
                    LOG.info(metadataFile.getPath() + " with size : " + metadataFile.length());
            }
            else if (mediaType.equalsIgnoreCase("JSON")) {
                try {
                    metadata = parseJSON(metadataFile.getPath());
                    String ts = transferSyntax;
                    ArrayList<BulkDataChunk> bulkFiles = new ArrayList<BulkDataChunk>();
                    JsonObject object = loadJSON(metadataFile);
                    bulkFiles = extractBlkDataFiles(object);
                    boolean combine = false;
                    if(isMultiFrame(metadata)) {
                        //multiply frames
                        if(multipleFragments) {
                            //multiple fragment - reject
                            LOG.error("Compressed multiframe multi fragment"
                                    + " is not supported by stow in the DICOM standard");
                            return;
                        }
                        else {
                            //no fragments, just bulkdatauri
                            combine = false;
                        }
                    }
                    else {
                        //single frame
                        singleFrameMultipleFragmentsURI = UUID.randomUUID().toString().replaceAll("[a-z]*", "").replace("-", "");
                        metadata.setValue(Tag.PixelData, multipleFragments?VR.OB:VR.OW, new BulkData(null
                                , singleFrameMultipleFragmentsURI,
                                (ts.equalsIgnoreCase("1.2.840.10008.1.2.1.99") || ts.equalsIgnoreCase("1.2.840.10008.1.2.2"))?true : false));
                        combine = true;
                    }
                    addResponse(sendMetaDataAndBulkData(false, this, metadata, bulkFiles, combine));
                } catch (Exception e) {
                    LOG.error("error parsing metadata file");
                    return;
                }
            }
            else if (mediaType.equalsIgnoreCase("XML")) {

                try {
                    ContentHandlerAdapter ch = new ContentHandlerAdapter(metadata);
                    SAXParserFactory.newInstance().newSAXParser().parse(metadataFile,ch);
                    fmi = ch.getFileMetaInformation();
                    metadata.addAll(fmi);
                    String ts = transferSyntax;
                    ArrayList<BulkDataChunk> bulkFiles = new ArrayList<BulkDataChunk>();
                    Document doc = loadXml(metadataFile);
                    bulkFiles = extractBlkDataFiles(doc);
                    boolean combine = false;
                    if(isMultiFrame(metadata)) {
                        //multiply frames
                        if(doc.getElementsByTagName("DataFragment").getLength() > 1) {
                            //multiple fragment - reject
                            LOG.error("Compressed multiframe multi fragment"
                                    + " is not supported by stow in the DICOM standard");
                            return;
                        }
                        else {
                            //no fragments, just bulk data uri
                            combine = false;
                        }
                    }
                    else {
                        //single frame
                        singleFrameMultipleFragmentsURI = UUID.randomUUID().toString().replaceAll("[a-z]*", "").replace("-", "");
                        metadata.setValue(Tag.PixelData, doc.getElementsByTagName("DataFragment").getLength() > 1?VR.OB:VR.OW, new BulkData(null
                                , singleFrameMultipleFragmentsURI,
                                (ts.equalsIgnoreCase("1.2.840.10008.1.2.1.99") || ts.equalsIgnoreCase("1.2.840.10008.1.2.2"))?true : false));
                        LOG.info("Single frame multiple fragments, combining fragments");
                        combine = true;
                    }
                    addResponse(sendMetaDataAndBulkData(true, this, metadata, bulkFiles, combine));
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

    public List<StowRSResponse> getResponses() {
        return responses;
    }

    public void addResponse(StowRSResponse response) {
        this.responses.add(response);
    }

    private static StowRSResponse sendDicomFile(StowRS instance, File f) throws IOException, InterruptedException {
        return doRequestDICOM(instance.URL, f);
}

    private static MediaType forTransferSyntax(String ts) {
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

        if (UID.JPEGBaseline1.equals(ts) || UID.JPEGExtended24.equals(ts)
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

    private static Attributes parseJSON(String fname) throws Exception {
        Attributes attrs = new Attributes();
        attrs.addAll(parseJSON(fname, attrs));
        return attrs;
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        opts = new Options();
        opts.addOption(OptionBuilder.hasArgs(2).withArgName("[seq/]attr=value")
                .withValueSeparator().withDescription(rb.getString("metadata"))
                .create("m"));
        opts.addOption("u", "url", true, rb.getString("url"));
        opts.addOption("t", "metadata-type", true,
                rb.getString("metadata-type"));
        opts.addOption("ts", "transfer-syntax", true,
                rb.getString("transfer-syntax"));
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, StowRS.class);
    }

    private static Attributes configureKeys(StowRS main, CommandLine cl) {
        Attributes temp = new Attributes();
        CLIUtils.addAttributes(temp, cl.getOptionValues("m"));
        LOG.info("added keys for coercion: \n" + main.keys.toString());
        return temp;
    }

    private static ArrayList<BulkDataChunk> extractBlkDataFiles(Document doc)
            throws URISyntaxException {
        ArrayList<BulkDataChunk> files = new ArrayList<BulkDataChunk>();
        NodeList list = doc.getElementsByTagName("BulkData");
        for (int i = 0; i < list.getLength(); i++) {
            String fullUri = list.item(i).getAttributes().item(0)
                    .getNodeValue();
            String offset = fullUri.replaceAll(".*\\?", "")
                    .replaceAll("\\&.*", "").replaceAll(list.getLength() == 1?"offset=":"offsets=", "");
            
            String length = fullUri.replaceAll(".*\\?", "")
                    .replaceAll(".*\\&", "").replaceAll(list.getLength() == 1?"length=":"lengths=", "");
            String uri = fullUri.split("\\?")[0];
            
            for(int j=0; j<offset.split(",").length; j++) {
                String len = length.split(",")[j];
                String ofs = offset.split(",")[j];
                if(j==0) {
                    len = len.replace("lengths=", "");
                    len = len.replace("length=", "");
                    ofs = ofs.replace("offsets=", "");
                    ofs = ofs.replace("offset=", "");
                }
                    
                if(Integer.parseInt(len) > 0) {
                    files.add(new BulkDataChunk(fullUri, uri, ofs+"", len,
                            childOfPixelData(list.item(i))));
                }
            }
            
        }
        return files;
    }

    private ArrayList<BulkDataChunk> extractBlkDataFiles(
            JsonObject object) throws URISyntaxException {
        ArrayList<BulkDataChunk> files = new ArrayList<BulkDataChunk>();
        for (Entry<String, JsonValue> entry : object.entrySet()) {
            if (entry.getValue().getValueType().equals(ValueType.OBJECT)) {
                JsonObject entryObject = (JsonObject) entry.getValue();
                if (entryObject.containsKey("BulkDataURI")) {

                    String fullUri = entryObject.getString("BulkDataURI");
                    
                    if(fullUri.contains(","))
                        multipleFragments = true;
                    
                    String offset = fullUri.replaceAll(".*\\?", "")
                            .replaceAll("\\&.*", "").replaceAll(multipleFragments?"offsets=":"offset=", "");
                    
                    String length = fullUri.replaceAll(".*\\?", "")
                            .replaceAll(".*\\&", "").replaceAll(multipleFragments?"lengths=":"length=", "");
                    String uri = fullUri.split("\\?")[0];
                    if(multipleFragments)
                        for(int j=0; j<offset.split(",").length; j++) {
                            String len = length.split(",")[j];
                            String ofs = offset.split(",")[j];
                            if(Integer.parseInt(len) > 0) {
                                files.add(new BulkDataChunk(fullUri, uri, ofs, len,
                                        childOfPixelData(object.getJsonObject("7FE00010"),
                                                fullUri)));
                            }
                        }
                    else
                    files.add(new BulkDataChunk(fullUri, uri, offset, length,
                            childOfPixelData(object.getJsonObject("7FE00010"),
                                    fullUri)));
                } 
                }
            }
        return files;
    }

    private static boolean childOfPixelData(Node item) {
        if (item.getParentNode().getNodeName().equalsIgnoreCase("DataFragment"))
            return childOfPixelData(item.getParentNode());
        return item.getParentNode().getAttributes().getNamedItem("keyword")
                .getNodeValue().equalsIgnoreCase("PixelData") ? true : false;
    }

    private static boolean childOfPixelData(JsonObject pixelData,
            String uri) {
        if (pixelData == null)
            return false;
        else if (pixelData.containsKey("BulkDataURI")) {
            String pixelDatauri =  pixelData.get("BulkDataURI").toString().replace("\"", "");
            if(pixelDatauri.equalsIgnoreCase(uri))
            return true;
        }
        return false;
    }

    private static Document loadXml(File metadataFile) {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                .newInstance();
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

    private static JsonObject loadJSON(File metadataFile)
            throws FileNotFoundException {
        Map<String, Object> config = new HashMap<String, Object>();
        // if you need pretty printing
        config.put("javax.json.stream.JsonGenerator.prettyPrinting",
                Boolean.valueOf(true));
        JsonReaderFactory readerFactory = Json.createReaderFactory(config);
        JsonReader reader = readerFactory.createReader(new FileInputStream(
                metadataFile));
        JsonObject doc = reader.readObject();
        return doc;
    }

    private static boolean isMultiFrame(Attributes metadata) {
        return metadata.contains(Tag.NumberOfFrames)
                && metadata.getInt(Tag.NumberOfFrames, 1) > 1;
    }

    private static void coerceattributes(Attributes metadata, StowRS instance) {
        if (instance.keys.tags().length > 0)
            LOG.info("Coercing the following keys from specified attributes to metadata:");
        metadata.update(instance.keys, new Attributes());
        LOG.info(instance.keys.toString());
    }

    private static StowRSResponse sendMetaDataAndBulkData(Boolean xml, StowRS instance,
            Attributes metadata, ArrayList<BulkDataChunk> files,
            boolean combined) throws IOException {
        Attributes responseAttrs = new Attributes();
        int rspCode = 0;
        String rspMessage = null;
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
                "multipart/related; type="+(xml?"application/dicom+xml":"application/json")+"; boundary="
                        + boundary);
        bulkDataTransferSyntax = "transfer-syntax="
                + instance.transferSyntax;
        MediaType type = getBulkDataMediaType(metadata);
        contentTypeBulkData = type.getSubtype() != null ? type.getType() + "/"
                + type.getSubtype() : type.getType();
        connection.setRequestProperty("charset", "utf-8");
        connection.setUseCaches(false);

        DataOutputStream wr;

        wr = new DataOutputStream(connection.getOutputStream());

        try {
            wr.writeBytes("\r\n--" + boundary + "\r\n");
            // write metadata
            if(xml)
                wr.writeBytes("Content-Type: application/dicom+xml; "
                    + bulkDataTransferSyntax + " \r\n");
            else
                wr.writeBytes("Content-Type: application/json; "
                    + bulkDataTransferSyntax + " \r\n");
            wr.writeBytes("\r\n");
        } catch (IOException e1) {
            LOG.error("Error writing metadata");
        }

        coerceattributes(metadata, instance);
        try {
            if(xml)
            SAXTransformer.getSAXWriter(new StreamResult(wr)).write(metadata);
            else {
                JsonGenerator gen = Json.createGenerator(wr);
                JSONWriter writer = new JSONWriter(gen);
                writer.write(metadata);
                gen.flush();
            }
        } catch (TransformerConfigurationException e) {
            LOG.error("Error transforming xml" + e.getStackTrace().toString());
        } catch (SAXException e) {
            // exception wrapper
        }

        if (!combined)
            for (BulkDataChunk chunk : files) {
                try {
                    wr.writeBytes("\r\n--" + boundary + "\r\n");
                    // write bulkdata
                    wr.writeBytes("Content-Type: " + contentTypeBulkData
                            + " \r\n");
                    wr.writeBytes("Content-Location: " + chunk.getBulkDataUri()
                            + " \r\n");
                    wr.writeBytes("\r\n");
                    getByteandWrite(new File(chunk.getFileUrl().getPath()),
                            chunk.getOffset(), chunk.getLength(), wr);
                    wr.writeBytes("\r\n--" + boundary + "--\r\n");
                    wr.flush();
                    wr.close();
                    String response = connection.getResponseMessage();
                    rspCode = connection.getResponseCode();
                    rspMessage = response;
                    LOG.info("response: " + response);
                    try {
                        responseAttrs = SAXReader.parse(connection.getInputStream());
                    } catch (Exception e) {
                        LOG.error("Error creating response attributes, {}",e);
                    }
                    connection.disconnect();

                } catch (IOException e) {
                    LOG.error("Error writing bulk data");
                }
            }
        else {
            ArrayList<BulkDataChunk> onlyPixelData = new ArrayList<BulkDataChunk>();
            for (Iterator<BulkDataChunk> iter = files.iterator(); iter
                    .hasNext();) {
                BulkDataChunk chunk = iter.next();
                if (chunk.isPixelData()) {
                    onlyPixelData.add(chunk);
                    iter.remove();
                }
            }
            if (!files.isEmpty()) {
                for (BulkDataChunk chunk : files) {
                    try {
                        wr.writeBytes("\r\n--" + boundary + "\r\n");
                        // write bulkdata
                        wr.writeBytes("Content-Type: " + contentTypeBulkData
                                + " \r\n");
                        wr.writeBytes("Content-Location: "
                                + chunk.getBulkDataUri() + " \r\n");
                        wr.writeBytes("\r\n");
                        getByteandWrite(new File(chunk.getFileUrl().getPath()),
                                chunk.getOffset(), chunk.getLength(), wr);
                        wr.writeBytes("\r\n--" + boundary + "--\r\n");
                        wr.flush();
                        wr.close();
                        String response = connection.getResponseMessage();
                        rspCode = connection.getResponseCode();
                        rspMessage = response;
                        LOG.info("response: " + response);
                        try {
                            responseAttrs = SAXReader.parse(connection.getInputStream());
                        } catch (Exception e) {
                            LOG.error("Error creating response attributes, {}",e);
                        }
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
                wr.writeBytes("Content-Location: " + instance.singleFrameMultipleFragmentsURI
                        + " \r\n");
                wr.writeBytes("\r\n");
                for (BulkDataChunk chunk : onlyPixelData) {
                    FileInputStream fis = new FileInputStream(chunk
                            .getFileUrl().getPath());
                    fis.skip(chunk.getOffset());
                    byte[] bytes = new byte[chunk.getLength()];
                    fis.read(bytes, 0, chunk.getLength());
                    fis.close();
                    wr.write(bytes);
                    wr.flush();
                }
                wr.writeBytes("\r\n--" + boundary + "--\r\n");
                wr.flush();
                wr.close();
                String response = connection.getResponseMessage();
                rspCode = connection.getResponseCode();
                rspMessage = response;
                LOG.info("response: " + response);
                try {
                    responseAttrs = SAXReader.parse(connection.getInputStream());
                } catch (Exception e) {
                    LOG.error("Error creating response attributes, {}",e);
                }
                connection.disconnect();

            } catch (IOException e) {
                LOG.error("Error writing bulk data");
            }
        }
        return new StowRSResponse(rspCode, rspMessage, responseAttrs);
    }

    private static MediaType getBulkDataMediaType(Attributes metadata) {
        return forTransferSyntax(metadata.getString(Tag.TransferSyntaxUID));
    }

    private static boolean getByteandWrite(File file, int offset, int length,
            DataOutputStream wr) throws IOException {
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
                    wr.write(buffer, 0, cnt);
                    offset += cnt;
                }
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

    private static StowRSResponse doRequestDICOM(String url, File f) {
        Attributes responseAttrs = new Attributes();
        int rspCode = 0;
        String rspMessage = null;
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
            rspCode = connection.getResponseCode();
            rspMessage = connection.getResponseMessage();
            LOG.info("response: " + response);
            try {
                responseAttrs = SAXReader.parse(connection.getInputStream());
            } catch (Exception e) {
                LOG.error("Error creating response attributes, {}",e);
            }
            connection.disconnect();
        } catch (IOException e) {
            LOG.error("error writing to http data output stream"
                    + e.getStackTrace().toString());
        }
        return new StowRSResponse(rspCode, rspMessage, responseAttrs);
    }

    private static Attributes parseJSON(String fname, Attributes attrs)
            throws IOException {
        InputStream in = fname.equals("-") ? System.in : new FileInputStream(
                fname);
        try {
            JSONReader reader = new JSONReader(
                    Json.createParser(new InputStreamReader(in, "UTF-8")));
            reader.readDataset(attrs);
            Attributes fmi = reader.getFileMetaInformation();
            return fmi;
        } finally {
            if (in != System.in)
                SafeClose.close(in);
        }
    }

    public String getTransferSyntax() {
        return transferSyntax;
    }

    public void setTransferSyntax(String transferSyntax) {
        this.transferSyntax = transferSyntax;
    }
}

class BulkDataChunk {
    private final int offset;
    private final int length;
    private final URI fileUrl;
    private final String bulkDataURI;
    private boolean isPixelData = false;

    public BulkDataChunk(String bulkDataURI, String uri, String offset,
            String length, boolean isPixelData) throws URISyntaxException {
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
