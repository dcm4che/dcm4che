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

package org.dcm4che3.tool.agfa2sr;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.*;
import org.dcm4che3.io.ContentHandlerAdapter;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.tool.common.CLIUtils;
import org.dcm4che3.util.AttributesFormat;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.UIDUtils;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Oct 2021
 */
public class Agfa2SR {

    private static final ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.agfa2sr.messages");
    private static final String XML_1_0 = "1.0";
    private static final String XML_1_1 = "1.1";
    private static final byte[] START_OF_REPORT = {
            '<', 'a', 'g', 'f', 'a', ':', 'D', 'i', 'a', 'g', 'n', 'o', 's', 't', 'i', 'c',
            'R', 'a', 'd', 'i', 'o', 'l', 'o', 'g', 'y', 'R', 'e', 'p', 'o', 'r', 't', ' '};
    private static final byte[] END_OF_REPORT = {
            '<', '/', 'a', 'g', 'f', 'a', ':', 'D', 'i', 'a', 'g', 'n', 'o', 's', 't', 'i', 'c',
            'R', 'a', 'd', 'i', 'o', 'l', 'o', 'g', 'y', 'R', 'e', 'p', 'o', 'r', 't', '>'};
    private static final String START_OF_STUDY_IUID = "<StudyInstanceUID>";
    private static final String END_OF_STUDY_IUID = "</StudyInstanceUID>";
    private static final String DEFAULT_XSL = "agfa2sr.xsl";
    private static final String DEFAULT_DOC_TITLE = "(18748-4, LN, \"Diagnostic Imaging Report\")";
    private static final String DEFAULT_LANGUAGE = "(en, RFC5646, \"English\")";
    private static final String DEFAULT_VERIFYING_ORGANIZATION = "N/A";
    private static final String DEFAULT_FORMAT= "{0020000D}";

    private boolean indent;
    private boolean xml;
    private String xsltURL;
    private String xmlVersion;
    private Code languageCode;
    private Code docTitleCode;
    private String verifyingOrganization;
    private AttributesFormat format;
    private Attributes attrs;

    private static String toURL(String fileOrURL) {
        try {
            new URL(fileOrURL);
            return fileOrURL;
        } catch (MalformedURLException e) {
            URL resource = Thread.currentThread().getContextClassLoader().getResource(fileOrURL);
            return resource != null ? resource.toString() : Paths.get(fileOrURL).toUri().toString();
        }
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Agfa2SR main = new Agfa2SR();
            String xsltURL1 = cl.getOptionValue("x", DEFAULT_XSL);
            main.xsltURL = xsltURL1.equalsIgnoreCase("none") ? null : toURL(xsltURL1);
            main.xml = cl.hasOption("xml");
            main.languageCode = new Code(cl.getOptionValue("lang", DEFAULT_LANGUAGE));
            main.docTitleCode = new Code(cl.getOptionValue("title", DEFAULT_DOC_TITLE));
            main.verifyingOrganization = cl.getOptionValue("verifying-org", DEFAULT_VERIFYING_ORGANIZATION);
            main.format = new AttributesFormat(cl.getOptionValue("p", DEFAULT_FORMAT));
            main.indent = cl.hasOption("I");
            main.xmlVersion = cl.hasOption("xml11") ? XML_1_1 : XML_1_0;
            main.attrs = new Attributes();
            CLIUtils.addAttributes(main.attrs, cl.getOptionValues("s"));
            main.mconvert(Paths.get(fname(cl.getArgList())), cl.getOptionValue("d", "."));
        } catch (ParseException e) {
            System.err.println("agfa2sr: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("agfa2sr: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        opts.addOption(Option.builder("x")
                .longOpt("xsl")
                .hasArg()
                .argName("xsl-file")
                .desc(rb.getString("xsl"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("title")
                .hasArg()
                .argName("code")
                .desc(rb.getString("title"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("lang")
                .hasArg()
                .argName("code")
                .desc(rb.getString("lang"))
                .build());
        opts.addOption(Option.builder()
                .longOpt("org")
                .hasArg()
                .argName("name")
                .desc(rb.getString("org"))
                .build());
        opts.addOption(Option.builder("d")
                .hasArg()
                .argName("out-dir")
                .desc(rb.getString("out-dir"))
                .build());
        opts.addOption(Option.builder("p")
                .hasArg()
                .argName("pattern")
                .desc(rb.getString("pattern"))
                .build());
        opts.addOption(Option.builder("s")
                .hasArgs()
                .argName("[seq.]attr=value")
                .desc(rb.getString("set"))
                .build());
        opts.addOption("I", "indent", false, rb.getString("indent"));
        opts.addOption(null, "xml", false, rb.getString("xml"));
        opts.addOption(null, "xml11", false, rb.getString("xml11"));
        return CLIUtils.parseComandLine(args, opts, rb, Agfa2SR.class);
    }

    private static String fname(List<String> argList) throws ParseException {
        int numArgs = argList.size();
        if (numArgs == 0)
            throw new ParseException(rb.getString("missing"));
        if (numArgs > 1)
            throw new ParseException(rb.getString("too-many"));
        return argList.get(0);
    }

    private void mconvert(Path srcPath, String outDir) throws Exception {
        Path dir = Paths.get(outDir);
        Files.createDirectories(dir);
        Transformer t = newTransformer();
        int extracted = 0;
        int missingStudyIUIDs = 0;
        List<Exception> failures = new ArrayList<>();
        try (InputStream in = new BufferedInputStream(Files.newInputStream(srcPath))) {
            ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
            while (indexOf(in, START_OF_REPORT, null) >= 0) {
                out.reset();
                out.write(START_OF_REPORT);
                int reportLength = indexOf(in, END_OF_REPORT, out);
                if (reportLength < 0) {
                    System.out.printf("%nUnexpected EOF - missing </agfa:DiagnosticRadiologyReport>");
                    break;
                }
                String report = new String(out.toByteArray(), StandardCharsets.UTF_8);
                String siuid = cutStudyInstanceUID(report);
                if (siuid == null) {
                    missingStudyIUIDs++;
                    System.out.print('I');
                    continue;
                }
                try {
                    if (t == null) {
                        try (Writer w = getWriter(outDir, siuid)) {
                            w.write(report);
                        }
                    } else {
                        t.setParameter("SeriesInstanceUID", UIDUtils.remapUID(siuid));
                        t.setParameter("SOPInstanceUID", UIDUtils.remapUID(siuid + "1"));
                        if (xml) {
                            try (Writer w = getWriter(outDir, siuid)) {
                                t.transform(new StreamSource(new StringReader(report)), new StreamResult(w));
                            }
                        } else {
                            Attributes attrs = new Attributes(this.attrs);
                            t.transform(new StreamSource(new StringReader(report)),
                                    new SAXResult(new ContentHandlerAdapter(attrs)));
                            File file = new File(outDir, format.format(attrs));
                            file.getParentFile().mkdirs();
                            try (DicomOutputStream dos = new DicomOutputStream(file)) {
                                dos.writeDataset(attrs.createFileMetaInformation(UID.ExplicitVRLittleEndian), attrs);
                            }
                        }
                    }
                } catch (Exception e) {
                    failures.add(e);
                    System.out.print('E');
                    continue;
                }
                extracted++;
                System.out.print('.');
            }
        }
        System.out.println();
        if (missingStudyIUIDs > 0) {
            System.out.printf("Ignored %d reports with missing Study Instance UID.%n", missingStudyIUIDs);
        }
        if (!failures.isEmpty()) {
            System.out.printf("Failed to extract %d reports:%n", failures.size());
            failures.forEach(System.out::println);
        }
        System.out.printf("Extracted %d reports from %s into %s/.%n",
                extracted, srcPath.toAbsolutePath(), dir.toAbsolutePath());
    }

    private BufferedWriter getWriter(String outDir, String siuid) throws IOException {
        return Files.newBufferedWriter(
                Paths.get(outDir, siuid + ".xml"),
                StandardCharsets.UTF_8);
    }

    private static String cutStudyInstanceUID(String report) {
        int start;
        if ((start = report.indexOf(START_OF_STUDY_IUID)) >= 0) {
            int end;
            if ((end = report.indexOf(END_OF_STUDY_IUID, start += START_OF_STUDY_IUID.length())) >= 0) {
                return report.substring(start, end).trim();
            }
        }
        return null;
    }

    private Transformer newTransformer() throws Exception {
        if (xsltURL == null) return null;
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        Transformer t = tf.newTemplates(new StreamSource(xsltURL)).newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
        if (indent) {
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        }
        t.setOutputProperty(OutputKeys.VERSION, xmlVersion);
        t.setParameter("langCodeValue", languageCode.getCodeValue());
        t.setParameter("langCodingSchemeDesignator", languageCode.getCodingSchemeDesignator());
        t.setParameter("langCodeMeaning", languageCode.getCodeMeaning());
        t.setParameter("docTitleCodeValue", docTitleCode.getCodeValue());
        t.setParameter("docTitleCodingSchemeDesignator", docTitleCode.getCodingSchemeDesignator());
        t.setParameter("docTitleCodeMeaning", docTitleCode.getCodeMeaning());
        t.setParameter("VerifyingOrganization", verifyingOrganization);
        t.setParameter("SpecificCharacterSet", attrs.getString(Tag.SpecificCharacterSet, ""));
        t.setParameter("SOPClassUID", attrs.getString(Tag.SOPClassUID, UID.BasicTextSRStorage));
        t.setParameter("Manufacturer", attrs.getString(Tag.Manufacturer, ""));
        t.setParameter("SeriesNumber", attrs.getString(Tag.SeriesNumber, "0"));
        t.setParameter("InstanceNumber", attrs.getString(Tag.InstanceNumber, "1"));
        return t;
    }

    private static int indexOf(InputStream in, byte[] pattern, OutputStream out) throws IOException {
        int index = 0;
        int length = pattern.length;
        byte b0 = pattern[0];
        byte[] b = new byte[length];
        int off = 0, len;
        while (StreamUtils.readAvailable(in, b, off, len = length - off) == len) {
            if (out != null) {
                out.write(b, off, len);
            }
            if (Arrays.equals(b, pattern)) return index;
            int i = 0;
            while (++i < length && b[i] != b0) ;
            index += i;
            if ((off = length - i) > 0) {
                System.arraycopy(b, i, b, 0, off);
            }
        }
        return -1;
    }
}
