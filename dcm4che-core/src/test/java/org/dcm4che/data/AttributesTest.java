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

package org.dcm4che.data;

import static org.junit.Assert.*;

import java.util.Date;

import org.dcm4che.util.DateUtils;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class AttributesTest {

    /**
     * Test method for {@link org.dcm4che.data.Attributes#getDate(long, java.util.Date)}.
     */
    @Test
    public void testGetDateLongDate() {
        Attributes a = new Attributes();
        a.setString(Tag.StudyDate, VR.DA, "20110404");
        a.setString(Tag.StudyTime, VR.TM, "15");
        Date d = a.getDate(Tag.StudyDateAndTime, null);
        assertEquals("20110404150000.000", DateUtils.formatDT(null, d));
    }

    /**
     * Test method for {@link org.dcm4che.data.Attributes#getDateRange(int, org.dcm4che.data.DateRange)}.
     */
    @Test
    public void testGetDateRangeIntDateRange() {
        Attributes a = new Attributes();
        a.setString(Tag.StudyDate, VR.DA, "20110404-20110405");
        DateRange range = a.getDateRange(Tag.StudyDate, null);
        assertEquals("20110404000000.000",
                DateUtils.formatDT(null, range.getStartDate()));
        assertEquals("20110405235959.999",
                DateUtils.formatDT(null, range.getEndDate()));
    }

    /**
     * Test method for {@link org.dcm4che.data.Attributes#getDateRange(long, org.dcm4che.data.DateRange)}.
     */
    @Test
    public void testGetDateRangeLongDateRange() {
        Attributes a = new Attributes();
        a.setString(Tag.StudyDate, VR.DA, "20110404");
        a.setString(Tag.StudyTime, VR.TM, "15-20");
        DateRange range = a.getDateRange(Tag.StudyDateAndTime, null);
        assertEquals("20110404150000.000",
                DateUtils.formatDT(null, range.getStartDate()));
        assertEquals("20110404205959.999",
                DateUtils.formatDT(null, range.getEndDate()));
    }

    /**
     * Test method for {@link org.dcm4che.data.Attributes#setDate(long, java.util.Date)}.
     */
    @Test
    public void testSetDateLongDate() {
        Attributes a = new Attributes();
        a.setDate(Tag.StudyDateAndTime,
                DateUtils.parseDT(null, "20110404150000.000"));
        assertEquals("20110404", a.getString(Tag.StudyDate, null));
        assertEquals("150000.000", a.getString(Tag.StudyTime, null));
    }

    /**
     * Test method for {@link org.dcm4che.data.Attributes#setDateRange(int, org.dcm4che.data.VR, org.dcm4che.data.DateRange)}.
     */
    @Test
    public void testSetDateRangeIntVRDateRange() {
        Attributes a = new Attributes();
        Date lower = DateUtils.parseDA(null, "20110404");
        Date upper = DateUtils.parseDA(null, "20110405");
        a.setDateRange(Tag.StudyDate, VR.DA, new DateRange(lower, upper));
        assertEquals("20110404-20110405", a.getString(Tag.StudyDate, null));
    }

    /**
     * Test method for {@link org.dcm4che.data.Attributes#setDateRange(long, org.dcm4che.data.DateRange)}.
     */
    @Test
    public void testSetDateRangeLongDateRange() {
        Attributes a = new Attributes();
        Date lower = DateUtils.parseDT(null, "2011040415");
        a.setDateRange(Tag.StudyDateAndTime, new DateRange(lower, null));
        assertEquals("20110404-", a.getString(Tag.StudyDate, null));
        assertEquals("150000.000-", a.getString(Tag.StudyTime, null));
    }

    @Test
    public void testCreatorTagOf() {
        Attributes a = new Attributes();
        a.setString(0x00090010, VR.LO, "CREATOR1");
        a.setString(0x00091010, VR.LO, "VALUE1");
        a.setNull(0x00090015, VR.LO);
        a.setString(0x00090020, VR.LO, "CREATOR2");
        a.setString(0x00092010, VR.LO, "VALUE2");
        a.setNull("CREATOR3", 0x00090010, VR.LO);
        assertEquals("VALUE1", a.getString("CREATOR1", 0x00090010, null, null));
        assertEquals("VALUE2", a.getString("CREATOR2", 0x00090010, null, null));
        assertEquals("CREATOR3", a.getString(0x00090021));
    }

    @Test
    public void testEqualsPrivate() {
        Attributes a1 = new Attributes();
        a1.setString(0x00090010, VR.LO, "CREATOR1");
        a1.setString(0x00091010, VR.LO, "VALUE1");
        Attributes a2 = new Attributes();
        a2.setString(0x00090020, VR.LO, "CREATOR1");
        a2.setString(0x00092010, VR.LO, "VALUE1");
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));
    }
}
