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
 * Portions created by the Initial Developer are Copyright (C) 2011
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
package org.dcm4che3.tool.wadors;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.io.SAXWriter;
import org.dcm4che3.mime.MultipartInputStream;
import org.dcm4che3.mime.MultipartParser;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.tool.common.SimpleHTTPResponse;
import org.dcm4che3.tool.wadors.test.WadoRSResponse;
import org.dcm4che3.ws.rs.MediaTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WADO-RS client tool.
 * 
 * @see <a href=
 *      "http://medical.nema.org/medical/dicom/current/output/html/part18.html#sect_6.5">
 *      DICOM PS3.18 (Web Services) WADO-RS</a>
 * 
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public class WadoRS {

    private static final Logger LOG = LoggerFactory.getLogger(WadoRS.class);

    private static byte[] copyHolder;

    private static Options options;

    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.wadors.messages");

    private SAXTransformerFactory saxTransformer;

    private Templates xsltTemplates;

    private File outDir;

    private File xsltFile;

    private String requestTimeOut;

    private boolean xmlIndent = false;

    private boolean xmlIncludeKeyword = true;

    private boolean isMetadata = false;

    private String[] acceptTypes;

    private String url;

    private ResponseWriter writerType;

    private int partIndex = 0;

    private boolean dumpHeader = false;

    private final Map<String, Path> retrievedInstances = new HashMap<String, Path>();

    private WadoRSResponse response;

    public WadoRS() {
    }

    public WadoRS(String url, File retrieveDir) {
        this.outDir = retrieveDir;
        this.url = url;
    }

    @SuppressWarnings("static-access")
    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        options = new Options();

        options.addOption(null, "request-timeout", true,
                rb.getString("request-timeout"));

        options.addOption(OptionBuilder.withArgName("mediaType;ts")
                .withLongOpt("accept-type")
                .withDescription(rb.getString("accept-type")).hasArg(true)
                .create());

        addOutputOptions(options);

        CLIUtils.addCommonOptions(options);

        return CLIUtils.parseComandLine(args, options, rb, WadoRS.class);
    }

    @SuppressWarnings("static-access")
    private static void addOutputOptions(Options opts) {
        opts.addOption(OptionBuilder.withLongOpt("out-dir").hasArg()
                .withArgName("directory")
                .withDescription(rb.getString("out-dir")).create());
        opts.addOption(OptionBuilder.withLongOpt("dump-headers").hasArg(false)
                .withDescription(rb.getString("dump-headers")).create());
        opts.addOption(OptionBuilder.withLongOpt("xsl").hasArg()
                .withArgName("xsl-file").withDescription(rb.getString("xsl"))
                .create("x"));
        opts.addOption("I", "indent", false, rb.getString("indent"));
        opts.addOption("K", "no-keyword", false, rb.getString("no-keyword"));
    }

    public static void main(String[] args) {
        CommandLine cl = null;
        try {
            WadoRS main = new WadoRS();
            cl = parseComandLine(args);

            if (cl.hasOption("out-dir"))
                main.setOutDir(new File(cl.getOptionValue("out-dir")));

            if (cl.hasOption("x"))
                main.setXsltFile(new File(cl.getOptionValue("x")));

            main.setXmlIndent(cl.hasOption("I"));

            main.setXmlIncludeKeyword(!cl.hasOption("K"));

            if (cl.hasOption("request-timeout"))
                main.setRequestTimeOut(cl.getOptionValue("request-timout"));

            if (cl.hasOption("accept-type")) {
                main.setAcceptType(cl.getOptionValues("accept-type"));
            } else {
                System.out.println("wadors: missing required option accept-type");
                System.err.println(rb.getString("try"));
                System.exit(2);
            }

            if (cl.hasOption("dump-headers"))
                main.dumpHeader = true;

            main.setUrl(cl.getArgs()[0]);

            main.isMetadata = isMetaDataURL(main.getUrl());

            String response = null;
            try {
                response = sendRequest(main).toString();
            } catch (IOException e) {
                System.out.print("Error sending request {}" + e);
            }

            System.out.print(response);
        } catch (ParseException e) {
            LOG.error("wadors: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        }
    }

    public void wadors() throws IOException {
        isMetadata = isMetaDataURL(url);

        sendRequest(this);
    }

    private static boolean isMetaDataURL(String url) {
        return url.contains("/metadata");
    }

    private static SimpleHTTPResponse sendRequest(final WadoRS main) throws IOException {
        URL newUrl = new URL(main.getUrl());

        LOG.info("WADO-RS URL: {}", newUrl);

        HttpURLConnection connection = (HttpURLConnection) newUrl.openConnection();

        connection.setDoOutput(true);

        connection.setDoInput(true);

        connection.setInstanceFollowRedirects(false);

        connection.setRequestMethod("GET");

        connection.setRequestProperty("charset", "utf-8");

        String[] acceptHeaders = compileAcceptHeader(main.acceptTypes);

        LOG.info("Accept-Headers: {}", Arrays.toString(acceptHeaders));

        for (String acceptStr : acceptHeaders)
            connection.addRequestProperty("Accept", acceptStr);

        if (main.getRequestTimeOut() != null) {
            connection.setConnectTimeout(Integer.valueOf(main.getRequestTimeOut()));
            connection.setReadTimeout(Integer.valueOf(main.getRequestTimeOut()));
        }

        connection.setUseCaches(false);

        int responseCode = connection.getResponseCode();
        String reponseMessage = connection.getResponseMessage();

        InputStream in = null;
        if (connection.getHeaderField("content-type").contains("application/json") || connection.getHeaderField("content-type").contains("application/zip")) {
            String headerPath;
            in = connection.getInputStream();
            if (main.dumpHeader)
                headerPath = writeHeader(connection.getHeaderFields(), new File(main.outDir, "out.json" + "-head"));
            else {
                headerPath = connection.getHeaderField("content-location");
            }
            File f = new File(main.outDir, connection.getHeaderField("content-type").contains("application/json") ? "out.json" : "out.zip");
            Files.copy(in, f.toPath(), StandardCopyOption.REPLACE_EXISTING);
            main.retrievedInstances.put(headerPath, f.toPath().toAbsolutePath());
        } else {
            if (main.dumpHeader)
                dumpHeader(main, connection.getHeaderFields());
            in = connection.getInputStream();
            try {
                File spool = new File(main.outDir, "Spool");
                Files.copy(in, spool.toPath(), StandardCopyOption.REPLACE_EXISTING);
                String boundary;
                BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(spool)));
                boundary = (rdr.readLine());
                boundary = boundary.substring(2, boundary.length());
                rdr.close();
                FileInputStream fin = new FileInputStream(spool);
                new MultipartParser(boundary).parse(fin, new MultipartParser.Handler() {

                    @Override
                    public void bodyPart(int partNumber, MultipartInputStream partIn) throws IOException {

                        Map<String, List<String>> headerParams = partIn.readHeaderParams();
                        String mediaType;
                        String contentType = headerParams.get("content-type").get(0);

                        if (contentType.contains("transfer-syntax"))
                            mediaType = contentType.split(";")[0];
                        else
                            mediaType = contentType;

                        // choose writer
                        if (main.isMetadata) {
                            main.writerType = ResponseWriter.XML;

                        } else {
                            if (mediaType.equalsIgnoreCase("application/dicom")) {
                                main.writerType = ResponseWriter.DICOM;
                            } else if (isBulkMediaType(mediaType)) {
                                main.writerType = ResponseWriter.BULK;
                            } else {
                                throw new IllegalArgumentException("Unknown media type " + "returned by server, media type = " + mediaType);
                            }

                        }
                        try {
                            main.writerType.readBody(main, partIn, headerParams);
                        } catch (Exception e) {
                            System.out.println("Error parsing media type to determine extension" + e);
                        }
                    }

                    private boolean isBulkMediaType(String mediaType) {
                        if (mediaType.contains("octet-stream"))
                            return true;
                        for (Field field : MediaTypes.class.getFields()) {
                            try {
                                if (field.getType().equals(String.class)) {
                                    String tmp = (String) field.get(field);
                                    if (tmp.equalsIgnoreCase(mediaType))
                                        return true;
                                }
                            } catch (Exception e) {
                                System.out.println("Error deciding media type " + e);
                            }
                        }

                        return false;
                    }
                });
                fin.close();
                spool.delete();
            } catch (Exception e) {
                System.out.println("Error parsing Server response - " + e);
            }
        }
        connection.disconnect();

        main.response = new WadoRSResponse(responseCode, reponseMessage, main.retrievedInstances);
        return new SimpleHTTPResponse(responseCode, reponseMessage);

    }

    private static String dumpHeader(WadoRS main, Map<String, List<String>> headerFields) throws FileNotFoundException {
        File f = new File(main.outDir, "request-head");
        main.retrievedInstances.put("multipart-request-head", f.toPath().toAbsolutePath());
        return writeHeader(headerFields, f);
    }

    private static String writeHeader(Map<String, List<String>> headerFields,
            File f) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(f);
        for(String key : headerFields.keySet()) {
            if(key != null)
            writer.print(key+":\t");
            writer.print(headerFields.get(key));
            writer.println();
        }
        writer.close();
        return f.getAbsolutePath();
    }

    private static String[] compileAcceptHeader(String[] acceptTypes) {
        String[] acceptHeaders = new String[acceptTypes.length];
        for (int i = 0; i < acceptTypes.length; i++) {
            String acceptType = acceptTypes[i];
            if (acceptType.contains("application/json") || acceptType.contains("application/zip") || acceptType.contains("multipart/related")) {
                acceptHeaders[i] = acceptType;
            } else {
                String mediaType = acceptType;
                String transferSyntaxUID = null;
                if (mediaType.contains(";")) {
                    String[] splittedAcceptHeader = acceptType.split(";");
                    mediaType = splittedAcceptHeader[0];
                    transferSyntaxUID = splittedAcceptHeader[1];
                }

                acceptHeaders[i] = "multipart/related; type=" + mediaType;

                if (transferSyntaxUID != null) {
                    acceptHeaders[i] += "; transfer-syntax=" + transferSyntaxUID;
                }
            }
        }
        return acceptHeaders;
    }

    private enum ResponseWriter {
        XML {
            @Override
            boolean readBody(WadoRS wadors, InputStream in, Map<String, List<String>> headerParams)
                    throws Exception {
                TransformerHandler th = getTransformerHandler(wadors);
                th.getTransformer().setOutputProperty(OutputKeys.INDENT, wadors.xmlIndent ? "yes" : "no");
                // here the sax parser would actually close the input stream immediately
                InputStream is = cloneStream(in);

                Attributes attrs = SAXReader.parse(is);
                SAXWriter saxWriter = new SAXWriter(th);

                File outputDirectory = wadors.getOutDir();

                String fileName = attrs.getString(Tag.SOPInstanceUID);
                if (fileName == null) {
                    fileName = "" + wadors.getNextPartIndex();
                    LOG.info("Unable to decide SopInstanceUID, using part counter");
                }
                outputDirectory = ensureDirs(wadors, attrs);

                File out = new File(outputDirectory, fileName + ".xml");
                th.setResult(new StreamResult(out));
                saxWriter.setIncludeKeyword(wadors.xmlIncludeKeyword);
                saxWriter.write(attrs);
                String headPath;
                if (wadors.dumpHeader) {
                    headPath = writeHeader(headerParams, new File(out.getAbsolutePath() + "-head"));
                } else {
                    headPath = attrs.getString(Tag.SOPInstanceUID);
                }
                wadors.retrievedInstances.put(headPath, out.toPath().toAbsolutePath());
                return true;
            }

            private TransformerHandler getTransformerHandler(WadoRS main)
                    throws Exception {

                SAXTransformerFactory stf = main.saxTransformer;

                if (stf == null)
                    main.saxTransformer = stf = (SAXTransformerFactory) TransformerFactory
                            .newInstance();

                if (main.getXsltFile() == null)
                    return stf.newTransformerHandler();

                Templates templates = main.xsltTemplates;

                if (templates == null) {
                    templates = stf
                            .newTemplates(new StreamSource(main.xsltFile));
                    main.xsltTemplates = templates;
                }

                return stf.newTransformerHandler(templates);
            }
        },
        DICOM {
            @Override
            boolean readBody(WadoRS wadors, InputStream in, Map<String, List<String>> headerParams)
                    throws IOException {
                Attributes fmi;
                Attributes attrs;
                DicomInputStream dis = new DicomInputStream(in);
                try {
                    fmi = dis.readFileMetaInformation();
                    attrs = dis.readDataset(-1, -1);
                } finally {
                    dis.close();
                }

                File outputDirectory = wadors.getOutDir();

                String fileName = attrs.getString(Tag.SOPInstanceUID);
                if (fileName == null) {
                    fileName = "" + wadors.getNextPartIndex();
                    LOG.info("Unable to decide SopInstanceUID, using part counter");
                }
                outputDirectory = ensureDirs(wadors, attrs);
                    
                File out = new File(outputDirectory, fileName + ".dcm");
                DicomOutputStream os = new DicomOutputStream(out);
                os.writeDataset(fmi, attrs);
                os.close();
                String headPath;
                if (wadors.dumpHeader) {
                    headPath = writeHeader(headerParams, new File(out.getAbsolutePath() + "-head"));
                } else {
                    headPath = attrs.getString(Tag.SOPInstanceUID);
                }
                wadors.retrievedInstances.put(headPath, out.toPath().toAbsolutePath());
                return true;
            }
        },
        BULK {
            @Override
            boolean readBody(WadoRS wadors, InputStream in, Map<String, List<String>> headerParams)
                    throws IOException {

                String frame = null;
                List<String> headerContentLocations = headerParams.get("content-location");
                String contentLocation = (headerContentLocations != null && !headerContentLocations.isEmpty()) ? headerContentLocations.get(0) : null;
                if (contentLocation != null && contentLocation.contains("frames/"))
                    frame = contentLocation.split("frames/")[1];

                File outputDirectory = wadors.getOutDir();
                String fileName = "" + wadors.getNextPartIndex();

                if (frame != null) {
                    fileName += "-frame" + frame;
                }

                File out = new File(outputDirectory,fileName + ".blk");
                Files.copy(in,out.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                String headPath;
                if(wadors.dumpHeader) {
                    headPath = writeHeader(headerParams, new File(out.getAbsolutePath()+"-head"));
                }
                else {
                    headPath = fileName;
                }
                wadors.retrievedInstances.put(headPath, out.toPath().toAbsolutePath());
                return true;
            }
        };

        abstract boolean readBody(WadoRS wadors, InputStream in, Map<String, List<String>> headerParams)
                throws IOException, Exception;

        protected File ensureDirs(WadoRS wadors, Attributes attrs) {
            String url = wadors.getUrl();
            String seriesuid,studyuid;
            File parentDir = wadors.getOutDir();
                studyuid = url.split("studies/")[1].split("/")[0];
            if(url.contains("series")) {
                seriesuid = url.split("series/")[1].split("/")[0];
            }
            else {
                seriesuid = attrs.getString(Tag.SeriesInstanceUID);
            }
            File outDir = new File(new File(parentDir,studyuid), seriesuid);
            if(!outDir.exists())
                outDir.mkdirs();
            return outDir;
        }

        private static InputStream cloneStream(InputStream inputStream) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                byte[] buf = new byte[4096];

                // inputStream is your original stream.
                for (int n; 0 < (n = inputStream.read(buf));) {
                    baos.write(buf, 0, n);
                }
                baos.close();
                copyHolder = baos.toByteArray();
            } catch (Exception e) {
                System.out
                        .println("wadors : Error copying the stream " + e);
            }
            // This is the new stream that you can pass it to other code and
            // use its data.
            return new ByteArrayInputStream(copyHolder);
        }
    }

    public File getOutDir() {
        return outDir;
    }

    public void setOutDir(File outDir) {
        outDir.mkdirs();
        this.outDir = outDir;
    }

    public File getXsltFile() {
        return xsltFile;
    }

    public void setXsltFile(File xsltFile) {
        this.xsltFile = xsltFile;
    }

    public String getRequestTimeOut() {
        return requestTimeOut;
    }

    public void setRequestTimeOut(String requestTimeOut) {
        this.requestTimeOut = requestTimeOut;
    }

    public boolean isXmlIndent() {
        return xmlIndent;
    }

    public void setXmlIndent(boolean xmlIndent) {
        this.xmlIndent = xmlIndent;
    }

    public boolean isXmlIncludeKeyword() {
        return xmlIncludeKeyword;
    }

    public void setXmlIncludeKeyword(boolean xmlIncludeKeyword) {
        this.xmlIncludeKeyword = xmlIncludeKeyword;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String[] getAcceptType() {
        return acceptTypes;
    }

    public void setAcceptType(String[] acceptTypes) {
        this.acceptTypes = acceptTypes;
    }

    public int getNextPartIndex() {
        return ++this.partIndex;
    }

    public Map<String, Path> getRetrievedInstances() {
        return retrievedInstances;
    }

    public WadoRSResponse getResponse() {
        return response;
    }

    public void setDumpHeaders(boolean b) {
        this.dumpHeader = b;
    }
}
