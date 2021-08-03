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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Tests for {@link SAXReader} and {@link SAXWriter}.
 * 
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public class XMLTest {

    private static final String REFERENCE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><NativeDicomModel xml:space=\"preserve\"><DicomAttribute keyword=\"SpecificCharacterSet\" tag=\"00080005\" vr=\"CS\"><Value number=\"1\">ISO 2022 IR 87</Value></DicomAttribute><DicomAttribute keyword=\"ImageType\" tag=\"00080008\" vr=\"CS\"><Value number=\"1\">DERIVED</Value><Value number=\"2\"/><Value number=\"3\">PRIMARY</Value><Value number=\"4\"/><Value number=\"5\">TEST</Value></DicomAttribute><DicomAttribute keyword=\"AccessionNumber\" tag=\"00080050\" vr=\"SH\"/><DicomAttribute keyword=\"SourceImageSequence\" tag=\"00082112\" vr=\"SQ\"><Item number=\"1\"><DicomAttribute keyword=\"ReferencedSOPClassUID\" tag=\"00081150\" vr=\"UI\"><Value number=\"1\">1.2.840.10008.5.1.4.1.1.2</Value></DicomAttribute><DicomAttribute keyword=\"ReferencedSOPInstanceUID\" tag=\"00081155\" vr=\"UI\"><Value number=\"1\">1.2.3.4</Value></DicomAttribute></Item></DicomAttribute><DicomAttribute tag=\"00090002\" privateCreator=\"PRIVATE\" vr=\"OB\"><InlineBinary>AAE=</InlineBinary></DicomAttribute><DicomAttribute keyword=\"PatientName\" tag=\"00100010\" vr=\"PN\"><PersonName number=\"1\"><Alphabetic><FamilyName>af</FamilyName><GivenName>ag</GivenName></Alphabetic><Ideographic><FamilyName>if</FamilyName><GivenName>ig</GivenName></Ideographic><Phonetic><FamilyName>pf</FamilyName><GivenName>pg</GivenName></Phonetic></PersonName></DicomAttribute><DicomAttribute keyword=\"FrameTime\" tag=\"00181063\" vr=\"DS\"><Value number=\"1\">33</Value></DicomAttribute><DicomAttribute keyword=\"SamplesPerPixel\" tag=\"00280002\" vr=\"US\"><Value number=\"1\">1</Value></DicomAttribute><DicomAttribute keyword=\"NumberOfFrames\" tag=\"00280008\" vr=\"IS\"><Value number=\"1\">1</Value></DicomAttribute><DicomAttribute keyword=\"FrameIncrementPointer\" tag=\"00280009\" vr=\"AT\"><Value number=\"1\">00181063</Value></DicomAttribute><DicomAttribute keyword=\"OverlayData\" tag=\"60003000\" vr=\"OW\"><BulkData uuid=\"someuuid\"/></DicomAttribute><DicomAttribute keyword=\"PixelData\" tag=\"7FE00010\" vr=\"OB\"><DataFragment number=\"2\"><BulkData uri=\"file:/PixelData?offset=1234&amp;length=5678\"/></DataFragment></DicomAttribute></NativeDicomModel>";
    
    @Test
    public void testDcm2Xml() throws Exception {
        Attributes dataset = createTestDataset();
        
        String xml = dcm2xml(dataset);

        Assert.assertEquals(REFERENCE_XML, xml);
    }

    private String dcm2xml(Attributes dataset) throws TransformerFactoryConfigurationError, TransformerConfigurationException, SAXException, UnsupportedEncodingException {
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();

        ByteArrayOutputStream xmlOutput = new ByteArrayOutputStream();

        TransformerHandler handler = tf.newTransformerHandler();
        //handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        //handler.getTransformer().setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        handler.setResult(new StreamResult(xmlOutput));

        SAXWriter writer = new SAXWriter(handler);
        writer.write(dataset);

        return xmlOutput.toString(StandardCharsets.UTF_8.name());
    }

    private Attributes createTestDataset() {
        Attributes dataset = new Attributes();
        dataset.setString(Tag.SpecificCharacterSet, VR.CS, "ISO 2022 IR 87");
        dataset.setString(Tag.ImageType, VR.CS, "DERIVED", null, "PRIMARY", "", "TEST");
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
        dataset.setValue(Tag.OverlayData, VR.OW, new BulkData("someuuid", null, false));
        Fragments frags = dataset.newFragments(Tag.PixelData, VR.OB, 2);
        frags.add(null);
        frags.add(new BulkData(null, "file:/PixelData?offset=1234&length=5678", false));

        return dataset;
    }

    @Test
    public void testXml2Dcm() throws Exception {
        Attributes dataset = xml2dcm(REFERENCE_XML);

        Attributes referenceDataset = createTestDataset();
        // the null value will be an empty string after converting to XML, therefore we have to adapt the reference
        referenceDataset.setString(Tag.ImageType, VR.CS, "DERIVED", "", "PRIMARY", "", "TEST");

        Assert.assertEquals(referenceDataset, dataset);
    }

    private Attributes xml2dcm(String xml) throws ParserConfigurationException, SAXException, IOException {
        Attributes dataset = new Attributes();
        ContentHandlerAdapter ch = new ContentHandlerAdapter(dataset);

        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser p = f.newSAXParser();
        p.parse(new InputSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))), ch);

        return dataset;
    }

}
