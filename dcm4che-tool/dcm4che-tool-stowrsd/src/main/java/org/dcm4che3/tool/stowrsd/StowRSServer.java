/*
 * **** BEGIN LICENSE BLOCK *****
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
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2015-2019
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
 * **** END LICENSE BLOCK *****
 *
 */

package org.dcm4che3.tool.stowrsd;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.cli.*;
import org.dcm4che3.mime.MultipartInputStream;
import org.dcm4che3.mime.MultipartParser;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Mar 2021
 */
public class StowRSServer {
    private static final Logger LOG = LoggerFactory.getLogger(StowRSServer.class);
    private static final ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.stowrsd.messages");

    private final byte[] responseXML;
    private final byte[] responseJSON;
    private final HttpServer server;
    private Path storageDir;
    private boolean unpack;

    public StowRSServer(InetSocketAddress addr, int backlog, int threads) throws IOException {
        responseXML = loadResource("resource:response.xml");
        responseJSON = loadResource("resource:response.json");
        server = HttpServer.create(addr, backlog);
        if (threads > 1)
            server.setExecutor(Executors.newFixedThreadPool(threads));
        server.createContext("/", this::handle);
    }

    public void start() {
        LOG.info("Start listening on {}", server.getAddress());
        server.start();
    }

    public void stop(int delay) {
        server.stop(delay);
    }

    private static class LogHeaders {
        final String prefix;
        final Set<Map.Entry<String, List<String>>> headers;

        LogHeaders(String prefix, Set<Map.Entry<String, List<String>>> headers) {
            this.prefix = prefix;
            this.headers = headers;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, List<String>> entry : headers) {
                sb.append(System.lineSeparator())
                        .append(prefix).append(entry.getKey())
                        .append(": ").append(entry.getValue());
            }
            return sb.toString();
        }
    }

    private void handle(HttpExchange httpExchange) throws IOException {
        LOG.info("{} -> {}{}< {} {} {}{}",
                httpExchange.getRemoteAddress(),
                httpExchange.getLocalAddress(),
                System.lineSeparator(),
                httpExchange.getRequestMethod(),
                httpExchange.getRequestURI(),
                httpExchange.getProtocol(),
                new LogHeaders("< ", httpExchange.getRequestHeaders().entrySet()));
        switch (httpExchange.getRequestMethod()) {
            case "POST":
                onPOST(httpExchange);
                break;
            case "OPTIONS":
                onOPTIONS(httpExchange);
                break;
            default:
                sendResponseHeaders(httpExchange, 405,"Method Not Allowed", -1);
        }
    }

    private void onOPTIONS(HttpExchange httpExchange) throws IOException {
        Headers requestHeaders = httpExchange.getRequestHeaders();
        Headers responseHeaders = httpExchange.getResponseHeaders();
        setNotNull(responseHeaders, "Access-control-allow-origin",
                requestHeaders.getFirst("Origin"));
        setNotNull(responseHeaders, "Access-control-allow-methods",
                requestHeaders.getFirst("Access-control-request-method"));
        setNotNull(responseHeaders, "Access-control-allow-headers",
                requestHeaders.getFirst("Access-control-request-headers"));
        sendResponseHeaders(httpExchange, 204,"No Content", -1);
    }

    private void onPOST(HttpExchange httpExchange) throws IOException {
        String requestContentType = httpExchange.getRequestHeaders().getFirst("Content-type");
        if (requestContentType == null) {
            LOG.warn("Missing Content-type header");
            sendResponseHeaders(httpExchange, 400,"Bad Request", -1);
            return;
        }
        String[] requestContentTypeParams = StringUtils.split(requestContentType, ';');
        if (!"multipart/related".equalsIgnoreCase(requestContentTypeParams[0])) {
            sendResponseHeaders(httpExchange, 415,"Unsupported Media Type", -1);
            return;
        }
        if (unpack) {
            String boundary = boundary(requestContentTypeParams);
            if (boundary == null) {
                LOG.warn("Missing boundary parameter in Content-type: {}", requestContentType);
                sendResponseHeaders(httpExchange, 400, "Bad Request", -1);
                return;
            }
            new MultipartParser(boundary).parse(httpExchange.getRequestBody(), new MultipartHandler());
        } else {
            if (storageDir != null) {
                Date now = new Date();
                for (; ; ) {
                    try {
                        Path file = storageDir.resolve(DateUtils.formatDT(null, now) + ".multipart");
                        Files.copy(httpExchange.getRequestBody(), file);
                        LOG.info("* M-WRITE {}", file);
                        break;
                    } catch (FileAlreadyExistsException e) {
                        now = new Date(now.getTime() + 1);
                    }
                }
            } else {
                StreamUtils.skipAll(httpExchange.getRequestBody());
            }
        }
        String mediaType = selectMediaType(httpExchange);
        if (mediaType != null) {
            byte[] response = mediaType.endsWith("xml") ? responseXML : responseJSON;
            Headers requestHeaders = httpExchange.getRequestHeaders();
            Headers responseHeaders = httpExchange.getResponseHeaders();
            setNotNull(responseHeaders, "Access-control-allow-origin", requestHeaders.getFirst("Origin"));
            responseHeaders.set("Content-type", mediaType);
            sendResponseHeaders(httpExchange, 200, "OK", response.length);
            try (OutputStream out = httpExchange.getResponseBody()) {
                out.write(response);
            }
        } else {
            sendResponseHeaders(httpExchange, 406,"Not Acceptable", -1);
        }
    }

    private static void setNotNull(Headers responseHeaders, String key, String value) {
        if (value != null) responseHeaders.set(key, value);
    }

    private static String boundary(String[] params) {
        for (int i = 1; i < params.length; i++) {
            String param = params[i].trim();
            if (param.length() > 12 && param.startsWith("boundary=")) {
                return param.charAt(9) == '"' && param.charAt(param.length() - 1) == '"'
                        ? param.substring(10, param.length() - 1)
                        : param.substring(9);
            }
        }
        return null;
    }

    private static void sendResponseHeaders(HttpExchange httpExchange, int rCode, String rMsg, int responseLength)
            throws IOException {
        httpExchange.sendResponseHeaders(rCode, responseLength);
        LOG.info("{} -> {}{}> {} {} {}{}",
                httpExchange.getRemoteAddress(),
                httpExchange.getLocalAddress(),
                System.lineSeparator(),
                httpExchange.getProtocol(), rCode, rMsg,
                new LogHeaders("> ", httpExchange.getResponseHeaders().entrySet()));
    }

    private static byte[] loadResource(String name) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamUtils.copy(StreamUtils.openFileOrURL(name), out);
        return out.toByteArray();
    }

    private static String selectMediaType(HttpExchange httpExchange) {
        for (String acceptHeader: httpExchange.getRequestHeaders().get("Accept")) {
            for (String mediaType : StringUtils.split(acceptHeader, ',')) {
                switch (withoutParams(mediaType).toLowerCase()) {
                    case "*":
                    case "*/*":
                    case "application/*":
                    case "application/dicom+json":
                        return "application/dicom+json";
                    case "application/json":
                        return "application/json";
                    case "application/dicom+xml":
                        return "application/dicom+xml";
                    case "text/*":
                    case "text/xml":
                        return "text/xml";
                }
            }
        }
        return null;
    }

    private static String withoutParams(String mediaType) {
        int endIndex = mediaType.indexOf(';');
        return endIndex < 0 ? mediaType : mediaType.substring(0, endIndex);
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            StowRSServer main = new StowRSServer(
                    toInetSocketAddress(cl),
                    CLIUtils.getIntOption(cl, "backlog", 0),
                    CLIUtils.getIntOption(cl, "threads", 1));
            if (!cl.hasOption("ignore")) {
                main.setStorageDirectory(
                        Paths.get(cl.getOptionValue("d", ".")));
            }
            main.setUnpack(cl.hasOption("u"));
            main.start();
        } catch (ParseException e) {
            System.err.println("stowrsd: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("stowrsd: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    public void setStorageDirectory(Path storageDir) throws IOException {
        if (storageDir != null)
            Files.createDirectories(storageDir);
        this.storageDir = storageDir;
    }

    public void setUnpack(boolean unpack) {
        this.unpack = unpack;
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        Options opts = new Options();
        addOptions(opts);
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, StowRSServer.class);
    }

    public static void addOptions(Options opts) {
        opts.addOption("u", "unpack", false, rb.getString("unpack"));
        opts.addOption(null, "ignore", false, rb.getString("ignore"));
        opts.addOption(Option.builder("d")
                .hasArg()
                .argName("path")
                .desc(rb.getString("directory"))
                .longOpt("directory")
                .build());
        opts.addOption(Option.builder()
                .hasArg()
                .argName("no")
                .desc(rb.getString("backlog"))
                .longOpt("backlog")
                .build());
        opts.addOption(Option.builder("t")
                .hasArg()
                .argName("no")
                .desc(rb.getString("threads"))
                .longOpt("threads")
                .build());
        opts.addOption(Option.builder("b")
                .hasArg()
                .argName("[ip:]port")
                .desc(rb.getString("bind-server"))
                .longOpt("bind")
                .build());
    }

    private static InetSocketAddress toInetSocketAddress(CommandLine cl) throws MissingOptionException {
        if (!cl.hasOption("b"))
            throw new MissingOptionException(rb.getString("missing-bind-opt"));
        String s = cl.getOptionValue("b");
        int index = s.indexOf(':');
        return index < 0
                ? new InetSocketAddress(Integer.parseInt(s))
                : new InetSocketAddress(s.substring(0, index), Integer.parseInt(s.substring(index + 1)));
    }

    private class MultipartHandler implements MultipartParser.Handler {
        private Path storageSupDir;

        MultipartHandler() throws IOException {
            if (storageDir != null) {
                Date now = new Date();
                for (; ; ) {
                    try {
                        storageSupDir = Files.createDirectory(storageDir.resolve(DateUtils.formatDT(null, now)));
                        break;
                    } catch (FileAlreadyExistsException e) {
                        now = new Date(now.getTime() + 1);
                    }
                }
            }
        }

        @Override
        public void bodyPart(int partNumber, MultipartInputStream in) throws IOException {
            Map<String, List<String>> partHeaders = in.readHeaderParams();
            LOG.info("< Part #{}:{}", partNumber, new LogHeaders("< ", partHeaders.entrySet()));
            if (storageSupDir != null) {
                Path file = storageSupDir.resolve(
                        String.format("%03d", partNumber) + suffix(partHeaders.get("Content-type")));
                LOG.info("* M-WRITE {}", file);
                Files.copy(in, file);
            } else {
                in.skipAll();
            }
        }
    }

    private static String suffix(List<String> contentTypes) {
        if (contentTypes.isEmpty()) return "";
        String[] split = StringUtils.split(contentTypes.get(0).toLowerCase(), ';');
        return split[0].endsWith("dicom") ? ".dcm"
                : split[0].endsWith("xml") ? ".xml"
                : split[0].endsWith("json") ? ".json"
                : "";
    }

    private static final OutputStream NULL = new OutputStream() {
        @Override
        public void write(int b) {
        }

        @Override
        public void write(byte[] b, int off, int len) {
        }
    };
}
