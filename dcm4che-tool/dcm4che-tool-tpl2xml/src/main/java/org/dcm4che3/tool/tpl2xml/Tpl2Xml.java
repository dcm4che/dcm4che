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
 *  Portions created by the Initial Developer are Copyright (C) 2015-2020
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

package org.dcm4che3.tool.tpl2xml;

import org.apache.commons.cli.*;
import org.dcm4che3.tool.common.CLIUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since June 2020
 */
public class Tpl2Xml {
    private static final ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.tpl2xml.messages");
    private static final String XML_1_0 = "1.0";
    private static final String XML_1_1 = "1.1";
    private static final String licenseBlock = "~ ***** BEGIN LICENSE BLOCK *****\n" +
            "  ~ Version: MPL 1.1/GPL 2.0/LGPL 2.1\n" +
            "  ~\n" +
            "  ~ The contents of this file are subject to the Mozilla Public License Version\n" +
            "  ~ 1.1 (the \"License\"); you may not use this file except in compliance with\n" +
            "  ~ the License. You may obtain a copy of the License at\n" +
            "  ~ http://www.mozilla.org/MPL/\n" +
            "  ~\n" +
            "  ~ Software distributed under the License is distributed on an \"AS IS\" basis,\n" +
            "  ~ WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License\n" +
            "  ~ for the specific language governing rights and limitations under the\n" +
            "  ~ License.\n" +
            "  ~\n" +
            "  ~ The Original Code is part of dcm4che, an implementation of DICOM(TM) in\n" +
            "  ~ Java(TM), hosted at https://github.com/dcm4che.\n" +
            "  ~\n" +
            "  ~ The Initial Developer of the Original Code is\n" +
            "  ~ J4Care.\n" +
            "  ~ Portions created by the Initial Developer are Copyright (C) 2015-2019\n" +
            "  ~ the Initial Developer. All Rights Reserved.\n" +
            "  ~\n" +
            "  ~ Contributor(s):\n" +
            "  ~ See @authors listed below\n" +
            "  ~\n" +
            "  ~ Alternatively, the contents of this file may be used under the terms of\n" +
            "  ~ either the GNU General Public License Version 2 or later (the \"GPL\"), or\n" +
            "  ~ the GNU Lesser General Public License Version 2.1 or later (the \"LGPL\"),\n" +
            "  ~ in which case the provisions of the GPL or the LGPL are applicable instead\n" +
            "  ~ of those above. If you wish to allow use of your version of this file only\n" +
            "  ~ under the terms of either the GPL or the LGPL, and not to allow others to\n" +
            "  ~ use your version of this file under the terms of the MPL, indicate your\n" +
            "  ~ decision by deleting the provisions above and replace them with the notice\n" +
            "  ~ and other provisions required by the GPL or the LGPL. If you do not delete\n" +
            "  ~ the provisions above, a recipient may use your version of this file under\n" +
            "  ~ the terms of any one of the MPL, the GPL or the LGPL.\n" +
            "  ~\n" +
            "  ~ ***** END LICENSE BLOCK *****\n" +
            "  ~";
    private static final String elements = "elements";

    private boolean indent = false;
    private String xmlVersion = XML_1_0;
    private String outDir;

    public final void setIndent(boolean indent) {
        this.indent = indent;
    }

    public final void setXMLVersion(String xmlVersion) {
        this.xmlVersion = xmlVersion;
    }

    public final void setOutDir(String outDir) {
        this.outDir = outDir;
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseComandLine(args);
            Tpl2Xml main = new Tpl2Xml();
            init(cl, main);
            for (String template : cl.getArgList())
                main.convert(template);
        } catch (ParseException e) {
            System.err.println("tpl2xml: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("tpl2xml: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static CommandLine parseComandLine(String[] args) throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        opts.addOption(Option.builder()
                .longOpt("out-dir")
                .hasArg()
                .argName("directory")
                .desc(rb.getString("out-dir"))
                .build());
        opts.addOption("I", "indent", false, rb.getString("indent"));
        opts.addOption(null, "xml11", false, rb.getString("xml11"));
        return CLIUtils.parseComandLine(args, opts, rb, Tpl2Xml.class);
    }

    private static void init(CommandLine cl, Tpl2Xml main) throws Exception {
        if (cl.getArgList().isEmpty())
            throw new MissingArgumentException(rb.getString("missing-template"));
        main.setOutDir(cl.getOptionValue("out-dir"));
        main.setIndent(cl.hasOption("I"));
        if (cl.hasOption("xml11"))
            main.setXMLVersion(XML_1_1);
    }

    private void convert(String template) throws Exception {
        Path dir = outputDirectory(template);
        System.out.println(MessageFormat.format(rb.getString("convert-template"), template));
        for (Map.Entry<String, List<DictionaryElement>> entry : privateDictsFrom(template).entrySet()) {
            Path file = Files.createFile(dir.resolve(
                    entry.getKey().replaceAll("[:;?\\s/]", "-") + ".xml"));
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            document.insertBefore(
                    document.createComment("\n" + licenseBlock + "\n"),
                    document.getDocumentElement());
            Element root = document.createElement(elements);
            document.appendChild(root);
            Set<String> keywords = new HashSet<>();
            Set<String> tags = new HashSet<>();
            for (DictionaryElement dictElement : entry.getValue()) {
                if (duplicateTagsOrKeywords(dictElement, keywords, tags))
                    continue;

                Element el = document.createElement("el");
                root.appendChild(el);
                el.setAttribute("tag", dictElement.getTag());
                el.setAttribute("keyword", dictElement.getKeyword());
                el.setAttribute("vr", dictElement.getVr());
                el.setAttribute("vm", dictElement.getVm());
                el.appendChild(document.createTextNode(dictElement.getValue()));
            }
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(file.toFile());
            getTransformer().transform(domSource, streamResult);
            System.out.println(MessageFormat.format(rb.getString("converted"), file));
        }
    }

    private boolean duplicateTagsOrKeywords(DictionaryElement dictElement, Set<String> keywords, Set<String> tags) {
        if (keywords.add(dictElement.getKeyword()) && tags.add(dictElement.getTag()))
            return false;

        System.out.println("Ignoring duplicate tag or keyword entry: [tag=" + dictElement.getTag()
                + ", keyword=" + dictElement.getKeyword()
                + ", vr=" + dictElement.getVr()
                + ", vm=" + dictElement.getVm()
                + ", value=" + dictElement.getValue() + "]");
        return true;
    }

    private Transformer getTransformer() throws TransformerConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
        if (indent)
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.VERSION, xmlVersion);
        return transformer;
    }

    private Path outputDirectory(String template) throws IOException {
        if (outDir == null)
            return Paths.get(template).getParent();

        return Files.createDirectories(Paths.get(outDir));
    }

    private static Map<String, List<DictionaryElement>> privateDictsFrom(String template) throws IOException {
        Map<String, List<DictionaryElement>> privateDictionaries = new HashMap<>();
        Files.readAllLines(Paths.get(template))
                .stream()
                .filter(line -> line.length() > 0)
                .forEach(line -> {
                    String[] fields = line.split("[)\"][\\s\t\n]+");
                    privateDictionaries.computeIfAbsent(
                            fields[4].substring(7), dictionaryElement -> new ArrayList<>())
                            .add(new DictionaryElement(fields));
                });
        return privateDictionaries;
    }

    static class DictionaryElement {
        private final String vr;
        private final String vm;
        private final String value;
        private String tag;
        private String keyword;

        DictionaryElement(String[] fields) {
            this.vr = fields[2].substring(4);
            this.vm = fields[3].substring(4);
            this.value = fields[6].endsWith("\"")
                    ? fields[6].substring(6, fields[6].length() - 1)
                    : fields[6].substring(6);
            setTagAndKeyword(fields[0], fields[5].substring(9));
        }

        String getVr() {
            return vr;
        }

        String getKeyword() {
            return keyword;
        }

        String getTag() {
            return tag;
        }

        String getVm() {
            return vm;
        }

        String getValue() {
            return value;
        }

        private void setTagAndKeyword(String tag, String keyword) {
            String groupTag = tag.substring(1, 5).toUpperCase();
            String elementTag = "xx" + tag.substring(8,10).toUpperCase();
            this.keyword = keyword.equals("?")
                    ? "_" + groupTag + "_" + elementTag + "_"
                    : !Pattern.compile("^[a-zA-Z][a-zA-Z0-9]*$").matcher(keyword).matches()
                        ? improveInvalidKeyword(keyword) : keyword;
            this.tag = groupTag + elementTag;
        }

        private String improveInvalidKeyword(String keyword) {
            System.out.println(MessageFormat.format(rb.getString("invalid-keyword"), keyword));
            if (Character.isDigit(keyword.charAt(0)))
                keyword = wordForFirstDigit(keyword) + keyword.substring(1);
            return keyword.replaceAll("[^A-Za-z0-9]", "");
        }

        private String wordForFirstDigit(String keyword) {
            switch (keyword.charAt(0)) {
                case '0':
                    return "Zero";
                case '1':
                    return "One";
                case '2':
                    return "Two";
                case '3':
                    return "Three";
                case '4':
                    return "Four";
                case '5':
                    return "Five";
                case '6':
                    return "Six";
                case '7':
                    return "Seven";
                case '8':
                    return "Eight";
                case '9':
                    return "Nine";
            }
            return null;
        }
    }
}
