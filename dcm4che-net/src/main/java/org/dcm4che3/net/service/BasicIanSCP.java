/*
 * *** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2015
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */

package org.dcm4che3.net.service;

import java.io.IOException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Commands;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.PresentationContext;

/**
 * Base Service Class Provider (SCP) for the Instance Available Notification (IAN) SOP Class.
 *
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public class BasicIanSCP extends AbstractDicomService {

    public BasicIanSCP() {
        super(UID.InstanceAvailabilityNotificationSOPClass);
    }

    @Override
    public void onDimseRQ(Association as, PresentationContext pc,
                          Dimse dimse, Attributes cmd, Attributes data)
            throws IOException {
        if (dimse != Dimse.N_CREATE_RQ)
            throw new DicomServiceException(Status.UnrecognizedOperation);
        onNCreateRQ(as, pc, cmd, data);
    }

    protected void onNCreateRQ(Association as, PresentationContext pc, Attributes cmd, Attributes data) throws IOException {
        Attributes rsp = Commands.mkNCreateRSP(cmd, getDefaultResponseStatus());
        Attributes rspAttrs = create(as, cmd, data);
        as.tryWriteDimseRSP(pc, rsp, rspAttrs);
    }

    protected int getDefaultResponseStatus() {
        return Status.Success;
    }

    protected Attributes create(Association as, Attributes cmd, Attributes data) throws DicomServiceException {
        return null;
    }

}
