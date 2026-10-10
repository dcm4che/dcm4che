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
import java.util.ArrayList;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.SafeClose;
import org.weasis.opencv.waveform.T261Parameters;
import org.weasis.opencv.waveform.T261Parameters.SignalType;
import org.weasis.opencv.waveform.WaveformReader;

/**
 * Transcodes the Waveform Data of a DICOM waveform object between the native transfer syntaxes and
 * the T.261 ones of Supplement 253 (native to lossless or lossy, T.261 to native, lossless to
 * lossy), through streams: every multiplex group is read and encoded per frame sequence, so the
 * memory is bounded by one source window plus one frame sequence whatever the size of the object.
 * The counterpart of {@code org.dcm4che3.imageio.codec.Transcoder} for Waveform objects, with the
 * same File Meta Information and encoding options.
 * <p>
 * Note: the supplement under public comment does not assign the Lossy Waveform Compression
 * attributes yet, so the lossy transcoding does not record the compression in the data set.
 *
 * @author Nicolas Roduit
 * @since Oct 2026
 */
public class WaveformTranscoder implements Closeable {

    private final DicomInputStream dis;
    private final String srcTransferSyntax;
    private String destTransferSyntax;
    private boolean includeFileMetaInformation = true;
    private boolean retainFileMetaInformation;
    private boolean closeInputStream = true;
    private boolean closeOutputStream = true;
    private boolean deleteBulkDataFiles = true;
    private DicomEncodingOptions encOpts = DicomEncodingOptions.DEFAULT;
    private File tmpDir;
    private double stepSize;
    private SignalType signalType = SignalType.ECG;
    private int intraPeriod;
    private int profileLevel;

    public WaveformTranscoder(File f) throws IOException {
        this(new DicomInputStream(f));
    }

    public WaveformTranscoder(DicomInputStream dis) throws IOException {
        this.dis = dis;
        dis.setIncludeBulkData(IncludeBulkData.URI);
        dis.readFileMetaInformation();
        srcTransferSyntax = dis.getTransferSyntax();
        destTransferSyntax = srcTransferSyntax;
    }

    public String getSourceTransferSyntax() {
        return srcTransferSyntax;
    }

    public String getDestinationTransferSyntax() {
        return destTransferSyntax;
    }

    /**
     * @param tsuid a native transfer syntax, {@link T261TransferSyntax#lossless()} or
     *            {@link T261TransferSyntax#lossy()}
     */
    public void setDestinationTransferSyntax(String tsuid) {
        if (tsuid == null)
            throw new NullPointerException("tsuid");
        this.destTransferSyntax = tsuid;
    }

    public void setIncludeFileMetaInformation(boolean includeFileMetaInformation) {
        this.includeFileMetaInformation = includeFileMetaInformation;
    }

    public void setRetainFileMetaInformation(boolean retainFileMetaInformation) {
        this.retainFileMetaInformation = retainFileMetaInformation;
    }

    public void setCloseInputStream(boolean closeInputStream) {
        this.closeInputStream = closeInputStream;
    }

    public void setCloseOutputStream(boolean closeOutputStream) {
        this.closeOutputStream = closeOutputStream;
    }

    public void setDeleteBulkDataFiles(boolean deleteBulkDataFiles) {
        this.deleteBulkDataFiles = deleteBulkDataFiles;
    }

    public void setEncodingOptions(DicomEncodingOptions encOpts) {
        this.encOpts = encOpts;
    }

    /** The directory of the spool files (one per multiplex group), null for the default. */
    public void setTemporaryDirectory(File tmpDir) {
        this.tmpDir = tmpDir;
    }

    /**
     * The quantization step size of the lossy transfer syntax (0 keeps the preset value of the
     * codec, 1.0 is lossless). The sample error grows with it and is of its order in sample units,
     * not strictly bounded by it: the quantization applies in the transform domain.
     */
    public void setStepSize(double stepSize) {
        if (stepSize < 0)
            throw new IllegalArgumentException("Negative step size");
        this.stepSize = stepSize;
    }

    /** The preset family of the encoder (ECG by default). */
    public void setSignalType(SignalType signalType) {
        if (signalType == null)
            throw new NullPointerException("signalType");
        this.signalType = signalType;
    }

    /**
     * The frame sequence length in samples, the unit of random access and of each Item of the
     * encapsulated Waveform Data (0 keeps the preset value, 98 304 samples).
     */
    public void setIntraPeriod(int intraPeriod) {
        if (intraPeriod < 0)
            throw new IllegalArgumentException("Negative intra period");
        this.intraPeriod = intraPeriod;
    }

    /** The T.261 Main profile level, 0 for the preset value. */
    public void setProfileLevel(int profileLevel) {
        this.profileLevel = profileLevel;
    }

    /** True when the source or the destination is a T.261 transfer syntax. */
    public boolean isWaveformTranscoding() {
        return T261TransferSyntax.isT261(srcTransferSyntax)
                || T261TransferSyntax.isT261(destTransferSyntax);
    }

    /** The encoder parameters for one multiplex group, derived from its descriptor. */
    public T261Parameters t261Parameters(WaveformDescriptor desc) {
        if (!desc.getInterpretation().isSigned())
            throw new IllegalArgumentException(
                    "Sup 253 restricts T.261 to signed samples (SB, SS, SL); the Item uses "
                            + desc.getInterpretation());
        if (desc.getBitsStored() > WaveformDescriptor.T261_MAX_BITS_STORED)
            throw new IllegalArgumentException("Waveform Bits Stored " + desc.getBitsStored()
                    + " exceeds the " + WaveformDescriptor.T261_MAX_BITS_STORED + " bits of T.261");
        boolean lossless = !T261TransferSyntax.isLossy(destTransferSyntax);
        return new T261Parameters(desc.getChannels(), desc.getBitsAllocated(),
                desc.getBitsStored(), desc.getSamplingFrequency(), lossless,
                lossless ? 0 : stepSize, signalType, intraPeriod, profileLevel);
    }

    /** Transcodes to a file. */
    public void transcode(File dest) throws IOException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(dest), 1 << 16);
        try {
            transcode(out);
        } finally {
            if (!closeOutputStream)
                out.close();
        }
    }

    /** Reads the data set, transcodes every multiplex group and writes the result. */
    public void transcode(OutputStream out) throws IOException {
        Attributes dataset = dis.readDataset();
        List<EncodedWaveform> encoded = transcode(dataset, srcTransferSyntax);
        try {
            Attributes fmi = null;
            if (includeFileMetaInformation) {
                Attributes srcFmi = dis.getFileMetaInformation();
                if (retainFileMetaInformation && srcFmi != null) {
                    fmi = new Attributes(srcFmi);
                    fmi.setString(Tag.TransferSyntaxUID, VR.UI, destTransferSyntax);
                } else {
                    fmi = dataset.createFileMetaInformation(destTransferSyntax);
                }
            }
            DicomOutputStream dos = new DicomOutputStream(out,
                    fmi != null ? UID.ExplicitVRLittleEndian : destTransferSyntax);
            dos.setEncodingOptions(encOpts);
            try {
                dos.writeDataset(fmi, dataset);
                dos.finish();
            } finally {
                if (closeOutputStream)
                    dos.close();
                else
                    dos.flush();
            }
        } finally {
            EncodedWaveform.closeAll(encoded);
        }
    }

    /**
     * Replaces the Waveform Data of every Waveform Sequence Item of a data set by its transcoded
     * value, for the destination transfer syntax of this transcoder. The data set is modified in
     * place and can then be written with {@code DicomOutputStream}; close the returned values
     * afterwards to delete their spool files. Nothing is done between two native transfer
     * syntaxes.
     *
     * @param dataset a data set read with bulk data references or in memory
     * @param srcTsuid the transfer syntax of the data set
     * @return the transcoded values, one per multiplex group
     */
    public List<EncodedWaveform> transcode(Attributes dataset, String srcTsuid)
            throws IOException {
        boolean srcT261 = T261TransferSyntax.isT261(srcTsuid);
        boolean dstT261 = T261TransferSyntax.isT261(destTransferSyntax);
        List<EncodedWaveform> encoded = new ArrayList<EncodedWaveform>();
        if (!srcT261 && !dstT261)
            return encoded;
        Sequence groups = dataset.getSequence(Tag.WaveformSequence);
        if (groups == null || groups.isEmpty())
            throw new IOException("No Waveform Sequence in the data set");
        try {
            for (Attributes item : groups) {
                WaveformDescriptor desc = WaveformDescriptor.of(item);
                WaveformReader reader = WaveformReaders.open(item, desc, srcTsuid);
                try {
                    EncodedWaveform value = dstT261
                            ? EncodedWaveform.encodeT261(reader, t261Parameters(desc), tmpDir)
                            : EncodedWaveform.decodeNative(reader, desc, tmpDir);
                    encoded.add(value);
                    value.setTo(item);
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    throw new IOException(
                            "Transcoding the waveform group " + desc.getLabel(), e);
                } finally {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        throw new IOException("Closing the waveform reader", e);
                    }
                }
            }
        } catch (IOException e) {
            EncodedWaveform.closeAll(encoded);
            throw e;
        } catch (RuntimeException e) {
            EncodedWaveform.closeAll(encoded);
            throw e;
        }
        return encoded;
    }

    @Override
    public void close() throws IOException {
        if (closeInputStream)
            SafeClose.close(dis);
        if (deleteBulkDataFiles)
            for (File tmpFile : dis.getBulkDataFiles())
                tmpFile.delete();
    }
}
