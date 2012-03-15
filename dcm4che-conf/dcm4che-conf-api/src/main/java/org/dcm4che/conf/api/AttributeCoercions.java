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
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;

import org.dcm4che.net.Dimse;
import org.dcm4che.net.TransferCapability.Role;


/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class AttributeCoercions {

    private final EnumMap<Dimse,EnumMap<Role,HashMap<String,HashMap<String,AttributeCoercion>>>> dimseMap =
            new EnumMap<Dimse,EnumMap<Role,HashMap<String,HashMap<String,AttributeCoercion>>>>(Dimse.class);

    public AttributeCoercion add(AttributeCoercion ac) {
        EnumMap<Role, HashMap<String, HashMap<String, AttributeCoercion>>> roleMap =
                dimseMap.get(ac.getDimse());
        if (roleMap == null) {
            roleMap =  new EnumMap<Role,HashMap<String,HashMap<String,AttributeCoercion>>>(Role.class);
            dimseMap.put(ac.getDimse(), roleMap);
        }
        HashMap<String, HashMap<String, AttributeCoercion>> aetMap = roleMap.get(ac.getRole());
        if (aetMap == null) {
            aetMap = new HashMap<String,HashMap<String,AttributeCoercion>>();
            roleMap.put(ac.getRole(), aetMap);
        }
        HashMap<String, AttributeCoercion> cuidMap = aetMap.get(ac.getAETitle());
        if (cuidMap == null) {
            cuidMap = new HashMap<String, AttributeCoercion>();
            aetMap.put(ac.getAETitle(), cuidMap);
        }
        return cuidMap.put(ac.getSopClass(), ac);
    }

    public AttributeCoercion getEquals(AttributeCoercion ac) {
        EnumMap<Role, HashMap<String, HashMap<String, AttributeCoercion>>> roleMap =
                dimseMap.get(ac.getDimse());
        if (roleMap == null)
            return null;
        HashMap<String, HashMap<String, AttributeCoercion>> aetMap = roleMap.get(ac.getRole());
        if (aetMap == null)
            return null;
        HashMap<String, AttributeCoercion> cuidMap = aetMap.get(ac.getAETitle());
        if (cuidMap == null)
            return null;
        return cuidMap.get(ac.getSopClass());
    }

    public AttributeCoercion get(String sopClass, Dimse dimse, Role role, String aeTitle) {
        EnumMap<Role, HashMap<String, HashMap<String, AttributeCoercion>>> roleMap =
                dimseMap.get(dimse);
        if (roleMap == null)
            return null;
        HashMap<String, HashMap<String, AttributeCoercion>> aetMap = roleMap.get(role);
        if (aetMap == null)
            return null;

        AttributeCoercion ac = get(aetMap, aeTitle, sopClass);
        if (ac == null) {
            ac = get(aetMap, aeTitle, null);
            if (ac == null) {
                ac = get(aetMap, null, sopClass);
                if (ac == null) {
                    ac = get(aetMap, null, null);
                }
            }
        }
        return ac;
    }

    private AttributeCoercion get(HashMap<String, HashMap<String, AttributeCoercion>> map1,
            String aeTitle, String sopClass) {
        HashMap<String, AttributeCoercion> map2 = map1.get(aeTitle);
        return map2 != null ? map2.get(sopClass) : null;
    }

    public AttributeCoercion remove(String sopClass, Dimse dimse, Role role, String aeTitle) {
        EnumMap<Role, HashMap<String, HashMap<String, AttributeCoercion>>> roleMap =
                dimseMap.get(dimse);
        if (roleMap == null)
            return null;
        HashMap<String, HashMap<String, AttributeCoercion>> aetMap = roleMap.get(role);
        if (aetMap == null)
            return null;
        HashMap<String, AttributeCoercion> cuidMap = aetMap.get(aeTitle);
        if (cuidMap == null)
            return null;
        return cuidMap.remove(sopClass);
    }

    public Collection<AttributeCoercion> getAll() {
        ArrayList<AttributeCoercion> list = new ArrayList<AttributeCoercion>();
        for (EnumMap<Role, HashMap<String, HashMap<String, AttributeCoercion>>> roleMap : dimseMap.values())
            for (HashMap<String, HashMap<String, AttributeCoercion>> aetMap : roleMap.values())
                for (HashMap<String, AttributeCoercion> cuidMap : aetMap.values())
                    list.addAll(cuidMap.values());
        return list;
    }

    public void clear() {
        dimseMap.clear();
    }
}
