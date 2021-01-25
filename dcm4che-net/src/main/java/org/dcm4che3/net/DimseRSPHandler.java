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

package org.dcm4che3.net;

import java.io.IOException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.pdu.PresentationContext;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class DimseRSPHandler {

    private final int msgId;
    private PresentationContext pc;
    private volatile Timeout timeout;
    private volatile boolean stopOnPending;
    private volatile boolean canceled;

    public DimseRSPHandler(int msgId) {
        this.msgId = msgId;
    }

    final void setPC(PresentationContext pc) {
        this.pc = pc;
    }

    public final int getMessageID() {
        return msgId;
    }

    final void setTimeout(Timeout timeout, boolean stopOnPending) {
        this.timeout = timeout;
        this.stopOnPending = stopOnPending;
    }

    boolean isStopOnPending() {
        return stopOnPending;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void cancel(Association as) throws IOException {
        as.cancel(pc, msgId);
        canceled = true;
    }

    public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
        if (stopOnPending || !Status.isPending(cmd.getInt(Tag.Status, -1)))
            stopTimeout(as);
    }

    public void onClose(Association as) {
        stopTimeout(as);
    }

    void stopTimeout(Association as) {
        if (timeout != null) {
            timeout.stop();
            timeout = null;
        }
    }
}
