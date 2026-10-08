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

import java.io.IOException;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.support.DefaultConsumer;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Commands;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.DimseRQHandler;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.DicomServiceException;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class DicomConsumer extends DefaultConsumer implements DimseRQHandler {

    public DicomConsumer(DicomEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }

    @Override
    public DicomEndpoint getEndpoint() {
        return (DicomEndpoint) super.getEndpoint();
    }

    public String[] getSopClasses() {
        return getEndpoint().getSopClasses();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        getEndpoint().getComponent().registerDicomConsumer(this);
    }

    @Override
    protected void doStop() throws Exception {
        getEndpoint().getComponent().unregisterDicomConsumer(this);
        super.doStop();
    }

    @Override
    public void onClose(Association as) {
    }

    @Override
    public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse,
            Attributes cmd, PDVInputStream data) throws IOException {
        final int msgid = cmd.getInt(Tag.MessageID, 0);

        Attributes dicomData = new Attributes();

        if (data != null) {
            dicomData = data.readDataset(pc.getTransferSyntax());
        }

        Exchange exchange = getEndpoint().createExchange();
        DicomMessage dicomMessage = new DicomMessage(exchange, dimse, cmd, dicomData, pc.getTransferSyntax());

        exchange.getIn().setBody(dicomMessage);

        AsyncCallback callback = new EndpointDimseRQHandlerAsyncCallback(as, pc, dimse, msgid, exchange, cmd);
        getAsyncProcessor().process(exchange, callback);
    }

    private final class EndpointDimseRQHandlerAsyncCallback
            implements AsyncCallback {

        private final Association as;
        private final PresentationContext pc;
        private final Dimse dimse;
        private final int msgId;
        private final Exchange exchange;
        private final Attributes cmds;

        public EndpointDimseRQHandlerAsyncCallback(Association as,
                PresentationContext pc, Dimse dimse, int msgId,
                Exchange exchange, Attributes cmds) {
            this.as = as;
            this.pc = pc;
            this.dimse = dimse;
            this.msgId = msgId;
            this.exchange = exchange;
            this.cmds = cmds;
        }

        @Override
        public void done(boolean doneSync) {
            Attributes cmd;
            Attributes data = null;

            if (exchange.getException() != null) {
                Exception ex = exchange.getException();
                DicomServiceException dse = (ex instanceof DicomServiceException) ? (DicomServiceException) ex : new DicomServiceException(Status.ProcessingFailure, ex);
                cmd = dse.mkRSP(dimse.commandFieldOfRSP(), msgId);
                data = dse.getDataset();
            } else {
                cmd = Commands.mkRSP(cmds, Status.Success, dimse);
            }

            as.tryWriteDimseRSP(pc, cmd, data);
        }


    }
}
