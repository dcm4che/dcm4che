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
 * Portions created by the Initial Developer are Copyright (C) 2014
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
package org.dcm4che3.conf.prefs.generic;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.naming.NamingException;

import org.dcm4che3.conf.api.ConfigurationNotFoundException;
import org.dcm4che3.conf.prefs.PreferencesUtils;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigReader;
import org.dcm4che3.data.Code;

public class PrefsConfigReader implements ConfigReader {
    private final Preferences prefs;

    public PrefsConfigReader(Preferences prefs) {
        this.prefs = prefs;
    }

    @Override
    public String[] asStringArray(String propName) {
        return PreferencesUtils.stringArray(prefs, propName);
    }

    @Override
    public Code[] asCodeArray(String propName) throws ConfigurationException {
        return PreferencesUtils.codeArray(prefs, propName);
    }

    @Override
    public int[] asIntArray(String propName) {
        return PreferencesUtils.intArray(prefs, propName);
    }

    @Override
    public int asInt(String propName, String def) {
        return prefs.getInt(propName, Integer.parseInt(def));
    }

    @Override
    public String asString(String propName, String def) {
        return prefs.get(propName, def);
    }

    @Override
    public boolean asBoolean(String propName, String def) {
        return PreferencesUtils.booleanValue(prefs.get(propName, def));
    }

    @Override
    public Map<String, ConfigReader> readCollection(String keyName) throws ConfigurationException {
        Map<String, ConfigReader> map = new HashMap<String, ConfigReader>();

        try {
            for (String name : prefs.childrenNames()) {
                map.put(name, new PrefsConfigReader(prefs.node(name)));
            }
        } catch (BackingStoreException e) {
            throw new ConfigurationException("Cannot read child configuration nodes from "+prefs.name(),e);
        }

        return map;
    }

    @Override
    public ConfigReader getChildReader(String propName) throws ConfigurationException {
        try {
            if (!prefs.nodeExists(propName)) throw new ConfigurationNotFoundException("Node "+propName+" does not exist for "+prefs.absolutePath()); else
            return new PrefsConfigReader(prefs.node(propName));
        } catch (BackingStoreException e) {
            throw new ConfigurationException("Cannot read child configuration node from "+prefs.absolutePath(),e);
        }
    }
}