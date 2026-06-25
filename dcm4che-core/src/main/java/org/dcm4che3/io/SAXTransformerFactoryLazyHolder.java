/*
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
 * Portions created by the Initial Developer are Copyright (C) 2015-2018
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
 */

package org.dcm4che3.io;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;

/**
 * @author Gunter Zeilinger <gunterze@protonmail.com>
 * @since Jun 2026
 */
public class SAXTransformerFactoryLazyHolder {
    private static class LazyHolder {
        static final SAXTransformerFactory factory;
        static {
            factory = (SAXTransformerFactory) TransformerFactory.newInstance();
            try {
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, !"false".equalsIgnoreCase(
                        getPropertyOrEnv(
                                "javax.xml.featureSecureProcessing",
                                "JAVAX_XML_FEATURE_SECURE_PROCESSING",
                                null)));
            } catch (TransformerConfigurationException e) {
                throw new AssertionError("All implementations are required to support the XMLConstants.FEATURE_SECURE_PROCESSING feature", e);
            }
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET,
                    getPropertyOrEnv( "javax.xml.accessExternalStylesheet",
                            "JAVAX_XML_ACCESS_EXTERNAL_STYLESHEET",
                            "file"));
        }
    }

    private static String getPropertyOrEnv(String property, String env, String def) {
        String value = System.getProperty(property);
        if (value == null) {
            value = System.getenv(env);
            if (value == null) {
                value = def;
            }
        }
        return value;
    }

    public static SAXTransformerFactory getInstance() {
        return LazyHolder.factory;
    }

}
