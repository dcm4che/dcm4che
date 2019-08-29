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
 * Portions created by the Initial Developer are Copyright (C) 2013
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

import static org.junit.Assert.*;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.ValueSelector;
import org.dcm4che3.util.AttributesFormat;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class ValueSelectorTest {

    private static final String XPATH =
            "DicomAttribute[@tag=\"00400275\"]/Item[@number=\"1\"]/"
          + "DicomAttribute[@tag=\"0020000D\"]/Value[@number=\"1\"]";

    private static final String PRIVATE_XPATH =
            "DicomAttribute[@tag=\"00E10024\" and @privateCreator=\"ELCINT1\"]/Value[@number=\"1\"]";

    @Test
    public void testToString() {
        ItemPointer ip = new ItemPointer(Tag.RequestAttributesSequence, 0);
        ValueSelector vs = new ValueSelector(Tag.StudyInstanceUID, null, 0, ip);
        assertEquals(XPATH, vs.toString());
    }

   @Test
    public void testValueOf() {
        ValueSelector vs = ValueSelector.valueOf(XPATH);
        assertEquals(Tag.StudyInstanceUID, vs.tag());
        assertNull(vs.privateCreator());
        assertEquals(0, vs.valueIndex());
        assertEquals(1, vs.level());
        ItemPointer ip = vs.itemPointer(0);
        assertEquals(Tag.RequestAttributesSequence, ip.sequenceTag);
        assertNull(ip.privateCreator);
        assertEquals(0, ip.itemIndex);
    }

   @Test
    public void testPrivateValueOf() {
        ValueSelector vs = ValueSelector.valueOf("DicomAttribute[@privateCreator='ELCINT1' and @tag='00E10024']/Value[@number='1']");
        Attributes attrs = new Attributes(2);
        attrs.setBytes("ELCINT1", 0x00E10024, VR.UN, new byte[]{89, 32});
        System.out.println(vs.selectStringValue(attrs, null));
    }

}
