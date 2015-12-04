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

package org.dcm4che3.tool.ianscp;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Commands;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.AbstractDicomService;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.BasicIanSCP;
import org.dcm4che3.net.service.DicomService;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.dcm4che3.util.SafeClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Service Class Provider (SCP) for the Instance Available Notification (IAN) SOP Class. It listens on a specific TCP/IP
 * port for incoming association requests from a Service Class User (SCU) of the IAN SOP Class. Instance Available
 * Notifications received in N-CREATE requests are stored in DICOM files. It also supports the Verification Service
 * Class as a SCP.
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public class IanSCPTool {

    private static final Logger LOG = LoggerFactory.getLogger(IanSCP.class);

    private final Device device;
    private final ApplicationEntity ae;

    protected final DicomService ianSCP = new BasicIanSCP() {
        @Override
        protected Attributes create(Association as, Attributes cmd, Attributes data) throws DicomServiceException {
            return IanSCPTool.this.create(as, cmd, data);
        }

        @Override
        protected int getDefaultResponseStatus() {
            return IanSCPTool.this.getResponseStatus();
        }
    };

    private File storageDir;

    private int status;

    public IanSCPTool(Device device, ApplicationEntity ae) {
        this.device = device;
        this.ae = ae;
        ae.setAssociationAcceptor(true);
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(new BasicCEchoSCP());
        serviceRegistry.addDicomService(ianSCP);
        ae.setDimseRQHandler(serviceRegistry);
    }

    public void setStorageDirectory(File storageDir) {
        if (storageDir != null)
            storageDir.mkdirs();
        this.storageDir = storageDir;
    }

    public File getStorageDirectory() {
        return storageDir;
    }

    public void setResponseStatus(int status) {
        this.status = status;
    }

    protected int getResponseStatus() {
        return status;
    }

    protected Attributes create(Association as, Attributes rq, Attributes rqAttrs)
            throws DicomServiceException {
        if (storageDir == null)
            return null;
        String cuid = rq.getString(Tag.AffectedSOPClassUID);
        String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
        File file = new File(storageDir, iuid);
        if (file.exists())
            throw new DicomServiceException(Status.DuplicateSOPinstance).
                setUID(Tag.AffectedSOPInstanceUID, iuid);
        DicomOutputStream out = null;
        LOG.info("{}: M-WRITE {}", as, file);
        try {
            out = new DicomOutputStream(file);
            out.writeDataset(
                    Attributes.createFileMetaInformation(iuid, cuid,
                            UID.ExplicitVRLittleEndian),
                    rqAttrs);
            out.close();
            out = null;
        } catch (IOException e) {
            LOG.warn(as + ": Failed to store Instance Available Notification:", e);
            throw new DicomServiceException(Status.ProcessingFailure, e);
        } finally {
            SafeClose.close(out);
        }
        return null;
    }

}
