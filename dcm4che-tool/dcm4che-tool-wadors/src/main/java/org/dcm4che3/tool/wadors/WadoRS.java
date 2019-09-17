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
import org.dcm4che3.util.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since June 2018
 */

public class WadoRS {
    private static final Logger LOG = LoggerFactory.getLogger(WadoRS.class);
    private static final ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.wadors.messages");
    private static String user;
    private static String bearer;
    private static boolean header;
    private String accept = "*";
    private static String outDir;

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
        if (cl.hasOption("a"))
            wadoRS.setAccept(cl.getOptionValues("a"));
        user = cl.getOptionValue("u");
        bearer = cl.getOptionValue("bearer");
        outDir = cl.getOptionValue("out-dir");
    }

    private void wado(String url) throws Exception {
        final String uid = uidFrom(url);
        if (!header)
            url = appendAcceptToURL(url);
        URL newUrl = new URL(url);
        final HttpURLConnection connection = (HttpURLConnection) newUrl.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        if (header)
            connection.setRequestProperty("Accept", accept);
        logOutgoing(connection);
        authorize(connection);
        logIncoming(connection);
        unpack(connection, uid);
        connection.disconnect();
    }

    private void authorize(HttpURLConnection connection) {
        if (user == null && bearer == null)
            return;

        String authorization = user != null ? basicAuth() : "Bearer " + bearer;
        LOG.info("> Authorization: " + authorization);
        connection.setRequestProperty("Authorization", authorization);
    }

    private static String basicAuth() {
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

    private void logOutgoing(HttpURLConnection connection) {
        LOG.info("> " + connection.getRequestMethod() + " " + connection.getURL());
        LOG.info("> Accept: " + accept);
    }

    private void logIncoming(HttpURLConnection connection) throws Exception {
        LOG.info("< Content-Length: " + connection.getContentLength());
        LOG.info("< HTTP/1.1 Response: " + connection.getResponseCode() + " " + connection.getResponseMessage());
        LOG.info("< Transfer-Encoding: " + connection.getContentEncoding());
        LOG.info("< ETag: " + connection.getHeaderField("ETag"));
        LOG.info("< Last-Modified: " + connection.getHeaderField("Last-Modified"));
        LOG.info("< Content-Type: " + connection.getContentType());
        LOG.info("< Date: " + connection.getHeaderField("Date"));
    }

    private void unpack(HttpURLConnection connection, final String uid) throws Exception {
        if (connection.getResponseCode() != 200 && connection.getResponseCode() != 206) {
            LOG.info(connection.getResponseMessage() + ": " + connection.getResponseCode());
            return;
        }

        try (InputStream is = connection.getInputStream()) {
            String contentType = connection.getContentType();
            if (!contentType.contains("multipart/related")) {
                write(uid, partExtension(contentType), is);
                return;
            }

            String boundary = boundary(connection);
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

    private String boundary(HttpURLConnection connection) {
        String[] respContentTypeParams = connection.getContentType().split(";");
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