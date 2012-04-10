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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.io.DicomOutputStream;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationStateException;
import org.dcm4che.net.Commands;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDVInputStream;
import org.dcm4che.net.Status;
import org.dcm4che.net.pdu.PresentationContext;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class BasicCStoreSCP extends DicomService {

    public static class FileHolder {
        private File file;

        public FileHolder(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }
    }

    public BasicCStoreSCP(String... sopClasses) {
        super(sopClasses);
    }

    @Override
    public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse,
            Attributes rq, PDVInputStream data) throws IOException {
        if (dimse != Dimse.C_STORE_RQ)
            throw new DicomServiceException(Status.UnrecognizedOperation);

        Attributes rsp = Commands.mkCStoreRSP(rq, Status.Success);
        store(as, pc, rq, data, rsp);
        try {
            as.writeDimseRSP(pc, rsp);
        } catch (AssociationStateException e) {
            LOG.warn("{} << C-STORE-RSP failed: {}", as, e.getMessage());
        }
    }

    protected void store(Association as, PresentationContext pc, Attributes rq,
            PDVInputStream data, Attributes rsp) throws IOException {
        Object storage = selectStorage(as, rq);
        File file = createFile(as, rq, storage);
        LOG.info("{}: M-WRITE {}", as, file);
        FileHolder fileHolder = new FileHolder(file);
        try {
            FileOutputStream fout = new FileOutputStream(file);
            MessageDigest digest = getMessageDigest(as);
            BufferedOutputStream bout = new BufferedOutputStream(
                    digest == null ? fout : new DigestOutputStream(fout, digest));
            DicomOutputStream out = new DicomOutputStream(bout, UID.ExplicitVRLittleEndian);
            out.writeFileMetaInformation(createFileMetaInformation(as, rq, pc.getTransferSyntax()));
            try {
                data.copyTo(out);
            } finally {
                out.close();
            }
            if (process(as, pc, rq, rsp, storage, fileHolder, digest))
                fileHolder.setFile(null);
        } finally {
            deleteFile(as, fileHolder.getFile());
        }
    }

    private void deleteFile(Association as, File file) {
        if (file != null)
            if (file.delete())
                LOG.info("{}: M-DELETE {}", as, file);
            else
                LOG.warn("{}: Failed to M-DELETE {}", as, file);
    }

    protected MessageDigest getMessageDigest(Association as) {
        return null;
    }

    protected Object selectStorage(Association as, Attributes rq) throws IOException {
        return null;
    }

    protected File createFile(Association as, Attributes rq, Object storage)
            throws IOException {
        return new File(rq.getString(Tag.AffectedSOPInstanceUID));
    }

    protected Attributes createFileMetaInformation(Association as, Attributes rq, String tsuid) {
        Attributes fmi = new Attributes(7);
        fmi.setBytes(Tag.FileMetaInformationVersion, VR.OB, new byte[]{ 0, 1 });
        fmi.setString(Tag.MediaStorageSOPClassUID, VR.UI, rq.getString(Tag.AffectedSOPClassUID));
        fmi.setString(Tag.MediaStorageSOPInstanceUID, VR.UI,
                rq.getString(Tag.AffectedSOPInstanceUID));
        fmi.setString(Tag.TransferSyntaxUID, VR.UI, tsuid);
        fmi.setString(Tag.ImplementationClassUID, VR.UI, as.getRemoteImplClassUID());
        String versionName = as.getRemoteImplVersionName();
        if (versionName != null)
            fmi.setString(Tag.ImplementationVersionName, VR.SH, versionName);
        fmi.setString(Tag.SourceApplicationEntityTitle, VR.SH, as.getRemoteAET());
        return fmi;
    }

    protected boolean process(Association as, PresentationContext pc, Attributes rq, Attributes rsp,
            Object storage, FileHolder fileHolder, MessageDigest digest) throws IOException {
        return true;
    }

}
