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

package org.dcm4che.media;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.io.RAFInputStreamAdapter;
import org.dcm4che.util.IntHashMap;
import org.dcm4che.util.SafeClose;
import org.dcm4che.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class DicomDirReader implements Closeable {

    protected final File file;
    protected final RandomAccessFile raf;
    protected final DicomInputStream in;
    protected final Attributes fmi;
    protected final Attributes fsInfo;
    protected final IntHashMap<Attributes> cache = new IntHashMap<Attributes>();

    public DicomDirReader(File file) throws IOException {
        this(file, "r");
    }

    protected DicomDirReader(File file, String mode) throws IOException {
        this.file = file;
        this.raf = new RandomAccessFile(file, mode);
        try {
            this.in = new DicomInputStream(new RAFInputStreamAdapter(raf));
            this.fmi = in.readFileMetaInformation();
            this.fsInfo = in.readDataset(-1, Tag.DirectoryRecordSequence);
            if (in.tag() != Tag.DirectoryRecordSequence)
                throw new IOException("Missing Directory Record Sequence");
        } catch (IOException e) {
            SafeClose.close(raf);
            throw e;
        }
    }

    public final File getFile() {
        return file;
    }

    public final Attributes getFileMetaInformation() {
        return fmi;
    }

    public final Attributes getFileSetInformation() {
        return fsInfo;
    }

    public void close() throws IOException {
        raf.close();
    }

    public String getFileSetUID() {
        return fmi.getString(Tag.MediaStorageSOPInstanceUID, null);
    }

    public String getTransferSyntaxUID() {
        return fmi.getString(Tag.TransferSyntaxUID, null);
    }

    public String getFileSetID() {
        return fsInfo.getString(Tag.FileSetID, null);
    }

    public File getDescriptorFile() {
        return toFile(fsInfo.getStrings(Tag.FileSetDescriptorFileID));
    }

    public File toFile(String[] fileIDs) {
        if (fileIDs == null || fileIDs.length == 0)
            return null;

        return new File(file.getParent(),
                StringUtils.join(fileIDs, File.separatorChar));
    }

    public String getDescriptorFileCharacterSet() {
        return fsInfo.getString(
                Tag.SpecificCharacterSetOfFileSetDescriptorFile, null);
    }

    public int getFileSetConsistencyFlag() {
        return fsInfo.getInt(Tag.FileSetConsistencyFlag, 0);
    }

    protected void setFileSetConsistencyFlag(int i) {
        fsInfo.setInt(Tag.FileSetConsistencyFlag, VR.US, i);
    }

    public boolean knownInconsistencies() {
        return getFileSetConsistencyFlag() != 0;
    }

    public int getOffsetOfFirstRootDirectoryRecord() {
        return fsInfo.getInt(
                Tag.OffsetOfTheFirstDirectoryRecordOfTheRootDirectoryEntity, 0);
    }

    protected void setOffsetOfFirstRootDirectoryRecord(int i) {
        fsInfo.setInt(
                Tag.OffsetOfTheFirstDirectoryRecordOfTheRootDirectoryEntity,
                VR.UL, i);
    }

    public int getOffsetOfLastRootDirectoryRecord() {
        return fsInfo.getInt(
                Tag.OffsetOfTheLastDirectoryRecordOfTheRootDirectoryEntity, 0);
    }

    protected void setOffsetOfLastRootDirectoryRecord(int i) {
        fsInfo.setInt(
                Tag.OffsetOfTheLastDirectoryRecordOfTheRootDirectoryEntity,
                VR.UL, i);
    }

    public boolean isEmpty() {
        return getOffsetOfFirstRootDirectoryRecord() == 0;
    }

    public void clearCache() {
        cache.clear();
    }

    public Attributes readFirstRootDirectoryRecord() throws IOException {
        return readRecord(getOffsetOfFirstRootDirectoryRecord());
    }

    public Attributes readLastRootDirectoryRecord() throws IOException {
        return readRecord(getOffsetOfLastRootDirectoryRecord());
    }

    public Attributes readNextDirectoryRecord(Attributes rec)
            throws IOException {
        return readRecord(
                rec.getInt(Tag.OffsetOfTheNextDirectoryRecord, 0));
    }

    public Attributes readLowerDirectoryRecord(Attributes rec)
            throws IOException {
        return readRecord(
                rec.getInt(Tag.OffsetOfReferencedLowerLevelDirectoryEntity, 0));
    }

    protected Attributes findLastLowerDirectoryRecord(Attributes rec)
            throws IOException {
        Attributes lower = readLowerDirectoryRecord(rec);
        if (lower == null)
            return null;

        Attributes next;
        while ((next = readNextDirectoryRecord(lower)) != null)
            lower = next;
        return lower;
    }

    public Attributes findFirstRootDirectoryRecordInUse() throws IOException {
        return findRootDirectoryRecord(null, false, false);
    }

    public Attributes findRootDirectoryRecord(Attributes keys,
            boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return findRecordInUse(
                getOffsetOfFirstRootDirectoryRecord(),
                keys, ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findNextDirectoryRecordInUse(Attributes rec)
            throws IOException {
        return findNextDirectoryRecord(rec, null, false, false);
    }

    public Attributes findNextDirectoryRecord(Attributes rec, Attributes keys,
            boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return findRecordInUse(
                rec.getInt(Tag.OffsetOfTheNextDirectoryRecord, 0),
                keys, ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findLowerDirectoryRecordInUse(Attributes rec)
            throws IOException {
        return findLowerDirectoryRecord(rec, null, false, false);
    }

    public Attributes findLowerDirectoryRecord(Attributes rec, Attributes keys,
            boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        return findRecordInUse(
                rec.getInt(Tag.OffsetOfReferencedLowerLevelDirectoryEntity, 0),
                keys, ignoreCaseOfPN, matchNoValue);
    }

    public Attributes findPatientRecord(String id) throws IOException {
        return findRootDirectoryRecord(
                pk(RecordType.PATIENT, Tag.PatientID, VR.LO, id), false, false);
    }

    public Attributes findStudyRecord(Attributes patRec, String... iuids)
            throws IOException {
        return findLowerDirectoryRecord(patRec,
                pk(RecordType.STUDY, Tag.StudyInstanceUID, VR.UI, iuids),
                false, false);
    }

    public Attributes findNextStudyRecord(Attributes studyRec, String... iuids)
            throws IOException {
        return findNextDirectoryRecord(studyRec,
                pk(RecordType.STUDY, Tag.StudyInstanceUID, VR.UI, iuids),
                false, false);
    }

    public Attributes findSeriesRecord(Attributes studyRec, String... iuids)
            throws IOException {
        return findLowerDirectoryRecord(studyRec, 
                pk(RecordType.SERIES, Tag.SeriesInstanceUID, VR.UI, iuids),
                false, false);
    }

    public Attributes findNextSeriesRecord(Attributes seriesRec, String... iuids)
            throws IOException {
        return findNextDirectoryRecord(seriesRec, 
                pk(RecordType.SERIES, Tag.SeriesInstanceUID, VR.UI, iuids),
                false, false);
    }

    public Attributes findInstanceRecord(Attributes seriesRec, String... iuids)
            throws IOException {
        return findLowerDirectoryRecord(seriesRec, pk(iuids), false, false);
    }

    public Attributes findNextInstanceRecord(Attributes instRec, String... iuids)
            throws IOException {
        return findNextDirectoryRecord(instRec, pk(iuids), false, false);
    }

    public Attributes findInstanceRecord(String... iuids) throws IOException {
        return findRootDirectoryRecord(pk(iuids), false, false);
    }

    private Attributes pk(RecordType type, int tag, VR vr, String... ids) {
        Attributes pk = new Attributes(2);
        pk.setString(Tag.DirectoryRecordType, VR.CS, type.code());
        pk.setString(tag, vr, ids);
        return pk;
    }

    private Attributes pk(String[] iuids) {
        Attributes pk = new Attributes(1);
        pk.setString(Tag.ReferencedSOPInstanceUIDInFile, VR.UI, iuids);
        return pk;
    }

    private Attributes findRecordInUse(int offset, Attributes keys,
            boolean ignoreCaseOfPN, boolean matchNoValue) throws IOException {
        while (offset != 0) {
            Attributes item = readRecord(offset);
            if (inUse(item) && (keys == null
                    || item.matches(keys, ignoreCaseOfPN, matchNoValue)))
                return item;
            offset = item.getInt(Tag.OffsetOfTheNextDirectoryRecord, 0);
        }
        return null;
    }

    private synchronized Attributes readRecord(int offset) throws IOException {
        if (offset == 0)
            return null;

        Attributes item = cache.get(offset);
        if (item == null) {
            long off = offset & 0xffffffffL;
            raf.seek(off);
            in.setPosition(off);
            item = in.readItem();
            cache.put(offset, item);
        }
        return item;
    }

    public static boolean inUse(Attributes rec) {
        return rec.getInt(Tag.RecordInUseFlag, 0) != 0;
    }
}
