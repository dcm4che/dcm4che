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

package org.dcm4che.net;

import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;

import org.dcm4che.net.pdu.AAssociateAC;
import org.dcm4che.net.pdu.AAssociateRJ;
import org.dcm4che.net.pdu.AAssociateRQ;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Device {

    private static final int DEF_CONN_LIMIT = 100;

    private int connLimit = DEF_CONN_LIMIT;

    private final AtomicInteger connCount = new AtomicInteger(0);
    private final LinkedHashMap<String, ApplicationEntity> aes = 
            new LinkedHashMap<String, ApplicationEntity>();

    public final int getLimitOfOpenConnections() {
        return connLimit;
    }

    public final void setLimitOfOpenConnections(int limit) {
        if (limit <= 0)
            throw new IllegalArgumentException("limit: " + limit);

        this.connLimit = limit;
    }

    public int getNumberOfOpenConnections() {
        return connCount.intValue();
    }

    void incrementNumberOfOpenConnections() {
        connCount.incrementAndGet();
    }

    void decrementNumberOfOpenConnections() {
        connCount.decrementAndGet();
    }

    boolean isLimitOfOpenConnectionsExceeded() {
        return getNumberOfOpenConnections() > connLimit;
    }

    public SSLContext getSSLContext() {
        // TODO Auto-generated method stub
        return null;
    }

    public ApplicationEntity getApplicationEntity(String aet) {
        return aes.get(aet);
    }

    public AAssociateAC negotiate(Association as, AAssociateRQ rq)
            throws AAssociateRJ {
        if (isLimitOfOpenConnectionsExceeded())
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_TRANSIENT,
                    AAssociateRJ.SOURCE_SERVICE_PROVIDER_ACSE,
                    AAssociateRJ.REASON_LOCAL_LIMIT_EXCEEDED);
        ApplicationEntity ae = aes.get(rq.getCalledAET());
        if (ae == null)
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_CALLED_AET_NOT_RECOGNIZED);
        return ae.negotiate(as, rq);
    }

}
