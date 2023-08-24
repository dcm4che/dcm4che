/*
 * *** BEGIN LICENSE BLOCK *****
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
 * Portions created by the Initial Developer are Copyright (C) 2013-2021
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
 * *** END LICENSE BLOCK *****
 */

package org.dcm4che3.tool.xroad;

import org.apache.commons.cli.*;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.xroad.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.ws.Holder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Jun 2023
 */
public class XRoad implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(XRoad.class);
    private static final ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.xroad.messages");
    private static final String[] HEADERS = {
            "#",
            "cIsikukoodid",
            "cValjad",
            "cIsikukood",
            "cPerenimi",
            "cEesnimi",
            "cMPerenimed",
            "cMEesnimed",
            "cRiikKood",
            "cRiik",
            "cIsanimi",
            "cSugu",
            "cSynniaeg",
            "cSurmKpv",
            "cTeoVoime",
            "cIsStaatus",
            "cKirjeStaatus",
            "cEKRiik",
            "cEKMaak",
            "cEKVald",
            "cEKAsula",
            "cEKTanav",
            "cEKIndeks",
            "cEKAlgKpv",
            "cEKVallaKpv",
            "cEKAadress",
            "cSynniRiik",
            "cSaabusEestiKpv",
            "faultCode",
            "faultString"
    };
    private final XRoadAdapterPortType port;
    private final String cValjad;
    private final XRoadClientIdentifierType clientIdentifierType;
    private final XRoadServiceIdentifierType serviceIdentifierType;
    private final String userID;
    private final String id;
    private final String protocolVersion;
    private final BufferedWriter csvWriter;
    private final boolean csvHeader;
    private final char csvDelim;
    private final Character csvQuote;
    private final boolean continueOnError;

    private XRoad(String userID, String url, CommandLine cl) throws IOException {
        port = new XRoadService().getXRoadServicePort();
        XRoadUtils.setEndpointAddress(port, url);
        cValjad = cl.getOptionValue("rr441.cValjad", "1,2,6,7,9,10");
        clientIdentifierType = XRoadUtils.createXRoadClientIdentifierType(
                cl.getOptionValue("client.objectType", XRoadObjectType.SUBSYSTEM.name()),
                cl.getOptionValue("client.xRoadInstance", "EE"),
                cl.getOptionValue("client.memberClass", "NGO"),
                cl.getOptionValue("client.memberCode", "90007945"),
                cl.getOptionValue("client.subsystemCode", "mia"));
        serviceIdentifierType = XRoadUtils.createXRoadServiceIdentifierType(
                cl.getOptionValue("service.serviceCode", "RR441"),
                cl.getOptionValue("service.objectType", XRoadObjectType.SERVICE.name()),
                cl.getOptionValue("service.xRoadInstance", "EE"),
                cl.getOptionValue("service.memberClass", "GOV"),
                cl.getOptionValue("service.memberCode", "70008440"),
                cl.getOptionValue("service.subsystemCode", "rr"),
                cl.getOptionValue("service.serviceVersion", "v1"));
        this.userID = userID;
        id = cl.getOptionValue("id", "");
        protocolVersion = cl.getOptionValue("protocolVersion", "4.0");
        csvWriter = newBufferedWriter(cl.getOptionValue("csv"));
        csvHeader = !cl.hasOption("csv-no-header");
        csvDelim = csvDelim(cl.getOptionValue("csv-delim"));
        csvQuote = csvQuote(cl.getOptionValue("csv-quote"));
        continueOnError = cl.hasOption("c");
    }

    private BufferedWriter newBufferedWriter(String pathName) throws IOException {
        if (pathName == null) return null;
        Path path = Paths.get(pathName);
        Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);
        return Files.newBufferedWriter(path);
    }

    private char csvDelim(String delim) {
        return delim != null && delim.length() == 1 ? delim.charAt(0) : ',';
    }

    private Character csvQuote(String quote) {
        return quote != null && quote.length() == 1 ? quote.charAt(0) : null;
    }

    @Override
    public void close() throws IOException {
        if (csvWriter != null) csvWriter.close();
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            String userID = cl.getOptionValue("u");
            if (userID == null)
                throw new MissingOptionException(
                        rb.getString("missing-user-opt"));
            String url = cl.getOptionValue("U");
            if (url == null)
                throw new MissingOptionException(
                        rb.getString("missing-url-opt"));

            List<String> pids = cl.getArgList();
            if (pids.isEmpty())
                throw new MissingOptionException(
                        rb.getString("missing-pid"));
            try (XRoad xRoad = new XRoad(userID, url, cl)) {
                xRoad.writeCsvHeaders();
                for (String pid : pids) {
                    if (pid.startsWith("@")) {
                        for (String pid2 : readLines(pid.substring(1))) {
                            xRoad.rr441(pid2);
                        }
                    } else {
                        xRoad.rr441(pid);
                    }
                }
            }
        } catch (ParseException e) {
            System.err.println("XRoad: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("XRoad: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static List<String> readLines(String pathName) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(pathName))) {
            return reader.lines().collect(Collectors.toList());
        }
    }

    private void rr441(String pid) throws Exception {
        RR441RequestType rq = XRoadUtils.createRR441RequestType(cValjad, pid);
        LOG.info("<< RR441Request{cIsikukoodid={}, cValjad={}}", pid, cValjad);
        try {
            RR441ResponseType rsp = rr441(rq);
            LOG.info(">> RR441Response{{}}", new Object() {
                public String toString() {
                    return XRoad.this.toString(rsp);
                }
            });
            if (csvWriter != null) writeCsvRows(rq, rsp);
        } catch (Exception e) {
            LOG.warn("Failed to receive RR441Response for RR441Request{cIsikukoodid={}, cValjad={}}:\n",
                    pid, cValjad, e);
            if (!continueOnError) throw e;
            if (csvWriter != null) writeCsvRow(rq, e);
        }
    }

    private String toString(RR441ResponseType rsp) {
        StringBuffer sb = new StringBuffer(256);
        List<RR441ResponseType.TtIsikuid.TtIsikud> ttIsikudList = rsp.getTtIsikuid().getTtIsikud();
        if (!ttIsikudList.isEmpty()) {
            sb.append('[');
            for (RR441ResponseType.TtIsikuid.TtIsikud ttIsikud : ttIsikudList) {
                sb.append('(');
                appendTo(sb, 1, ttIsikud.getTtIsikudCIsikukood());
                appendTo(sb, 2, ttIsikud.getTtIsikudCPerenimi());
                appendTo(sb, 3, ttIsikud.getTtIsikudCEesnimi());
                appendTo(sb, 4, ttIsikud.getTtIsikudCMPerenimed());
                appendTo(sb, 5, ttIsikud.getTtIsikudCMEesnimed());
                appendTo(sb, 6, ttIsikud.getTtIsikudCRiikKood());
                appendTo(sb, 7, ttIsikud.getTtIsikudCRiik());
                appendTo(sb, 8, ttIsikud.getTtIsikudCIsanimi());
                appendTo(sb, 9, ttIsikud.getTtIsikudCSugu());
                appendTo(sb, 10, ttIsikud.getTtIsikudCSynniaeg());
                appendTo(sb, 11, ttIsikud.getTtIsikudCSurmKpv());
                appendTo(sb, 12, ttIsikud.getTtIsikudCTeoVoime());
                appendTo(sb, 13, ttIsikud.getTtIsikudCIsStaatus());
                appendTo(sb, 14, ttIsikud.getTtIsikudCKirjeStaatus());
                appendTo(sb, 15, ttIsikud.getTtIsikudCEKRiik());
                appendTo(sb, 16, ttIsikud.getTtIsikudCEKMaak());
                appendTo(sb, 17, ttIsikud.getTtIsikudCEKVald());
                appendTo(sb, 18, ttIsikud.getTtIsikudCEKAsula());
                appendTo(sb, 19, ttIsikud.getTtIsikudCEKTanav());
                appendTo(sb, 20, ttIsikud.getTtIsikudCEKIndeks());
                appendTo(sb, 21, ttIsikud.getTtIsikudCEKAlgKpv());
                appendTo(sb, 22, ttIsikud.getTtIsikudCEKVallaKpv());
                appendTo(sb, 23, ttIsikud.getTtIsikudCEKAadress());
                appendTo(sb, 24, ttIsikud.getTtIsikudCSynniRiik());
                appendTo(sb, 25, ttIsikud.getTtIsikudCSaabusEestiKpv());
                sb.setLength(sb.length()-2);
                sb.append(')').append(',').append(' ');
            }
            sb.setLength(sb.length()-2);
            sb.append(']').append(',').append(' ');
        }
        appendTo(sb,26, rsp.getFaultCode());
        appendTo(sb,27, rsp.getFaultString());
        sb.setLength(Math.max(0, sb.length()-2));
        return sb.toString();
    }

    private void appendTo(StringBuffer sb, int index, String value) {
        if (value != null && !value.isEmpty())
            sb.append(HEADERS[index]).append('=').append(value).append(',').append(' ');
    }

    private void writeCsvRows(RR441RequestType rq, RR441ResponseType rsp) throws IOException {
        int size = rsp.getTtIsikuid().getTtIsikud().size();
        if (size == 0)
            writeCsvRow(rq, rsp, -1);
        else for (int i = 0; i < size; i++) {
            writeCsvRow(rq, rsp, i);
        }
    }

    private void writeCsvRow(RR441RequestType rq, Exception e) throws IOException {
        writeCsvValue("-1");
        writeCsvDelimiterAndValue(rq.getCIsikukoodid());
        writeCsvDelimiterAndValue(rq.getCValjad());
        for (int i = 0; i < HEADERS.length - 3; i++) {
            csvWriter.write(csvDelim);
        }
        writeCsvDelimiterAndValue(e.getMessage());
        csvWriter.write("\r\n");
    }

    private void writeCsvRow(RR441RequestType rq, RR441ResponseType rsp, int index) throws IOException {
        writeCsvValue(Integer.toString(index + 1));
        writeCsvDelimiterAndValue(rq.getCIsikukoodid());
        writeCsvDelimiterAndValue(rq.getCValjad());
        if (index < 0) {
            for (int i = 0; i < HEADERS.length - 4; i++) {
                csvWriter.write(csvDelim);
            }
        } else {
            RR441ResponseType.TtIsikuid.TtIsikud ttIsikud = rsp.getTtIsikuid().getTtIsikud().get(index);
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCIsikukood());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCPerenimi());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCEesnimi());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCMPerenimed());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCMEesnimed());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCRiikKood());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCRiik());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCIsanimi());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCSugu());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCSynniaeg());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCSurmKpv());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCTeoVoime());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCIsStaatus());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCKirjeStaatus());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCEKRiik());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCEKMaak());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCEKVald());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCEKAsula());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCEKTanav());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCEKIndeks());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCEKAlgKpv());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCEKVallaKpv());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCEKAadress());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCSynniRiik());
            writeCsvDelimiterAndValue(ttIsikud.getTtIsikudCSaabusEestiKpv());
        }
        writeCsvDelimiterAndValue(rsp.getFaultCode());
        writeCsvDelimiterAndValue(rsp.getFaultString());
        csvWriter.write("\r\n");
    }

    private void writeCsvDelimiterAndValue(String value) throws IOException {
        csvWriter.write(csvDelim);
        if (value != null && !value.isEmpty()) writeCsvValue(value);
    }

    private void writeCsvValue(String value) throws IOException {
        if (csvQuote != null || mustBeQuoted(value))
            writeCsvValueQuoted(value, csvQuote != null ? csvQuote : '\"');
        else
            csvWriter.write(value);
    }

    private boolean mustBeQuoted(String value) {
        for (int i = 0, l = value.length(); i < l; i++) {
            char c = value.charAt(i);
            if (c == csvDelim || c ==  '\"' || c == '\r' || c == '\n')
                return true;
        }
        return false;
    }

    private void writeCsvValueQuoted(String value, char quote) throws IOException {
        csvWriter.write(quote);
        for (int i = 0, l = value.length(); i < l; i++) {
            char ch = value.charAt(i);
            csvWriter.write(ch);
            if (ch == quote)
                csvWriter.write(quote);
        }
        csvWriter.write(quote);
    }

    private void writeCsvHeaders() throws IOException {
        if (csvWriter != null && csvHeader) {
            for (int i = 0; i < HEADERS.length; i++) {
                if (i > 0) csvWriter.write(csvDelim);
                if (csvQuote != null) csvWriter.write(csvQuote);
                csvWriter.write(HEADERS[i]);
                if (csvQuote != null) csvWriter.write(csvQuote);
            }
            csvWriter.write("\r\n");
        }
    }

    private RR441ResponseType rr441(RR441RequestType rq) {
        return XRoadUtils.rr441(port,
                rq,
                clientIdentifierType,
                serviceIdentifierType,
                userID,
                id,
                protocolVersion,
                new Holder<>());
    }

    private static CommandLine parseComandLine(String[] args) throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        opts.addOption(Option.builder("U")
                .longOpt("url")
                .hasArg()
                .argName("url")
                .desc(rb.getString("url"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("rr441.cValjad")
                .hasArg()
                .argName("value")
                .desc(rb.getString("rr441.cValjad"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("client.objectType")
                .hasArg()
                .argName("value")
                .desc(rb.getString("client.objectType"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("client.xRoadInstance")
                .hasArg()
                .argName("value")
                .desc(rb.getString("client.xRoadInstance"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("client.memberClass")
                .hasArg()
                .argName("value")
                .desc(rb.getString("client.memberClass"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("client.memberCode")
                .hasArg()
                .argName("value")
                .desc(rb.getString("client.memberCode"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("client.subsystemCode")
                .hasArg()
                .argName("value")
                .desc(rb.getString("client.subsystemCode"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("service.objectType")
                .hasArg()
                .argName("value")
                .desc(rb.getString("service.objectType"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("service.xRoadInstance")
                .hasArg()
                .argName("value")
                .desc(rb.getString("service.xRoadInstance"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("service.memberClass")
                .hasArg()
                .argName("value")
                .desc(rb.getString("service.memberClass"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("service.memberCode")
                .hasArg()
                .argName("value")
                .desc(rb.getString("service.memberCode"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("service.subsystemCode")
                .hasArg()
                .argName("value")
                .desc(rb.getString("service.subsystemCode"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("service.serviceVersion")
                .hasArg()
                .argName("value")
                .desc(rb.getString("service.serviceVersion"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("service.serviceCode")
                .hasArg()
                .argName("value")
                .desc(rb.getString("service.serviceCode"))
                .build());
        opts.addOption(Option.builder("u")
                .longOpt("user")
                .hasArg()
                .argName("id")
                .desc(rb.getString("user"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("id")
                .hasArg()
                .argName("value")
                .desc(rb.getString("id"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("protocolVersion")
                .hasArg()
                .argName("value")
                .desc(rb.getString("protocolVersion"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("csv")
                .hasArg()
                .argName("csv-file")
                .desc(rb.getString("csv"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("csv-no-header")
                .desc(rb.getString("csv-no-header"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("csv-delim")
                .hasArg()
                .argName("char")
                .desc(rb.getString("csv-delim"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("csv-quote")
                .hasArg()
                .argName("char")
                .desc(rb.getString("csv-quote"))
                .build());
        opts.addOption(Option.builder("c")
                .desc(rb.getString("continue-on-error"))
                .build());
        return CLIUtils.parseComandLine(args, opts, rb, XRoad.class);
    }

}
