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
 *  Portions created by the Initial Developer are Copyright (C) 2014
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
package org.dcm4che3.conf.dicom.adapters;

import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.core.api.internal.AnnotatedConfigurableProperty;
import org.dcm4che3.conf.core.api.internal.BeanVitalizer;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.adapters.DefaultReferenceAdapter;
import org.dcm4che3.conf.core.api.internal.ConfigurationManager;
import org.dcm4che3.conf.core.util.PathPattern;
import org.dcm4che3.conf.dicom.DicomPath;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;

import java.util.Iterator;


public class DicomReferenceHandlerAdapter extends DefaultReferenceAdapter {

    public DicomReferenceHandlerAdapter(BeanVitalizer vitalizer, Configuration config) {
        super(vitalizer, config);
    }

    @Override
    protected Object getReferencedConfigurableObject(String uuid, BeanVitalizer vitalizer, AnnotatedConfigurableProperty property) {


        // if object already started being loaded - return it
        Object instanceFromPool = vitalizer.getInstanceFromThreadLocalPoolByUUID(uuid, property.getRawClass());

        if (instanceFromPool != null) return instanceFromPool;

        // find corresponding corresponding device
        Configuration configuration = vitalizer.getContext(ConfigurationManager.class).getConfigurationStorage();
        Iterator deviceUUIDIterator = configuration.search(DicomPath.DeviceUUIDByAnyUUID.set("UUID", uuid).path());
        String deviceUUID;
        try {
            deviceUUID = (String) deviceUUIDIterator.next();
        } catch (Exception e) {
            throw new ConfigurationException("Cannot find a device that contains an object with UUID " + uuid, e);
        }

        Device device = vitalizer.getInstanceFromThreadLocalPoolByUUID(deviceUUID, Device.class);

        // create instance and put it into the pool
        // at this point we know it's not found in there
        Object instance = vitalizer.newInstance(property.getRawClass());
        vitalizer.registerInstanceInThreadLocalPool(uuid, instance);

        // is this device already there?
        // if yes, then this fresh instance will get populated later when the deserializer gets there (ReflectiveAdapter will find it in the pool)
        // if no, then trigger then load correspondent device - same will happen - instance will be populated with properties
        if (device == null) {
            // call find device
            vitalizer.getContext(DicomConfiguration.class).findDeviceByUUID(deviceUUID);
        }

        return instance;
    }

}
