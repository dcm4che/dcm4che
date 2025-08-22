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

package org.dcm4che3.io;

import java.util.Arrays;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.TagUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Bill Wallace <wayfarer3130@gmail.com>
 *
 */
public abstract class BulkDataDescriptor {

    public final static BulkDataDescriptor DEFAULT = new BulkDataDescriptor() {

        @Override
        public boolean isBulkData(List<ItemPointer> itemPointer, String privateCreator, int tag, VR vr, int length) {
            return switch (TagUtils.normalizeRepeatingGroup(tag)) {
                case Tag.PixelDataProviderURL, Tag.AudioSampleData, Tag.CurveData, Tag.SpectroscopyData,
                     Tag.OverlayData, Tag.EncapsulatedDocument, Tag.FloatPixelData, Tag.DoubleFloatPixelData,
                     Tag.PixelData -> itemPointer.isEmpty();
                case Tag.WaveformData -> itemPointer.size() == 1
                        && itemPointer.get(0).sequenceTag == Tag.WaveformSequence;
                default -> false;
            };
        }

        @Override
        public String toString() {
            return "BulkDataDescriptor.DEFAULT";
        }
    };

    public final static BulkDataDescriptor PIXELDATA = new BulkDataDescriptor() {

        @Override
        public boolean isBulkData(List<ItemPointer> itemPointer, String privateCreator, int tag, VR vr, int length) {
            if (tag == Tag.PixelData)
                return true;
            // Don't need any private tags larger than 64k
            return TagUtils.isPrivateTag(tag) && length>64*1024;
        }

        @Override
        public String toString() {
            return "BulkDataDescriptor.PIXELDATA";
        }
    };

    public static BulkDataDescriptor valueOf(final Attributes blkAttrs) {
        return new BulkDataDescriptor() {

            @Override
            public boolean isBulkData(List<ItemPointer> itemPointer, String privateCreator, int tag, VR vr, int length) {
                Attributes item = blkAttrs;
                for (ItemPointer ip : itemPointer) {
                    item = item.getNestedDataset(
                            ip.privateCreator, ip.sequenceTag, ip.itemIndex);
                }
                return item != null && item.contains(privateCreator, tag);
            }
        };
    }

    public static BulkDataDescriptor or(BulkDataDescriptor... descriptors) {
        return new BulkDataDescriptor() {
            @Override
            public boolean isBulkData(List<ItemPointer> itemPointer, String privateCreator, int tag, VR vr, int length) {
                for(BulkDataDescriptor bd : descriptors) {
                    if(bd.isBulkData(itemPointer, privateCreator, tag,vr,length))
                        return true;
                }
                return false;
            }
        };
    }

    public static BulkDataDescriptor privateTagGreaterThan( final int maxSize) {
        return new BulkDataDescriptor() {
            @Override
            public boolean isBulkData(List<ItemPointer> itemPointer, String privateCreator, int tag, VR vr, int length) {
                return TagUtils.isPrivateTag(tag) && length > maxSize;
            }
        };
    }


    public abstract boolean isBulkData(List<ItemPointer> itemPointer, String privateCreator, int tag, VR vr, int length);

}
