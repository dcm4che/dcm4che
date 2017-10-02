/*
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  J4Care.
 *  Portions created by the Initial Developer are Copyright (C) 2015-2017
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 */

package org.dcm4che3.conf.api;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Sep 2017
 */
public class ConfigurationChanges {
    public enum ChangeType {C, U, D}
    public static class ModifiedAttribute {
        private final String name;
        private final List<Object> addedValues = new ArrayList<>(1);
        private final List<Object> removedValues = new ArrayList<>(1);

        public ModifiedAttribute(String name) {
            this.name = name;
        }

        public ModifiedAttribute(String name, Object prev, Object val) {
            this.name = name;
            removeValue(prev);
            addValue(val);
        }

        public String name() {
            return name;
        }

        public List<Object> addedValues() {
            return addedValues;
        }

        public List<Object> removedValues() {
            return removedValues;
        }

        public void addValue(Object value) {
            if (value != null && !removedValues.remove(value))
                addedValues.add(value);
        }

        public void removeValue(Object value) {
            if (value != null && !addedValues.remove(value))
                removedValues.add(value);
        }

    }

    public static class ModifiedObject {
        private final String dn;
        private final ChangeType changeType;
        private final List<ModifiedAttribute> attributes = new ArrayList<>();

        public ModifiedObject(String dn, ChangeType changeType) {
            this.dn = dn;
            this.changeType = changeType;
        }

        public String dn() {
            return dn;
        }

        public ChangeType changeType() {
            return changeType;
        }

        public boolean isEmpty() {
            return attributes.isEmpty();
        }

        public List<ModifiedAttribute> modifiedAttributes() {
            return attributes;
        }

        public void add(ModifiedAttribute attribute) {
            this.attributes.add(attribute);
        }
    }

    private final List<ModifiedObject> objects = new ArrayList<>();

    private final boolean verbose ;

    public ConfigurationChanges(boolean verbose) {
        this.verbose = verbose;
    }

    public static <T> T nullifyIfNotVerbose(ConfigurationChanges diffs, T obj) {
        return diffs != null && diffs.isVerbose() ? obj : null;
    }

    public static ModifiedObject addModifiedObjectIfVerbose(ConfigurationChanges diffs, String dn, ChangeType changeType) {
        if (diffs == null || !diffs.isVerbose())
            return null;

        ModifiedObject object = new ModifiedObject(dn, changeType);
        diffs.add(object);
        return object;
    }

    public static ModifiedObject addModifiedObject(ConfigurationChanges diffs, String dn, ChangeType changeType) {
        if (diffs == null)
            return null;

        ModifiedObject object = new ModifiedObject(dn, changeType);
        diffs.add(object);
        return object;
    }

    public static void removeLastIfEmpty(ConfigurationChanges diffs, ModifiedObject obj) {
        if (obj != null && obj.isEmpty())
            diffs.removeLast();
    }

    private void removeLast() {
        objects.remove(objects.size()-1);
    }

    public List<ModifiedObject> modifiedObjects() {
        return objects;
    }

    public void add(ModifiedObject object) {
        objects.add(object);
    }

    public boolean isEmpty() {
        return objects.isEmpty();
    }

    public boolean isVerbose() {
        return verbose;
    }

    @Override
    public String toString() {
        if (isEmpty()) return "[]";

        StringBuilder sb = new StringBuilder(objects.size() * 64);
        for (ModifiedObject obj : objects) {
            sb.append(obj.changeType).append(' ').append(obj.dn).append('\n');
            if (obj.attributes != null) {
                for (ModifiedAttribute attr : obj.attributes) {
                    sb.append("  ").append(attr.name).append(": ")
                            .append(attr.removedValues).append("=>")
                            .append(attr.addedValues).append('\n');
                }
            }
        }
        return sb.toString();
    }
}
