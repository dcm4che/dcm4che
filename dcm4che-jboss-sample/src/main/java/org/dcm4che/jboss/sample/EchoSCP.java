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
 * Portions created by the Initial Developer are Copyright (C) 2012
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

package org.dcm4che.jboss.sample;

import java.lang.management.ManagementFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.net.ssl.KeyManager;

import org.dcm4che.conf.api.DicomConfiguration;
import org.dcm4che.net.Device;
import org.dcm4che.net.DeviceService;
import org.dcm4che.net.SSLManagerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@Singleton
@DependsOn("DicomConfiguration")
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class EchoSCP extends DeviceService<Device> implements EchoSCPMBean {

    static final String DEVICE_NAME = "org.dcm4che.jboss.sample.deviceName";
    static final String JMX_NAME = "org.dcm4che.jboss.sample.jmxName";
    static final String KS_TYPE = "org.dcm4che.jboss.sample.keyStoreType";
    static final String KS_URL = "org.dcm4che.jboss.sample.keyStoreURL";
    static final String KS_PASSWORD = "org.dcm4che.jboss.sample.storePassword";
    static final String KEY_PASSWORD = "org.dcm4che.jboss.sample.keyPassword";

    @EJB(name="DicomConfiguration")
    DicomConfiguration dicomConfiguration;

    private ObjectInstance mbean;

    @PostConstruct
    void init() {
        try {
            super.init(dicomConfiguration.findDevice(
                    System.getProperty(DEVICE_NAME, "echoscp")));
            mbean = ManagementFactory.getPlatformMBeanServer()
                    .registerMBean(this, new ObjectName(
                            System.getProperty(JMX_NAME, "dcm4chee:service=echoSCP")));
            start();
        } catch (Exception e) {
            destroy();
            throw new RuntimeException(e);
        }
        
    }

    @PreDestroy
    void destroy() {
        stop();
        if (mbean != null)
            try {
                ManagementFactory.getPlatformMBeanServer()
                    .unregisterMBean(mbean.getObjectName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        mbean = null;
        device = null;
    }

    @Override
    public Device unwrapDevice() {
        return device;
    }

    @Override
    public void reloadConfiguration() throws Exception {
        device.reconfigure(dicomConfiguration.findDevice(device.getDeviceName()));
    }

    protected KeyManager keyManager() throws Exception {
        String url = System.getProperty(KS_URL, "resource:key.jks");
        String kstype = System.getProperty(KS_TYPE, "JKS");
        String kspw = System.getProperty(KS_PASSWORD, "secret");
        String keypw = System.getProperty(KEY_PASSWORD, kspw);
        return SSLManagerFactory.createKeyManager(kstype, url, kspw, keypw);
    }

}
