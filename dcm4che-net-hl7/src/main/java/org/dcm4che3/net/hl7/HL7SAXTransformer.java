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

package org.dcm4che3.net.hl7;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.hl7.HL7Charset;
import org.dcm4che3.hl7.HL7ContentHandler;
import org.dcm4che3.hl7.HL7Parser;
import org.dcm4che3.io.ContentHandlerAdapter;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.io.SAXWriter;
import org.xml.sax.SAXException;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import java.io.*;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Jan 2020
 */
public class HL7SAXTransformer {

    private HL7SAXTransformer() {}

    private static SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();

    public static Attributes transform(byte[] data, String hl7charset, String dicomCharset, Templates templates,
            SAXTransformer.SetupTransformer setup)
            throws TransformerConfigurationException, IOException, SAXException {
        Attributes attrs = new Attributes();
        if (dicomCharset != null)
            attrs.setString(Tag.SpecificCharacterSet, VR.CS, dicomCharset);
        TransformerHandler th = factory.newTransformerHandler(templates);
        th.setResult(new SAXResult(new ContentHandlerAdapter(attrs)));
        if (setup != null)
            setup.setup(th.getTransformer());
        new HL7Parser(th).parse(new InputStreamReader(
                new ByteArrayInputStream(data),
                HL7Charset.toCharsetName(hl7charset)));
        return attrs;
    }

    public static byte[] transform(Attributes attrs, String hl7charset, Templates templates,
            boolean includeNameSpaceDeclaration, boolean includeKeword,
            SAXTransformer.SetupTransformer setup)
            throws TransformerConfigurationException, SAXException, UnsupportedEncodingException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        TransformerHandler th = factory.newTransformerHandler(templates);
        th.setResult(new SAXResult(new HL7ContentHandler(new OutputStreamWriter(out, HL7Charset.toCharsetName(hl7charset)))));
        if (setup != null)
            setup.setup(th.getTransformer());

        SAXWriter saxWriter = new SAXWriter(th);
        saxWriter.setIncludeKeyword(includeKeword);
        saxWriter.setIncludeNamespaceDeclaration(includeNameSpaceDeclaration);
        saxWriter.write(attrs);

        return out.toByteArray();
    }
}
