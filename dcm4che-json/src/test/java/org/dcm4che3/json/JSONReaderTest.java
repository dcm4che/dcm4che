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

package org.dcm4che3.json;

import jakarta.json.Json;
import jakarta.json.stream.JsonParser;
import org.dcm4che3.data.*;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.*;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Aug 2018
 */
public class JSONReaderTest {

    private static final String JSON = "{" +
            "\"00720064\":{\"vr\":\"IS\",\"Value\":[null,1,\"-2\"]}," +
            "\"00720072\":{\"vr\":\"DS\",\"Value\":[null,1.0,\"-2.0\"]}," +
            "\"00720074\":{\"vr\":\"FD\",\"Value\":[-1.7976931348623157E308,null,1.7976931348623157E308]}," +
            "\"00720076\":{\"vr\":\"FL\",\"Value\":[-1.7976931348623157E308,null,1.7976931348623157E308]}," +
            "\"00720078\":{\"vr\":\"UL\",\"Value\":[0,1,4294967294]}," +
            "\"0072007A\":{\"vr\":\"US\",\"Value\":[0,1,65534]}," +
            "\"0072007C\":{\"vr\":\"SL\",\"Value\":[0,1,-2]}," +
            "\"0072007E\":{\"vr\":\"SS\",\"Value\":[0,1,-2]}," +
            "\"00720082\":{\"vr\":\"SV\",\"Value\":[0,\"1\",\"-2\"]}," +
            "\"00720083\":{\"vr\":\"UV\",\"Value\":[0,\"1\",\"18446744073709551614\"]}" +
            "}";
    private static final String[] IS = { null, "1", "-2" };
    private static final String[] DS = { null, "1.0", "-2.0" };
    private static final int[] INTS = { 0, 1, -2 };
    private static final int[] UINTS = { 0, 1, -2 & 0xffff };
    private static final long[] LONGS = { 0L, 1L, -2L };

    private static final String JSON_EXT = "{" +
            "\"00100010\":{\"vr\":\"PN\",\"Value\":[{\"Alphabetic\":\"Smith^John\",\"Ideographic\":\"山田^太郎\",\"Phonetic\":\"やま^たろう\"}]}," +
            "\"00080018\":{\"vr\":\"UI\",\"Value\":[\"1.2.3\"]}," +
            "\"00091001\":{\"vr\":\"LO\",\"Value\":[\"Private Value\"]}," +
            "\"7FE00010\":{\"vr\":\"OW\",\"InlineBinary\":\"AQID\"}," +
            "\"00081110\":{\"vr\":\"SQ\",\"Value\":[{" +
                "\"00081150\":{\"vr\":\"UI\",\"Value\":[\"1.2.3.4\"]}," +
                "\"00081155\":{\"vr\":\"UI\",\"Value\":[\"1.2.3.5\"]}" +
            "}]}" +
            "}";

    @Test
    public void test() {
        StringReader reader = new StringReader(JSON);
        JsonParser parser = Json.createParser(reader);
        Attributes dataset = new JSONReader(parser).readDataset(new Attributes());
        assertArrayEquals(IS, dataset.getStrings(Tag.SelectorISValue));
        assertArrayEquals(DS, dataset.getStrings(Tag.SelectorDSValue));
        assertInfinityAndNaN(dataset.getDoubles(Tag.SelectorFDValue));
        assertInfinityAndNaN(dataset.getFloats(Tag.SelectorFLValue));
        assertArrayEquals(INTS, dataset.getInts(Tag.SelectorULValue));
        assertArrayEquals(UINTS, dataset.getInts(Tag.SelectorUSValue));
        assertArrayEquals(INTS, dataset.getInts(Tag.SelectorSLValue));
        assertArrayEquals(INTS, dataset.getInts(Tag.SelectorSLValue));
        assertArrayEquals(LONGS, dataset.getLongs(Tag.SelectorSVValue));
        assertArrayEquals(LONGS, dataset.getLongs(Tag.SelectorUVValue));
    }

    @Test
    public void testExtended() {
        StringReader reader = new StringReader(JSON_EXT);
        JsonParser parser = Json.createParser(reader);
        Attributes dataset = new JSONReader(parser).readDataset(new Attributes());

        assertEquals("Smith^John=山田^太郎=やま^たろう", dataset.getString(Tag.PatientName));
        assertEquals("1.2.3", dataset.getString(Tag.SOPInstanceUID));
        assertEquals("Private Value", dataset.getString(0x00091001));
        assertArrayEquals(new byte[]{1, 2, 3}, dataset.getSafeBytes(Tag.PixelData));

        Sequence seq = dataset.getSequence(0x00081110);
        assertNotNull("Sequence 00081110 should not be null", seq);
        assertEquals(1, seq.size());
        Attributes item = seq.get(0);
        assertEquals("1.2.3.4", item.getString(Tag.ReferencedSOPClassUID));
        assertEquals("1.2.3.5", item.getString(Tag.ReferencedSOPInstanceUID));
    }

    @Test
    public void testBulkData() {
        String json = "{\"00420011\":{\"vr\":\"OB\",\"BulkDataURI\":\"http://localhost/bulkdata\"}}";
        StringReader reader = new StringReader(json);
        JsonParser parser = Json.createParser(reader);
        Attributes dataset = new JSONReader(parser).readDataset(new Attributes());

        Object val = dataset.getValue(Tag.EncapsulatedDocument);
        assertTrue(val instanceof BulkData);
        assertEquals("http://localhost/bulkdata", ((BulkData) val).getURI());
    }

    @Test
    public void testSkipBulkData() {
        String json = "{\"00420011\":{\"vr\":\"OB\",\"BulkDataURI\":\"http://localhost/bulkdata\"}}";
        StringReader reader = new StringReader(json);
        JsonParser parser = Json.createParser(reader);
        JSONReader jsonReader = new JSONReader(parser);
        jsonReader.setSkipBulkDataURI(true);
        Attributes dataset = jsonReader.readDataset(new Attributes());

        assertNull(dataset.getValue(Tag.EncapsulatedDocument));
    }

    @Test
    public void testReadDatasets() {
        String json = "[{\"00080018\":{\"vr\":\"UI\",\"Value\":[\"1.2.3\"]}},{\"00080018\":{\"vr\":\"UI\",\"Value\":[\"1.2.4\"]}}]";
        StringReader reader = new StringReader(json);
        JsonParser parser = Json.createParser(reader);
        final java.util.List<Attributes> datasets = new java.util.ArrayList<>();
        new JSONReader(parser).readDatasets(new JSONReader.Callback() {
            @Override
            public void onDataset(Attributes fmi, Attributes dataset) {
                datasets.add(dataset);
            }
        });

        assertEquals(2, datasets.size());
        assertEquals("1.2.3", datasets.get(0).getString(Tag.SOPInstanceUID));
        assertEquals("1.2.4", datasets.get(1).getString(Tag.SOPInstanceUID));
    }

    private static void assertInfinityAndNaN(double[] doubles) {
        assertEquals(3, doubles.length);
        assertTrue(Double.NEGATIVE_INFINITY == doubles[0]);
        assertTrue(Double.isNaN(doubles[1]));
        assertTrue(Double.POSITIVE_INFINITY == doubles[2]);
    }

    private static void assertInfinityAndNaN(float[] floats) {
        assertEquals(3, floats.length);
        assertTrue(Float.NEGATIVE_INFINITY == floats[0]);
        assertTrue(Float.isNaN(floats[1]));
        assertTrue(Float.POSITIVE_INFINITY == floats[2]);
    }

}