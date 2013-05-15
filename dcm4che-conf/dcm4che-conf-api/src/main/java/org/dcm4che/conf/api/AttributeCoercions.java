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

package org.dcm4che.conf.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.dcm4che.net.Dimse;
import org.dcm4che.net.TransferCapability.Role;


/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class AttributeCoercions
        implements Iterable<AttributeCoercion>, Serializable {
    
    private static final long serialVersionUID = -1960600890844978686L;

    private final ArrayList<AttributeCoercion> list =
            new ArrayList<AttributeCoercion>();

    public void add(AttributeCoercion ac) {
        int index = Collections.binarySearch(list, ac);
        if (index < 0)
            index = -(index+1);
        list.add(index, ac);
    }

    public void add(AttributeCoercions acs) {
         for (AttributeCoercion ac : acs.list)
             add(ac);
    }

    public boolean remove(AttributeCoercion ac) {
        return list.remove(ac);
    }

    public void clear() {
        list.clear();
    }

    public AttributeCoercion findEquals(String sopClass, Dimse dimse,
            Role role, String aeTitle) {
        for (AttributeCoercion ac2 : list)
            if (ac2.getDimse() == dimse
            && ac2.getRole() == role
            && equals(ac2.getSopClass(), sopClass)
            && equals(ac2.getAETitle(), aeTitle))
                return ac2;
        return null;
    }

    public AttributeCoercion findMatching(String sopClass, Dimse dimse,
            Role role, String aeTitle) {
        for (AttributeCoercion ac : list)
            if (ac.getDimse() == dimse
            && ac.getRole() == role
            && matches(ac.getSopClass(), sopClass)
            && matches(ac.getAETitle(), aeTitle))
                return ac;
        return null;
    }

    private static boolean equals(Object o1, Object o2) {
        return o1 == o2 || o1 != null && o1.equals(o2);
    }

    private static boolean matches(Object o1, Object o2) {
        return o1 == null || o2 == null || o1.equals(o2);
    }

    @Override
    public Iterator<AttributeCoercion> iterator() {
        return list.iterator();
    }
}
