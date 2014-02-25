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

package org.dcm4che3.net;

import java.io.IOException;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class FutureDimseRSP extends DimseRSPHandler implements DimseRSP {

    private static class Entry {
        final Attributes command;
        final Attributes dataset;
        Entry next;

        public Entry(Attributes command, Attributes dataset) {
            this.command = command;
            this.dataset = dataset;
        }
    }

    private Entry entry = new Entry(null, null);
    private boolean finished;
    private int autoCancel;
    private IOException ex;

    public FutureDimseRSP(int msgID) {
        super(msgID);
    }

    @Override
    public synchronized void onDimseRSP(Association as, Attributes cmd,
            Attributes data) {
        super.onDimseRSP(as, cmd, data);
        Entry last = entry;
        while (last.next != null)
            last = last.next;

        last.next = new Entry(cmd, data);
        if (Status.isPending(cmd.getInt(Tag.Status, 0))) {
            if (autoCancel > 0 && --autoCancel == 0)
                try {
                    super.cancel(as);
                } catch (IOException e) {
                    ex = e;
                }
        } else {
            finished = true;
        }
        notifyAll();
    }

    @Override
    public synchronized void onClose(Association as) {
        super.onClose(as);
        if (!finished) {
            ex = as.getException();
            if (ex == null)
                ex = new IOException("Association to " + as.getRemoteAET()
                            + " released before receive of outstanding DIMSE RSP");
            notifyAll();
        }
    }

    public final void setAutoCancel(int autoCancel) {
        this.autoCancel = autoCancel;
    }

    @Override
    public void cancel(Association a) throws IOException {
        if (ex != null)
            throw ex;
        if (!finished)
            super.cancel(a);
    }

    public final Attributes getCommand() {
        return entry.command;
    }

    public final Attributes getDataset() {
        return entry.dataset;
    }

    public synchronized boolean next() throws IOException, InterruptedException {
        if (entry.next == null) {
            if (finished)
                return false;

            while (entry.next == null && ex == null)
                wait();

            if (ex != null)
                throw ex;
        }
        entry = entry.next;
        return true;
    }
}
