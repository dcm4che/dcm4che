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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.dcm4che.net.TransferCapability.Role;


/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class AttributeCoercions {

    @SuppressWarnings("rawtypes")
    private final HashMap[] maps = new HashMap[AttributeCoercion.DIMSE.values().length * 2];

    public AttributeCoercion add(AttributeCoercion ac) {
        HashMap<String,HashMap<String,AttributeCoercion>> map1 =
                mapOf(ac.getDimse(), ac.getRole());
        if (map1 == null)
            maps[ac.getDimse().ordinal() * 2 + ac.getRole().ordinal()] = 
                map1 = new HashMap<String,HashMap<String,AttributeCoercion>>();
        HashMap<String, AttributeCoercion> map2 = map1.get(ac.getAETitle());
        if (map2 == null)
            map1.put(ac.getAETitle(),
                    map2 = new HashMap<String, AttributeCoercion>());
        return map2.put(ac.getSopClass(), ac);
    }

    public AttributeCoercion getEquals(AttributeCoercion ac) {
        HashMap<String,HashMap<String,AttributeCoercion>> map1 =
            mapOf(ac.getDimse(), ac.getRole());
        if (map1 == null)
            return null;
        HashMap<String, AttributeCoercion> map2 = map1.get(ac.getAETitle());
        if (map2 == null)
            return null;
        return map2.get(ac.getSopClass());
    }

    public AttributeCoercion get(String sopClass, AttributeCoercion.DIMSE cmd,
            Role role, String aeTitle) {
        HashMap<String,HashMap<String,AttributeCoercion>> map1 =
                mapOf(cmd, role);
        if (map1 == null)
            return null;

        AttributeCoercion ac = get(map1, aeTitle, sopClass);
        if (ac == null) {
            ac = get(map1, aeTitle, null);
            if (ac == null) {
                ac = get(map1, null, sopClass);
                if (ac == null) {
                    ac = get(map1, null, null);
                }
            }
        }
        return ac;
    }

    public AttributeCoercion remove(String sopClass, AttributeCoercion.DIMSE cmd,
            Role role, String aeTitle) {
        HashMap<String,HashMap<String,AttributeCoercion>> map1 =
                mapOf(cmd, role);
        if (map1 == null)
            return null;
        HashMap<String, AttributeCoercion> map2 = map1.get(aeTitle);
        if (map2 == null)
            return null;
        return map2.remove(sopClass);
    }

    @SuppressWarnings("unchecked")
    private HashMap<String,HashMap<String,AttributeCoercion>> mapOf(
            AttributeCoercion.DIMSE cmd, Role role) {
        return maps[cmd.ordinal() * 2 + role.ordinal()];
    }

    private AttributeCoercion get(HashMap<String, HashMap<String, AttributeCoercion>> map1,
            String aeTitle, String sopClass) {
        HashMap<String, AttributeCoercion> map2 = map1.get(aeTitle);
        return map2 != null ? map2.get(sopClass) : null;
    }

    @SuppressWarnings("unchecked")
    public Collection<AttributeCoercion> getAll() {
        ArrayList<AttributeCoercion> list = new ArrayList<AttributeCoercion>();
        for (HashMap<String, HashMap<String, AttributeCoercion>> map1 : maps)
            if (map1 != null)
                for (HashMap<String, AttributeCoercion> map2 : map1.values())
                    list.addAll(map2.values());
        return list;
    }

    public void clear() {
        Arrays.fill(maps, null);
    }
}
