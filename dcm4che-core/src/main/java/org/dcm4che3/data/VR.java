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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public enum VR {
    AE(0x4145, 8, ' ', StringValueType.ASCII, false),
    AS(0x4153, 8, ' ', StringValueType.ASCII, false),
    AT(0x4154, 8, 0, BinaryValueType.TAG, false),
    CS(0x4353, 8, ' ', StringValueType.ASCII, false),
    DA(0x4441, 8, ' ', StringValueType.DA, false),
    DS(0x4453, 8, ' ', StringValueType.DS, false),
    DT(0x4454, 8, ' ', StringValueType.DT, false),
    FD(0x4644, 8, 0, BinaryValueType.DOUBLE, false),
    FL(0x464c, 8, 0, BinaryValueType.FLOAT, false),
    IS(0x4953, 8, ' ', StringValueType.IS, false),
    LO(0x4c4f, 8, ' ', StringValueType.STRING, false),
    LT(0x4c54, 8, ' ', StringValueType.TEXT, false),
    OB(0x4f42, 12, 0, BinaryValueType.BYTE, true),
    OD(0x4f44, 12, 0, BinaryValueType.DOUBLE, true),
    OF(0x4f46, 12, 0, BinaryValueType.FLOAT, true),
    OL(0x4f4c, 12, 0, BinaryValueType.INT, true),
    OW(0x4f57, 12, 0, BinaryValueType.SHORT, true),
    PN(0x504e, 8, ' ', StringValueType.PN, false),
    SH(0x5348, 8, ' ', StringValueType.STRING, false),
    SL(0x534c, 8, 0, BinaryValueType.INT, false),
    SQ(0x5351, 12, 0, SequenceValueType.SQ, false),
    SS(0x5353, 8, 0, BinaryValueType.SHORT, false),
    ST(0x5354, 8, ' ', StringValueType.TEXT, false),
    TM(0x544d, 8, ' ', StringValueType.TM, false),
    UC(0x5543, 12, ' ', StringValueType.STRING, false),
    UI(0x5549, 8, 0, StringValueType.ASCII, false),
    UL(0x554c, 8, 0, BinaryValueType.INT, false),
    UN(0x554e, 12, 0, BinaryValueType.BYTE, true),
    UR(0x5552, 12, ' ', StringValueType.UR, false),
    US(0x5553, 8, 0, BinaryValueType.USHORT, false),
    UT(0x5554, 12, ' ', StringValueType.TEXT, false);

    private static Logger LOG = LoggerFactory.getLogger(VR.class);

    protected final int code;
    protected final int headerLength;
    protected final int paddingByte;
    protected final ValueType valueType;
    protected final boolean inlineBinary;

    VR(int code, int headerLength, int paddingByte, ValueType valueType,
            boolean inlineBinary) {
        this.code = code;
        this.headerLength = headerLength;
        this.paddingByte = paddingByte;
        this.valueType = valueType;
        this.inlineBinary = inlineBinary;
    }

    private static int indexOf(int code) {
        int code1 = code - 0x4141;
        return (code1 & 0xffffe0e0) == 0 ? ((code1 & 0xff00) >> 3) + (code1 & 0xff) : -1;
    }

    private static final VR[] VALUE_OF = new VR[indexOf(UT.code)+1];
    static {
        for (VR vr : VR.values())
            VALUE_OF[indexOf(vr.code)] = vr;
    }

    public static VR valueOf(int code) {
        try {
            VR vr = VALUE_OF[indexOf(code)];
            if (vr != null)
                return vr;
        } catch (IndexOutOfBoundsException e) {}
        LOG.warn("Unrecogniced VR code: {0}H - treat as UN",
                Integer.toHexString(code));
        return UN;
    }

    public int code() {
        return code;
    }

    public int headerLength() {
        return headerLength;
    }

    public int paddingByte() {
        return paddingByte;
    }

    public boolean isTemporalType() {
        return valueType.isTemporalType();
    }

    public boolean isStringType() {
        return valueType.isStringValue();
    }

    public boolean useSpecificCharacterSet() {
        return valueType.useSpecificCharacterSet();
    }

    public boolean isIntType() {
        return valueType.isIntValue();
    }

    public boolean isInlineBinary() {
        return inlineBinary;
    }

    public int numEndianBytes() {
        return valueType.numEndianBytes();
    }

    public byte[] toggleEndian(byte[] b, boolean preserve) {
        return valueType.toggleEndian(b, preserve);
    }

    public byte[] toBytes(Object val, SpecificCharacterSet cs) {
        return valueType.toBytes(val, cs);
    }

    public Object toStrings(Object val, boolean bigEndian, SpecificCharacterSet cs) {
        return valueType.toStrings(val, bigEndian, cs);
    }

    public String toString(Object val, boolean bigEndian, int valueIndex,
            String defVal) {
        return valueType.toString(val, bigEndian, valueIndex, defVal);
    }

    public int toInt(Object val, boolean bigEndian, int valueIndex, int defVal) {
        return valueType.toInt(val, bigEndian, valueIndex, defVal);
    }

    public int[] toInts(Object val, boolean bigEndian) {
        return valueType.toInts(val, bigEndian);
    }

    public float toFloat(Object  val, boolean bigEndian, int valueIndex, float defVal) {
        return valueType.toFloat(val, bigEndian, valueIndex, defVal);
    }

    public float[] toFloats(Object val, boolean bigEndian) {
        return valueType.toFloats(val, bigEndian);
    }

    public double toDouble(Object val, boolean bigEndian, int valueIndex,
            double defVal) {
        return valueType.toDouble(val, bigEndian, valueIndex, defVal);
    }

    public double[] toDoubles(Object val, boolean bigEndian) {
        return valueType.toDoubles(val, bigEndian);
    }

    public Date toDate(Object val, TimeZone tz, int valueIndex, boolean ceil,
            Date defVal, DatePrecision precision) {
        return valueType.toDate(val, tz, valueIndex, ceil, defVal, precision);
    }

    public Date[] toDates(Object val, TimeZone tz, boolean ceil,
            DatePrecisions precisions) {
        return valueType.toDate(val, tz, ceil, precisions);
    }

    Object toValue(byte[] b) {
        return valueType.toValue(b);
    }

    Object toValue(String s, boolean bigEndian) {
        return valueType.toValue(s, bigEndian);
    }

    Object toValue(String[] ss, boolean bigEndian) {
        return valueType.toValue(ss, bigEndian);
    }

    Object toValue(int[] is, boolean bigEndian) {
        return valueType.toValue(is, bigEndian);
    }

    Object toValue(float[] fs, boolean bigEndian) {
        return valueType.toValue(fs, bigEndian);
    }

    Object toValue(double[] ds, boolean bigEndian) {
        return valueType.toValue(ds, bigEndian);
    }

    public Object toValue(Date[] ds, TimeZone tz, DatePrecision precision) {
        return valueType.toValue(ds, tz, precision);
    }

    public boolean prompt(Object val, boolean bigEndian,
            SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
        return valueType.prompt(val, bigEndian, cs, maxChars, sb);
    }

    public int vmOf(Object val) {
        return headerLength == 12 ? 1 : valueType.vmOf(val);
    }

    public static class Holder {
        public VR vr;
    }
}
