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
 * Portions created by the Initial Developer are Copyright (C) 2026
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
package org.dcm4che3.opencv.waveform;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.Random;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.Transcoder;
import org.dcm4che3.imageio.codec.TransferSyntaxType;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.UIDUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.weasis.opencv.waveform.T261WaveformReader;
import org.weasis.opencv.waveform.WaveformReader;

/**
 * @author Nicolas Roduit
 * @since Oct 2026
 */
public class WaveformTranscoderTest {

    private static final int CHANNELS = 12;
    private static final int SAMPLES = 5000;
    private static final double FREQUENCY = 500.0;
    private static final int INTRA_PERIOD = 1024;

    private static final File DIR = new File("target/test-out/waveform");

    @BeforeClass
    public static void before() {
        DIR.mkdirs();
        File[] files = DIR.listFiles();
        if (files != null)
            for (File f : files)
                f.delete();
    }

    private static short[] ecgSamples() {
        Random random = new Random(42);
        short[] samples = new short[SAMPLES * CHANNELS];
        for (int s = 0; s < SAMPLES; s++)
            for (int c = 0; c < CHANNELS; c++) {
                double value = 1500 * Math.sin(2 * Math.PI * (c + 1) * s / 400.0)
                        + random.nextGaussian() * 40;
                samples[s * CHANNELS + c] =
                        (short) Math.max(-2048, Math.min(2047, Math.round(value)));
            }
        return samples;
    }

    private static byte[] littleEndian(short[] samples) {
        ByteBuffer buf = ByteBuffer.allocate(2 * samples.length).order(ByteOrder.LITTLE_ENDIAN);
        buf.asShortBuffer().put(samples);
        return buf.array();
    }

    private static Attributes ecgDataset(short[] samples) {
        Attributes dcm = new Attributes();
        dcm.setString(Tag.SOPClassUID, VR.UI, UID.TwelveLeadECGWaveformStorage);
        dcm.setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());
        dcm.setString(Tag.Modality, VR.CS, "ECG");
        dcm.setString(Tag.PatientName, VR.PN, "Waveform^Test");
        Sequence groups = dcm.newSequence(Tag.WaveformSequence, 1);
        Attributes group = new Attributes();
        group.setString(Tag.MultiplexGroupLabel, VR.SH, "12-lead");
        group.setInt(Tag.NumberOfWaveformChannels, VR.US, CHANNELS);
        group.setInt(Tag.NumberOfWaveformSamples, VR.UL, SAMPLES);
        group.setDouble(Tag.SamplingFrequency, VR.DS, FREQUENCY);
        group.setInt(Tag.WaveformBitsAllocated, VR.US, 16);
        group.setString(Tag.WaveformSampleInterpretation, VR.CS, "SS");
        Sequence channels = group.newSequence(Tag.ChannelDefinitionSequence, CHANNELS);
        for (int c = 0; c < CHANNELS; c++) {
            Attributes channel = new Attributes();
            channel.setInt(Tag.WaveformBitsStored, VR.US, 12);
            channel.setDouble(Tag.ChannelSensitivity, VR.DS, 1.25);
            channels.add(channel);
        }
        group.setBytes(Tag.WaveformData, VR.OW, littleEndian(samples));
        groups.add(group);
        return dcm;
    }

    private static File writeNativeFile(String name, short[] samples) throws IOException {
        File file = new File(DIR, name);
        Attributes dcm = ecgDataset(samples);
        DicomOutputStream dos = new DicomOutputStream(file);
        try {
            dos.writeDataset(dcm.createFileMetaInformation(UID.ExplicitVRLittleEndian), dcm);
        } finally {
            dos.close();
        }
        return file;
    }

    private static Attributes read(File file, IncludeBulkData mode) throws IOException {
        DicomInputStream dis = new DicomInputStream(file);
        try {
            dis.setIncludeBulkData(mode);
            return dis.readDataset();
        } finally {
            dis.close();
        }
    }

    private static String transferSyntax(File file) throws IOException {
        DicomInputStream dis = new DicomInputStream(file);
        try {
            dis.readFileMetaInformation();
            return dis.getTransferSyntax();
        } finally {
            dis.close();
        }
    }

    private static File transcode(File src, String name, String tsuid, double stepSize)
            throws IOException {
        File dest = new File(DIR, name);
        WaveformTranscoder transcoder = new WaveformTranscoder(src);
        try {
            transcoder.setDestinationTransferSyntax(tsuid);
            transcoder.setIntraPeriod(INTRA_PERIOD);
            transcoder.setStepSize(stepSize);
            transcoder.transcode(dest);
        } finally {
            transcoder.close();
        }
        return dest;
    }

    private static short[] readAll(WaveformReader reader) {
        Mat window = reader.read(0, (int) reader.length());
        try {
            assertEquals(CvType.CV_16S, window.type());
            short[] values = new short[(int) window.total()];
            window.get(0, 0, values);
            return values;
        } finally {
            window.release();
        }
    }

    @Test
    public void testLosslessItemsAndOffsetTable() throws Exception {
        short[] samples = ecgSamples();
        File src = writeNativeFile("ecg-native.dcm", samples);
        File dst = transcode(src, "ecg-t261.dcm", T261TransferSyntax.lossless(), 0);

        assertEquals(T261TransferSyntax.lossless(), transferSyntax(dst));
        Attributes dcm = read(dst, IncludeBulkData.URI);
        Attributes group = dcm.getSequence(Tag.WaveformSequence).get(0);
        assertEquals("Waveform^Test", dcm.getString(Tag.PatientName));
        Fragments fragments = (Fragments) group.getValue(Tag.WaveformData);
        int expectedItems = (SAMPLES + INTRA_PERIOD - 1) / INTRA_PERIOD;
        assertEquals(expectedItems + 1, fragments.size());

        BulkData table = (BulkData) fragments.get(0);
        assertEquals(4 * expectedItems, table.length());
        ByteBuffer offsets = ByteBuffer.wrap(Files.readAllBytes(dst.toPath()),
                (int) table.offset(), table.length()).order(ByteOrder.LITTLE_ENDIAN);
        long firstItem = ((BulkData) fragments.get(1)).offset() - 8;
        for (int i = 0; i < expectedItems; i++) {
            BulkData item = (BulkData) fragments.get(i + 1);
            assertEquals("Item " + i, item.offset() - 8 - firstItem, offsets.getInt() & 0xFFFFFFFFL);
        }

        WaveformReader reader = WaveformReaders.open(group, T261TransferSyntax.lossless());
        try {
            assertTrue(reader instanceof T261WaveformReader);
            assertEquals(expectedItems, ((T261WaveformReader) reader).frameSequences());
            assertEquals(FREQUENCY, reader.samplingFrequency(), 0.0);
            assertArrayEquals(samples, readAll(reader));
        } finally {
            reader.close();
        }
        assertTrue("T.261 lossless should compress the ECG", dst.length() < src.length() * 0.7);
    }

    @Test
    public void testBackToNativeIsByteIdentical() throws Exception {
        short[] samples = ecgSamples();
        File src = writeNativeFile("ecg-native2.dcm", samples);
        File t261 = transcode(src, "ecg-t261-2.dcm", T261TransferSyntax.lossless(), 0);
        File back = transcode(t261, "ecg-back.dcm", UID.ExplicitVRLittleEndian, 0);

        assertEquals(UID.ExplicitVRLittleEndian, transferSyntax(back));
        Attributes group = read(back, IncludeBulkData.YES).getSequence(Tag.WaveformSequence).get(0);
        VR.Holder vr = new VR.Holder();
        byte[] data = (byte[]) group.getValue(Tag.WaveformData, vr);
        assertEquals(VR.OW, vr.vr);
        assertArrayEquals(littleEndian(samples), data);
        assertEquals(src.length(), back.length());
    }

    @Test
    public void testLossyFromLossless() throws Exception {
        short[] samples = ecgSamples();
        File src = writeNativeFile("ecg-native3.dcm", samples);
        File lossless = transcode(src, "ecg-lossless.dcm", T261TransferSyntax.lossless(), 0);
        File lossy = transcode(lossless, "ecg-lossy.dcm", T261TransferSyntax.lossy(), 8.0);

        assertEquals(T261TransferSyntax.lossy(), transferSyntax(lossy));
        assertTrue(lossy.length() < lossless.length());
        Attributes group = read(lossy, IncludeBulkData.YES).getSequence(Tag.WaveformSequence).get(0);
        WaveformReader reader = WaveformReaders.open(group, T261TransferSyntax.lossy());
        try {
            short[] decoded = readAll(reader);
            int maxError = 0;
            for (int i = 0; i < samples.length; i++)
                maxError = Math.max(maxError, Math.abs(decoded[i] - samples[i]));
            assertTrue("lossy should not be exact", maxError > 0);
            assertTrue("max error " + maxError, maxError <= 16);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testNativeToNativeCopiesTheObject() throws Exception {
        short[] samples = ecgSamples();
        File src = writeNativeFile("ecg-native4.dcm", samples);
        File copy = transcode(src, "ecg-ivrle.dcm", UID.ImplicitVRLittleEndian, 0);
        assertEquals(UID.ImplicitVRLittleEndian, transferSyntax(copy));
        Attributes group = read(copy, IncludeBulkData.YES).getSequence(Tag.WaveformSequence).get(0);
        assertArrayEquals(littleEndian(samples), (byte[]) group.getValue(Tag.WaveformData));
    }

    @Test
    public void testPixelDataTranscoderRejectsEncapsulatedWaveform() throws IOException {
        File src = writeNativeFile("ecg-native-4.dcm", ecgSamples());
        File t261 = transcode(src, "ecg-t261-4.dcm", T261TransferSyntax.lossless(), 0);
        Transcoder transcoder = new Transcoder(t261);
        try {
            assertEquals(TransferSyntaxType.T261, transcoder.getSourceTransferSyntaxType());
            transcoder.setDestinationTransferSyntax(UID.ExplicitVRLittleEndian);
            fail("Transcoder accepted encapsulated Waveform Data");
        } catch (UnsupportedOperationException e) {
            // expected: the pixel data Transcoder cannot decode a T.261 Waveform Data Stream
        } finally {
            transcoder.close();
        }
    }
}
