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
 * Portions created by the Initial Developer are Copyright (C) 2013
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

package org.dcm4che3.sample.cdi.device.impl;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DeviceService;
import org.dcm4che3.net.service.DicomService;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.dcm4che3.sample.cdi.device.EchoDeviceService;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@ApplicationScoped
public class EchoDeviceServiceImpl extends DeviceService implements EchoDeviceService {

    @Inject
    private Instance<DicomService> services;

    @Inject
    private DicomConfiguration conf;

    private String deviceName = System.getProperty("dcm4che-cdi.deviceName", "echoscp");

    private final DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();

    // force eager initialisation (only works if EAR contains WAR)
    void startup(@Observes @Initialized(ApplicationScoped.class) Object o) {
    }

    @PostConstruct
    public void init() {
        try {
            Device device = conf.findDevice(deviceName);
            init(device);
            for (DicomService service : services) {
                serviceRegistry.addDicomService(service);
            }
            device.setDimseRQHandler(serviceRegistry);
            start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void destroy() {
        if (isRunning())
            stop();

        for (DicomService service : services) {
            serviceRegistry.removeDicomService(service);
        }
    }

    @Override
    public void reload() throws Exception {
        device.reconfigure(conf.findDevice(device.getDeviceName()));
        device.rebindConnections();
    }

}
