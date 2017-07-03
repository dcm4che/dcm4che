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
 * Java(TM), hosted at https://github.com/dcm4che/dcm4che.
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
import java.util.Observable;
import java.util.Observer;
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
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 */
public class BasicRetrieveTask<T extends InstanceLocator> implements
        RetrieveTask {

    protected static final Logger LOG = LoggerFactory
            .getLogger(BasicRetrieveTask.class);

    protected final List<T> insts;
    protected final Dimse rq;
    protected final Association rqas;
    protected final Association storeas;
    protected final PresentationContext pc;
    protected final Attributes rqCmd;
    protected final int msgId;
    protected final int priority;
    protected boolean pendingRSP;
    protected int pendingRSPInterval;
    protected int outstandingRSP = 0;
    protected Object outstandingRSPLock = new Object();

    private CStoreSCU<T> storescu;
    private ScheduledFuture<?> writePendingRSP;

    public BasicRetrieveTask(Dimse rq, Association rqas,
            PresentationContext pc, Attributes rqCmd, List<T> insts,
            Association storeas, CStoreSCU<T> storescu) {
        this.rq = rq;
        this.rqas = rqas;
        this.storeas = storeas;
        this.pc = pc;
        this.rqCmd = rqCmd;
        this.insts = insts;
        this.msgId = rqCmd.getInt(Tag.MessageID, -1);
        this.priority = rqCmd.getInt(Tag.Priority, 0);
        this.storescu = storescu;
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

    public Association getRequestAssociation() {
        return rqas;
    }

    public Association getStoreAssociation() {
        return storeas;
    }

    @Override
    public void onCancelRQ(Association as) {
        storescu.cancel();
    }

    @Override
    public void run() {
        rqas.addCancelRQHandler(msgId, this);
        ((Observable)storescu).addObserver(this);
        try {
            if (pendingRSPInterval > 0)
                startWritingAsyncRSP();
            storescu.cstore(insts, storeas, priority);
            if (isCMove())
                releaseStoreAssociation(storeas);
            stopWritingAsyncRSP();
            writeRSP(); //last response
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

    protected void releaseStoreAssociation(Association storeas) {
        try {
            storeas.release();
        } catch (IOException e) {
            LOG.warn("{}: failed to release association to {}", rqas,
                    storeas.getRemoteAET(), e);
        }
    }

    private void startWritingAsyncRSP() {
        writePendingRSP = rqas.getApplicationEntity().getDevice()
                .scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        BasicRetrieveTask.this.writeRSP(); // async response
                    }
                }, 0, pendingRSPInterval, TimeUnit.SECONDS);
    }

    private void stopWritingAsyncRSP() {
        if (writePendingRSP != null) {
            writePendingRSP.cancel(false);
        }
    }

    private void writeRSP() {
        try {

            Attributes cmd = Commands.mkRSP(rqCmd, storescu.getStatus(), rq);
            if (storescu.getStatus() == Status.Pending
                    || storescu.getStatus() == Status.Cancel)
                cmd.setInt(Tag.NumberOfRemainingSuboperations, VR.US,
                        storescu.getRemaining());
            cmd.setInt(Tag.NumberOfCompletedSuboperations, VR.US, storescu
                    .getCompleted().size());
            cmd.setInt(Tag.NumberOfFailedSuboperations, VR.US, storescu
                    .getFailed().size());
            cmd.setInt(Tag.NumberOfWarningSuboperations, VR.US, storescu
                    .getWarning().size());
            Attributes data = null;
            if (!storescu.getFailed().isEmpty()
                    && storescu.getStatus() != Status.Pending) {
                data = new Attributes(1);
                String[] iuids = new String[storescu.getFailed().size()];
                for (int i = 0; i < iuids.length; i++) {
                    iuids[i] = storescu.getFailed().get(i).iuid;
                }
                data.setString(Tag.FailedSOPInstanceUIDList, VR.UI, iuids);
            }
            rqas.writeDimseRSP(pc, cmd, data);

        } catch (IOException e) {
            pendingRSP = false;
            stopWritingAsyncRSP();
            LOG.warn(
                    "{}: Unable to send C-GET or C-MOVE RSP on association to {}",
                    rqas, rqas.getRemoteAET(), e);
        }
    }

    protected void close() {
    }

    // notification from cstorescu
    public void update(Observable obj, Object arg) {

        storescu = (CStoreSCU<T>) obj;

        if (pendingRSP && storescu.getStatus() == Status.Pending)
            writeRSP(); // sync response
    }

}
