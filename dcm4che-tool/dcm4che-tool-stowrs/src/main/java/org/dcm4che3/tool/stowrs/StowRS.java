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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Attributes.Visitor;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.ContentHandlerAdapter;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONWriter;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.tool.stowrs.test.StowRSResponse;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.ws.rs.MediaTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * STOW-RS client.
 *
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public class StowRS {

    public enum MetaDataType {
        JSON, XML, NO_METADATA_DICOM;
    }

    private static final Logger LOG = LoggerFactory.getLogger(StowRS.class);

    private static final String MULTIPART_BOUNDARY = "-------gc0p4Jq0M2Yt08jU534c0p";

    private Attributes keys = new Attributes();
    private static Options opts;
    private String URL;
    private final List<StowRSResponse> responses = new ArrayList<StowRSResponse>();
    private static ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.stowrs.messages");

    private MetaDataType mediaType;
    private String transferSyntax;
    private List<File> files = new ArrayList<File>();

    private StowRS() {
        // Should only be used by main(), which fills in all the required fields.
    }

    public StowRS(Attributes overrideAttrs, MetaDataType mediaType, List<File> files, String url, String transferSyntax) {
        this.URL = url;
        this.keys = overrideAttrs;
        this.transferSyntax = transferSyntax;
        this.mediaType = mediaType;
        this.files = files;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        CommandLine cl = null;
        try {
            cl = parseComandLine(args);
        } catch (ParseException e) {
            failWithError("Error parsing commandline: \n", e);
        }

        if (!cl.hasOption("u")) {
            failWithError("Missing required option -u");
        }

        if (cl.hasOption("t") && !cl.hasOption("ts")) {
            failWithError("Option ts is required when sending metadata.");
        } else if (cl.hasOption("ts") && !cl.hasOption("t")) {
            failWithError("Option ts is only valid when sending metadata.");
        }

        if (cl.getArgList().isEmpty()) {
            failWithError("Error: missing files.");
        }

        try {
            StowRS instance = new StowRS();
            if (cl.hasOption("m")) {
                instance.keys = configureKeys(instance, cl);
            }
            instance.URL = cl.getOptionValue("u");

            if (cl.hasOption("t")) {
                instance.transferSyntax = cl.getOptionValue("ts");

                String mediaTypeString = cl.getOptionValue("t");
                if ("JSON".equalsIgnoreCase(mediaTypeString)) {
                    instance.mediaType = MetaDataType.JSON;
                } else if ("XML".equalsIgnoreCase(mediaTypeString)) {
                    instance.mediaType = MetaDataType.XML;
                } else {
                    failWithError("Bad Type " + mediaTypeString + " specified for metadata; specify either XML or JSON");
                }
            } else {
                instance.mediaType = MetaDataType.NO_METADATA_DICOM;
            }

            instance.files.addAll(cl.getArgList());

        } catch (Exception e) {
            LOG.error("Error: \n", e);
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void failWithError(String message) {
        failWithError(message, null);
    }

    private static void failWithError(String message, Exception e) {
        if (e == null) {
            LOG.error(message);
        } else {
            LOG.error(message, e);
        }
        LOG.error("Try 'stowrs --help' for more information.");

        System.exit(2);
    }

    public void stow() {
        for (File file : files) {
            LOG.info("Sending {}", file);

            if (mediaType == MetaDataType.NO_METADATA_DICOM) {
                stowDicomFile(file);
            } else {
                stowMetaDataAndBulkData(file);
            }
        }
    }

    private void stowMetaDataAndBulkData(File file) {
        Attributes metadata;
        if (mediaType == MetaDataType.JSON) {
            try {
                metadata = parseJSON(file.getPath());
            } catch (Exception e) {
                LOG.error("error parsing metadata JSON file {}", file, e);
                return;
            }
        } else if (mediaType == MetaDataType.XML) {
            metadata = new Attributes();
            try {
                ContentHandlerAdapter ch = new ContentHandlerAdapter(metadata);
                SAXParserFactory.newInstance().newSAXParser().parse(file, ch);
                Attributes fmi = ch.getFileMetaInformation();
                if (fmi != null) {
                  metadata.addAll(fmi);
                }
            } catch (Exception e) {
                LOG.error("error parsing metadata XML file {}", file, e);
                return;
            }
        } else {
            throw new IllegalArgumentException("Unsupported media type " + mediaType);
        }

        ExtractedBulkData extractedBulkData = extractBulkData(metadata);

        if (isMultiFrame(metadata)) {
            if (extractedBulkData.pixelDataBulkData.size() > 1) {
                // Multiple fragments - reject.
                LOG.error("Compressed multiframe with multiple fragments in file {} is not supported by STOW-RS in the current DICOM standard (2015b)", file);
                return;
            }
        }

        if (!extractedBulkData.pixelDataBulkData.isEmpty()) {
            // Replace the pixel data bulk data URI, because we might have to merge multiple fragments into one.
            metadata.setValue(Tag.PixelData, metadata.getVR(Tag.PixelData), new BulkData(null, extractedBulkData.pixelDataBulkDataURI, extractedBulkData.pixelDataBulkData.get(0).bigEndian));
        }

        try {
            addResponse(sendMetaDataAndBulkData(metadata, extractedBulkData));
        } catch (IOException e) {
            LOG.error("Error for file {}", file, e);
        }
    }

    private void stowDicomFile(File file) {
        try {
            addResponse(sendDicomFile(URL, file));
            LOG.info(file.getPath() + " with size : " + file.length());
        } catch (IOException e) {
            LOG.error("Error for file {}", file, e);
        }
    }

    private static class ExtractedBulkData {
        final List<BulkData> pixelDataBulkData = new ArrayList<BulkData>();
        final List<BulkData> otherBulkDataChunks = new ArrayList<BulkData>();

        final String pixelDataBulkDataURI = createRandomBulkDataURI();
    }

    private static String createRandomBulkDataURI() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private ExtractedBulkData extractBulkData(Attributes dataset) {
        final ExtractedBulkData extractedBulkData = new ExtractedBulkData();

        try {
            dataset.accept(new Visitor() {

                @Override
                public boolean visit(Attributes attrs, int tag, VR vr, Object value) {
                    if (attrs.isRoot() && tag == Tag.PixelData) {
                        if (value instanceof BulkData) {
                            extractedBulkData.pixelDataBulkData.add((BulkData) value);
                        } else if (value instanceof Fragments) {
                            Fragments frags = (Fragments) value;
                            if (frags.size() > 1 && frags.get(1) instanceof BulkData) {
                                // please note that we are ignoring the first fragment (offset table) here
                                // (it's okay as we are anyways not supporting fragmented multi-frames at the moment)
                                for (int i = 1; i < frags.size(); i++) {
                                    if (frags.get(i) instanceof BulkData) {
                                        extractedBulkData.pixelDataBulkData.add((BulkData) frags.get(i));
                                    }
                                }
                            }
                        }
                    } else {
                        // other bulk data tags (not top-level pixel data)

                        if (value instanceof BulkData) {
                            extractedBulkData.otherBulkDataChunks.add((BulkData) value);
                        }

                        // Note: at the moment we support fragments only for top-level PixelData.
                        // Maybe we should also support it for others, seems to be at least allowed for PixelData inside sequences.
                        // (see DICOM PS3.5 2015b A.4 Transfer Syntaxes For Encapsulation of Encoded Pixel Data)
                    }

                    return true;
                }
            }, true);
        } catch (Exception e) {
            throw new RuntimeException(e); // should not happen
        }

        return extractedBulkData;
    }

    public List<StowRSResponse> getResponses() {
        return responses;
    }

    public void addResponse(StowRSResponse response) {
        this.responses.add(response);
    }

    private static Attributes parseJSON(String fname) throws Exception {
        Attributes attrs = new Attributes();
        Attributes fmi = parseJSON(fname, attrs);
        if (fmi != null)
        	attrs.addAll(fmi);
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

    private static boolean isMultiFrame(Attributes metadata) {
        return metadata.contains(Tag.NumberOfFrames)
                && metadata.getInt(Tag.NumberOfFrames, 1) > 1;
    }

    private static void coerceAttributes(Attributes metadata, Attributes keys) {
        if (!keys.isEmpty()) {
            LOG.info("Coercing the following keys from specified attributes to metadata:");
            metadata.update(keys, null);
            LOG.info(keys.toString());
        }
    }

    private StowRSResponse sendMetaDataAndBulkData(Attributes metadata, ExtractedBulkData extractedBulkData) throws IOException {
        Attributes responseAttrs = new Attributes();

        URL newUrl;
        try {
            newUrl = new URL(URL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        HttpURLConnection connection = (HttpURLConnection) newUrl.openConnection();
        connection.setChunkedStreamingMode(2048);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");

        String metaDataType = mediaType == MetaDataType.XML ? "application/dicom+xml" : "application/json";
        connection.setRequestProperty("Content-Type", "multipart/related; type=\"" + metaDataType + "\"; boundary=" + MULTIPART_BOUNDARY);
        String bulkDataTransferSyntax = "transfer-syntax=" + transferSyntax;

        MediaType pixelDataMediaType = getBulkDataMediaType(metadata);
        connection.setRequestProperty("Accept", "application/dicom+xml");
        connection.setRequestProperty("charset", "utf-8");
        connection.setUseCaches(false);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());

        // Write metadata.
        wr.writeBytes("\r\n--" + MULTIPART_BOUNDARY + "\r\n");

        if (mediaType == MetaDataType.XML) {
            wr.writeBytes("Content-Type: application/dicom+xml; " + bulkDataTransferSyntax + " \r\n");
        } else {
            wr.writeBytes("Content-Type: application/json; " + bulkDataTransferSyntax + " \r\n");
        }
        wr.writeBytes("\r\n");

        coerceAttributes(metadata, keys);

        try {
            if (mediaType == MetaDataType.XML)
                SAXTransformer.getSAXWriter(new StreamResult(wr)).write(metadata);
            else {
                JsonGenerator gen = Json.createGenerator(wr);
                JSONWriter writer = new JSONWriter(gen);
                writer.write(metadata);
                gen.flush();
            }
        } catch (TransformerConfigurationException e) {
            throw new IOException(e);
        } catch (SAXException e) {
            throw new IOException(e);
        }

        // Write bulkdata.
        for (BulkData chunk : extractedBulkData.otherBulkDataChunks) {
            writeBulkDataPart(MediaType.APPLICATION_OCTET_STREAM_TYPE, wr, chunk.getURIOrUUID(), Collections.singletonList(chunk));
        }

        if (!extractedBulkData.pixelDataBulkData.isEmpty()) {
            // Write pixeldata as a single bulk data part.

            if (extractedBulkData.pixelDataBulkData.size() > 1) {
                LOG.info("Combining bulk data of multiple pixel data fragments");
            }

            writeBulkDataPart(pixelDataMediaType, wr, extractedBulkData.pixelDataBulkDataURI, extractedBulkData.pixelDataBulkData);
        }

        // End of multipart message.
        wr.writeBytes("\r\n--" + MULTIPART_BOUNDARY + "--\r\n");
        wr.close();
        String response = connection.getResponseMessage();
        int rspCode = connection.getResponseCode();
        LOG.info("response: " + response);
        try {
            responseAttrs = SAXReader.parse(connection.getInputStream());
        } catch (Exception e) {
            LOG.error("Error creating response attributes", e);
        }
        connection.disconnect();

        return new StowRSResponse(rspCode, response, responseAttrs);
    }

    private static void writeBulkDataPart(MediaType mediaType, DataOutputStream wr, String uri, List<BulkData> chunks) throws IOException {
        wr.writeBytes("\r\n--" + MULTIPART_BOUNDARY + "\r\n");
        wr.writeBytes("Content-Type: " + toContentType(mediaType) + " \r\n");
        wr.writeBytes("Content-Location: " + uri + " \r\n");
        wr.writeBytes("\r\n");

        for (BulkData chunk : chunks) {
            writeBulkDataToStream(chunk, wr);
        }
    }

    private static String toContentType(MediaType mediaType) {
        StringBuilder sb = new StringBuilder();
        sb.append(mediaType.getType()).append('/').append(mediaType.getSubtype());
        String tsuid = mediaType.getParameters().get("transfer-syntax");
        if (tsuid != null ) {
            sb.append("; transfer-syntax=").append(tsuid);
        }
        return sb.toString();
    }

    private MediaType getBulkDataMediaType(Attributes metadata) {
        return MediaTypes.forTransferSyntax(metadata.getString(Tag.TransferSyntaxUID, transferSyntax));
    }

    private static void writeBulkDataToStream(BulkData bulkData, DataOutputStream wr) throws IOException {
        InputStream in = null;
        try {
            in = bulkData.openStream();

            int length = bulkData.length();

            if (length >= 0) {
                StreamUtils.copy(in, wr, length);
            } else { // unspecified length
                StreamUtils.copy(in, wr);
            }

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.error("Error closing stream", e);
                }
            }
        }
    }

    private static StowRSResponse sendDicomFile(String url, File f) throws IOException {
        int rspCode = 0;
        String rspMessage = null;

        URL newUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) newUrl.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/related; type=\"application/dicom\"; boundary=" + MULTIPART_BOUNDARY);
        connection.setRequestProperty("Accept", "application/dicom+xml");
        connection.setRequestProperty("charset", "utf-8");
        connection.setUseCaches(false);

        DataOutputStream wr;
        wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes("\r\n--" + MULTIPART_BOUNDARY + "\r\n");
        wr.writeBytes("Content-Disposition: inline; name=\"file[]\"; filename=\"" + f.getName() + "\"\r\n");
        wr.writeBytes("Content-Type: application/dicom\r\n");
        wr.writeBytes("\r\n");
        FileInputStream fis = new FileInputStream(f);
        StreamUtils.copy(fis, wr);
        fis.close();
        wr.writeBytes("\r\n--" + MULTIPART_BOUNDARY + "--\r\n");
        wr.flush();
        wr.close();
        String response = connection.getResponseMessage();
        rspCode = connection.getResponseCode();
        rspMessage = connection.getResponseMessage();
        LOG.info("response: " + response);
        Attributes responseAttrs = null;
        try {
            InputStream in;
            boolean isErrorCase = rspCode >= HttpURLConnection.HTTP_BAD_REQUEST;
            if (!isErrorCase) {
                in = connection.getInputStream();
            } else {
                in = connection.getErrorStream();
            }
            if (!isErrorCase || rspCode == HttpURLConnection.HTTP_CONFLICT) {
                responseAttrs = SAXReader.parse(in);
            }
        } catch (SAXException e) {
            throw new IOException(e);
        } catch (ParserConfigurationException e) {
            throw new IOException(e);
        }
        connection.disconnect();

        return new StowRSResponse(rspCode, rspMessage, responseAttrs);
    }

    private static Attributes parseJSON(String fname, Attributes attrs)
            throws IOException {
        InputStream in = fname.equals("-") ? System.in : new FileInputStream(fname);
        try {
            JSONReader reader = new JSONReader(
                    Json.createParser(new InputStreamReader(in, "UTF-8")));
            reader.readDataset(attrs);
            Attributes fmi = reader.getFileMetaInformation();
            return fmi;
        } finally {
            if (in != System.in) {
                SafeClose.close(in);
            }
        }
    }
}