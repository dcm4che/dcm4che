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

import org.dcm4che3.io.SAXWriter;
import org.xml.sax.SAXException;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
interface ValueType {

    boolean isStringValue();

    boolean useSpecificCharacterSet();

    boolean isIntValue();

    boolean isTemporalType();

    int numEndianBytes();

    byte[] toggleEndian(byte[] b, boolean preserve);

    byte[] toBytes(Object val, SpecificCharacterSet cs);

    String toString(Object val, boolean bigEndian, int valueIndex, String defVal);

    Object toStrings(Object val, boolean bigEndian, SpecificCharacterSet cs);

    int toInt(Object val, boolean bigEndian, int valueIndex, int defVal);

    int[] toInts(Object val, boolean bigEndian);

    float toFloat(Object val, boolean bigEndian, int valueIndex, float defVal);

    float[] toFloats(Object val, boolean bigEndian);

    double toDouble(Object val, boolean bigEndian, int valueIndex,
            double defVal);

    double[] toDoubles(Object val, boolean bigEndian);

    Date toDate(Object val, TimeZone tz, int valueIndex, boolean ceil,
            Date defVal, DatePrecision precision);

    Date[] toDate(Object val, TimeZone tz, boolean ceil,
            DatePrecisions precisions);

    Object toValue(byte[] b);

    Object toValue(String s, boolean bigEndian);

    Object toValue(String[] ss, boolean bigEndian);

    Object toValue(int[] is, boolean bigEndian);

    Object toValue(float[] fs, boolean bigEndian);

    Object toValue(double[] ds, boolean bigEndian);

    Object toValue(Date[] ds, TimeZone tz, DatePrecision precision);

    boolean prompt(Object val, boolean bigEndian, SpecificCharacterSet cs,
            int maxChars, StringBuilder sb);

    void toXML(Object val, boolean bigEndian, SpecificCharacterSet cs,
            SAXWriter saxWriter, boolean inlineBinary) throws SAXException;

    int vmOf(Object val);
}
