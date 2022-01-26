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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Commands;
import org.dcm4che3.net.DataWriter;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.InputStreamDataWriter;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.PresentationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class BasicRetrieveTask<T extends InstanceLocator> implements RetrieveTask {

    protected static final Logger LOG = LoggerFactory.getLogger(BasicRetrieveTask.class);

    protected final Dimse rq;
    protected final Association rqas;
    protected final Association storeas;
    protected final PresentationContext pc;
    protected final Attributes rqCmd;
    protected final int msgId;
    protected final int priority;
    protected int status = Status.Success;
    protected boolean pendingRSP;
    protected int pendingRSPInterval;
    protected boolean canceled;
    protected final List<T> insts;
    protected final List<T> completed;
    protected final List<T> warning;
    protected final List<T> failed;
    protected int outstandingRSP = 0;
    protected Object outstandingRSPLock = new Object();

    private ScheduledFuture<?> writePendingRSP;


    public BasicRetrieveTask(Dimse rq, 
            Association rqas,
            PresentationContext pc, 
            Attributes rqCmd,
            List<T> insts,
            Association storeas) {
        this.rq = rq;
        this.rqas = rqas;
        this.storeas = storeas;
        this.pc = pc;
        this.rqCmd = rqCmd;
        this.insts = insts;
        this.msgId = rqCmd.getInt(Tag.MessageID, -1);
        this.priority = rqCmd.getInt(Tag.Priority, 0);
        this.completed = new ArrayList<T>(insts.size());
        this.warning = new ArrayList<T>(insts.size());
        this.failed = new ArrayList<T>(insts.size());
    }

    public void setSendPendingRSP(boolean pendingRSP) {
        this.pendingRSP = pendingRSP;
    }

    public void setSendPendingRSPInterval(int pendingRSPInterval) {
        this.pendingRSPInterval = pendingRSPInterval;
    }

    public boolean isCMove() {
        return rq == Dimse.C_MOVE_RQ;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public int getStatus() {
        return status;
    }

    public Association getRequestAssociation() {
        return rqas;
    }

    public Association getStoreAssociation() {
        return storeas;
    }

    public List<T> getCompleted() {
        return completed;
    }

    public List<T> getWarning() {
        return warning;
    }

    public List<T> getFailed() {
        return failed;
    }

    @Override
    public void onCancelRQ(Association as) {
        canceled = true;
    }

    @Override
    public void run() {
        rqas.addCancelRQHandler(msgId, this);
        try {
            if (pendingRSPInterval > 0)
                startWritePendingRSP();
            for (Iterator<T> iter = insts.iterator(); iter.hasNext();) {
                T inst = iter.next();
                if (canceled) {
                    status = Status.Cancel;
                    break;
                }
                if (pendingRSP)
                    writePendingRSP();
                String tsuid;
                DataWriter dataWriter;
                try {
                    tsuid = selectTransferSyntaxFor(storeas, inst);
                    dataWriter = createDataWriter(inst, tsuid);
                } catch (Exception e) {
                    status = Status.OneOrMoreFailures;
                    LOG.info("{}: Unable to retrieve {}/{} to {}", rqas,
                            UID.nameOf(inst.cuid), UID.nameOf(inst.tsuid),
                            storeas.getRemoteAET(), e);
                    failed.add(inst);
                    continue;
                }
                try {
                    cstore(storeas, inst, tsuid, dataWriter);
                } catch (Exception e) {
                    status = Status.UnableToPerformSubOperations;
                    LOG.warn("{}: Unable to perform sub-operation on association to {}",
                            rqas, storeas.getRemoteAET(), e);
                    failed.add(inst);
                    while (iter.hasNext())
                        failed.add(iter.next());
                }
            }
            waitForOutstandingCStoreRSP(storeas);
            if (isCMove())
                releaseStoreAssociation(storeas);
            stopWritePendingRSP();
            writeRSP(status);
        } finally {
            rqas.removeCancelRQHandler(msgId);
            try {
                close();
            } catch (Throwable e) {
                LOG.warn("Exception thrown by {}.close()",
                        getClass().getName(), e);
            }
        }
    }

    private void startWritePendingRSP() {
        writePendingRSP = rqas.getApplicationEntity().getDevice()
                .scheduleAtFixedRate(
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
            LOG.warn("{}: failed to wait for outstanding RSP on association to {}",
                    rqas, storeas.getRemoteAET(), e);
        }
    }

    protected void releaseStoreAssociation(Association storeas) {
        try {
            if (storeas.isReadyForDataTransfer())
                storeas.release();
        } catch (IOException e) {
            LOG.warn("{}: failed to release association to {}",
                    rqas, storeas.getRemoteAET(), e);
        }
    }

    protected void cstore(Association storeas, T inst, String tsuid, 
            DataWriter dataWriter) throws IOException, InterruptedException {
        DimseRSPHandler rspHandler =
                new CStoreRSPHandler(storeas.nextMessageID(), inst);
        if (isCMove())
            storeas.cstore(inst.cuid, inst.iuid, priority,
                    rqas.getRemoteAET(), msgId,
                    dataWriter, tsuid, rspHandler);
        else
            storeas.cstore(inst.cuid, inst.iuid, priority,
                    dataWriter, tsuid, rspHandler);
        synchronized (outstandingRSPLock) {
            outstandingRSP++;
        }
    }

    private final class CStoreRSPHandler extends DimseRSPHandler {

        private final T inst;

        public CStoreRSPHandler(int msgId, T inst) {
            super(msgId);
            this.inst = inst;
        }

        @Override
        public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
            super.onDimseRSP(as, cmd, data);
            int storeStatus = cmd.getInt(Tag.Status, -1);
            if (storeStatus == Status.Success)
                completed.add(inst);
            else if ((storeStatus & 0xB000) == 0xB000)
                warning.add(inst);
            else {
                failed.add(inst);
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

    protected String selectTransferSyntaxFor(Association storeas, T inst)
            throws Exception {
        return inst.tsuid;
    }

    protected DataWriter createDataWriter(T inst, String tsuid) throws Exception {
        DicomInputStream in = new DicomInputStream(inst.getFile());
        in.readFileMetaInformation();
        return new InputStreamDataWriter(in);
    }

    public void writePendingRSP() {
        writeRSP(Status.Pending);
    }

    private void writeRSP(int status) {
        Attributes cmd = Commands.mkRSP(rqCmd, status, rq);
        if (status == Status.Pending || status == Status.Cancel)
            cmd.setInt(Tag.NumberOfRemainingSuboperations, VR.US, remaining());
        cmd.setInt(Tag.NumberOfCompletedSuboperations, VR.US, completed.size());
        cmd.setInt(Tag.NumberOfFailedSuboperations, VR.US, failed.size());
        cmd.setInt(Tag.NumberOfWarningSuboperations, VR.US, warning.size());
        Attributes data = null;
        if (!failed.isEmpty() && status != Status.Pending) {
            data = new Attributes(1);
            String[] iuids = new String[failed.size()];
            for (int i = 0; i < iuids.length; i++) {
                iuids[i] = failed.get(i).iuid;
            }
            data.setString(Tag.FailedSOPInstanceUIDList, VR.UI, iuids);
        }
        writeRSP(cmd, data);
    }

    private void writeRSP(Attributes cmd, Attributes data) {
        try {
            rqas.writeDimseRSP(pc, cmd, data);
        } catch (IOException e) {
            pendingRSP = false;
            stopWritePendingRSP();
            LOG.warn("{}: Unable to send C-GET or C-MOVE RSP on association to {}",
                    rqas, rqas.getRemoteAET(), e);
        }
    }

    private int remaining() {
        return insts.size() - completed.size() - warning.size() - failed.size();
    }

    protected void close() {
    }

}
