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


import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Random;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.media.DicomDirWriter;
import org.dcm4che.media.RecordFactory;
import org.dcm4che.media.RecordType;
import org.dcm4che.net.Association;
import org.dcm4che.net.Status;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.net.service.BasicCStoreSCP;
import org.dcm4che.net.service.DicomServiceException;
import org.dcm4che.util.SafeClose;
import org.dcm4che.util.TagUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
class CStoreSCPImpl extends BasicCStoreSCP {

    private final DcmQRSCP main;

    public CStoreSCPImpl(DcmQRSCP main) {
        super("*");
        this.main = main;
    }

    @Override
    protected File createFile(Association as, Attributes rq, Object storage)
            throws DicomServiceException {
        try {
            return File.createTempFile("dcm", ".dcm", main.getStorageDirectory());
        } catch (IOException e) {
            LOG.warn(as + ": Failed to create temp file:", e);
            throw new DicomServiceException(Status.OutOfResources, e);
        }
    }

    @Override
    protected File process(Association as, PresentationContext pc, Attributes rq,
            Attributes rsp, Object storage, File file, MessageDigest digest)
            throws DicomServiceException {
        Attributes fmi, ds;
        DicomInputStream in = null;
        try {
            in = new DicomInputStream(file);
            in.setIncludeBulkData(false);
            fmi = in.readFileMetaInformation();
            ds = in.readDataset(-1, Tag.PixelData);
        } catch (IOException e) {
            LOG.warn(as + ": Failed to decode dataset:", e);
            throw new DicomServiceException(Status.CannotUnderstand);
        } finally {
            SafeClose.close(in);
        }
        File dst = new File(main.getStorageDirectory(), main.getFilePathFormat().format(ds));
        File dir = dst.getParentFile();
        dir.mkdirs();
        while (dst.exists()) {
            dst = new File(dir, TagUtils.toHexString(new Random().nextInt()));
        }
        if (file.renameTo(dst))
            LOG.info("{}: M-RENAME {} to {}", new Object[] {as, file, dst});
        else {
            LOG.warn("{}: Failed to M-RENAME {} to {}", new Object[] {as, file, dst});
            throw new DicomServiceException(Status.OutOfResources, "Failed to rename file");
        }
        try {
            if (addDicomDirRecords(as, ds, fmi, dst)) {
                LOG.info("{}: M-UPDATE {}", as, main.getDicomDirectory());
                return null;
            }
            LOG.info("{}: ignore received object", as);
        } catch (IOException e) {
            LOG.warn(as + ": Failed to M-UPDATE " + main.getDicomDirectory(), e);
            String errorComment = e.getMessage();
            if (errorComment.length() > 64)
                errorComment = errorComment.substring(0, 64);
            rsp.setInt(Tag.Status, VR.US, Status.OutOfResources);
            rsp.setString(Tag.ErrorComment, VR.LO, errorComment);
        }
        return dst;
    }

    private boolean addDicomDirRecords(Association as, Attributes ds, 
            Attributes fmi, File f) throws IOException {
        DicomDirWriter ddWriter = main.getDicomDirWriter();
        RecordFactory recFact = main.getRecordFactory();
        String pid = ds.getString(Tag.PatientID, null);
        String styuid = ds.getString(Tag.StudyInstanceUID, null);
        String seruid = ds.getString(Tag.SeriesInstanceUID, null);
        String iuid = fmi.getString(Tag.MediaStorageSOPInstanceUID, null);
        if (pid == null)
            ds.setString(Tag.PatientID, VR.LO, pid = styuid);

        Attributes patRec = ddWriter.findPatientRecord(pid);
        if (patRec == null) {
            patRec = recFact.createRecord(RecordType.PATIENT, null,
                    ds, null, null);
            ddWriter.addRootDirectoryRecord(patRec);
        }
        Attributes studyRec = ddWriter.findStudyRecord(patRec, styuid);
        if (studyRec == null) {
            studyRec = recFact.createRecord(RecordType.STUDY, null,
                    ds, null, null);
            ddWriter.addLowerDirectoryRecord(patRec, studyRec);
        }
        Attributes seriesRec = ddWriter.findSeriesRecord(studyRec, seruid);
        if (seriesRec == null) {
            seriesRec = recFact.createRecord(RecordType.SERIES, null,
                    ds, null, null);
            ddWriter.addLowerDirectoryRecord(studyRec, seriesRec);
        }
        Attributes instRec = ddWriter.findLowerInstanceRecord(seriesRec, false, iuid);
        if (instRec != null)
            return false;

        instRec = recFact.createRecord(ds, fmi, ddWriter.toFileIDs(f));
        ddWriter.addLowerDirectoryRecord(seriesRec, instRec);
        ddWriter.commit();
        return true;
    }

}

