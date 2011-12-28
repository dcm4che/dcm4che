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

package org.dcm4che.tool.dcmqrscp;


import java.io.IOException;
import java.security.KeyManagementException;
import java.util.List;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.net.Association;
import org.dcm4che.net.Connection;
import org.dcm4che.net.IncompatibleConnectionException;
import org.dcm4che.net.QueryOption;
import org.dcm4che.net.Status;
import org.dcm4che.net.pdu.ExtendedNegotiation;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.net.service.BasicCMoveSCP;
import org.dcm4che.net.service.BasicRetrieveTask;
import org.dcm4che.net.service.DicomServiceException;
import org.dcm4che.net.service.InstanceLocator;
import org.dcm4che.net.service.QueryRetrieveLevel;
import org.dcm4che.net.service.RetrieveTask;
import org.dcm4che.util.AttributesValidator;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
class CMoveSCPImpl extends BasicCMoveSCP {

    private final Main main;
    private final String[] qrLevels;
    private final QueryRetrieveLevel rootLevel;

    public CMoveSCPImpl(Main main, String sopClass, String... qrLevels) {
        super(main.getDevice(), sopClass);
        this.main = main;
        this.qrLevels = qrLevels;
        this.rootLevel = QueryRetrieveLevel.valueOf(qrLevels[0]);
    }

    @Override
    protected RetrieveTask calculateMatches(Association as, PresentationContext pc,
            final Attributes rq, Attributes keys) throws DicomServiceException {
        AttributesValidator validator = new AttributesValidator(keys);
        QueryRetrieveLevel level = QueryRetrieveLevel.valueOf(validator, qrLevels);
        level.validateRetrieveKeys(validator, rootLevel, relational(as, rq));
        String dest = rq.getString(Tag.MoveDestination);
        final Connection remote = main.getRemoteConnection(dest);
        if (remote == null)
            throw new DicomServiceException(Status.MoveDestinationUnknown,
                    "Move Destination: " + dest + " unknown");
        List<InstanceLocator> matches = main.calculateMatches(rq, keys);
        BasicRetrieveTask retrieveTask = new BasicRetrieveTask(as, pc, rq, matches ) {

            @Override
            protected Association getStoreAssociation() throws DicomServiceException {
                try {
                    return as.getApplicationEntity().connect(
                            as.getConnection(), remote, makeAAssociateRQ());
                } catch (IOException e) {
                    throw new DicomServiceException(Status.UnableToPerformSubOperations, e);
                } catch (InterruptedException e) {
                    throw new DicomServiceException(Status.UnableToPerformSubOperations, e);
                } catch (IncompatibleConnectionException e) {
                    throw new DicomServiceException(Status.UnableToPerformSubOperations, e);
                } catch (KeyManagementException e) {
                    throw new DicomServiceException(Status.UnableToPerformSubOperations, e);
                }
            }

        };
        retrieveTask.setSendPendingRSPInterval(main.getSendPendingCMoveInterval());
        return retrieveTask;
    }

    private boolean relational(Association as, Attributes rq) {
        String cuid = rq.getString(Tag.AffectedSOPClassUID);
        ExtendedNegotiation extNeg = as.getAAssociateAC().getExtNegotiationFor(cuid);
        return QueryOption.toOptions(extNeg).contains(QueryOption.RELATIONAL);
    }
}

