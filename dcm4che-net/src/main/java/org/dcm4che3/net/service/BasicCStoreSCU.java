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
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 *
 */
public class BasicCStoreSCU<T extends InstanceLocator> {

    protected static final Logger LOG = LoggerFactory
            .getLogger(BasicCStoreSCU.class);

    protected final Association storeas;
    protected int status = Status.Success;
    protected final int priority;
    protected final List<T> insts;
    protected final List<T> completed;
    protected final List<T> warning;
    protected final List<T> failed;
    protected int outstandingRSP = 0;
    protected Object outstandingRSPLock = new Object();

    public BasicCStoreSCU(List<T> insts,
            Association storeas, Integer priority) {
        this.storeas = storeas;
        this.insts = insts;
        this.priority = priority != null ? priority : 0;
        this.completed = new ArrayList<T>(insts.size());
        this.warning = new ArrayList<T>(insts.size());
        this.failed = new ArrayList<T>(insts.size());
    }
    
    public int getStatus() {
        return status;
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

    public BasicCStoreSCUResp store() {
        try {
            for (Iterator<T> iter = insts.iterator(); iter.hasNext();) {
                T inst = iter.next();
                String tsuid;
                DataWriter dataWriter;
                try {
                    tsuid = selectTransferSyntaxFor(storeas, inst);
                    dataWriter = createDataWriter(inst, tsuid);
                } catch (Exception e) {
                    status = Status.OneOrMoreFailures;
                    LOG.info("Unable to store {}/{} to {}",
                            UID.nameOf(inst.cuid), UID.nameOf(inst.tsuid),
                            storeas.getRemoteAET(), e);
                    failed.add(inst);
                    continue;
                }
                try {
                    cstore(storeas, inst, tsuid, dataWriter);
                } catch (Exception e) {
                    status = Status.UnableToPerformSubOperations;
                    LOG.warn(
                            "Unable to perform sub-operation on association to {}",
                            storeas.getRemoteAET(), e);
                    failed.add(inst);
                    while (iter.hasNext())
                        failed.add(iter.next());
                }
            }
            waitForOutstandingCStoreRSP(storeas);
            return makeRSP(status);
        } finally {
            try {
                close();
            } catch (Throwable e) {
                LOG.warn("Exception thrown by {}.close()",
                        getClass().getName(), e);
            }
        }
    }

    private void waitForOutstandingCStoreRSP(Association storeas) {
        try {
            synchronized (outstandingRSPLock) {
                while (outstandingRSP > 0)
                    outstandingRSPLock.wait();
            }
        } catch (InterruptedException e) {
            LOG.warn("Failed to wait for outstanding RSP on association to {}",
                    storeas.getRemoteAET(), e);
        }
    }

    protected void releaseStoreAssociation(Association storeas) {
        try {
            storeas.release();
        } catch (IOException e) {
            LOG.warn("Failed to release association to {}",
                    storeas.getRemoteAET(), e);
        }
    }

    protected int cstore(Association storeas, T inst, String tsuid,
            DataWriter dataWriter) throws IOException, InterruptedException {
        int messageID = storeas.nextMessageID();
        DimseRSPHandler rspHandler = new CStoreRSPHandler(
                messageID, inst);
        storeas.cstore(inst.cuid, inst.iuid, priority, dataWriter, tsuid,
                rspHandler);
        synchronized (outstandingRSPLock) {
            outstandingRSP++;
        }
        return messageID;
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

    protected DataWriter createDataWriter(T inst, String tsuid)
            throws Exception {
        DicomInputStream in = new DicomInputStream(inst.getFile());
        in.readFileMetaInformation();
        return new InputStreamDataWriter(in);
    }

    /**
     * returns an aggregated store rsp
     */
    private BasicCStoreSCUResp makeRSP(int status) {

        BasicCStoreSCUResp rsp = new BasicCStoreSCUResp();
        rsp.setStatus(status);
        rsp.setCompleted(completed.size());
        rsp.setFailed(failed.size());
        rsp.setWarning(warning.size());
        if (!failed.isEmpty()) {
            String[] iuids = new String[failed.size()];
            for (int i = 0; i < iuids.length; i++)
                iuids[i] = failed.get(0).iuid;
            rsp.setFailesUIDs(iuids);
        }
        return rsp;
    }
    
    protected void close() {
    }

}
