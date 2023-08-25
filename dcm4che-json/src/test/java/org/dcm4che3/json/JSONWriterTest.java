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

package org.dcm4che3.json;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import jakarta.json.Json;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonGenerator;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class JSONWriterTest {

    private static byte[] BYTE01 = { 0, 1 };
    private static final int[] INTS = { 0, 1, -2 };
    private static final int[] UINTS = { 0, 1, -2 & 0xffff };
    private static final long[] LONGS = { 0L, 1L, -2L, Long.MAX_VALUE };
    private String RESULT_STRING = "{"
            + "\"00080005\":{\"vr\":\"CS\",\"Value\":[null,\"ISO 2022 IR 87\"]},"
            + "\"00080008\":{\"vr\":\"CS\",\"Value\":[\"DERIVED\",\"PRIMARY\"]},"
            + "\"00082112\":{\"vr\":\"SQ\",\"Value\":["
                + "{"
                    + "\"00081150\":{\"vr\":\"UI\",\"Value\":[\"1.2.840.10008.5.1.4.1.1.2\"]},"
                    + "\"00081155\":{\"vr\":\"UI\",\"Value\":[\"1.2.3.4\"]}"
                + "}"
                + "]},"
            + "\"00090010\":{\"vr\":\"LO\",\"Value\":[\"PRIVATE\"]},"
            + "\"00091002\":{\"vr\":\"OB\",\"InlineBinary\":\"AAE=\"},"
            + "\"00100010\":{\"vr\":\"PN\",\"Value\":["
                + "{"
                    + "\"Alphabetic\":\"af^ag\","
                    + "\"Ideographic\":\"if^ig\","
                    + "\"Phonetic\":\"pf^pg\""
                + "}"
            + "]},"
            + "\"00181063\":{\"vr\":\"DS\",\"Value\":[\"33.0\"]},"
            + "\"00280002\":{\"vr\":\"US\",\"Value\":[1]},"
            + "\"00280008\":{\"vr\":\"IS\",\"Value\":[\"001\"]},"
            + "\"00280009\":{\"vr\":\"AT\",\"Value\":[\"00181063\"]},"
            + "\"00720078\":{\"vr\":\"UL\",\"Value\":[0,1,4294967294]},"
            + "\"0072007A\":{\"vr\":\"US\",\"Value\":[0,1,65534]},"
            + "\"0072007C\":{\"vr\":\"SL\",\"Value\":[0,1,-2]},"
            + "\"0072007E\":{\"vr\":\"SS\",\"Value\":[0,1,-2]},"
            + "\"00720082\":{\"vr\":\"SV\",\"Value\":[\"0\",\"1\",\"-2\",\"9223372036854775807\"]},"
            + "\"00720083\":{\"vr\":\"UV\",\"Value\":[\"0\",\"1\",\"18446744073709551614\",\"9223372036854775807\"]},"
            + "\"60003000\":{\"vr\":\"OW\",\"BulkDataURI\":\"file:/OverlayData\"},"
            + "\"7FE00010\":{\"vr\":\"OB\",\"DataFragment\":["
                + "null,"
                + "{\"BulkDataURI\":\"file:/PixelData\"}"
            + "]}}";

    private String RESULT_NUMBER = "{"
            + "\"00080005\":{\"vr\":\"CS\",\"Value\":[null,\"ISO 2022 IR 87\"]},"
            + "\"00080008\":{\"vr\":\"CS\",\"Value\":[\"DERIVED\",\"PRIMARY\"]},"
            + "\"00082112\":{\"vr\":\"SQ\",\"Value\":["
                + "{"
                    + "\"00081150\":{\"vr\":\"UI\",\"Value\":[\"1.2.840.10008.5.1.4.1.1.2\"]},"
                    + "\"00081155\":{\"vr\":\"UI\",\"Value\":[\"1.2.3.4\"]}"
                + "}"
                + "]},"
            + "\"00090010\":{\"vr\":\"LO\",\"Value\":[\"PRIVATE\"]},"
            + "\"00091002\":{\"vr\":\"OB\",\"InlineBinary\":\"AAE=\"},"
            + "\"00100010\":{\"vr\":\"PN\",\"Value\":["
                + "{"
                    + "\"Alphabetic\":\"af^ag\","
                    + "\"Ideographic\":\"if^ig\","
                    + "\"Phonetic\":\"pf^pg\""
                + "}"
            + "]},"
            + "\"00181063\":{\"vr\":\"DS\",\"Value\":[33.0]},"
            + "\"00280002\":{\"vr\":\"US\",\"Value\":[1]},"
            + "\"00280008\":{\"vr\":\"IS\",\"Value\":[1]},"
            + "\"00280009\":{\"vr\":\"AT\",\"Value\":[\"00181063\"]},"
            + "\"00720078\":{\"vr\":\"UL\",\"Value\":[0,1,4294967294]},"
            + "\"0072007A\":{\"vr\":\"US\",\"Value\":[0,1,65534]},"
            + "\"0072007C\":{\"vr\":\"SL\",\"Value\":[0,1,-2]},"
            + "\"0072007E\":{\"vr\":\"SS\",\"Value\":[0,1,-2]},"
            + "\"00720082\":{\"vr\":\"SV\",\"Value\":[0,1,-2,\"9223372036854775807\"]},"
            + "\"00720083\":{\"vr\":\"UV\",\"Value\":[0,1,\"18446744073709551614\",\"9223372036854775807\"]},"
            + "\"60003000\":{\"vr\":\"OW\",\"BulkDataURI\":\"file:/OverlayData\"},"
            + "\"7FE00010\":{\"vr\":\"OB\",\"DataFragment\":["
                + "null,"
                + "{\"BulkDataURI\":\"file:/PixelData\"}"
            + "]}}";

    private String INFINITY_AND_NAN = "{" +
            "\"00720074\":{\"vr\":\"FD\",\"Value\":[-1.7976931348623157E308,null,1.7976931348623157E308]}," +
            "\"00720076\":{\"vr\":\"FL\",\"Value\":[-1.7976931348623157E308,null,1.7976931348623157E308]}}";

    @Test
    public void testStringEncoding() {
        test(RESULT_STRING, JsonValue.ValueType.STRING);
    }

    @Test
    public void testNumberEncoding() {
        test(RESULT_NUMBER, JsonValue.ValueType.NUMBER);
    }
    private void test(String expected, JsonValue.ValueType jsonType) {
        Attributes dataset = new Attributes();
        dataset.setString(Tag.SpecificCharacterSet, VR.CS, null, "ISO 2022 IR 87");
        dataset.setString(Tag.ImageType, VR.CS, "DERIVED", "PRIMARY");
        Attributes item = new Attributes(2);
        dataset.newSequence(Tag.SourceImageSequence, 1).add(item);
        item.setString(Tag.ReferencedSOPClassUID, VR.UI, UID.CTImageStorage);
        item.setString(Tag.ReferencedSOPInstanceUID, VR.UI, "1.2.3.4");
        dataset.setString(Tag.PatientName, VR.PN, "af^ag=if^ig=pf^pg");
        dataset.setBytes("PRIVATE", 0x00090002, VR.OB, BYTE01);
        dataset.setString(Tag.FrameTime, VR.DS, "33.0");
        dataset.setInt(Tag.SamplesPerPixel, VR.US, 1);
        dataset.setString(Tag.NumberOfFrames, VR.IS, "001");
        dataset.setInt(Tag.FrameIncrementPointer, VR.AT, Tag.FrameTime);
        dataset.setInt(Tag.SelectorULValue, VR.UL, INTS);
        dataset.setInt(Tag.SelectorUSValue, VR.US, UINTS);
        dataset.setInt(Tag.SelectorSLValue, VR.SL, INTS);
        dataset.setInt(Tag.SelectorSSValue, VR.SS, INTS);
        dataset.setLong(Tag.SelectorSVValue, VR.SV, LONGS);
        dataset.setLong(Tag.SelectorUVValue, VR.UV, LONGS);
        dataset.setValue(Tag.OverlayData, VR.OW, new BulkData(null, "file:/OverlayData", false));
        Fragments frags = dataset.newFragments(Tag.PixelData, VR.OB, 2);
        frags.add(null);
        frags.add(new BulkData(null, "file:/PixelData", false));
        StringWriter writer = new StringWriter();
        JsonGenerator gen = Json.createGenerator(writer);
        JSONWriter jsonWriter = new JSONWriter(gen);
        jsonWriter.setJsonType(VR.DS, jsonType);
        jsonWriter.setJsonType(VR.IS, jsonType);
        jsonWriter.setJsonType(VR.SV, jsonType);
        jsonWriter.setJsonType(VR.UV, jsonType);
        jsonWriter.write(dataset);
        gen.flush();
        assertEquals(expected, writer.toString());
    }
    @Test
    public void testInfinityAndNaN() {
        Attributes dataset = new Attributes();
        dataset.setDouble(Tag.SelectorFDValue, VR.FD, Double.NEGATIVE_INFINITY, Double.NaN, Double.POSITIVE_INFINITY);
        dataset.setFloat(Tag.SelectorFLValue, VR.FL, Float.NEGATIVE_INFINITY, Float.NaN, Float.POSITIVE_INFINITY);
        StringWriter writer = new StringWriter();
        JsonGenerator gen = Json.createGenerator(writer);
        new JSONWriter(gen).write(dataset);
        gen.flush();
        assertEquals(INFINITY_AND_NAN, writer.toString());
    }

}
