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
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.io.DicomOutputStream;
import org.dcm4che.io.DicomInputStream.IncludeBulkData;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationStateException;
import org.dcm4che.net.Commands;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDVInputStream;
import org.dcm4che.net.Status;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.util.SafeClose;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class BasicCStoreSCP extends DicomService {

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
        Attributes fmi = createFileMetaInformation(as, rq, pc.getTransferSyntax());
        File spoolFile = getSpoolFile(as, fmi);
        File finalFile = null;
        try {
            MessageDigest digest = getMessageDigest(as);
            spool(as, fmi, data, spoolFile, digest);
            Attributes attrs = parse(as, spoolFile);
            finalFile = getFinalFile(as, fmi, attrs, spoolFile);
            if (!finalFile.equals(spoolFile)) {
                finalFile.getParentFile().mkdirs();
                if (!rename(as, spoolFile, finalFile))
                    throw new DicomServiceException(Status.OutOfResources,
                            "Failed to rename file");
            }
            process(as, fmi, attrs, finalFile, digest, rsp);
        } catch (IOException e) {
            cleanup(as, spoolFile, finalFile);
            throw e;
        }
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

    protected MessageDigest getMessageDigest(Association as) {
        return null;
    }

    protected File getSpoolFile(Association as, Attributes fmi)
            throws IOException {
        return new File(fmi.getString(Tag.MediaStorageSOPInstanceUID));
    }

    protected File getFinalFile(Association as, Attributes fmi, Attributes attrs,
            File spoolFile) {
        return spoolFile;
    }

    private void spool(Association as, Attributes fmi,
            PDVInputStream data, File file, MessageDigest digest)
            throws IOException {
        LOG.info("{}: M-WRITE {}", as, file);
        file.getParentFile().mkdirs();
        FileOutputStream fout = new FileOutputStream(file);
        BufferedOutputStream bout = new BufferedOutputStream(
                digest == null ? fout : new DigestOutputStream(fout, digest));
        DicomOutputStream out = new DicomOutputStream(bout, UID.ExplicitVRLittleEndian);
        out.writeFileMetaInformation(fmi);
        try {
            data.copyTo(out);
        } finally {
            out.close();
        }
    }

    protected Attributes parse(Association as, File file)
            throws DicomServiceException {
        DicomInputStream in = null;
        try {
            in = new DicomInputStream(file);
            in.setIncludeBulkData(IncludeBulkData.NO);
            return in.readDataset(-1, Tag.PixelData);
        } catch (IOException e) {
            LOG.warn(as + ": Failed to decode dataset:", e);
            throw new DicomServiceException(Status.CannotUnderstand);
        } finally {
            SafeClose.close(in);
        }
    }

    protected void process(Association as, Attributes fmi, Attributes attrs,
            File file, MessageDigest digest, Attributes rsp)
            throws DicomServiceException {
    }

    protected void cleanup(Association as, File spoolFile, File finalFile) {
        if (finalFile != null && finalFile.exists())
            delete(as, finalFile);
        else
            delete(as, spoolFile);
    }
}
