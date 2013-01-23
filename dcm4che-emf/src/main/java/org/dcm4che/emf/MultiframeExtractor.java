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

package org.dcm4che.emf;

import java.util.ArrayList;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.BulkDataLocator;
import org.dcm4che.data.Fragments;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class MultiframeExtractor {

    private enum Impl {
        EnhancedCTImageExtractor(UID.CTImageStorage),
        EnhancedMRImageExtractor(UID.MRImageStorage) {
            Attributes extract(MultiframeExtractor mfe, Attributes emf, int frame) {
                Attributes sf = super.extract(mfe, emf, frame);
                adjustImageType(sf);
                setEchoTime(sf);
                setScanningSequence(sf);
                setSequenceVariant(sf);
                setScanOptions(sf);
                return sf;
            }

            private void adjustImageType(Attributes sf) {
                // TODO Auto-generated method stub
                
            }

            void setEchoTime(Attributes sf) {
                double echoTime = sf.getDouble(Tag.EffectiveEchoTime, 0);
                if (echoTime == 0)
                    sf.setNull(Tag.EchoTime, VR.DS);
                else
                    sf.setDouble(Tag.EchoTime, VR.DS, echoTime);
            }

            void setScanningSequence(Attributes sf) {
                ArrayList<String> list = new ArrayList<String>(3);
                list.add(
                        "GRADIENT".equals(sf.getString(Tag.EchoPulseSequence))
                            ? "GR"
                            : "SE");
                if ("YES".equals(sf.getString(Tag.InversionRecovery)))
                    list.add("IR");
                if ("YES".equals(sf.getString(Tag.EchoPlanarPulseSequence)))
                    list.add("EP");
                sf.setString(Tag.ScanningSequence, VR.CS,
                        list.toArray(new String[list.size()]));
            }

            void setSequenceVariant(Attributes sf) {
                ArrayList<String> list = new ArrayList<String>(5);
                if ("PARTIAL".equals(sf.getString(Tag.SegmentedKSpaceTraversal)))
                    list.add("SK");
                String mf = sf.getString(Tag.MagnetizationTransfer);
                if (mf != null && !"NONE".equals(mf))
                    list.add("MTC");
                String ssps = sf.getString(Tag.SteadyStatePulseSequence);
                if (ssps != null && !"NONE".equals(ssps))
                    list.add("TIME_REVERSED".equals(ssps) ? "TRSS" :"SS");
                String sp = sf.getString(Tag.Spoiling);
                if (sp != null && !"NONE".equals(sp))
                    list.add("SP");
                String op = sf.getString(Tag.OversamplingPhase);
                if (op != null && !"NONE".equals(op))
                    list.add("OSP");
                if (list.isEmpty())
                    list.add("NONE");
                sf.setString(Tag.SequenceVariant, VR.CS,
                        list.toArray(new String[list.size()]));
            }

            void setScanOptions(Attributes sf) {
                ArrayList<String> list = new ArrayList<String>(3);
                String per = sf.getString(Tag.RectilinearPhaseEncodeReordering);
                if (per != null && !"LINEAR".equals(per))
                    list.add("PER");
                String frameType3 = sf.getString(Tag.FrameType, 2);
                if ("ANGIO".equals(frameType3))
                    sf.setString(Tag.AngioFlag, VR.CS, "Y");
                if (frameType3.startsWith("CARD"))
                    list.add("CG");
                if (frameType3.endsWith("RESP_GATED"))
                    list.add("RG");
                String pfd = sf.getString(Tag.PartialFourierDirection);
                if ("PHASE".equals(pfd))
                    list.add("PFP");
                if ("FREQUENCY".equals(pfd))
                    list.add("PFF");
                String sp = sf.getString(Tag.SpatialPresaturation);
                if (sp != null && !"NONE".equals(sp))
                    list.add("SP");
                String sss = sf.getString(Tag.SpectrallySelectedSuppression);
                if (sss != null && sss.startsWith("FAT"))
                    list.add("FS");
                String fc = sf.getString(Tag.FlowCompensation);
                if (fc != null && !"NONE".equals(fc))
                    list.add("FC");
                sf.setString(Tag.ScanOptions, VR.CS,
                        list.toArray(new String[list.size()]));
            }

        },
        EnhancedPETImageExtractor(UID.PositronEmissionTomographyImageStorage);

        private final String cuid;

        Impl(String cuid) {
            this.cuid = cuid;
        }

        Attributes extract(MultiframeExtractor mfe, Attributes emf, int frame) {
            return mfe.extract(emf, frame, cuid);
        }
    }

    private boolean preserveSeriesInstanceUID;
    private String instanceNumberFormat = "%s%04d";
    private UIDMapper uidMapper = new HashUIDMapper();
    private static final int[] EXCLUDE_TAGS = {
            Tag.NumberOfFrames,
            Tag.SharedFunctionalGroupsSequence,
            Tag.PerFrameFunctionalGroupsSequence,
            Tag.PixelData };

    public final boolean isPreserveSeriesInstanceUID() {
        return preserveSeriesInstanceUID;
    }

    public final void setPreserveSeriesInstanceUID(
            boolean preserveSeriesInstanceUID) {
        this.preserveSeriesInstanceUID = preserveSeriesInstanceUID;
    }

    public final String getInstanceNumberFormat() {
        return instanceNumberFormat;
    }

    public final void setInstanceNumberFormat(String instanceNumberFormat) {
        String.format(instanceNumberFormat, "1", 1);
        this.instanceNumberFormat = instanceNumberFormat;
    }

    public final UIDMapper getUIDMapper() {
        return uidMapper;
    }

    public final void setUIDMapper(UIDMapper uidMapper) {
        if (uidMapper == null)
            throw new NullPointerException();
        this.uidMapper = uidMapper;
    }

    /** Extract specified frame from Enhanced Multi-frame image and return it
     * as correponding legacy Single-frame image.
     * 
     * @param emf Enhanced Multi-frame image
     * @param frame 0 based frame index
     * @return legacy Single-frame image
     */
    public Attributes extract(Attributes emf, int frame) {
        return implFor(emf).extract(this, emf, frame);
    }

    private Impl implFor(Attributes emf) {
        String mfcuid = emf.getString(Tag.SOPClassUID);
        if (mfcuid.equals(UID.EnhancedCTImageStorage))
            return Impl.EnhancedCTImageExtractor;
        if (mfcuid.equals(UID.EnhancedMRImageStorage))
            return Impl.EnhancedMRImageExtractor;
        if (mfcuid.equals(UID.EnhancedPETImageStorage))
            return Impl.EnhancedPETImageExtractor;
        throw new IllegalArgumentException(
                "Unsupported SOP Class: " + mfcuid);
    }

    private Attributes extract(Attributes emf, int frame, String sopClassUID) {
        Attributes sfgs = emf.getNestedDataset(Tag.SharedFunctionalGroupsSequence);
        if (sfgs == null)
            throw new IllegalArgumentException(
                    "Missing (5200,9229) Shared Functional Groups Sequence");
        Attributes fgs = emf.getNestedDataset(Tag.PerFrameFunctionalGroupsSequence, frame);
        if (fgs == null)
            throw new IllegalArgumentException(
                    "Missing (5200,9230) Per-frame Functional Groups Sequence Item for frame #" + (frame + 1));
        Attributes dest = new Attributes(emf.size() * 2);
        dest.addNotSelected(emf, EXCLUDE_TAGS);
        addFunctionGroups(dest, sfgs);
        addFunctionGroups(dest, fgs);
        addPixelData(dest, emf, frame);
        dest.setString(Tag.SOPClassUID, VR.UI, sopClassUID);
        dest.setString(Tag.SOPInstanceUID, VR.UI, uidMapper.get(
                dest.getString(Tag.SOPInstanceUID)) + '.' + (frame + 1));
        dest.setString(Tag.InstanceNumber, VR.IS,
                createInstanceNumber(dest.getString(Tag.InstanceNumber, ""), frame));
        if (!preserveSeriesInstanceUID)
            dest.setString(Tag.SeriesInstanceUID, VR.UI, uidMapper.get(
                    dest.getString(Tag.SeriesInstanceUID)));
        return dest;
    }

    private void addFunctionGroups(Attributes dest, Attributes fgs) {
        for (int sequenceTag : fgs.tags())
            dest.addAll(fgs.getNestedDataset(sequenceTag));
    }

    private void addPixelData(Attributes dest, Attributes src, int frame) {
        VR.Holder vr = new VR.Holder();
        Object pixelData = src.getValue(Tag.PixelData, vr);
        if (pixelData instanceof byte[]) {
            dest.setBytes(Tag.PixelData, vr.vr, extractPixelData(
                    (byte[]) pixelData, frame, calcFrameLength(src)));
        } else if (pixelData instanceof BulkDataLocator) {
            dest.setValue(Tag.PixelData, vr.vr, extractPixelData(
                    (BulkDataLocator) pixelData, frame, calcFrameLength(src)));
        } else {
            Fragments destFrags = dest.newFragments(Tag.PixelData, vr.vr, 2);
            destFrags.add(null);
            destFrags.add(((Fragments) pixelData).get(frame + 1));
        }
    }

    private BulkDataLocator extractPixelData(BulkDataLocator src, int frame,
            int length) {
        return new BulkDataLocator(src.uri, src.transferSyntax,
                src.offset + frame * length, length);
    }

    private byte[] extractPixelData(byte[] src, int frame, int length) {
        byte[] dest = new byte[length];
        System.arraycopy(src, frame * length, dest, 0, length);
        return dest;
    }

    private int calcFrameLength(Attributes src) {
        return src.getInt(Tag.Rows, 0)
             * src.getInt(Tag.Columns, 0)
             * (src.getInt(Tag.BitsAllocated, 8) >> 3)
             * src.getInt(Tag.NumberOfSamples, 1);
    }

    private String createInstanceNumber(String mfinstno, int frame) {
        String s = String.format(instanceNumberFormat, mfinstno, frame + 1);
        return s.length() > 16 ? s.substring(s.length() - 16) : s;
    }

}
