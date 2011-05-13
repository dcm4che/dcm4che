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

package org.dcm4che.net.pdu;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class ExtendedNegotiations {

    public static enum Query {
        relational, datetime, fuzzy, timezone;

        public static byte[] toInfo(EnumSet<Query> flags) {
            byte[] info = ExtendedNegotiations.toInfo(flags);
            return info != null && info.length == 2
                    ? Arrays.copyOf(info, 3)
                    : info;
        }

        public static byte[] toInfoWorklistIM(EnumSet<Query> flags) {
            byte[] info = ExtendedNegotiations.toInfo(flags);
            if (info != null) {
                if (info.length < 3)
                    return null;
                info[0] = info[1] = 1;
            }
            return info;
        }

        public static EnumSet<Query> toSet(byte[] info) {
            return ExtendedNegotiations.toSet(info, Query.class);
        }
    }

    private static <E extends Enum<E>> byte[] toInfo(EnumSet<E> flags) {
        if (flags.isEmpty())
            return null;
        int len = Collections.max(flags).ordinal() + 1;
        byte[] info = new byte[len];
        for (Enum<E> flag : flags)
            info[flag.ordinal()] = 1;
        return info ;
    }

    private static <E extends Enum<E>> EnumSet<E> toSet(byte[] info,
            Class<E> elementType) {
        EnumSet<E> set = EnumSet.allOf(elementType);
        for (Iterator<E> iter = set.iterator(); iter.hasNext();) {
            int i = iter.next().ordinal();
            if (i >= info.length || info[i] == 0)
                iter.remove();
        }
        return set ;
    }

}
