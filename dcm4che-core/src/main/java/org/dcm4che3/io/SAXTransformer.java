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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
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

package org.dcm4che3.io;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.dcm4che3.data.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public abstract class SAXTransformer {

    private static SAXTransformerFactory factory =
            (SAXTransformerFactory) TransformerFactory.newInstance();

    public static SAXWriter getSAXWriter(Templates templates, Attributes result)
            throws TransformerConfigurationException {
        TransformerHandler th = factory.newTransformerHandler(templates);
        th.setResult(new SAXResult(new ContentHandlerAdapter(result)));
        return new SAXWriter(th);
    }

    public static SAXWriter getSAXWriter(Templates templates, Result result)
            throws TransformerConfigurationException {
        TransformerHandler th = factory.newTransformerHandler(templates);
        th.setResult(result);
        return new SAXWriter(th);
    }

    public static SAXWriter getSAXWriter(Result result)
            throws TransformerConfigurationException {
        TransformerHandler th = factory.newTransformerHandler();
        th.setResult(result);
        return new SAXWriter(th);
    }

    public static Attributes transform(Attributes ds, Templates templates,
            boolean includeNameSpaceDeclaration, boolean includeKeword)
            throws SAXException, TransformerConfigurationException {
        Attributes modify = new Attributes();
        SAXWriter w = SAXTransformer.getSAXWriter(templates, modify);
        w.setIncludeNamespaceDeclaration(includeNameSpaceDeclaration);
        w.setIncludeKeyword(includeKeword);
        w.write(ds);
        return modify;
    }

    public static Templates newTemplates(Source source)
            throws TransformerConfigurationException {
        return factory.newTemplates(source);
    }
}
