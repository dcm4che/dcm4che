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

package org.dcm4che3.camel.test;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;
import org.dcm4che3.camel.DicomDeviceComponent;
import org.dcm4che3.camel.DicomMessage;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.dicom.CommonDicomConfiguration;
import org.dcm4che3.conf.dicom.DicomConfigurationBuilder;
import org.dcm4che3.net.Commands;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.service.DicomServiceException;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class EchoSCP implements Processor {

    public static void main(String[] args) throws Exception {
        DicomConfiguration dicomConf = DicomConfigurationBuilder.newJsonConfigurationBuilder("config.json").build();
        Device device = dicomConf.findDevice(args[0]);
        Main main = new Main();
        main.bind("dicomDevice", new DicomDeviceComponent(device));
        main.addRouteBuilder(new RouteBuilder(){

            @Override
            public void configure() throws Exception {
                from("dicomDevice:dicom?sopClasses=1.2.840.10008.1.1").bean(EchoSCP.class);
            }});
        main.enableHangupSupport();
        System.out.println("Starting Camel. Use ctrl + c to terminate the JVM.\n");
        main.run();
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        DicomMessage in = exchange.getIn(DicomMessage.class);
        Dimse dimse = in.getHeader("dimse", Dimse.class);
        if (dimse != Dimse.C_ECHO_RQ)
            throw new DicomServiceException(Status.UnrecognizedOperation);
        DicomMessage out = new DicomMessage(
                Dimse.C_ECHO_RSP,
                Commands.mkEchoRSP(in.getCommand(), Status.Success));
        exchange.setOut(out);
    }

}
