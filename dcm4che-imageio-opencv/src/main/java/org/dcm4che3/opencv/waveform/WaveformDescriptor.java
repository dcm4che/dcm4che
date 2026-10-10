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

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.weasis.opencv.waveform.WaveformSampleInterpretation;

/**
 * The attributes of one Waveform Sequence (5400,0100) Item (one multiplex group) that drive the
 * decoding and the encoding of its Waveform Data (5400,1010).
 *
 * @author Nicolas Roduit
 * @since Oct 2026
 */
public final class WaveformDescriptor {

    /** The largest Bits Stored the T.261 codec accepts (Sup 253). */
    public static final int T261_MAX_BITS_STORED = 24;

    private final int channels;
    private final long numberOfSamples;
    private final double samplingFrequency;
    private final int bitsAllocated;
    private final int bitsStored;
    private final WaveformSampleInterpretation interpretation;
    private final String label;

    public WaveformDescriptor(int channels, long numberOfSamples, double samplingFrequency,
            int bitsAllocated, int bitsStored, WaveformSampleInterpretation interpretation,
            String label) {
        if (interpretation == null)
            throw new NullPointerException("interpretation");
        if (channels <= 0)
            throw new IllegalArgumentException("Number of Waveform Channels must be positive");
        if (numberOfSamples < 0)
            throw new IllegalArgumentException("Negative Number of Waveform Samples");
        if (bitsAllocated != interpretation.bitsAllocated())
            throw new IllegalArgumentException("Waveform Bits Allocated " + bitsAllocated
                    + " does not match Waveform Sample Interpretation " + interpretation);
        if (bitsStored <= 0 || bitsStored > bitsAllocated)
            throw new IllegalArgumentException("Waveform Bits Stored must be 1.." + bitsAllocated);
        this.channels = channels;
        this.numberOfSamples = numberOfSamples;
        this.samplingFrequency = samplingFrequency;
        this.bitsAllocated = bitsAllocated;
        this.bitsStored = bitsStored;
        this.interpretation = interpretation;
        this.label = label;
    }

    /** Reads the descriptor of one Waveform Sequence Item. */
    public static WaveformDescriptor of(Attributes item) {
        int channels = item.getInt(Tag.NumberOfWaveformChannels, 0);
        long samples = item.getInt(Tag.NumberOfWaveformSamples, 0) & 0xFFFFFFFFL;
        double frequency = item.getDouble(Tag.SamplingFrequency, 0.0);
        int bitsAllocated = item.getInt(Tag.WaveformBitsAllocated, 0);
        WaveformSampleInterpretation interpretation = WaveformSampleInterpretation
                .fromDicom(item.getString(Tag.WaveformSampleInterpretation));
        int bitsStored = maxBitsStored(item.getSequence(Tag.ChannelDefinitionSequence));
        if (bitsStored <= 0)
            bitsStored = Math.min(bitsAllocated, T261_MAX_BITS_STORED);
        return new WaveformDescriptor(channels, samples, frequency, bitsAllocated, bitsStored,
                interpretation, item.getString(Tag.MultiplexGroupLabel));
    }

    private static int maxBitsStored(Sequence channelDefinitions) {
        int max = 0;
        if (channelDefinitions != null)
            for (Attributes channel : channelDefinitions)
                max = Math.max(max, channel.getInt(Tag.WaveformBitsStored, 0));
        return max;
    }

    /** Number of Waveform Channels (003A,0005). */
    public int getChannels() {
        return channels;
    }

    /** Number of Waveform Samples (003A,0010). */
    public long getNumberOfSamples() {
        return numberOfSamples;
    }

    /** Sampling Frequency (003A,001A) in Hz. */
    public double getSamplingFrequency() {
        return samplingFrequency;
    }

    /** Waveform Bits Allocated (5400,1004): 8, 16 or 32. */
    public int getBitsAllocated() {
        return bitsAllocated;
    }

    /**
     * The largest Waveform Bits Stored (003A,021A) of the Channel Definition Sequence, or Bits
     * Allocated capped at 24 when absent.
     */
    public int getBitsStored() {
        return bitsStored;
    }

    /** Waveform Sample Interpretation (5400,1006). */
    public WaveformSampleInterpretation getInterpretation() {
        return interpretation;
    }

    /** Multiplex Group Label (003A,0020), may be null. */
    public String getLabel() {
        return label;
    }

    /** The native length of the Waveform Data in bytes. */
    public long getNativeLength() {
        return numberOfSamples * channels * interpretation.bytesPerSample();
    }
}
