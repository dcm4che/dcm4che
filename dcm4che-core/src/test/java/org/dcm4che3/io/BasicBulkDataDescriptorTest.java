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
 *
 * **** END LICENSE BLOCK *****
 *
 */

package org.dcm4che3.io;

import org.dcm4che3.data.VR;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Regression test for dcm4che/dcm4che#1446: adding multiple distinct VR length
 * thresholds must produce one non-null string entry per grouping. The buggy
 * loop never advanced its index, leaving a null entry that triggered a
 * NullPointerException downstream in LdapUtils.attr.
 */
public class BasicBulkDataDescriptorTest {

    @Test
    public void testGetLengthsThresholdsAsStringsSingleEntry() {
        BasicBulkDataDescriptor descriptor = new BasicBulkDataDescriptor();
        descriptor.addLengthsThreshold(1024, VR.OB, VR.OW);

        String[] ss = descriptor.getLengthsThresholdsAsStrings();

        assertEquals(1, ss.length);
        assertNotNull(ss[0]);
        assertEquals("OB,OW=1024", ss[0]);
    }

    @Test
    public void testGetLengthsThresholdsAsStringsMultipleEntries() {
        BasicBulkDataDescriptor descriptor = new BasicBulkDataDescriptor();
        descriptor.addLengthsThreshold(1024, VR.OB, VR.OW);
        descriptor.addLengthsThreshold(64, VR.DS);
        descriptor.addLengthsThreshold(256, VR.LT);

        String[] ss = descriptor.getLengthsThresholdsAsStrings();

        // One entry per distinct length threshold, no null entries.
        assertEquals(3, ss.length);
        for (String s : ss) {
            assertNotNull("getLengthsThresholdsAsStrings() produced a null entry", s);
        }

        // HashMap iteration order is not guaranteed, so compare on a sorted view.
        String[] sorted = ss.clone();
        Arrays.sort(sorted);
        assertArrayEquals(
                new String[]{ "DS=64", "LT=256", "OB,OW=1024" },
                sorted);
    }

    @Test
    public void testRoundTripFromStringsPreservesAllEntries() {
        String[] expected = { "DS=64", "LT=256", "OB,OW=1024" };

        BasicBulkDataDescriptor descriptor = new BasicBulkDataDescriptor();
        descriptor.setLengthsThresholdsFromStrings(expected);

        String[] ss = descriptor.getLengthsThresholdsAsStrings();

        assertEquals(expected.length, ss.length);
        for (String s : ss) {
            assertNotNull("getLengthsThresholdsAsStrings() produced a null entry", s);
        }

        String[] sorted = ss.clone();
        Arrays.sort(sorted);
        assertArrayEquals(expected, sorted);
    }
}
