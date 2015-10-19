//
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

package org.dcm4che3.tool.dcmqrscp;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.media.DicomDirWriter;
import org.dcm4che3.media.RecordFactory;
import org.dcm4che3.media.RecordType;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.AttributesFormat;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CStoreSCPImpl extends BasicCStoreSCP {
    private static final Logger LOG = LoggerFactory.getLogger(CStoreSCPImpl.class);

    private final File storageDir;
    private final DicomDirWriter dicomDirWriter;
    private final RecordFactory recordFactory;
    private final AttributesFormat filePathFormat;

    public CStoreSCPImpl(DicomDirWriter dicomDirWriter, AttributesFormat filePathFormat, RecordFactory recordFactory) {
        super("*");
        this.dicomDirWriter = dicomDirWriter;
        this.storageDir = dicomDirWriter.getFile().getParentFile();
        this.filePathFormat = filePathFormat;
        this.recordFactory = recordFactory;
    }

    @Override
    protected void store(Association as, PresentationContext pc, Attributes rq,
            PDVInputStream data, Attributes rsp) throws IOException {
        String cuid = rq.getString(Tag.AffectedSOPClassUID);
        String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
        String tsuid = pc.getTransferSyntax();
        File file = new File(storageDir, iuid);
        try {
            Attributes fmi = as.createFileMetaInformation(iuid, cuid, tsuid);
            storeTo(as, fmi, data, file);
            Attributes attrs = parse(file);
            File dest = getDestinationFile(attrs);
            renameTo(as, file, dest);
            file = dest;
            if (addDicomDirRecords(as, attrs, fmi, file)) {
                LOG.info("{}: M-UPDATE {}", as, dicomDirWriter.getFile());
            } else {
                LOG.info("{}: ignore received object", as);
                deleteFile(as, file);
            }

        } catch (Exception e) {
            deleteFile(as, file);
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }
    }

    private static void storeTo(Association as, Attributes fmi, PDVInputStream data, File file)
            throws IOException {
        LOG.info("{}: M-WRITE {}", as, file);
        file.getParentFile().mkdirs();
        DicomOutputStream out = new DicomOutputStream(file);
        try {
            out.writeFileMetaInformation(fmi);
            data.copyTo(out);
        } finally {
            SafeClose.close(out);
        }
    }

    private File getDestinationFile(Attributes attrs) {
        File file = new File(storageDir, filePathFormat.format(attrs));
        while (file.exists())
            file = new File(file.getParentFile(), TagUtils.toHexString(new Random().nextInt()));
        return file;
    }

    private static void renameTo(Association as, File from, File dest) throws IOException {
        LOG.info("{}: M-RENAME {}", new Object[] { as, from, dest });
        dest.getParentFile().mkdirs();
        if (!from.renameTo(dest))
            throw new IOException("Failed to rename " + from + " to " + dest);
    }

    private static Attributes parse(File file) throws IOException {
        DicomInputStream in = new DicomInputStream(file);
        try {
            in.setIncludeBulkData(IncludeBulkData.NO);
            return in.readDataset(-1, Tag.PixelData);
        } finally {
            SafeClose.close(in);
        }
    }

    private static void deleteFile(Association as, File file) {
        if (file.delete())
            LOG.info("{}: M-DELETE {}", as, file);
        else
            LOG.warn("{}: M-DELETE {} failed!", as, file);
    }

    protected boolean addDicomDirRecords(Association as, Attributes ds, Attributes fmi, File f)
            throws IOException {
        DicomDirWriter ddWriter = dicomDirWriter;
        RecordFactory recFact = recordFactory;
        String pid = ds.getString(Tag.PatientID, null);
        String styuid = ds.getString(Tag.StudyInstanceUID, null);
        String seruid = ds.getString(Tag.SeriesInstanceUID, null);
        String iuid = fmi.getString(Tag.MediaStorageSOPInstanceUID, null);
        if (pid == null)
            ds.setString(Tag.PatientID, VR.LO, pid = styuid);

        Attributes patRec = ddWriter.findPatientRecord(pid);
        if (patRec == null) {
            patRec = recFact.createRecord(RecordType.PATIENT, null, ds, null, null);
            ddWriter.addRootDirectoryRecord(patRec);
        }
        Attributes studyRec = ddWriter.findStudyRecord(patRec, styuid);
        if (studyRec == null) {
            studyRec = recFact.createRecord(RecordType.STUDY, null, ds, null, null);
            ddWriter.addLowerDirectoryRecord(patRec, studyRec);
        }
        Attributes seriesRec = ddWriter.findSeriesRecord(studyRec, seruid);
        if (seriesRec == null) {
            seriesRec = recFact.createRecord(RecordType.SERIES, null, ds, null, null);
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