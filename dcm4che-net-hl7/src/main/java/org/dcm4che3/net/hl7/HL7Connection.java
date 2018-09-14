/*
 * **** BEGIN LICENSE BLOCK *****
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
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2015-2018
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
 * **** END LICENSE BLOCK *****
 *
 */

package org.dcm4che3.net.hl7;

import org.dcm4che3.hl7.MLLPConnection;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Aug 2018
 */
public class HL7Connection implements Closeable {
    private final HL7Application hl7Application;
    private final MLLPConnection mllpConnection;
    private final HL7ConnectionMonitor monitor;

    public HL7Connection(HL7Application hl7Application, MLLPConnection mllpConnection) {
        this.hl7Application = hl7Application;
        this.mllpConnection = mllpConnection;
        this.monitor = hl7Application.getDevice()
                .getDeviceExtensionNotNull(HL7DeviceExtension.class)
                .getHL7ConnectionMonitor();
    }

    public void writeMessage(UnparsedHL7Message msg) throws IOException {
        try {
            mllpConnection.writeMessage(msg.data());
            if (monitor != null)
                monitor.onMessageSent(hl7Application, mllpConnection.getSocket(), msg, null);
        } catch (IOException e) {
            monitor.onMessageSent(hl7Application, mllpConnection.getSocket(), msg, e);
            throw e;
        }
    }

    public UnparsedHL7Message readMessage(UnparsedHL7Message msg) throws IOException {
        try {
            byte[] b = mllpConnection.readMessage();
            UnparsedHL7Message rsp = b != null ? new UnparsedHL7Message(b) : null;
            monitor.onMessageResponse(hl7Application, mllpConnection.getSocket(), msg, rsp, null);
            return rsp;
        } catch (IOException e) {
            monitor.onMessageResponse(hl7Application, mllpConnection.getSocket(), msg, null, e);
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        mllpConnection.close();
    }
}
