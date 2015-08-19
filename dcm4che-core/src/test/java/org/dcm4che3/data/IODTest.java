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

package org.dcm4che3.data;


import static org.junit.Assert.assertTrue;

import java.io.File;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.IOD;
import org.dcm4che3.data.ValidationResult;
import org.dcm4che3.io.DicomInputStream;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class IODTest {

    @Test
    public void testValidateDICOMDIR() throws Exception {
        IOD iod = IOD.load("resource:dicomdir-iod.xml");
        Attributes attrs = readDataset("DICOMDIR");
        ValidationResult result = attrs.validate(iod);
        assertTrue(result.isValid());
    }

    @Test
    public void testValidateCode() throws Exception {
        IOD iod = IOD.load("resource:code-iod.xml");
        Attributes attrs = new Attributes(2);
        attrs.newSequence(Tag.ConceptNameCodeSequence, 1).add(
                new Code("CV-9991", "99DCM4CHE", null, "CM-9991").toItem());
        Attributes contentNode = new Attributes(2);
        contentNode.newSequence(Tag.ConceptNameCodeSequence, 1).add(
                new Code("CV-9992", "99DCM4CHE", null, "CM-9992").toItem());
        contentNode.newSequence(Tag.ConceptCodeSequence, 1).add(
                new Code("CV-9993", "99DCM4CHE", null, "CM-9993").toItem());
        attrs.newSequence(Tag.ContentSequence, 1).add(contentNode);
        ValidationResult result = attrs.validate(iod);
        assertTrue(result.isValid());
    }

    private static Attributes readDataset(String name)
            throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        DicomInputStream in = new DicomInputStream(
                new File(cl.getResource(name).toURI()));
        try {
            return in.readDataset(-1, -1);
        } finally {
            in.close();
        }
    }
}
