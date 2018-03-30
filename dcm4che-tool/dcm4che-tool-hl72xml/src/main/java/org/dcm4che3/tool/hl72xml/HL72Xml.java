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
 * Java(TM), hosted at https://github.com/dcm4che.
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

package org.dcm4che3.tool.hl72xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option.Builder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.hl7.HL7Charset;
import org.dcm4che3.hl7.HL7Parser;
import org.dcm4che3.hl7.HL7Segment;
import org.dcm4che3.tool.common.CLIUtils;
import org.xml.sax.SAXException;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class HL72Xml {

    private static ResourceBundle rb =
            ResourceBundle.getBundle("org.dcm4che3.tool.hl72xml.messages");

        private URL xslt;
        private boolean indent = false;
        private boolean includeNamespaceDeclaration = false;
        private String charset;

        public final void setXSLT(URL xslt) {
            this.xslt = xslt;
        }

        public final void setIndent(boolean indent) {
            this.indent = indent;
        }

        public final void setIncludeNamespaceDeclaration(boolean includeNamespaceDeclaration) {
            this.includeNamespaceDeclaration = includeNamespaceDeclaration;
        }

        public String getCharacterSet() {
            return charset;
        }

        public void setCharacterSet(String charset) {
            this.charset = charset;
        }

        @SuppressWarnings("static-access")
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
                    .longOpt("charset")
                    .hasArg()
                    .argName("name")
                    .desc(rb.getString("charset"))
                    .build());
            opts.addOption("I", "indent", false, rb.getString("indent"));
            opts.addOption(null, "xmlns", false, rb.getString("xmlns"));

            return CLIUtils.parseComandLine(args, opts, rb, HL72Xml.class);
        }

        @SuppressWarnings("unchecked")
        public static void main(String[] args) {
            try {
                CommandLine cl = parseComandLine(args);
                HL72Xml main = new HL72Xml();
                if (cl.hasOption("x")) {
                    String s = cl.getOptionValue("x");
                    main.setXSLT(new File(s).toURI().toURL());
                }
                main.setCharacterSet(cl.getOptionValue("charset"));
                main.setIndent(cl.hasOption("I"));
                main.setIncludeNamespaceDeclaration(cl.hasOption("xmlns"));
                String fname = fname(cl.getArgList());
                if (fname.equals("-")) {
                    main.parse(System.in);
                } else {
                    FileInputStream dis = new FileInputStream(fname);
                    try {
                        main.parse(dis);
                    } finally {
                        dis.close();
                    }
                }
            } catch (ParseException e) {
                System.err.println("hl72xml: " + e.getMessage());
                System.err.println(rb.getString("try"));
                System.exit(2);
            } catch (Exception e) {
                System.err.println("hl72xml: " + e.getMessage());
                e.printStackTrace();
                System.exit(2);
            }
        }

        private static String fname(List<String> argList) throws ParseException {
            int numArgs = argList.size();
            if (numArgs == 0)
                throw new ParseException(rb.getString("missing"));
            if (numArgs > 1)
                throw new ParseException(rb.getString("too-many"));
            return argList.get(0);
        }

        public void parse(InputStream is) throws IOException,
                TransformerConfigurationException, SAXException {
            byte[] buf = new byte[256];
            int len = is.read(buf);
            HL7Segment msh = HL7Segment.parseMSH(buf, buf.length);
            String charsetName = HL7Charset.toCharsetName(msh.getField(17, charset));
            Reader reader = new InputStreamReader(
                    new SequenceInputStream(
                            new ByteArrayInputStream(buf, 0, len), is),
                    charsetName);
            TransformerHandler th = getTransformerHandler();
            th.getTransformer().setOutputProperty(OutputKeys.INDENT, 
                    indent ? "yes" : "no");
            th.setResult(new StreamResult(System.out));
            HL7Parser hl7Parser = new HL7Parser(th);
            hl7Parser.setIncludeNamespaceDeclaration(includeNamespaceDeclaration);
            hl7Parser.parse(reader);
        }

        private TransformerHandler getTransformerHandler()
                throws TransformerConfigurationException, IOException {
            SAXTransformerFactory tf = (SAXTransformerFactory)
                    TransformerFactory.newInstance();
            if (xslt == null)
                return tf.newTransformerHandler();

            TransformerHandler th = tf.newTransformerHandler(
                    new StreamSource(xslt.openStream(), xslt.toExternalForm()));
            return th;
        }

}
