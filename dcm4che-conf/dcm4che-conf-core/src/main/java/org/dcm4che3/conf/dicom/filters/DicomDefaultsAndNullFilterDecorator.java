/*
 * **** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
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
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2015
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
 *  ***** END LICENSE BLOCK *****
 */
package org.dcm4che3.conf.dicom.filters;

import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.normalization.DefaultsAndNullFilterDecorator;
import org.dcm4che3.conf.core.api.internal.ConfigIterators;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.hl7.HL7Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Triggers the tree traverse further for deviceExtensions,aeExtensions and hl7AppExtensions when applying/filtering defaults
 */
public class DicomDefaultsAndNullFilterDecorator extends DefaultsAndNullFilterDecorator {
    public static final Logger log = LoggerFactory.getLogger(DicomDefaultsAndNullFilterDecorator.class);

    private List<Class<?>> allExtensionClasses;

    public DicomDefaultsAndNullFilterDecorator(Configuration delegate, List<Class<?>> allExtensionClasses, boolean persistDefaults) {

        super(delegate, persistDefaults);
        this.allExtensionClasses = allExtensionClasses;
    }

    @Override
    protected void traverseTree(Object node, Class nodeClass, EntryFilter filter) throws ConfigurationException {
        super.traverseTree(node, nodeClass, filter);

        // if because of any reason this is not a map (e.g. a reference or a custom adapter for a configurableclass),
        // we don't care about defaults
        if (!(node instanceof Map)) return;

        if (nodeClass.equals(Device.class)) {
            traverseExtensions(node, "deviceExtensions", filter);
        } else if (nodeClass.equals(ApplicationEntity.class)) {
            traverseExtensions(node, "aeExtensions", filter);
        } else if (nodeClass.equals(HL7Application.class)) {
            traverseExtensions(node, "hl7AppExtensions", filter);
        }

    }

    private void traverseExtensions(Object node, String whichExtensions, EntryFilter filter) throws ConfigurationException {

        Map<String, Object> extensions = null;
        try {
            extensions = ((Map<String, Map<String, Object>>) node).get(whichExtensions);
        } catch (ClassCastException e) {
            log.warn("Extensions are stored in a malformed format");
        }

        if (extensions == null) return;

        for (Map.Entry<String, Object> entry : extensions.entrySet()) {
            try {
                traverseTree(entry.getValue(), ConfigIterators.getExtensionClassBySimpleName(entry.getKey(), allExtensionClasses), filter);
            } catch (ClassNotFoundException e) {
                // noop
                log.warn("Extension class {} not found", entry.getKey());
            }
        }

    }
}
