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

package org.dcm4che3.net;

import java.io.IOException;
import java.util.Objects;

import org.dcm4che3.data.Implementation;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.pdu.AAssociateAC;
import org.dcm4che3.net.pdu.AAssociateRJ;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.pdu.UserIdentityAC;
import org.dcm4che3.util.ByteUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class AssociationHandler {

    private UserIdentityNegotiator userIdNegotiator = new UserIdentityNegotiator(){};

    public UserIdentityNegotiator getUserIdNegotiator() {
        return userIdNegotiator;
    }

    public void setUserIdNegotiator(UserIdentityNegotiator userIdNegotiator) {
        this.userIdNegotiator = Objects.requireNonNull(userIdNegotiator);
    }

    protected AAssociateAC negotiate(Association as, AAssociateRQ rq)
            throws IOException {
        if ((rq.getProtocolVersion() & 1) == 0)
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_PROVIDER_ACSE,
                    AAssociateRJ.REASON_PROTOCOL_VERSION_NOT_SUPPORTED);
        if (!rq.getApplicationContext().equals(
                UID.DICOMApplicationContext))
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_APP_CTX_NAME_NOT_SUPPORTED);
        ApplicationEntity ae = as.getApplicationEntity();
        if (ae == null || !ae.getConnections().contains(as.getConnection())
                || !ae.isInstalled() || !ae.isAssociationAcceptor())
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_CALLED_AET_NOT_RECOGNIZED);
        if (!ae.isAcceptedCallingAETitle(rq.getCallingAET()))
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_CALLING_AET_NOT_RECOGNIZED);
        UserIdentityAC userIdentity = getUserIdNegotiator().negotiate(as, rq.getUserIdentityRQ());
        if (ae.getDevice().isLimitOfAssociationsExceeded(rq))
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_TRANSIENT,
                    AAssociateRJ.SOURCE_SERVICE_PROVIDER_PRES,
                    AAssociateRJ.REASON_LOCAL_LIMIT_EXCEEDED);
        return makeAAssociateAC(as, rq, userIdentity);
    }

    protected AAssociateAC makeAAssociateAC(Association as, AAssociateRQ rq,
            UserIdentityAC userIdentity) throws IOException {
        AAssociateAC ac = new AAssociateAC();
        ac.setImplVersionName(Implementation.getVersionName());
        ac.setCalledAET(rq.getCalledAET());
        ac.setCallingAET(rq.getCallingAET());
        Connection conn = as.getConnection();
        ac.setMaxPDULength(conn.getReceivePDULength());
        ac.setMaxOpsInvoked(Association.minZeroAsMax(rq.getMaxOpsInvoked(),
                conn.getMaxOpsPerformed()));
        ac.setMaxOpsPerformed(Association.minZeroAsMax(rq.getMaxOpsPerformed(),
                conn.getMaxOpsInvoked()));
        ac.setUserIdentityAC(userIdentity);
        ApplicationEntity ae = as.getApplicationEntity();
        for (PresentationContext rqpc : rq.getPresentationContexts())
            ac.addPresentationContext(ae.negotiate(rq, ac, rqpc));
        return ac;
    }

    protected void onClose(Association as) {
        DimseRQHandler tmp = as.getApplicationEntity().getDimseRQHandler();
        if (tmp != null)
            tmp.onClose(as);
    }

}
