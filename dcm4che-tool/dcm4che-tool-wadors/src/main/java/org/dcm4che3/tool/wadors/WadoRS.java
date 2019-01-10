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
 *  Portions created by the Initial Developer are Copyright (C) 2015-2018
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
    private static final String wildcard = "wildcard";
    private Input input = Input.METADATA_JSON;
    private String user;
    private Accept accept;
    private boolean header;
    private static Path outDir;

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

    private void setOutputDirectory(String dir) throws IOException {
        outDir = Files.createDirectories(Paths.get(dir));
    }

    private void setUser(String user) {
        this.user = user;
    }

    private void setAcceptType(String accept) {
        this.accept = Accept.valueOf(accept);
    }

    private void setHeader(boolean val) {
        this.header = val;
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
                .hasArg(false)
                .desc(rb.getString("header"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("out-dir")
                .hasArg()
                .argName("directory")
                .desc(rb.getString("out-dir"))
                .build());
        opts.addOption(Option.builder("u")
                .hasArg()
                .argName("user:password")
                .longOpt("user")
                .desc(rb.getString("user"))
                .build());
        return CLIUtils.parseComandLine(args, opts, rb, WadoRS.class);
    }

    private static void init(CommandLine cl, WadoRS wadoRS) throws Exception {
        if (cl.getArgList().isEmpty())
            throw new MissingArgumentException("Specify at least one url as an argument");
        wadoRS.setAcceptType(cl.hasOption("a") ? cl.getOptionValue("a") : wildcard);
        wadoRS.setUser(cl.getOptionValue("u"));
        if (cl.hasOption("out-dir"))
            wadoRS.setOutputDirectory(cl.getOptionValue("out-dir"));
        wadoRS.setHeader(cl.hasOption("header"));
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
            connection.setRequestProperty("Accept", accept.headerVal);
        logOutgoing(connection);
        if (user != null) {
            String basicAuth = basicAuth(user);
            LOG.info("> Authorization: " + basicAuth);
            connection.setRequestProperty("Authorization", basicAuth);
        }
        logIncoming(connection);
        unpack(connection, uid);
        connection.disconnect();
    }

    private String appendAcceptToURL(String url) {
        return url
                + (url.indexOf('?') != -1 ? "&" : "?")
                + "accept="
                + accept.headerVal.replace("+", "%2B");
    }

    private String uidFrom(String url) {
        return url.contains("metadata")
                ? url.substring(url.substring(0, url.lastIndexOf('/')).lastIndexOf('/') + 1, url.lastIndexOf('/'))
                : url.contains("?")
                    ? url.substring(url.substring(0, url.indexOf('?')).lastIndexOf('/') + 1, url.indexOf('?'))
                    : url.substring(url.lastIndexOf('/')+1);
    }

    enum Accept {
        wildcard("*"),
        dicom("multipart/related;type=application/dicom"),
        octetstream("multipart/related;type=application/octet-stream"),
        pdf("multipart/related;type=application/pdf"),
        jpeg("multipart/related;type=image/jpeg"),
        jp2("multipart/related;type=image/jp2"),
        jpx("multipart/related;type=image/jpx"),
        xdicomrle("multipart/related;type=image/x-dicom+rle"),
        xjls("multipart/related;type=image/x-jls"),
        mpeg("multipart/related;type=video/mpeg"),
        mp4("multipart/related;type=video/mp4"),
        xml("multipart/related;type=application/dicom+xml"),
        json("application/dicom+json");

        String headerVal;

        Accept(String headerVal) {
            this.headerVal = headerVal;
        }
    }

    private void logOutgoing(HttpURLConnection connection) {
        LOG.info("> " + connection.getRequestMethod() + " " + connection.getURL());
        LOG.info("> Accept: " + accept.headerVal);
    }

    private void logIncoming(HttpURLConnection connection) throws Exception {
        LOG.info("< Content-Length: " + connection.getContentLength());
        LOG.info("< HTTP/1.1 Response: " + String.valueOf(connection.getResponseCode()) + " " + connection.getResponseMessage());
        LOG.info("< Transfer-Encoding: " + connection.getContentEncoding());
        LOG.info("< ETag: " + connection.getHeaderField("ETag"));
        LOG.info("< Last-Modified: " + connection.getHeaderField("Last-Modified"));
        LOG.info("< Content-Type: " + connection.getContentType());
        LOG.info("< Date: " + connection.getHeaderField("Date"));
    }

    private void unpack(HttpURLConnection connection, final String uid) throws Exception {
        try (InputStream is = connection.getInputStream()) {
            if (connection.getContentType().endsWith("json")) {
                LOG.info("Extract metadata as json");
                input.writeBodyPart(is, 1, uid);
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
                        if (!input.writeBodyPart(multipartInputStream, partNumber, uid)) {
                            LOG.info("{}: Ignore Part with Content-Type={}"
                                    + getHeaderParamValue(headerParams, "content-type"));
                            multipartInputStream.skipAll();
                        }
                    } catch (Exception e) {
                        LOG.warn("Failed to process Part #" + partNumber + headerParams, e);
                    }
                }
            });
        }
    }

    private static String getHeaderParamValue(Map<String, List<String>> headerParams, String key) {
        List<String> list = headerParams.get(key);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    private String boundary(HttpURLConnection connection) {
        String contentType = connection.getContentType();
        String[] strings = contentType.split(";");
        String type = "";
        String boundary = null;
        for (String s : strings) {
            if (s.contains("boundary"))
                boundary = s.substring(s.indexOf("=")+1);
            if (s.contains("type"))
                type = s.substring(s.indexOf("=")+1).replaceAll("\"", "");
        }
        setInput(toInput(type));
        return boundary;
    }

    private static Input toInput(String type) {
        return type.endsWith("dicom")
                ? Input.DICOM
                : type.endsWith("dicom+xml")
                    ? Input.METADATA_XML
                    : type.startsWith("image")
                        ? Input.IMAGE
                        : type.startsWith("video")
                            ? Input.VIDEO
                            : type.endsWith("pdf")
                                ? Input.PDF : Input.OCTET_STREAM;
    }

    private static String basicAuth(String userPswd) {
        byte[] userPswdBytes = userPswd.getBytes();
        int len = (userPswdBytes.length * 4 / 3 + 3) & ~3;
        char[] ch = new char[len];
        Base64.encode(userPswdBytes, 0, userPswdBytes.length, ch, 0);
        return "Basic " + new String(ch);
    }

    private enum Input {
        DICOM {
            @Override
            boolean writeBodyPart(InputStream in, int partNumber, String uid) throws IOException {
                write(in, partNumber, uid, "dicom");
                return true;
            }
        },
        IMAGE {
            @Override
            boolean writeBodyPart(InputStream in, int partNumber, String uid) throws IOException {
                write(in, partNumber, uid, "jpeg");
                return true;
            }
        },
        VIDEO {
            @Override
            boolean writeBodyPart(InputStream in, int partNumber, String uid) throws IOException {
                write(in, partNumber, uid, "mpeg");
                return true;
            }
        },
        PDF {
            @Override
            boolean writeBodyPart(InputStream in, int partNumber, String uid) throws IOException {
                write(in, partNumber, uid, "pdf");
                return true;
            }
        },
        OCTET_STREAM {
            @Override
            boolean writeBodyPart(InputStream in, int partNumber, String uid) throws IOException {
                write(in, partNumber, uid, "native");
                return true;
            }
        },
        METADATA_XML {
            @Override
            boolean writeBodyPart(InputStream in, int partNumber, String uid) throws IOException {
                write(in, partNumber, uid, "xml");
                return true;
            }
        },
        METADATA_JSON {
            @Override
            boolean writeBodyPart(InputStream in, int partNumber, String uid) throws IOException {
                write(in, partNumber, uid, "json");
                return false;
            }
        };

        abstract boolean writeBodyPart(InputStream in, int partNumber, String uid) throws IOException;
    }

    private static void write(InputStream in, int partNumber, String uid, String ext) throws IOException {
        String file = uid + "-" + String.format("%03d", partNumber) + "." + ext;
        Path path = outDir != null
                ? outDir.resolve(file)
                : Paths.get(file);
        try (OutputStream out = Files.newOutputStream(path)) {
            StreamUtils.copy(in, out);
        }
    }

}
