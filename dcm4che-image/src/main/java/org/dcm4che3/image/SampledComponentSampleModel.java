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

package org.dcm4che3.image;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.SampleModel;

/**
 * @author Bill Wallace <wayfarer3130@gmail.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class SampledComponentSampleModel extends SampleModel {

    private final ColorSubsampling subsampling;

    public SampledComponentSampleModel(int w, int h, ColorSubsampling subsampling) {
        super(DataBuffer.TYPE_BYTE, w, h, 3);
        this.subsampling = subsampling;
    }

    @Override
    public SampleModel createCompatibleSampleModel(int w, int h) {
        return new SampledComponentSampleModel(w, h, subsampling);
    }

    @Override
    public DataBuffer createDataBuffer() {
        return new DataBufferByte(subsampling.frameLength(width, height));
    }

    @Override
    public SampleModel createSubsetSampleModel(int[] bands) {
        if (bands.length != 3 
                || bands[0] != 0
                || bands[1] != 1
                || bands[2] != 2)
            throw new UnsupportedOperationException();

        return this;
    }

    @Override
    public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
        byte[] ret;
        if ((obj instanceof byte[]) && ((byte[]) obj).length == 3)
            ret = (byte[]) obj;
        else
            ret = new byte[3];
        DataBufferByte dbb = (DataBufferByte) data;
        byte[] ba = dbb.getData();
        int iy = subsampling.indexOfY(x, y, width);
        int ibr = subsampling.indexOfBR(x, y, width);
        ret[0] = ba[iy];
        ret[1] = ba[ibr];
        ret[2] = ba[ibr+1];
        return ret;
    }

    @Override
    public int getNumDataElements() {
        return 3;
    }

    @Override
    public int getSample(int x, int y, int b, DataBuffer data) {
        return ((byte[]) getDataElements(x, y, null, data))[b];
    }

    @Override
    public int[] getSampleSize() {
        return new int[] { 8, 8, 8 };
    }

    @Override
    public int getSampleSize(int band) {
        return 8;
    }

    @Override
    public void setDataElements(int x, int y, Object obj, DataBuffer data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSample(int x, int y, int b, int s, DataBuffer data) {
        throw new UnsupportedOperationException();
    }

}
