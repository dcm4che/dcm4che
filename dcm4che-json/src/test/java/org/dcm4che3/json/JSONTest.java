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

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.junit.Test;

/**
 * Tests for {@link JSONReader} and {@link JSONWriter}.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public class JSONTest {

    private final String RESULT = "{"
            + "\"00080005\":{\"vr\":\"CS\",\"Value\":[\"ISO 2022 IR 87\"]},"
            + "\"00080008\":{\"vr\":\"CS\",\"Value\":[\"DERIVED\",null,null,\"PRIMARY\"]},"
            + "\"00080050\":{\"vr\":\"SH\"},"
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
            + "\"60003000\":{\"vr\":\"OW\",\"BulkDataURI\":\"file:/OverlayData\"},"
            + "\"7FE00010\":{\"vr\":\"OB\",\"BulkDataURI\":\"file:/PixelData?offsets=0,1234&lengths=0,5678\"}"
            + "}";

    @Test
    public void testJSONWriting() {
        Attributes dataset = createTestDataset();

        StringWriter writer = new StringWriter();
        JsonGenerator gen = Json.createGenerator(writer);
        new JSONWriter(gen).write(dataset);
        gen.flush();
        String json = writer.toString();
        assertEquals(RESULT, json);
    }

    private Attributes createTestDataset() {
        Attributes dataset = new Attributes();
        dataset.setString(Tag.SpecificCharacterSet, VR.CS, "ISO 2022 IR 87");
        dataset.setString(Tag.ImageType, VR.CS, "DERIVED", null, "", "PRIMARY");
        dataset.setNull(Tag.AccessionNumber, VR.SH);
        Attributes item = new Attributes(2);
        dataset.newSequence(Tag.SourceImageSequence, 1).add(item);
        item.setString(Tag.ReferencedSOPClassUID, VR.UI, UID.CTImageStorage);
        item.setString(Tag.ReferencedSOPInstanceUID, VR.UI, "1.2.3.4");
        dataset.setString(Tag.PatientName, VR.PN, "af^ag=if^ig=pf^pg");
        dataset.setBytes("PRIVATE", 0x00090002, VR.OB, new byte[] { 0, 1 });
        dataset.setDouble(Tag.FrameTime, VR.DS, 33.0);
        dataset.setInt(Tag.SamplesPerPixel, VR.US, 1);
        dataset.setInt(Tag.NumberOfFrames, VR.IS, 1);
        dataset.setInt(Tag.FrameIncrementPointer, VR.AT, Tag.FrameTime);
        dataset.setValue(Tag.OverlayData, VR.OW, new BulkData(null, "file:/OverlayData", false));
        Fragments frags = dataset.newFragments(Tag.PixelData, VR.OB, 2);
        frags.add(null);
        frags.add(new BulkData(null, "file:/PixelData?offset=1234&length=5678", false));
        return dataset;
    }

    @Test
    public void testJSONReading() {

        Attributes dataset = new Attributes();
        
        JSONReader reader = new JSONReader(Json.createParser(new ByteArrayInputStream(RESULT.getBytes(StandardCharsets.UTF_8))));
        reader.readDataset(dataset);
        
        Attributes referenceDataset = createTestDataset();
        // currently empty strings become null after reading them from JSON, so we have to adapt the reference a little:
        referenceDataset.setString(Tag.ImageType, VR.CS, "DERIVED", null, null, "PRIMARY");

        assertEquals(referenceDataset, dataset);
    }

}
