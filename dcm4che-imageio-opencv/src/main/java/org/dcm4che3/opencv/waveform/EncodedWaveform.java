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

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.SafeClose;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.weasis.opencv.natives.NativeLibrary;
import org.weasis.opencv.waveform.T261Parameters;
import org.weasis.opencv.waveform.T261WaveformWriter;
import org.weasis.opencv.waveform.WaveformReader;

/**
 * The Waveform Data (5400,1010) of one multiplex group produced by a transcoding, spooled to a
 * temporary file so that the data set can be written with {@code DicomOutputStream} without
 * holding the samples in memory. Close it after the data set has been written: the temporary file
 * is deleted.
 * <p>
 * For a T.261 transfer syntax the value is a {@link Fragments}: one Item per frame sequence of the
 * stream, preceded by a filled Basic Offset Table (the byte offset of each Item from the first Item
 * after the table, as for pixel data), so that a reader can locate any frame sequence without
 * decoding. For a native transfer syntax the value is a {@link BulkData} of little-endian samples,
 * sample-interleaved, at the Waveform Bits Allocated of the Item.
 *
 * @author Nicolas Roduit
 * @since Oct 2026
 */
public final class EncodedWaveform implements Closeable {

    static {
        // Load the native OpenCV library
        NativeLibrary.loadLibraryFromLibraryName();
    }

    private final File file;
    private final VR vr;
    private final Object value;
    private final int items;
    private final long samples;

    private EncodedWaveform(File file, VR vr, Object value, int items, long samples) {
        this.file = file;
        this.vr = vr;
        this.value = value;
        this.items = items;
        this.samples = samples;
    }

    /**
     * Encodes the samples of a reader into a T.261 stream, one Item per frame sequence. Memory is
     * bounded by one source window plus one frame sequence whatever the length of the recording.
     *
     * @param tmpDir the directory of the spool file, null for the default temporary directory
     */
    public static EncodedWaveform encodeT261(WaveformReader source, T261Parameters parameters,
            File tmpDir) throws IOException {
        final File file = File.createTempFile("t261-", ".bws", tmpDir);
        try {
            final List<Long> lengths = new ArrayList<Long>();
            final OutputStream out = new BufferedOutputStream(new FileOutputStream(file), 1 << 16);
            try {
                T261WaveformWriter.encode(source, parameters, new Consumer<byte[]>() {
                    @Override
                    public void accept(byte[] item) {
                        try {
                            out.write(item);
                            lengths.add((long) item.length);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
                });
            } catch (UncheckedIOException e) {
                throw e.getCause();
            } finally {
                out.close();
            }
            return new EncodedWaveform(file, VR.OB, fragmentsOf(file, lengths), lengths.size(),
                    source.length());
        } catch (IOException e) {
            file.delete();
            throw e;
        } catch (RuntimeException e) {
            file.delete();
            throw e;
        }
    }

    /**
     * Decodes the samples of a reader to native Waveform Data at the Bits Allocated of the
     * descriptor. Only signed interpretations are produced, as T.261 streams carry signed samples.
     */
    public static EncodedWaveform decodeNative(WaveformReader source, WaveformDescriptor desc,
            File tmpDir) throws IOException {
        if (!desc.getInterpretation().isSigned())
            throw new IOException("T.261 carries signed samples; Waveform Sample Interpretation "
                    + desc.getInterpretation() + " is not supported");
        if (source.channels() != desc.getChannels())
            throw new IOException("The stream has " + source.channels()
                    + " channels, the Item declares " + desc.getChannels());
        File file = File.createTempFile("waveform-", ".raw", tmpDir);
        try {
            long written = 0;
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file), 1 << 16);
            try {
                long length = source.length();
                int window = Math.max(1, source.preferredWindow());
                for (long start = 0; start < length; start += window) {
                    int count = (int) Math.min(window, length - start);
                    Mat chunk = source.read(start, count);
                    try {
                        byte[] bytes = toLittleEndian(chunk, desc.getInterpretation().cvType());
                        out.write(bytes);
                        written += bytes.length;
                    } finally {
                        chunk.release();
                    }
                }
            } finally {
                out.close();
            }
            if (written % 2 != 0)
                throw new IOException("Odd native waveform length " + written);
            VR vr = desc.getInterpretation().bytesPerSample() == 1 ? VR.OB : VR.OW;
            BulkData bulk = new BulkData(file.toURI().toString(), 0, written, false);
            return new EncodedWaveform(file, vr, bulk, 1, source.length());
        } catch (IOException e) {
            file.delete();
            throw e;
        } catch (RuntimeException e) {
            file.delete();
            throw e;
        }
    }

    private static Fragments fragmentsOf(File file, List<Long> lengths) {
        String uri = file.toURI().toString();
        Fragments fragments = new Fragments(VR.OB, false, lengths.size() + 1);
        ByteBuffer table = ByteBuffer.allocate(4 * lengths.size()).order(ByteOrder.LITTLE_ENDIAN);
        long fileOffset = 0;
        long itemOffset = 0;
        for (long length : lengths) {
            if (itemOffset > 0xFFFFFFFFL)
                throw new IllegalStateException(
                        "Waveform stream beyond the 32-bit Basic Offset Table");
            table.putInt((int) itemOffset);
            fragments.add(new BulkData(uri, fileOffset, length, false));
            fileOffset += length;
            itemOffset += 8 + ((length + 1) & ~1L);
        }
        fragments.add(0, table.array());
        return fragments;
    }

    private static byte[] toLittleEndian(Mat chunk, int cvType) {
        Mat converted = chunk;
        if (chunk.type() != cvType) {
            converted = new Mat();
            chunk.convertTo(converted, cvType);
        }
        try {
            int count = (int) converted.total();
            switch (CvType.depth(cvType)) {
            case CvType.CV_8S: {
                byte[] bytes = new byte[count];
                converted.get(0, 0, bytes);
                return bytes;
            }
            case CvType.CV_16S: {
                short[] values = new short[count];
                converted.get(0, 0, values);
                ByteBuffer buf = ByteBuffer.allocate(2 * count).order(ByteOrder.LITTLE_ENDIAN);
                buf.asShortBuffer().put(values);
                return buf.array();
            }
            case CvType.CV_32S: {
                int[] values = new int[count];
                converted.get(0, 0, values);
                ByteBuffer buf = ByteBuffer.allocate(4 * count).order(ByteOrder.LITTLE_ENDIAN);
                buf.asIntBuffer().put(values);
                return buf.array();
            }
            default:
                throw new IllegalArgumentException("Unsupported sample type " + cvType);
            }
        } finally {
            if (converted != chunk)
                converted.release();
        }
    }

    /** The {@link Fragments} (T.261) or {@link BulkData} (native) to set as Waveform Data. */
    public Object getValue() {
        return value;
    }

    /** OB for T.261 Items and 8-bit samples, OW otherwise. */
    public VR getVR() {
        return vr;
    }

    /** The number of Items (T.261: one per frame sequence), 1 for native data. */
    public int getItems() {
        return items;
    }

    /** The number of samples per channel. */
    public long getSamples() {
        return samples;
    }

    /** The temporary file holding the data; deleted by {@link #close()}. */
    public File getFile() {
        return file;
    }

    /** Sets this data as the Waveform Data (5400,1010) of a Waveform Sequence Item. */
    public void setTo(Attributes item) {
        item.setValue(Tag.WaveformData, vr, value);
    }

    @Override
    public void close() {
        file.delete();
    }

    static void closeAll(List<EncodedWaveform> encoded) {
        for (EncodedWaveform value : encoded)
            SafeClose.close(value);
    }
}
