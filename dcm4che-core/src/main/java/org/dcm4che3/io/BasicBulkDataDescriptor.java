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

package org.dcm4che3.io;

import org.dcm4che3.data.*;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;

import java.util.*;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Dec 2018
 */
public class BasicBulkDataDescriptor implements BulkDataDescriptor {
    private String bulkDataDescriptorID;
    private boolean excludeDefaults;
    private final List<AttributeSelector> selectors = new ArrayList<>();
    private final EnumMap<VR,Integer> lengthsThresholdByVR = new EnumMap<>(VR.class);

    public BasicBulkDataDescriptor() {}

    public BasicBulkDataDescriptor(String bulkDataDescriptorID) {
        this.bulkDataDescriptorID = bulkDataDescriptorID;
    }

    public String getBulkDataDescriptorID() {
        return bulkDataDescriptorID;
    }

    public void setBulkDataDescriptorID(String bulkDataDescriptorID) {
        this.bulkDataDescriptorID = bulkDataDescriptorID;
    }

    public boolean isExcludeDefaults() {
        return excludeDefaults;
    }

    public BasicBulkDataDescriptor excludeDefaults() {
        return excludeDefaults(true);
    }

    public BasicBulkDataDescriptor excludeDefaults(boolean excludeDefaults) {
        this.excludeDefaults = excludeDefaults;
        return this;
    }

    public BasicBulkDataDescriptor addAttributeSelector(AttributeSelector... selectors) {
        for (AttributeSelector selector : selectors) {
            this.selectors.add(Objects.requireNonNull(selector));
        }
        return this;
    }

    public AttributeSelector[] getAttributeSelectors() {
        return selectors.toArray(new AttributeSelector[0]);
    }

    public void setAttributeSelectorsFromStrings(String[] ss) {
        List<AttributeSelector> tmp = new ArrayList<>(ss.length);
        for (String s : ss) {
            tmp.add(AttributeSelector.valueOf(s));
        }
        selectors.clear();
        selectors.addAll(tmp);
    }

    public BasicBulkDataDescriptor addTag(int... tags) {
        for (int tag : tags) {
            this.selectors.add(new AttributeSelector(tag));
        }
        return this;
    }

    public BasicBulkDataDescriptor addTagPath(int... tagPaths) {
        if (tagPaths.length == 0)
            throw new IllegalArgumentException("tagPaths.length == 0");
        this.selectors.add(
                new AttributeSelector(tagPaths[tagPaths.length - 1], null, toItemPointers(tagPaths)));
        return this;
    }

    private static List<ItemPointer> toItemPointers(int[] tagPaths) {
        int level = tagPaths.length - 1;
        if (level == 0)
            return Collections.emptyList();

        List<ItemPointer> itemPointers = new ArrayList<>(level);
        for (int i = 0; i < level; i++) {
            itemPointers.add(new ItemPointer(tagPaths[i]));

        }
        return itemPointers;
    }

    public BasicBulkDataDescriptor addLengthsThreshold(int threshold, VR... vrs) {
        if (vrs.length == 0)
            throw new IllegalArgumentException("Missing VR");

        for (VR vr : vrs) {
            lengthsThresholdByVR.put(vr, threshold);
        }
        return this;
    }

    public String[] getLengthsThresholdsAsStrings() {
        if (lengthsThresholdByVR.isEmpty())
            return StringUtils.EMPTY_STRING;

        Map<Integer,EnumSet<VR>> vrsByLength = new HashMap<>();
        for (Map.Entry<VR, Integer> entry : lengthsThresholdByVR.entrySet()) {
            EnumSet<VR> vrs = vrsByLength.get(entry.getValue());
            if (vrs == null)
                vrsByLength.put(entry.getValue(), vrs = EnumSet.noneOf(VR.class));
            vrs.add(entry.getKey());
        }
        String[] ss = new String[vrsByLength.size()];
        int i = 0;
        for (Map.Entry<Integer, EnumSet<VR>> entry : vrsByLength.entrySet()) {
            StringBuilder sb = new StringBuilder();
            Iterator<VR> vr = entry.getValue().iterator();
            sb.append(vr.next());
            while (vr.hasNext())
                sb.append(',').append(vr.next());
            ss[i] = sb.append('=').append(entry.getKey()).toString();
        }
        return ss;
    }

    public void setLengthsThresholdsFromStrings(String... ss) {
        EnumMap<VR,Integer> tmp = new EnumMap<>(VR.class);
        for (String s : ss) {
            String[] entry = StringUtils.split(s, '=');
            if (entry.length != 2)
                throw new IllegalArgumentException(s);
            try {
                Integer length = Integer.valueOf(entry[1]);
                for (String vr : StringUtils.split(entry[0], ',')) {
                    tmp.put(VR.valueOf(vr), length);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(s);
            }
        }
        lengthsThresholdByVR.clear();
        lengthsThresholdByVR.putAll(tmp);
    }

    @Override
    public boolean isBulkData(List<ItemPointer> itemPointers, String privateCreator, int tag, VR vr, int length) {
        return !excludeDefaults && isStandardBulkData(itemPointers, tag)
                || selected(itemPointers, privateCreator, tag)
                || exeeds(length, lengthsThresholdByVR.get(vr));
    }

    private boolean selected(List<ItemPointer> itemPointers, String privateCreator, int tag) {
        for (AttributeSelector selector : selectors) {
            if (selector.matches(itemPointers, privateCreator, tag))
                return true;
        }
        return false;
    }

    static boolean isStandardBulkData(List<ItemPointer> itemPointer, int tag) {
        switch (TagUtils.normalizeRepeatingGroup(tag)) {
            case Tag.PixelDataProviderURL:
            case Tag.AudioSampleData:
            case Tag.CurveData:
            case Tag.SpectroscopyData:
            case Tag.RedPaletteColorLookupTableData:
            case Tag.GreenPaletteColorLookupTableData:
            case Tag.BluePaletteColorLookupTableData:
            case Tag.AlphaPaletteColorLookupTableData:
            case Tag.LargeRedPaletteColorLookupTableData:
            case Tag.LargeGreenPaletteColorLookupTableData:
            case Tag.LargeBluePaletteColorLookupTableData:
            case Tag.SegmentedRedPaletteColorLookupTableData:
            case Tag.SegmentedGreenPaletteColorLookupTableData:
            case Tag.SegmentedBluePaletteColorLookupTableData:
            case Tag.SegmentedAlphaPaletteColorLookupTableData:
            case Tag.OverlayData:
            case Tag.EncapsulatedDocument:
            case Tag.FloatPixelData:
            case Tag.DoubleFloatPixelData:
            case Tag.PixelData:
                return itemPointer.isEmpty();
            case Tag.WaveformData:
                return itemPointer.size() == 1
                        && itemPointer.get(0).sequenceTag == Tag.WaveformSequence;
        }
        return false;
    }

    private static boolean exeeds(int length, Integer lengthThreshold) {
        return lengthThreshold != null && length > lengthThreshold;
    }
}
