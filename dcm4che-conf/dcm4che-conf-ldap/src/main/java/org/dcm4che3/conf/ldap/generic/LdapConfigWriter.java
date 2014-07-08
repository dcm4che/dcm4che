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
package org.dcm4che3.conf.ldap.generic;

import java.lang.reflect.Field;
import java.util.List;

import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;

import org.dcm4che3.conf.ldap.LdapUtils;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Only used by the archive for writing/storediffing the device level attributes ( with disabled flushing since flush is done by the ArchiveConfExension), 
 * children are handled in a separate routine with the GenericExtension
 * @author Roman K
 *
 */
@Deprecated
public class LdapConfigWriter implements ConfigWriter {

    public static final Logger log = LoggerFactory.getLogger(LdapConfigWriter.class);

    private final Attributes attrs;
    private final List<ModificationItem> mods;

    // for writing
    public LdapConfigWriter(Attributes attrs) {
        this.attrs = attrs;
        this.mods = null;
    }

    // for storediffs
    public LdapConfigWriter(List<ModificationItem> mods) {
        this.mods = mods;
        this.attrs = null;
    }
    
    @Override
    public void storeNotDef(String propName, Object value, String def) {

        if (value instanceof Boolean) {
            LdapUtils.storeNotDef(attrs, propName, (Boolean) value, Boolean.parseBoolean(def));
        } else if (value instanceof Integer) {
            LdapUtils.storeNotDef(attrs, propName, (Integer) value, Integer.parseInt(def));
        }
    }

    @Override
    public void storeNotEmpty(String propName, Object value) {

        // workaround for arrays since varargs would perceive them
        // as Object not as Object[]
        if (value != null && value.getClass().isArray())
            LdapUtils.storeNotEmpty(attrs, propName, (Object[]) value);
        else
            log.error("Cannot use storeNotEmpty for a non-array");

    }

    @Override
    public void storeNotNull(String propName, Object value) {

        // workaround for arrays since varargs would perceive them
        // as Object not as Object[]
        if (value != null && value.getClass().isArray())
            LdapUtils.storeNotNull(attrs, propName, (Object[]) value);
        LdapUtils.storeNotNull(attrs, propName, value);

    }

    @Override
    public ConfigWriter getCollectionElementWriter(String keyName, String keyValue, Field field) throws ConfigurationException {
        return NoopConfigWriter.getInstance();
    }

    @Override
    public ConfigWriter createCollectionChild(String propName, Field field) throws ConfigurationException {
        return NoopConfigWriter.getInstance();
    }

    @Override
    public void flushWriter() throws ConfigurationException {
    }

    @Override
    public void storeDiff(String propName, Object prev, Object curr) {
        if (prev != null && curr != null && prev.getClass().isArray() && curr.getClass().isArray())
            LdapUtils.storeDiff(mods, propName, (Object[]) prev, (Object[]) curr);
        else
            LdapUtils.storeDiff(mods, propName, prev, curr);
    }

    @Override
    public void flushDiffs() throws ConfigurationException {
    }

    @Override
    public void removeCollectionElement(String keyName, String keyValue) throws ConfigurationException {
    }

    @Override
    public ConfigWriter getCollectionElementDiffWriter(String keyName, String keyValue) {
        return NoopConfigWriter.getInstance();
    }

    @Override
    public ConfigWriter getChildWriter(String propName, Field field) {
        return NoopConfigWriter.getInstance();
    }

    @Override
    public void removeCurrentNode() {
    }
}