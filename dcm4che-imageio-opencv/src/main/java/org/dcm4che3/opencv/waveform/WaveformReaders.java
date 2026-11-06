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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.StreamUtils;
import org.weasis.opencv.natives.NativeLibrary;
import org.weasis.opencv.waveform.NativeWaveformReader;
import org.weasis.opencv.waveform.T261WaveformReader;
import org.weasis.opencv.waveform.WaveformReader;

/**
 * Opens a {@link WaveformReader} on the Waveform Data (5400,1010) of one Waveform Sequence Item,
 * as parsed by {@code DicomInputStream}.
 * <p>
 * Both representations are supported: {@link BulkData} / {@link Fragments} of {@link BulkData}
 * when the data set was read with {@code IncludeBulkData.URI} (the samples stay in the file: native
 * data is memory-mapped per window, a T.261 stream is decoded one frame sequence at a time from the
 * Items), and {@code byte[]} values when the data set was read in memory.
 *
 * @author Nicolas Roduit
 * @since Oct 2026
 */
public final class WaveformReaders {

    static {
        // Load the native OpenCV library
        NativeLibrary.loadLibraryFromLibraryName();
    }

    private WaveformReaders() {
    }

    /**
     * Opens a reader on the Waveform Data of one multiplex group.
     *
     * @param item a Waveform Sequence Item
     * @param tsuid the transfer syntax of the data set the Item belongs to
     * @return a reader on the samples; close it when done
     * @throws IOException when the Waveform Data is missing or cannot be read
     */
    public static WaveformReader open(Attributes item, String tsuid) throws IOException {
        return open(item, WaveformDescriptor.of(item), tsuid);
    }

    public static WaveformReader open(Attributes item, WaveformDescriptor desc, String tsuid)
            throws IOException {
        VR.Holder vr = new VR.Holder();
        Object value = item.getValue(Tag.WaveformData, vr);
        if (value == null)
            throw new IOException("No Waveform Data in the Waveform Sequence Item");
        if (T261TransferSyntax.isT261(tsuid))
            return openT261(value, desc);
        return openNative(value, desc, item.bigEndian());
    }

    private static WaveformReader openT261(Object value, WaveformDescriptor desc)
            throws IOException {
        if (value instanceof Fragments) {
            Fragments fragments = (Fragments) value;
            List<BulkData> items = bulkItems(fragments);
            if (items != null) {
                long[] positions = new long[items.size()];
                long[] lengths = new long[items.size()];
                for (int i = 0; i < items.size(); i++) {
                    positions[i] = items.get(i).offset();
                    lengths[i] = items.get(i).longLength();
                }
                return T261WaveformReader.ofFile(pathOf(items.get(0)), positions, lengths,
                        desc.getSamplingFrequency());
            }
            return T261WaveformReader.ofMemory(concatenate(fragments),
                    desc.getSamplingFrequency());
        }
        if (value instanceof BulkData)
            return T261WaveformReader.ofMemory(readAll((BulkData) value),
                    desc.getSamplingFrequency());
        if (value instanceof byte[])
            return T261WaveformReader.ofMemory((byte[]) value, desc.getSamplingFrequency());
        throw new IOException("Unsupported Waveform Data value: " + value.getClass().getName());
    }

    private static WaveformReader openNative(Object value, WaveformDescriptor desc,
            boolean datasetBigEndian) throws IOException {
        if (value instanceof BulkData) {
            BulkData bulk = (BulkData) value;
            return NativeWaveformReader.ofFile(pathOf(bulk), bulk.offset(),
                    Math.min(bulk.longLength(), desc.getNativeLength()), desc.getChannels(),
                    desc.getInterpretation(), bulk.bigEndian(), desc.getSamplingFrequency());
        }
        if (value instanceof byte[]) {
            byte[] bytes = (byte[]) value;
            return NativeWaveformReader.ofMemory(
                    ByteBuffer.wrap(bytes, 0, (int) Math.min(bytes.length, desc.getNativeLength())),
                    desc.getChannels(), desc.getInterpretation(), datasetBigEndian,
                    desc.getSamplingFrequency());
        }
        if (value instanceof Fragments)
            throw new IOException("Encapsulated Waveform Data in a native transfer syntax");
        throw new IOException("Unsupported Waveform Data value: " + value.getClass().getName());
    }

    /** The Items after the Basic Offset Table, when they all reference a file; null otherwise. */
    private static List<BulkData> bulkItems(Fragments fragments) {
        List<BulkData> items = new ArrayList<BulkData>(fragments.size());
        for (int i = 1; i < fragments.size(); i++) {
            Object fragment = fragments.get(i);
            if (fragment instanceof BulkData)
                items.add((BulkData) fragment);
            else
                return null;
        }
        return items.isEmpty() ? null : items;
    }

    private static byte[] concatenate(Fragments fragments) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 1; i < fragments.size(); i++) {
            Object fragment = fragments.get(i);
            if (fragment instanceof byte[])
                out.write((byte[]) fragment);
            else if (fragment instanceof BulkData)
                out.write(readAll((BulkData) fragment));
        }
        return out.toByteArray();
    }

    private static byte[] readAll(BulkData bulk) throws IOException {
        byte[] bytes = new byte[bulk.length()];
        InputStream in = bulk.openStream();
        try {
            StreamUtils.readFully(in, bytes, 0, bytes.length);
        } finally {
            in.close();
        }
        return bytes;
    }

    static Path pathOf(BulkData bulk) throws IOException {
        try {
            return Paths.get(URI.create(bulk.uriWithoutOffsetAndLength()));
        } catch (IllegalArgumentException e) {
            throw new IOException("Bulk data is not a local file: " + bulk.getURI(), e);
        }
    }
}
