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
import java.util.Arrays;
import java.util.List;

import org.dcm4che.net.Dimse;
import org.dcm4che.net.TransferCapability.Role;


/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class AttributeCoercions implements Serializable {
    
    private static final long serialVersionUID = -1960600890844978686L;

    private final ArrayList<AttributeCoercion> list =
            new ArrayList<AttributeCoercion>();
    private final int[] insertIndex = new int[4];

    public void add(AttributeCoercion ac) {
        if (ac == null)
            throw new NullPointerException();

        int i = 3;
        if (ac.getAETitle() == null)
            i--;
        if (ac.getSopClass() == null)
            i -= 2;
        list.add(insertIndex[i]++, ac);
        while (--i >= 0)
            insertIndex[i]++;
    }

    public boolean remove(AttributeCoercion ac) {
        if (ac == null)
            throw new NullPointerException();

        int index = list.indexOf(ac);
        if (index < 0)
            return false;
        list.remove(index);
        int i = 4;
        while (--i >= 0 && insertIndex[i] > index)
            insertIndex[i]--;
        return true;
    }

    public void clear() {
        list.clear();
        Arrays.fill(insertIndex, 0);
    }

    public AttributeCoercion findEquals(String sopClass, Dimse dimse,
            Role role, String aeTitle) {
        for (AttributeCoercion ac2 : list)
            if (ac2.equals(sopClass, dimse, role, aeTitle))
                return ac2;
        return null;
    }

    public AttributeCoercion findMatching(String sopClass, Dimse dimse,
            Role role, String aeTitle) {
        for (AttributeCoercion ac : list)
            if (ac.matches(sopClass, dimse, role, aeTitle))
                return ac;
        return null;
    }

    public List<AttributeCoercion> getAll() {
        return list;
    }
}
