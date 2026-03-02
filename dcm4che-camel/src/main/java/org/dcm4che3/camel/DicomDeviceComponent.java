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

 package org.dcm4che3.camel;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.support.DefaultComponent;
import org.apache.camel.util.ObjectHelper;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.DicomServiceRegistry;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class DicomDeviceComponent extends DefaultComponent {

    private Device device;
    private ExecutorService executor;
    private ScheduledExecutorService scheduledExecutor;
    private final DicomServiceRegistry dicomServiceRegistry = new DicomServiceRegistry();

    public DicomDeviceComponent() {
    }

    public DicomDeviceComponent(CamelContext context) {
        super(context);
    }

    public DicomDeviceComponent(Device device) {
        this.device = device;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    @Override
    protected void doInit() throws Exception {
        dicomServiceRegistry.addDicomService(new BasicCEchoSCP());
    }

    public void registerDicomConsumer(DicomConsumer dicomConsumer) {
        dicomServiceRegistry.addDimseRQHandler(dicomConsumer,
                dicomConsumer.getSopClasses());
    }

    public void unregisterDicomConsumer(DicomConsumer dicomConsumer) {
        dicomServiceRegistry.removeDimseRQHandler(dicomConsumer.getSopClasses());
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining,
            Map<String, Object> parameters) throws Exception {
        return new DicomEndpoint(uri, this);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        ObjectHelper.notNull(device, "device");
        device.setDimseRQHandler(dicomServiceRegistry);
        device.setExecutor(getExecutor());
        device.setScheduledExecutor(getScheduledExecutor());
        device.bindConnections();
    }

    private Executor getExecutor() {
        if (executor == null) {
            executor = getCamelContext().getExecutorServiceManager()
                    .newDefaultThreadPool(this, device.getDeviceName());
        }
        return executor;
    }

    private ScheduledExecutorService getScheduledExecutor() {
        if (scheduledExecutor == null) {
            scheduledExecutor = getCamelContext().getExecutorServiceManager()
                    .newDefaultScheduledThreadPool(this, device.getDeviceName());
        }
        return scheduledExecutor;
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        if (device != null)
            device.unbindConnections();
    }

    @Override
    protected void doShutdown() throws Exception {
        super.doShutdown();
        if (scheduledExecutor != null)
            scheduledExecutor.shutdown();
        if (executor != null)
            executor.shutdown();
        executor = null;
        scheduledExecutor = null;
    }

}
