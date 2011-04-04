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

package org.dcm4che.data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * 
 */
public class DateRange implements Serializable {

    private static final long serialVersionUID = 88574297440294935L;

    private final Date lower;
    private final Date upper;

    public DateRange(Date lower, Date upper) {
        if (lower != null && upper != null && lower.after(upper))
            throw new IllegalArgumentException("lower: " + lower
                    + " after upper: " + upper);
        this.lower = lower;
        this.upper = upper;
    }

    public final Date getLower() {
        return lower;
    }

    public final Date getUpper() {
        return upper;
    }

    public boolean contains(Date when) {
        return !(lower != null && lower.after(when)
              || upper != null && upper.before(when));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof DateRange))
            return false;

        DateRange other = (DateRange) obj;
        return (lower == null 
                ? other.lower == null
                : lower.equals(other.lower)) 
            && (upper == null
                ? other.upper == null
                : upper.equals(other.upper));
    }

    @Override
    public int hashCode() {
        int code = 0;
        if (lower != null)
            code = lower.hashCode();
        if (upper != null)
            code ^= lower.hashCode();
        return code;
    }

    @Override
    public String toString() {
        return "[" + lower + ", " + upper + "]";
    }

}
