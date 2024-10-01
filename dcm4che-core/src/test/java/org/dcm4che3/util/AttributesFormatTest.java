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

package org.dcm4che3.util;

import static org.junit.Assert.*;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class AttributesFormatTest {

    private static final String TEST_PATTERN = 
        "{00080020,date,yyyy/MM/dd}/{00080030,time,HH}/{0020000D,hash}/{0020000E,hash}/{00080008[1]}/{00080018}.dcm";
    private static final String TEST_PATTERN_MD5 = 
        "{00080020,date,yyyy/MM/dd}/{00080030,time,HH}/{0020000D,hash}/{0020000E,hash}/{00080018,md5}.dcm";
    private static final String TEST_PATTERN_RND =
        "{rnd}/{rnd,uuid}/{rnd,uid}";
    private static final String TEST_PATTERN_OFFSET =
        "{00200011,offset,100}/{00200013,offset,-1}";
    private static final String TEST_PATTERN_SLICE =
        "{00100020,slice,3}/{00100020,slice,3,6}/{00100020,slice,-3}/{00100020,slice,-6,-3}";
    private static final Pattern ASSERT_PATTERN_RND =
            Pattern.compile("[0-9A-F]{8}+/[0-9a-f]{8}+(-[0-9a-f]{4}+){3}+-[0-9a-f]{12}+/2\\.25\\.\\d*");

    @Test
    public void testFormat() {
        Attributes attrs = new Attributes();
        attrs.setString(Tag.ImageType, VR.CS, "ORIGINAL", "PRIMARY", "AXIAL");
        attrs.setString(Tag.StudyDate, VR.DA, "20111012");
        attrs.setString(Tag.StudyTime, VR.TM, "0930");
        attrs.setString(Tag.StudyInstanceUID, VR.UI, "1.2.3");
        attrs.setString(Tag.SeriesInstanceUID, VR.UI, "1.2.3.4");
        attrs.setString(Tag.SOPInstanceUID, VR.UI, "1.2.3.4.5");
        assertEquals("2011/10/12/09/02C82A3A/71668980/PRIMARY/1.2.3.4.5.dcm",
                new AttributesFormat(TEST_PATTERN).format(attrs));
    }
    
    @Test
    public void testFormatMD5() {
        Attributes attrs = new Attributes();
        attrs.setString(Tag.StudyDate, VR.DA, "20111012");
        attrs.setString(Tag.StudyTime, VR.TM, "0930");
        attrs.setString(Tag.StudyInstanceUID, VR.UI, "1.2.3");
        attrs.setString(Tag.SeriesInstanceUID, VR.UI, "1.2.3.4");
        attrs.setString(Tag.SOPInstanceUID, VR.UI, "1.2.3.4.5");
        assertEquals("2011/10/12/09/02C82A3A/71668980/08vpsu2l2shpb0kc3orpgfnhv0.dcm",
                new AttributesFormat(TEST_PATTERN_MD5).format(attrs));
    }

    @Test
    public void testFormatRND() {
        assertTrue(ASSERT_PATTERN_RND.matcher(
                new AttributesFormat(TEST_PATTERN_RND).format(new Attributes())).matches());
    }

    @Test
    public void testOffset() {
        Attributes attrs = new Attributes();
        attrs.setString(Tag.SeriesNumber, VR.IS, "1");
        attrs.setString(Tag.InstanceNumber, VR.IS, "2");
        assertEquals("101/1", new AttributesFormat(TEST_PATTERN_OFFSET).format(attrs));
    }

    @Test
    public void testSlice() {
        Attributes attrs = new Attributes(1);
        attrs.setString(Tag.PatientID, VR.LO, "123456789");
        assertEquals("456789/456/789/456", new AttributesFormat(TEST_PATTERN_SLICE).format(attrs));
    }

    @Test
    public void testUpper() {
        Attributes attrs = new Attributes(1);
        attrs.setString(Tag.PatientName, VR.PN, "Simson^Homer");
        assertEquals("SIMSON^HOMER", new AttributesFormat("{00100010,upper}").format(attrs));
    }

    @Test
    public void testDateTimeOffset() {
        Attributes attrs = new Attributes(1);
        attrs.setString(Tag.StudyDate, VR.DA, "20111012");
        attrs.setString(Tag.StudyTime, VR.TM, "0930");
        assertEquals("2011/09/12/10/00",
                new AttributesFormat("{00080020,date-P1M,yyyy/MM/dd}/{00080030,time+PT30M,HH/mm}")
                        .format(attrs));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        cal.add(Calendar.MINUTE, 30);
        assertEquals(new SimpleDateFormat("yyyy/MM/dd/HH/mm").format(cal.getTime()),
                new AttributesFormat("{now,date-P1M,yyyy/MM/dd}/{now,time+PT30M,HH/mm}")
                        .format(attrs));
    }
}
