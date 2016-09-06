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

package org.dcm4che3.io;

import java.util.List;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.TagUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public abstract class BulkDataDescriptor {

    public final static BulkDataDescriptor DEFAULT = new BulkDataDescriptor() {

        @Override
        public boolean isBulkData(List<ItemPointer> itemPointer,
                String privateCreator, int tag, VR vr, int length) {
            switch (TagUtils.normalizeRepeatingGroup(tag)) {
            case Tag.PixelDataProviderURL:
            case Tag.AudioSampleData:
            case Tag.CurveData:
            case Tag.SpectroscopyData:
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
            switch (vr) {
                case OB:
                case OD:
                case OF:
                case OL:
                case OW:
                case UN:
                    return length > 64;
            }
            return false;
        }
    };

    public final static BulkDataDescriptor PIXELDATA = new BulkDataDescriptor() {

        @Override
        public boolean isBulkData(List<ItemPointer> itemPointer,
                String privateCreator, int tag, VR vr, int length) {
            return tag == Tag.PixelData;
        }
    };

    public static BulkDataDescriptor valueOf(final Attributes blkAttrs) {
        return new BulkDataDescriptor() {

            @Override
            public boolean isBulkData(List<ItemPointer> itemPointer,
                    String privateCreator, int tag, VR vr, int length) {
                Attributes item = blkAttrs;
                for (ItemPointer ip : itemPointer) {
                    item = item.getNestedDataset(
                            ip.privateCreator, ip.sequenceTag, ip.itemIndex);
                }
                return item.contains(privateCreator, tag);
            }
        };
    }

    public abstract boolean isBulkData(List<ItemPointer> itemPointer,
            String privateCreator, int tag, VR vr, int length);

}
