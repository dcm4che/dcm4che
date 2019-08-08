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
    private Input input = Input.APPLICATION_DICOM_JSON;
    private static String user;
    private static String bearer;
    private AcceptType acceptType = AcceptType.wildcard;
    private static boolean header;
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

    private void setAcceptType(String acceptType) {
        this.acceptType = AcceptType.valueOf(acceptType);
    }

    private void setInput(Input input) {
        this.input = input;
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
        if (cl.hasOption("a"))
            wadoRS.setAcceptType(cl.getOptionValue("a"));
        user = cl.getOptionValue("u");
        bearer = cl.getOptionValue("bearer");
        outDir = cl.getOptionValue("out-dir");
        header = cl.hasOption("header");
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
            connection.setRequestProperty("Accept", acceptType.getAccept());
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
                + acceptType.getAccept().replace("+", "%2B");
    }

    private String uidFrom(String url) {
        return url.contains("metadata")
                ? url.substring(url.substring(0, url.lastIndexOf('/')).lastIndexOf('/') + 1, url.lastIndexOf('/'))
                : url.contains("?")
                    ? url.substring(url.substring(0, url.indexOf('?')).lastIndexOf('/') + 1, url.indexOf('?'))
                    : url.substring(url.lastIndexOf('/')+1);
    }

    enum AcceptType {
        wildcard("*"),
        dicom("multipart/related;type=application/dicom"),
        octetstream("multipart/related;type=application/octet-stream"),
        pdf("multipart/related;type=application/pdf"),
        cda("multipart/related;type=text/xml"),
        stl("multipart/related;type=model/stl"),
        jpeg("multipart/related;type=image/jpeg"),
        jp2("multipart/related;type=image/jp2"),
        jpx("multipart/related;type=image/jpx"),
        xdicomrle("multipart/related;type=image/x-dicom+rle"),
        xjls("multipart/related;type=image/x-jls"),
        mpeg("multipart/related;type=video/mpeg"),
        mp4("multipart/related;type=video/mp4"),
        zip("application/zip"),
        xml("multipart/related;type=application/dicom+xml"),
        json("application/dicom+json");

        private String accept;

        AcceptType(String accept) {
            this.accept = accept;
        }

        String getAccept() {
            return accept;
        }
    }

    private void logOutgoing(HttpURLConnection connection) {
        LOG.info("> " + connection.getRequestMethod() + " " + connection.getURL());
        LOG.info("> Accept: " + acceptType.getAccept());
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
        try (InputStream is = connection.getInputStream()) {
            String contentType = connection.getContentType();
            if (contentType.equals("application/zip")) {
                LOG.info("Extract DICOM data as zip");
                write(is, 1, uid, Input.APPLICATION_ZIP.getExt());
                return;
            }
            if (contentType.endsWith("json")) {
                LOG.info("Extract metadata as json");
                write(is, 1, uid, input.getExt());
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
                    LOG.info("Extract Part #{}{}", partNumber, headerParams);
                    try {
                        write(multipartInputStream, partNumber, uid, input.getExt());
                    } catch (Exception e) {
                        LOG.warn("Failed to process Part #" + partNumber + headerParams, e);
                    }
                }
            });
        }
    }

    private String boundary(HttpURLConnection connection) {
        String contentType = connection.getContentType();
        String[] strings = contentType.split(";");
        String boundary = null;
        for (String s : strings) {
            if (s.contains("boundary="))
                boundary = s.substring(s.indexOf("=")+1).replaceAll("\"", "");
            if (s.contains("type=")) {
                String type = s.substring(s.indexOf("=") + 1).replaceAll("\"", "");
                setInput(Input.valueOf(type.substring(type.indexOf("=")+1)
                        .toUpperCase()
                        .replace("/", "_")
                        .replace("-", "_")
                        .replace("+", "_")));
            }
        }
        return boundary;
    }

    private enum Input {
        APPLICATION_DICOM("dicom"),
        IMAGE_JPEG("jpeg"),
        IMAGE_JP2("jp2"),
        IMAGE_JPX("jpf"),
        IMAGE_X_JLS("jls"),
        IMAGE_X_DICOM_RLE("rle"),
        VIDEO_MPEG("mpeg"),
        VIDEO_MP4("mp4"),
        APPLICATION_ZIP("zip"),
        APPLICATION_PDF("pdf"),
        TEXT_XML("xml"),
        MODEL_STL("stl"),
        APPLICATION_OCTET_STREAM("native"),
        APPLICATION_DICOM_XML("xml"),
        APPLICATION_DICOM_JSON("json");

        private String ext;

        Input(String ext) {
            this.ext = ext;
        }

        String getExt() {
            return ext;
        }
    }

    private static void write(InputStream in, int partNumber, String uid, String ext) throws IOException {
        String file = uid + "-" + String.format("%03d", partNumber) + "." + ext;
        Path path = outDir != null
                ? Files.createDirectories(Paths.get(outDir)).resolve(file)
                : Paths.get(file);
        try (OutputStream out = Files.newOutputStream(path)) {
            StreamUtils.copy(in, out);
        }
    }

}
