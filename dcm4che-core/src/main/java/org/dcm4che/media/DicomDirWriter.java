package org.dcm4che.media;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.io.DicomOutputStream;
import org.dcm4che.io.RAFOutputStreamAdapter;
import org.dcm4che.util.StringUtils;

public class DicomDirWriter extends DicomDirReader {

    private final static int IN_USE = 0xFFFF;
    private final static int INACTIVE = 0;

    private final DicomOutputStream out;

    protected DicomDirWriter(File file) throws IOException {
        super(file, "rw");
        out = new DicomOutputStream(new RAFOutputStreamAdapter(raf),
                super.getTransferSyntaxUID());
    }

    public static DicomDirWriter open(File file) throws IOException {
        if (!file.isFile())
            throw new FileNotFoundException();

        return new DicomDirWriter(file);
    }

    public static DicomDirWriter create(File file, String iuid, String id,
            File descFile, String charset) throws IOException {
        Attributes fmi = Attributes.createFileMetaInformation(iuid,
                UID.MediaStorageDirectoryStorage, UID.ExplicitVRLittleEndian);
        return create(file, fmi, id, descFile, charset);
    }

    public static DicomDirWriter create(File file, Attributes fmi, String id,
            File descFile, String charset) throws IOException {
        Attributes fsInfo =
                createFileSetInformation(file, id, descFile, charset);
        DicomOutputStream out = new DicomOutputStream(file);
        try {
            out.writeDataset(fmi, fsInfo);
        } finally {
            out.close();
        }
        return new DicomDirWriter(file);
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

    public String[] toFileIDs(File f) {
        return toFileIDs(file, f);
    }

    private static String[] toFileIDs(File file, File descFile) {
        String dpath = file.getParent();
        int dend = dpath.length();
        String fpath = descFile.getPath();
        if (!fpath.startsWith(dpath)
                || fpath.charAt(dend) != File.separatorChar)
            throw new IllegalArgumentException("file: " + fpath
                    + " not in directory: " + dpath);
        return StringUtils.split(fpath.substring(dend+1), File.separatorChar);
    }
}
