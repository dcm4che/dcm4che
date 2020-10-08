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
 * Portions created by the Initial Developer are Copyright (C) 2011
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

package org.dcm4che3.data;

import java.util.Date;
import java.util.TimeZone;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
enum SequenceValueType implements ValueType {
    SQ;

    @Override
    public boolean isStringValue() {
        return false;
    }

    @Override
    public boolean useSpecificCharacterSet() {
        return false;
    }

    @Override
    public boolean isIntValue() {
        return false;
    }

    @Override
    public boolean isTemporalType() {
        return false;
    }

    @Override
    public int numEndianBytes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] toggleEndian(byte[] b, boolean preserve) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] toBytes(Object val, SpecificCharacterSet cs) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public String toString(Object val, boolean bigEndian, int valueIndex,
            String defVal) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public Object toStrings(Object val, boolean bigEndian,
            SpecificCharacterSet cs) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public int toInt(Object val, boolean bigEndian, int valueIndex,
            int defVal) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public int[] toInts(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long toLong(Object val, boolean bigEndian, int valueIndex, long defVal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long[] toLongs(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float toFloat(Object val, boolean bigEndian, int valueIndex,
            float defVal) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public float[] toFloats(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public double toDouble(Object val, boolean bigEndian, int valueIndex,
            double defVal) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public double[] toDoubles(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public Date toDate(Object val, TimeZone tz, int valueIndex, boolean ceil,
            Date defVal, DatePrecision precision) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date[] toDate(Object val, TimeZone tz, boolean ceil,
            DatePrecisions precisions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object toValue(byte[] b) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public Object toValue(String s, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public Object toValue(String[] ss, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public Object toValue(int[] is, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public Object toValue(long[] ls, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object toValue(float[] fs, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public Object toValue(double[] ds, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public Object toValue(Date[] ds, TimeZone tz, DatePrecision precision) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean prompt(Object val, boolean bigEndian,
            SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
        sb.append(val);
        return true;
    }

    @Override
    public int vmOf(Object val) {
        return 1;
    }
}
