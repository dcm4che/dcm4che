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

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public abstract class ElementDictionary {
    private static final ServiceLoader<ElementDictionary> loader =
            ServiceLoader.load(ElementDictionary.class);
    private static final Map<String, ElementDictionary> map = new HashMap<>();
    private final String privateCreator;
    private final Class<?> tagClass;

    protected ElementDictionary(String privateCreator, Class<?> tagClass) {
        this.privateCreator = privateCreator;
        this.tagClass = tagClass;
    }

    public static ElementDictionary getStandardElementDictionary() {
        return StandardElementDictionary.INSTANCE;
    }

    public static ElementDictionary getElementDictionary(String privateCreator) {
        if (privateCreator != null) {
            ElementDictionary dict1 = map.get(privateCreator);
            if (dict1 != null)
                return dict1;
            if (!map.containsKey(privateCreator))
                synchronized (loader) {
                    for (ElementDictionary dict : loader) {
                        map.putIfAbsent(dict.getPrivateCreator(), dict);
                        if (privateCreator.equals(dict.getPrivateCreator()))
                            return dict;
                    }
                    map.put(privateCreator, null);
                }
        }
        return getStandardElementDictionary();
    }

    public static void reload() {
        synchronized (loader) {
            loader.reload();
        }
    }

    public static VR vrOf(int tag, String privateCreator) {
        return getElementDictionary(privateCreator).vrOf(tag);
    }

    public static String keywordOf(int tag, String privateCreator) {
        return getElementDictionary(privateCreator).keywordOf(tag);
    }

    public static int tagForKeyword(String keyword, String privateCreatorID) {
        return getElementDictionary(privateCreatorID).tagForKeyword(keyword);
    }

    public final String getPrivateCreator() {
        return privateCreator;
    }

    public abstract VR vrOf(int tag);

    public abstract String keywordOf(int tag);

    public int tmTagOf(int daTag) {
        return 0;
    }

    public int daTagOf(int tmTag) {
        return 0;
    }

    public int tagForKeyword(String keyword) {
        if (tagClass != null)
            try {
                return tagClass.getField(keyword).getInt(null);
            } catch (Exception ignore) { }
        return -1;
    }
}
