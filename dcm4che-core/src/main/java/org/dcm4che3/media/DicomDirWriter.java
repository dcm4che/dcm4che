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

package org.dcm4che3.media;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.RAFOutputStreamAdapter;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class DicomDirWriter extends DicomDirReader {

    private final static Logger LOG = 
            LoggerFactory.getLogger(DicomDirWriter.class);

    private final static int KNOWN_INCONSISTENCIES = 0xFFFF;
    private final static int NO_KNOWN_INCONSISTENCIES = 0;
    private final static int IN_USE = 0xFFFF;
    private final static int INACTIVE = 0;

    private final byte[] dirInfoHeader = { 
            0x04, 0x00, 0x00, 0x12, 'U', 'L', 4, 0, 0, 0, 0, 0, 
            0x04, 0x00, 0x02, 0x12, 'U', 'L', 4, 0, 0, 0, 0, 0, 
            0x04, 0x00, 0x12, 0x12, 'U', 'S', 2, 0, 0, 0, 
            0x04, 0x00, 0x20, 0x12, 'S', 'Q', 0, 0, 0, 0, 0, 0 };

    private final byte[] dirRecordHeader = { 
            0x04, 0x00, 0x00, 0x14, 'U', 'L', 4, 0, 0, 0, 0, 0, 
            0x04, 0x00, 0x10, 0x14, 'U', 'S', 2, 0, 0, 0, 
            0x04, 0x00, 0x20, 0x14, 'U', 'L', 4, 0, 0, 0, 0, 0 };

    private final DicomOutputStream out;
    private final int firstRecordPos;
    private int nextRecordPos;
    private int rollbackLen = -1;
    private IdentityHashMap<Attributes,Attributes> lastChildRecords =
            new IdentityHashMap<Attributes,Attributes>();
    private final ArrayList<Attributes> dirtyRecords =
            new ArrayList<Attributes>();

    private DicomDirWriter(File file) throws IOException {
        super(file, "rw");
        out = new DicomOutputStream(new RAFOutputStreamAdapter(raf),
                super.getTransferSyntaxUID());
        int seqLen = in.length();
        boolean undefSeqLen = seqLen <= 0;
        setEncodingOptions(
                new DicomEncodingOptions(false, 
                        undefSeqLen,
                        false,
                        undefSeqLen,
                        false));
        this.nextRecordPos = this.firstRecordPos = (int) in.getPosition();
        if (!isEmpty()) {
            if (seqLen > 0)
                this.nextRecordPos += seqLen;
            else
                this.nextRecordPos = (int) (raf.length() - 12); 
        }
        updateDirInfoHeader();
    }

    public DicomEncodingOptions getEncodingOptions() {
        return out.getEncodingOptions();
    }

    public void setEncodingOptions(DicomEncodingOptions encOpts) {
        out.setEncodingOptions(encOpts);
    }

    public static DicomDirWriter open(File file) throws IOException {
        if (!file.isFile())
            throw new FileNotFoundException();

        return new DicomDirWriter(file);
    }

    public static void createEmptyDirectory(File file, String iuid,
            String id, File descFile, String charset) throws IOException {
        Attributes fmi = Attributes.createFileMetaInformation(iuid,
                UID.MediaStorageDirectoryStorage, UID.ExplicitVRLittleEndian);
        createEmptyDirectory(file, fmi, id, descFile, charset);
    }

    public static void createEmptyDirectory(File file, Attributes fmi,
            String id, File descFile, String charset) throws IOException {
        Attributes fsInfo =
                createFileSetInformation(file, id, descFile, charset);
        DicomOutputStream out = new DicomOutputStream(file);
        try {
            out.writeDataset(fmi, fsInfo);
        } finally {
            out.close();
        }
    }

    private static Attributes createFileSetInformation(File file, String id,
            File descFile, String charset) {
        Attributes fsInfo = new Attributes(7);
        fsInfo.setString(Tag.FileSetID, VR.CS, id);
        if (descFile != null) {
            fsInfo.setString(Tag.FileSetDescriptorFileID, VR.CS,
                    toFileIDs(file, descFile));
            if (charset != null && !charset.isEmpty())
                fsInfo.setString(
                        Tag.SpecificCharacterSetOfFileSetDescriptorFile,
                        VR.CS, charset);
        }
        fsInfo.setInt(
                Tag.OffsetOfTheFirstDirectoryRecordOfTheRootDirectoryEntity,
                VR.UL, 0);
        fsInfo.setInt(
                Tag.OffsetOfTheLastDirectoryRecordOfTheRootDirectoryEntity,
                VR.UL, 0);
        fsInfo.setInt(Tag.FileSetConsistencyFlag, VR.US, 0);
        fsInfo.setNull(Tag.DirectoryRecordSequence, VR.SQ);
        return fsInfo;
    }

    public synchronized Attributes addRootDirectoryRecord(Attributes rec)
            throws IOException {
        Attributes lastRootRecord = readLastRootDirectoryRecord();
        if (lastRootRecord == null) {
            writeRecord(firstRecordPos, rec);
            setOffsetOfFirstRootDirectoryRecord(firstRecordPos);
        } else {
            addRecord(Tag.OffsetOfTheNextDirectoryRecord, lastRootRecord, rec);
        }
        setOffsetOfLastRootDirectoryRecord((int) rec.getItemPosition());
        return rec;
    }

    public synchronized Attributes addLowerDirectoryRecord(
            Attributes parentRec, Attributes rec) throws IOException {
        Attributes prevRec = lastChildRecords.get(parentRec);
        if (prevRec == null)
            prevRec = findLastLowerDirectoryRecord(parentRec);

        if (prevRec != null)
            addRecord(Tag.OffsetOfTheNextDirectoryRecord, prevRec, rec);
        else
            addRecord(Tag.OffsetOfReferencedLowerLevelDirectoryEntity,
                    parentRec, rec);

        lastChildRecords.put(parentRec, rec);
        return rec;
    }
 
    public synchronized Attributes findOrAddPatientRecord(Attributes rec) throws IOException {
        Attributes patRec = super.findPatientRecord(rec.getString(Tag.PatientID));
        return patRec != null ? patRec : addRootDirectoryRecord(rec);
    }

    public synchronized Attributes findOrAddStudyRecord(Attributes patRec, Attributes rec)
            throws IOException {
        Attributes studyRec = super.findStudyRecord(patRec, rec.getString(Tag.StudyInstanceUID));
        return studyRec != null ? studyRec : addLowerDirectoryRecord(patRec, rec);
    }

    public synchronized Attributes findOrAddSeriesRecord(Attributes studyRec, Attributes rec)
            throws IOException {
        Attributes seriesRec = super.findSeriesRecord(studyRec, rec.getString(Tag.SeriesInstanceUID));
        return seriesRec != null ? seriesRec : addLowerDirectoryRecord(studyRec, rec);
    }

   public synchronized boolean deleteRecord(Attributes rec)
            throws IOException {
        if (rec.getInt(Tag.RecordInUseFlag, 0) == INACTIVE)
            return false; // already disabled

        for (Attributes lowerRec = readLowerDirectoryRecord(rec);
                lowerRec != null; 
                lowerRec = readNextDirectoryRecord(lowerRec))
            deleteRecord(lowerRec);

        rec.setInt(Tag.RecordInUseFlag, VR.US, INACTIVE);
        markAsDirty(rec);
        return true;
    }

    public synchronized void rollback() throws IOException {
        if (dirtyRecords.isEmpty())
            return;

        clearCache();
        dirtyRecords.clear();
        if (rollbackLen != -1) {
            restoreDirInfo();
            nextRecordPos = rollbackLen;
            if (getEncodingOptions().undefSequenceLength) {
                writeSequenceDelimitationItem();
                raf.setLength(raf.getFilePointer());
            } else {
                raf.setLength(rollbackLen);
            }
            writeFileSetConsistencyFlag(NO_KNOWN_INCONSISTENCIES);
            rollbackLen = -1;
        }
    }

    public void clearCache() {
        lastChildRecords.clear();
        super.clearCache();
    }

    public synchronized void commit() throws IOException {
        if (dirtyRecords.isEmpty())
            return;

        if (rollbackLen == -1)
            writeFileSetConsistencyFlag(KNOWN_INCONSISTENCIES);

        for (Attributes rec : dirtyRecords)
            writeDirRecordHeader(rec);

        dirtyRecords.clear();

        if (rollbackLen != -1 && getEncodingOptions().undefSequenceLength)
            writeSequenceDelimitationItem();

        writeDirInfoHeader();

        rollbackLen = -1;
    }

    @Override
    public void close() throws IOException {
        commit();
        super.close();
    }

    public String[] toFileIDs(File f) {
        return toFileIDs(file, f);
    }

    private static String[] toFileIDs(File dfile, File f) {
        String dfilepath = dfile.getAbsolutePath();
        int dend = dfilepath.lastIndexOf(File.separatorChar) + 1;
        String dpath = dfilepath.substring(0, dend);
        String fpath = f.getAbsolutePath();
        if (dend == 0 || !fpath.startsWith(dpath))
            throw new IllegalArgumentException("file: " + fpath
                    + " not in directory: " + dfile.getAbsoluteFile());
        return StringUtils.split(fpath.substring(dend), File.separatorChar);
    }

    private void updateDirInfoHeader() {
        ByteUtils.intToBytesLE(
                getOffsetOfFirstRootDirectoryRecord(),
                dirInfoHeader, 8);
        ByteUtils.intToBytesLE(
                getOffsetOfLastRootDirectoryRecord(),
                dirInfoHeader, 20);
        ByteUtils.intToBytesLE(
                getEncodingOptions().undefSequenceLength 
                        ? -1 : nextRecordPos - firstRecordPos,
                dirInfoHeader, 42);
    }

    private void restoreDirInfo() {
        setOffsetOfFirstRootDirectoryRecord(
                ByteUtils.bytesToIntLE(dirInfoHeader, 8));
        setOffsetOfLastRootDirectoryRecord(
                ByteUtils.bytesToIntLE(dirInfoHeader, 20));
    }

    private void writeDirInfoHeader() throws IOException {
        updateDirInfoHeader();
        raf.seek(firstRecordPos - dirInfoHeader.length);
        raf.write(dirInfoHeader);
    }

    private void writeDirRecordHeader(Attributes rec) throws IOException {
        ByteUtils.intToBytesLE(
                rec.getInt(Tag.OffsetOfTheNextDirectoryRecord, 0),
                dirRecordHeader, 8);
        ByteUtils.shortToBytesLE(
                rec.getInt(Tag.RecordInUseFlag, 0),
                dirRecordHeader, 20);
        ByteUtils.intToBytesLE(
                rec.getInt(Tag.OffsetOfReferencedLowerLevelDirectoryEntity, 0),
                dirRecordHeader, 30);
        raf.seek(rec.getItemPosition() + 8);
        raf.write(dirRecordHeader);
    }

    private void writeSequenceDelimitationItem() throws IOException {
        raf.seek(nextRecordPos);
        out.writeHeader(Tag.SequenceDelimitationItem, null, 0);
    }

    private void addRecord(int tag, Attributes prevRec, Attributes rec)
            throws IOException {
        prevRec.setInt(tag, VR.UL, nextRecordPos);
        markAsDirty(prevRec);
        writeRecord(nextRecordPos, rec);
    }

    private void writeRecord(int offset, Attributes rec) throws IOException {
        if (LOG.isInfoEnabled())
            LOG.info("M-UPDATE {}: add {} Record", file,
                    rec.getString(Tag.DirectoryRecordType, null));
        LOG.debug("Directory Record:\n{}", rec);
        rec.setItemPosition(offset);
        if (rollbackLen == -1) {
            rollbackLen = offset;
            writeFileSetConsistencyFlag(KNOWN_INCONSISTENCIES);
        }
        raf.seek(offset);
        rec.setInt(Tag.OffsetOfTheNextDirectoryRecord, VR.UL, 0);
        rec.setInt(Tag.RecordInUseFlag, VR.US, IN_USE);
        rec.setInt(Tag.OffsetOfReferencedLowerLevelDirectoryEntity, VR.UL, 0);
        rec.writeItemTo(out);
        nextRecordPos = (int) raf.getFilePointer();
        cache.put(offset, rec);
    }

    private void writeFileSetConsistencyFlag(int flag) throws IOException {
        raf.seek(firstRecordPos - 14);
        raf.writeShort(flag);
        setFileSetConsistencyFlag(flag);
    }

    private static final Comparator<Attributes> offsetComparator =
            new Comparator<Attributes>() {
        public int compare(Attributes item1, Attributes item2) {
            long d = item1.getItemPosition() - item2.getItemPosition();
            return d < 0 ? -1 : d > 0 ? 1 : 0;
        }
    };

    private void markAsDirty(Attributes rec) {
        int index = Collections.binarySearch(dirtyRecords, rec, offsetComparator);
        if (index < 0)
            dirtyRecords.add(-(index + 1), rec);
    }

    public synchronized int purge() throws IOException {
        int[] count = { 0 };
        purge(findFirstRootDirectoryRecordInUse(false), count);
        return count[0];
    }

    private boolean purge(Attributes rec, int[] count) throws IOException {
        boolean purge = true;
        while (rec != null) {
            if (purge(findLowerDirectoryRecordInUse(rec, false), count)
                    && !rec.containsValue(Tag.ReferencedFileID)) {
                deleteRecord(rec);
                count[0]++;
            } else
                purge = false;
            rec = readNextDirectoryRecord(rec);
        }
        return purge;
    }
}
