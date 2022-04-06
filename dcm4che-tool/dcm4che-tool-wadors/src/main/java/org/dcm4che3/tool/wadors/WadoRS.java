/*
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  J4Care.
 *  Portions created by the Initial Developer are Copyright (C) 2015-2019
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 */

package org.dcm4che3.tool.wadors;


import org.apache.commons.cli.*;
import org.dcm4che3.mime.MultipartInputStream;
import org.dcm4che3.mime.MultipartParser;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.Base64;
import org.dcm4che3.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since June 2018
 */

public class WadoRS {
    private static final Logger LOG = LoggerFactory.getLogger(WadoRS.class);
    private static final ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.wadors.messages");
    private static boolean header;
    private static boolean allowAnyHost;
    private static boolean disableTM;
    private static String accept = "*";
    private static String outDir;
    private static String authorization;
    private static Map<String, String> requestProperties;

    public WadoRS() {}

    public static void main(String[] args) {
        try {
            WadoRS wadoRS = new WadoRS();
            CommandLine cl = parseComandLine(args);
            init(cl, wadoRS);
            for (String url : cl.getArgList())
                wadoRS.wado(url);
        } catch (ParseException e) {
            System.err.println("wadors: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("wadors: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private void setAccept(String... accept) {
        StringBuilder sb = new StringBuilder();
        sb.append(!header ? accept[0].replace("+", "%2B") : accept[0]);
        for (int i = 1; i < accept.length; i++)
            sb.append(",").append(!header ? accept[i].replace("+", "%2B") : accept[i]);

        this.accept = sb.toString();
    }

    private static CommandLine parseComandLine(String[] args) throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        opts.addOption(Option.builder("a")
                .longOpt("accept")
                .hasArg()
                .desc(rb.getString("accept"))
                .build());
        opts.addOption(Option.builder("H")
                .hasArg()
                .argName("httpHeader:value")
                .desc(rb.getString("httpHeader"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("header")
                .desc(rb.getString("header"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("out-dir")
                .hasArg()
                .argName("directory")
                .desc(rb.getString("out-dir"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("allowAnyHost")
                .desc(rb.getString("allowAnyHost"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("disableTM")
                .desc(rb.getString("disableTM"))
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
        return CLIUtils.parseComandLine(args, opts, rb, WadoRS.class);
    }

    private static void init(CommandLine cl, WadoRS wadoRS) throws Exception {
        if (cl.getArgList().isEmpty())
            throw new MissingArgumentException("Missing url");
        header = cl.hasOption("header");
        allowAnyHost = cl.hasOption("allowAnyHost");
        disableTM = cl.hasOption("disableTM");
        if (cl.hasOption("a"))
            wadoRS.setAccept(cl.getOptionValues("a"));
        outDir = cl.getOptionValue("out-dir");
        authorization = cl.hasOption("u")
                        ? basicAuth(cl.getOptionValue("u"))
                        : cl.hasOption("bearer") ? "Bearer " + cl.getOptionValue("bearer") : null;
        requestProperties = requestProperties(cl.getOptionValues("H"));
    }

    private void wado(String url) throws Exception {
        final String uid = uidFrom(url);
        if (!header)
            url = appendAcceptToURL(url);
        if (url.startsWith("https"))
            wadoHttps(new URL(url), uid);
        else
            wado(new URL(url), uid);
    }

    private static Map<String, String> requestProperties(String[] httpHeaders) {
        Map<String, String> requestProperties = new HashMap<>();
        if (header)
            requestProperties.put("Accept", accept);
        if (authorization != null)
            requestProperties.put("Authorization", authorization);
        if (httpHeaders != null)
            for (String httpHeader : httpHeaders) {
                int delim = httpHeader.indexOf(':');
                requestProperties.put(httpHeader.substring(0, delim), httpHeader.substring(delim + 1));
            }
        return requestProperties;
    }

    private void wado(URL url, String uid) throws Exception {
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        requestProperties.forEach(connection::setRequestProperty);
        logOutgoing(url, connection.getRequestProperties());
        processWadoResp(connection, uid);
        connection.disconnect();
    }

    private void wadoHttps(URL url, String uid) throws Exception {
        final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        requestProperties.forEach(connection::setRequestProperty);
        if (disableTM)
            connection.setSSLSocketFactory(sslContext().getSocketFactory());
        connection.setHostnameVerifier((hostname, session) -> allowAnyHost);
        logOutgoing(url, connection.getRequestProperties());
        processWadoHttpsResp(connection, uid);
        connection.disconnect();
    }

    SSLContext sslContext() throws GeneralSecurityException {
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trustManagers(), new java.security.SecureRandom());
        return ctx;
    }

    TrustManager[] trustManagers() {
        return new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };
    }
    
    private static String basicAuth(String user) {
        byte[] userPswdBytes = user.getBytes();
        int len = (userPswdBytes.length * 4 / 3 + 3) & ~3;
        char[] ch = new char[len];
        Base64.encode(userPswdBytes, 0, userPswdBytes.length, ch, 0);
        return "Basic " + new String(ch);
    }

    private String appendAcceptToURL(String url) {
        return url
                + (url.indexOf('?') != -1 ? "&" : "?")
                + "accept="
                + accept;
    }

    private String uidFrom(String url) {
        return url.contains("metadata")
                ? url.substring(url.substring(0, url.lastIndexOf('/')).lastIndexOf('/') + 1, url.lastIndexOf('/'))
                : url.contains("?")
                ? url.substring(url.substring(0, url.indexOf('?')).lastIndexOf('/') + 1, url.indexOf('?'))
                : url.substring(url.lastIndexOf('/')+1);
    }

    private void logOutgoing(URL url, Map<String, List<String>> headerFields) {
        LOG.info("> GET " + url.toString());
        headerFields.forEach((k,v) -> LOG.info("> " + k + " : " + String.join(",", v)));
    }

    private void processWadoResp(HttpURLConnection connection, String uid) throws Exception {
        int respCode = connection.getResponseCode();
        logIncoming(respCode, connection.getResponseMessage(), connection.getHeaderFields());
        if (respCode != 200 && respCode != 206)
            return;

        unpack(connection.getInputStream(), connection.getContentType(), uid);
    }

    private void processWadoHttpsResp(HttpsURLConnection connection, String uid) throws Exception {
        int respCode = connection.getResponseCode();
        logIncoming(respCode, connection.getResponseMessage(), connection.getHeaderFields());
        if (respCode != 200 && respCode != 206)
            return;

        unpack(connection.getInputStream(), connection.getContentType(), uid);
    }

    private void logIncoming(int respCode, String respMsg, Map<String, List<String>> headerFields) {
        LOG.info("< HTTP/1.1 Response: " + respCode + " " + respMsg);
        for (Map.Entry<String, List<String>> header : headerFields.entrySet())
            if (header.getKey() != null)
                LOG.info("< " + header.getKey() + " : " + String.join(";", header.getValue()));
    }

    private void unpack(InputStream is, String contentType, final String uid) {
        try {
            if (!contentType.contains("multipart/related")) {
                write(uid, partExtension(contentType), is);
                return;
            }

            String boundary = boundary(contentType);
            if (boundary == null) {
                LOG.warn("Invalid response. Unpacking of parts not possible.");
                return;
            }

            new MultipartParser(boundary).parse(new BufferedInputStream(is), new MultipartParser.Handler() {
                @Override
                public void bodyPart(int partNumber, MultipartInputStream multipartInputStream) throws IOException {
                    Map<String, List<String>> headerParams = multipartInputStream.readHeaderParams();
                    try {
                        String fileName = fileName(partNumber, uid, partExtension(headerParams.get("content-type").get(0)));
                        LOG.info("Extract Part #{} {} \n{}", partNumber, fileName, headerParams);
                        write(multipartInputStream, fileName);
                    } catch (Exception e) {
                        LOG.warn("Failed to process Part #" + partNumber + headerParams, e);
                    }
                }
            });
        } catch (Exception e) {
            LOG.info("Exception caught on unpacking response \n", e);
        }
    }

    private void write(String uid, String ext, InputStream is) throws IOException {
        String fileName = fileName(1, uid, ext);
        LOG.info("Extract {} to {}", ext, fileName);
        write(is, fileName);
    }

    private String partExtension(String partContentType) {
        String contentType = partContentType.split(";")[0].replaceAll("[-+/]", "_");
        return contentType.substring(contentType.lastIndexOf("_") + 1);
    }

    private String boundary(String contentType) {
        String[] respContentTypeParams = contentType.split(";");
        for (String respContentTypeParam : respContentTypeParams)
            if (respContentTypeParam.replace(" ", "").startsWith("boundary="))
                return respContentTypeParam
                        .substring(respContentTypeParam.indexOf("=") + 1)
                        .replaceAll("\"", "");

        return null;
    }

    private String fileName(int partNumber, String uid, String ext) {
        return uid + "-" + String.format("%03d", partNumber) + "." + ext;
    }

    private static void write(InputStream in, String fileName) throws IOException {
        Path path = outDir != null
                ? Files.createDirectories(Paths.get(outDir)).resolve(fileName)
                : Paths.get(fileName);
        try (OutputStream out = Files.newOutputStream(path)) {
            StreamUtils.copy(in, out);
        }
    }

}