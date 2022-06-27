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

package org.dcm4che3.data;

import java.util.Calendar;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class DatePrecision {

    public DatePrecision() {
        this(Calendar.MILLISECOND, false);
    }

    public DatePrecision(int lastField) {
        this(lastField, false);
    }

    public DatePrecision(int lastField, boolean includeTimezone) {
        this.lastField = lastField;
        this.includeTimezone = includeTimezone;
    }

    /**
     * Specifies the precision of a date value (e.g. Calendar.MILLISECOND for millisecond precision).
     *
     * For methods that format a date (e.g. {@link Attributes#setDate}), this acts as an input to specify the precision
     * that should be stored.
     *
     * For methods that parse a date (e.g. {@link Attributes#getDate}), this field will be used as a return value. It
     * returns the precision of the date that was parsed.
     */
    public int lastField;

    /**
     * Specifies whether a formatted date includes a timezone in the stored value itself.
     *
     * This is only used for values of {@link VR#DT}.
     *
     * For methods that format a DT date time, this acts as an input to specify whether the timezone offset should
     * be appended to the formatted date (e.g. "+0100").
     *
     * For methods that parse a DT date time, this field will be used as a return value. It returns whether the parsed
     * date included a timezone offset.
     */
    public boolean includeTimezone;

}
