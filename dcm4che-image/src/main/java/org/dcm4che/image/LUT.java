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

package org.dcm4che.image;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public abstract class LUT {

    protected StoredValue inBits;
    protected int outBits;
    protected int offset;

    public LUT(StoredValue inBits, int outBits, int offset) {
        this.inBits = inBits;
        this.outBits = outBits;
        this.offset = offset;
    }

    public void lookup(DataBuffer src, DataBuffer dest) {
        if (src instanceof DataBufferByte)
            if (dest instanceof DataBufferByte)
                lookup(((DataBufferByte) src).getData(),
                        ((DataBufferByte) dest).getData());
            else
                lookup(((DataBufferByte) src).getData(),
                        ((DataBufferUShort) dest).getData());
        else 
            if (dest instanceof DataBufferByte)
                lookup(((DataBufferUShort) src).getData(),
                        ((DataBufferByte) dest).getData());
            else
                lookup(((DataBufferUShort) src).getData(),
                        ((DataBufferUShort) dest).getData());
    }

    public abstract void lookup(byte[] src, byte[] dest);

    public abstract void lookup(short[] src, byte[] dest);

    public abstract void lookup(byte[] src, short[] dest);

    public abstract void lookup(short[] src, short[] dest);

    public abstract LUT adjustOutBits(int outBits);

    public abstract void inverse();

}
