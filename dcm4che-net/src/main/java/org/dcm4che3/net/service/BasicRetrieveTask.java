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

package org.dcm4che3.net.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.AssociationStateException;
import org.dcm4che3.net.Commands;
import org.dcm4che3.net.DataWriter;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.InputStreamDataWriter;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class BasicRetrieveTask implements RetrieveTask {

    protected static final Logger LOG = LoggerFactory.getLogger(BasicRetrieveTask.class);

    public enum Service {
        C_GET {
            @Override
            Attributes mkRSP(Attributes rq, int status) {
                return Commands.mkCGetRSP(rq, status);
            }

            @Override
            void releaseStoreAssociation(Association storeas) throws IOException {
                // NO OP
            }

            @Override
            int commandFieldOfRSP() {
                return 0x8010;
            }
        },
        C_MOVE {
            @Override
            Attributes mkRSP(Attributes rq, int status) {
                return Commands.mkCMoveRSP(rq, status);
            }

            @Override
            void releaseStoreAssociation(Association storeas) throws IOException {
                storeas.release();
            }

            @Override
            int commandFieldOfRSP() {
                return 0x8021;
            }
        };
        abstract Attributes mkRSP(Attributes rq, int status);
        abstract void releaseStoreAssociation(Association storeas) throws IOException;
        abstract int commandFieldOfRSP();
    }

    protected final Service service;
    protected final Association as;
    protected final PresentationContext pc;
    protected final Attributes rq;
    protected int status = Status.Success;
    protected boolean pendingRSP;
    protected int pendingRSPInterval;
    protected boolean canceled;
    protected int warning;
    protected int completed;
    protected final ArrayList<String> failed = new ArrayList<String>();
    protected final List<InstanceLocator> insts;
    protected int outstandingRSP = 0;
    protected Object outstandingRSPLock = new Object();

    private ScheduledFuture<?> writePendingRSP;

    public BasicRetrieveTask(Service service, Association as,
            PresentationContext pc, Attributes rq, List<InstanceLocator> insts) {
        this.service = service;
        this.as = as;
        this.pc = pc;
        this.rq = rq;
        this.insts = insts;
    }

    public void setSendPendingRSP(boolean pendingRSP) {
        this.pendingRSP = pendingRSP;
    }

    public void setSendPendingRSPInterval(int pendingRSPInterval) {
        this.pendingRSPInterval = pendingRSPInterval;
    }

    @Override
    public void onCancelRQ(Association as) {
        canceled = true;
    }

    @Override
    public void run() {
        int msgId = rq.getInt(Tag.MessageID, -1);
        as.addCancelRQHandler(msgId, this);
        try {
            if (!insts.isEmpty()) {
                Association storeas = getStoreAssociation();
                if (pendingRSPInterval > 0)
                    startWritePendingRSP();
                for (InstanceLocator inst : insts) {
                    if (!storeas.isReadyForDataTransfer()) {
                        failed.add(inst.iuid);
                        if (status != Status.UnableToPerformSubOperations) {
                            status = Status.UnableToPerformSubOperations;
                            LOG.warn("{}: Unable to perform sub-operation: association to {} in state: {}",
                                    new Object[]{ as, storeas.getRemoteAET(), storeas.getState()});
                        }
                        continue;
                    }
                    if (canceled) {
                        status = Status.Cancel;
                        break;
                    }
                    if (pendingRSP)
                        writePendingRSP();
                    try {
                        cstore(storeas, inst);
                    } catch (Exception e) {
                        failed.add(inst.iuid);
                        status = Status.UnableToPerformSubOperations;
                        LOG.warn(as + ": Unable to perform sub-operation on association to "
                                + storeas.getRemoteAET(), e);
                    }
                }
                waitForOutstandingCStoreRSP(storeas);
                releaseStoreAssociation(storeas);
                stopWritePendingRSP();
            }
            writeRSP(status);
        } catch (DicomServiceException e) {
            Attributes rsp = e.mkRSP(service.commandFieldOfRSP(), rq.getInt(Tag.MessageID, 0));
            writeRSP(rsp, e.getDataset());
        } finally {
            as.removeCancelRQHandler(msgId);
            close();
        }
    }

    private void startWritePendingRSP() {
        writePendingRSP = as.getApplicationEntity().getDevice().scheduleAtFixedRate(
                new Runnable(){
                    @Override
                    public void run() {
                        BasicRetrieveTask.this.writePendingRSP();
                    }
                },
                0, pendingRSPInterval, TimeUnit.SECONDS);
    }

    private void stopWritePendingRSP() {
        if (writePendingRSP != null)
            writePendingRSP.cancel(false);
    }

    private void waitForOutstandingCStoreRSP(Association storeas) {
        try {
            synchronized (outstandingRSPLock) {
                while (outstandingRSP > 0)
                    outstandingRSPLock.wait();
            }
        } catch (InterruptedException e) {
            LOG.warn(as + ": failed to wait for outstanding RSP on association to "
                    + storeas.getRemoteAET(), e);
        }
    }

    protected Association getStoreAssociation() throws DicomServiceException {
        return as;
    }

    protected AAssociateRQ makeAAssociateRQ() {
        AAssociateRQ aarq = new AAssociateRQ();
        aarq.setCallingAET(as.getLocalAET());
        aarq.setCalledAET(rq.getString(Tag.MoveDestination));
        for (InstanceLocator inst : insts) {
            if (!aarq.containsPresentationContextFor(inst.cuid, inst.tsuid)) {
                aarq.addPresentationContext(
                        new PresentationContext(
                                aarq.getNumberOfPresentationContexts() * 2 + 1,
                                inst.cuid, inst.tsuid));
                String[] DEFAULT_TS = { UID.ExplicitVRLittleEndian, UID.ImplicitVRLittleEndian };
                for (String tsuid : DEFAULT_TS)
                    if (!inst.tsuid.equals(tsuid)
                            && !aarq.containsPresentationContextFor(inst.cuid, tsuid))
                        aarq.addPresentationContext(
                            new PresentationContext(
                                    aarq.getNumberOfPresentationContexts() * 2 + 1,
                                    inst.cuid, tsuid));
            }
        }
        return aarq ;
    }

    protected void releaseStoreAssociation(Association storeas) {
        try {
            service.releaseStoreAssociation(storeas);
        } catch (AssociationStateException e) {
        } catch (IOException e) {
            LOG.warn(as + ": failed to release association to "
                    + storeas.getRemoteAET(), e);
        }
    }

    protected void cstore(Association storeas, InstanceLocator inst)
            throws IOException, InterruptedException {
        String tsuid = selectTransferSyntaxFor(storeas, inst);
        DimseRSPHandler rspHandler = new CStoreRSPHandler(as.nextMessageID(), inst.iuid);

        if (storeas == as)
            storeas.cstore(inst.cuid, inst.iuid, rq.getInt(Tag.Priority, 0),
                            createDataWriter(inst, tsuid), tsuid, rspHandler);
        else
            storeas.cstore(inst.cuid, inst.iuid, rq.getInt(Tag.Priority, 0),
                    as.getRemoteAET(), rq.getInt(Tag.MessageID, 0),
                    createDataWriter(inst, tsuid), tsuid, rspHandler);

        synchronized (outstandingRSPLock) {
            outstandingRSP++;
        }
    }

  private final class CStoreRSPHandler extends DimseRSPHandler {

        private final String iuid;

        public CStoreRSPHandler(int msgId, String iuid) {
            super(msgId);
            this.iuid = iuid;
        }

        @Override
        public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
            super.onDimseRSP(as, cmd, data);
            int storeStatus = cmd.getInt(Tag.Status, -1);
            if (storeStatus == Status.Success)
                completed++;
            else if ((storeStatus & 0xB000) == 0xB000)
                warning++;
            else {
                failed.add(iuid);
                if (status == Status.Success)
                    status = Status.OneOrMoreFailures;
            }
            synchronized (outstandingRSPLock) {
                if (--outstandingRSP == 0)
                    outstandingRSPLock.notify();
            }
        }

        @Override
        public void onClose(Association as) {
            super.onClose(as);
            synchronized (outstandingRSPLock) {
                outstandingRSP = 0;
                outstandingRSPLock.notify();
            }
        }
    }

    protected String selectTransferSyntaxFor(Association storeas, InstanceLocator inst) {
        return inst.tsuid;
    }

    protected DataWriter createDataWriter(InstanceLocator inst, String tsuid) throws IOException {
        DicomInputStream in = new DicomInputStream(inst.getFile());
        in.readFileMetaInformation();
        return new InputStreamDataWriter(in);
    }

    public void writePendingRSP() {
        writeRSP(Status.Pending);
    }

    private void writeRSP(int status) {
        Attributes cmd = service.mkRSP(rq, status);
        if (status == Status.Pending || status == Status.Cancel)
            cmd.setInt(Tag.NumberOfRemainingSuboperations, VR.US, remaining());
        cmd.setInt(Tag.NumberOfCompletedSuboperations, VR.US, completed);
        cmd.setInt(Tag.NumberOfFailedSuboperations, VR.US, failed.size());
        cmd.setInt(Tag.NumberOfWarningSuboperations, VR.US, warning);
        Attributes data = null;
        if (!failed.isEmpty() && status != Status.Pending) {
            data = new Attributes(1);
            data.setString(Tag.FailedSOPInstanceUIDList, VR.UI,
                    failed.toArray(new String[failed.size()]));
        }
        writeRSP(cmd, data);
    }

    private void writeRSP(Attributes cmd, Attributes data) {
        try {
            as.writeDimseRSP(pc, cmd, data);
        } catch (IOException e) {
            pendingRSP = false;
            stopWritePendingRSP();
            LOG.warn(as + ": Unable to send C-GET or C-MOVE RSP on association to "
                    + as.getRemoteAET(), e);
        }
    }

    private int remaining() {
        return insts.size() - completed - warning - failed.size();
    }

    protected void close() {
    }

}
