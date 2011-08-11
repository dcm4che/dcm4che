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

package org.dcm4che.net.service;

import java.io.IOException;
import java.util.ArrayList;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.net.Association;
import org.dcm4che.net.Commands;
import org.dcm4che.net.DataWriter;
import org.dcm4che.net.DimseRSPHandler;
import org.dcm4che.net.InputStreamDataWriter;
import org.dcm4che.net.Status;
import org.dcm4che.net.pdu.PresentationContext;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class BasicRetrieveTask implements RetrieveTask {

    protected final Association as;
    protected Association storeas;
    protected final PresentationContext pc;
    protected final Attributes rq;
    protected int priority;
    protected int status = Status.Success;
    protected boolean pendingRSP;
    protected boolean release;
    protected boolean canceled;
    protected int warning;
    protected int completed;
    protected final ArrayList<String> failed = new ArrayList<String>();
    protected final ArrayList<InstanceLocator> insts = new ArrayList<InstanceLocator>();

    public BasicRetrieveTask(Association as, PresentationContext pc, Attributes rq) {
        this.storeas = this.as = as;
        this.pc = pc;
        this.rq = rq;
    }

    @Override
    public boolean isEmpty() {
        return insts.isEmpty();
    }

    @Override
    public void setStoreAssociation(Association dest, boolean release) {
        this.storeas = dest;
        this.release = release;
    }

    public void setSendPendingRSP(boolean pendingRSP) {
        this.pendingRSP = pendingRSP;
    }

    @Override
    public void onCancelRQ(Association as) {
        canceled = true;
    }

    @Override
    public void run() {
        try {
            for (InstanceLocator inst : insts) {
                if (!storeas.isReadyForDataTransfer()) {
                    failed.add(inst.iuid);
                    status = Status.UnableToPerformSubOperations;
                    continue;
                }
                if (canceled) {
                    status = Status.Cancel;
                    break;
                }
                if (pendingRSP)
                    writePendingRSP();
                try {
                    cstore(inst);
                } catch (Exception e) {
                    failed.add(inst.iuid);
                    status = Status.UnableToPerformSubOperations;
                }
            }
            if (storeas.isReadyForDataTransfer()) {
                try {
                    storeas.waitForOutstandingRSP();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (release)
                    try {
                        storeas.release();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
            }
            writeRSP(status);
        } finally {
            close();
        }
    }

    protected void cstore(final InstanceLocator inst) throws IOException, InterruptedException {
        String tsuid = selectTransferSyntaxFor(inst);
        DimseRSPHandler rspHandler = new DimseRSPHandler(as.nextMessageID()) {

            @Override
            public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
                super.onDimseRSP(as, cmd, data);
                BasicRetrieveTask.this.onDimseRSP(cmd, data, inst);
            }
        };

        storeas.cstore(inst.cuid, inst.iuid, priority, createDataWriter(inst, tsuid), tsuid,
                rspHandler);
    }

    protected void onDimseRSP(Attributes cmd, Attributes data, InstanceLocator inst) {
        int storeStatus = cmd.getInt(Tag.Status, -1);
        if (storeStatus == Status.Success)
            completed++;
        else if ((storeStatus & 0xB000) == 0xB000)
            warning++;
        else {
            failed.add(inst.iuid);
            if (status == Status.Success)
                status = Status.OneOrMoreFailures;
        }
    }

    protected String selectTransferSyntaxFor(InstanceLocator inst) {
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
        if (!as.isReadyForDataTransfer())
            return;

        Attributes cmd = Commands.mkRSP(rq, status);
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
        try {
            as.writeDimseRSP(pc, cmd, data);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private int remaining() {
        return insts.size() - completed - warning - failed.size();
    }

    protected void close() {
    }

}
