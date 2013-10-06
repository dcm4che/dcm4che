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

package org.dcm4che.sample.osgi;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.dcm4che.net.DeviceService;
import org.dcm4che.net.DeviceServiceInterface;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleReference;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RESTful Service implementing a Dicom device.
 * 
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * 
 */
@Path("")
public class EchoRSDevice extends DeviceService implements DeviceServiceInterface {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceService.class);
        
    @Resource
    private BundleContext context;
    
    /**
     * start of the service
     * 
     * @see org.dcm4che.net.DeviceService#start()
     */
    @GET
    @Path("start")
    public void start() throws Exception 
    {
        LOG.info("EchoRSDevice:starting Echo Dicom Device..:");
        getDeviceService().start();
    }

    /**
     * stop of the service
     * 
     * @see org.dcm4che.net.DeviceService#start()
     */
    @GET
    @Path("stop")
    public void stop()
    {
        LOG.info("EchoRSDevice:stopping Echo Dicom Device..:");
        getDeviceService().stop();
    }
    
    /**
     * check if service is running
     * 
     * @see org.dcm4che.net.DeviceService#start()
     */
    @GET
    @Path("running")
    public boolean isRunning()
    {
        boolean running = getDeviceService().isRunning();
        LOG.info("EchoRSDevice:is running..:"+running);
        return running;
    }
    
    private DeviceServiceInterface getDeviceService() {
        if (context == null) {
            LOG.warn("BundleContext not injected");
            context = getBundleContextFromClass(DeviceServiceInterface.class);
        }
        ServiceReference sref = context.getServiceReference("org.dcm4che.net.DeviceServiceInterface");
        DeviceServiceInterface device = (DeviceServiceInterface)context.getService(sref);
        return device;
    }

    private BundleContext getBundleContextFromClass(Class<?> clazz) {
        BundleReference bref = (BundleReference) clazz.getClassLoader();
        Bundle bundle = bref.getBundle();
        if (bundle.getState() != Bundle.ACTIVE) {
            try {
                bundle.start();
            } catch (BundleException ex) 
            {
                LOG.error("Cannot start bundle", ex);
            }
        }
        return bundle.getBundleContext();
    }

}
