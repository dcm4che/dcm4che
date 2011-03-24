package org.dcm4che.media;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.SpecificCharacterSet;
import org.dcm4che.data.Tag;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.io.RAFInputStreamAdapter;
import org.dcm4che.util.IntHashMap;
import org.dcm4che.util.StringUtils;

public class DicomDirReader {

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
        this.raf = new RandomAccessFile(file, "r");
        try {
            this.in = new DicomInputStream(new RAFInputStreamAdapter(raf));
            this.fmi = in.readFileMetaInformation();
            this.fsInfo = in.readDataset(-1, Tag.DirectoryRecordSequence);
        } catch (IOException e) {
            try { raf.close(); } catch (IOException ignore) {}
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

    public File getFileSetDescriptorFile() {
        return toFile(fsInfo.getStrings(Tag.FileSetDescriptorFileID));
    }

    public File toFile(String[] fileIDs) {
        if (fileIDs == null || fileIDs.length == 0)
            return null;

        return new File(file.getParent(),
                StringUtils.join(fileIDs, File.separatorChar));
    }

    public SpecificCharacterSet
            getSpecificCharacterSetOfFileSetDescriptorFile() {
        return SpecificCharacterSet.valueOf(
                fsInfo.getStrings(
                        Tag.SpecificCharacterSetOfFileSetDescriptorFile));
    }

    public int getFileSetConsistencyFlag() {
        return fsInfo.getInt(Tag.FileSetConsistencyFlag, 0);
    }

    public boolean isEmpty() {
        return fsInfo.getInt(
                Tag.OffsetOfTheFirstDirectoryRecordOfTheRootDirectoryEntity, 0)
                == 0;
    }

    public void clearCache() {
        cache.clear();
    }

    public Attributes readFirstRootDirectoryRecord() throws IOException {
        return readRecord(
                fsInfo.getInt(
                    Tag.OffsetOfTheFirstDirectoryRecordOfTheRootDirectoryEntity,
                    0));
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

    public Attributes findRootDirectoryRecord(Attributes keys,
            boolean ignoreCaseOfPN) throws IOException {
        return findRecord(
                fsInfo.getInt(
                    Tag.OffsetOfTheFirstDirectoryRecordOfTheRootDirectoryEntity,
                    0),
                keys, ignoreCaseOfPN);
    }

    public Attributes findNextDirectoryRecord(Attributes rec, Attributes keys,
            boolean ignoreCaseOfPN) throws IOException {
        return findRecord(
                rec.getInt(Tag.OffsetOfTheNextDirectoryRecord, 0),
                keys, ignoreCaseOfPN);
    }

    public Attributes findLowerDirectoryRecord(Attributes rec, Attributes keys,
            boolean ignoreCaseOfPN) throws IOException {
        return findRecord(
                rec.getInt(Tag.OffsetOfReferencedLowerLevelDirectoryEntity, 0),
                keys, ignoreCaseOfPN);
    }

    private Attributes findRecord(int offset, Attributes keys,
            boolean ignoreCaseOfPN) throws IOException {
        while (offset != 0) {
            Attributes item = readRecord(offset);
            if (item.matches(keys, ignoreCaseOfPN))
                return item;
            offset = item.getInt(Tag.OffsetOfTheNextDirectoryRecord, 0);
        }
        return null;
    }

    private Attributes readRecord(int offset) throws IOException {
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
}
