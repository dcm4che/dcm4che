package org.dcm4che.media;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;
import org.dcm4che.io.ContentHandlerAdapter;
import org.xml.sax.SAXException;

public class RecordFactory {

    private static final int IN_USE = 0xffff;

    private EnumMap<RecordType, int[]> recordKeys;

    private HashMap<String, RecordType> recordTypes;

    private HashMap<String, String> privateRecordUIDs;

    private HashMap<String, int[]> privateRecordKeys;

    private void lazyLoadDefaultConfiguration() {
        if (recordTypes == null)
            loadDefaultConfiguration();
    }

    public void loadDefaultConfiguration() {
        try {
            loadConfiguration(Thread.currentThread().getContextClassLoader()
                    .getResource("org/dcm4che/media/RecordFactory.xml")
                    .toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void loadConfiguration(String uri) 
            throws ParserConfigurationException, SAXException, IOException {
        Attributes attrs = parseXML(uri);
        Sequence sq = (Sequence) attrs.getValue(Tag.DirectoryRecordSequence);
        if (sq == null)
            throw new IllegalArgumentException(
                    "Missing Directory Record Sequence in " + uri);

        EnumMap<RecordType, int[]> recordKeys =
                new EnumMap<RecordType, int[]>(RecordType.class);
        HashMap<String, RecordType> recordTypes =
                new HashMap<String, RecordType>(134);
        HashMap<String, String> privateRecordUIDs =
                new HashMap<String, String>();
        HashMap<String, int[]> privateRecordKeys =
                new HashMap<String, int[]>();
        for (Attributes item : sq) {
            RecordType type = RecordType.forCode(
                    item.getString(Tag.DirectoryRecordType, null));
            String privuid = type == RecordType.PRIVATE 
                    ? item.getString(Tag.PrivateRecordUID, null)
                    : null;
            String[] cuids = item.getStrings(Tag.ReferencedSOPClassUIDInFile);
            if (cuids != null) {
                if (type != RecordType.PRIVATE) {
                    for (String cuid : cuids) {
                        recordTypes.put(cuid, type);
                    }
                } else if (privuid != null) {
                    for (String cuid : cuids) {
                        privateRecordUIDs.put(cuid, privuid);
                    }
                }
            }
            item.remove(Tag.DirectoryRecordType);
            item.remove(Tag.PrivateRecordUID);
            item.remove(Tag.ReferencedSOPClassUIDInFile);
            int[] keys = item.tags();
            if (privuid != null) {
                privateRecordKeys.put(privuid, keys);
            } else {
                recordKeys.put(type, keys);
            }
        }
        checkRecordTypes(recordTypes);
        this.recordTypes = recordTypes;
        this.recordKeys = recordKeys;
        this.privateRecordUIDs = privateRecordUIDs;
        this.privateRecordKeys = privateRecordKeys;
    }

    private void checkRecordTypes(HashMap<String, RecordType> recordTypes) {
        RecordType[] types = RecordType.values();
        if (recordTypes.size() < types.length)
            for (RecordType type : types)
                if (!recordTypes.containsKey(type))
                    throw new IllegalArgumentException(
                            "Missing Record Type: " + type);
    }

    private Attributes parseXML(String uri)
            throws ParserConfigurationException, SAXException, IOException {
        Attributes attrs = new Attributes();
        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser parser = f.newSAXParser();
        parser.parse(uri, new ContentHandlerAdapter(attrs));
        return attrs;
    }

    public RecordType getRecordType(String cuid) {
        if (cuid == null)
            throw new NullPointerException();
        lazyLoadDefaultConfiguration();
        RecordType recordType = recordTypes.get(cuid);
        return recordType != null ? recordType : RecordType.PRIVATE;
    }

    public RecordType setRecordType(String cuid, RecordType type) {
        if (cuid == null || type == null)
            throw new NullPointerException();
        lazyLoadDefaultConfiguration();
        return recordTypes.put(cuid, type);
    }

    public void setRecordKeys(RecordType type, int[] keys) {
        if (type == null)
            throw new NullPointerException();
        int[] tmp = keys.clone();
        Arrays.sort(tmp);
        lazyLoadDefaultConfiguration();
        recordKeys.put(type, keys);
    }

    public String getPrivateRecordUID(String cuid) {
        if (cuid == null)
            throw new NullPointerException();

        lazyLoadDefaultConfiguration();
        String uid = privateRecordUIDs.get(cuid);
        return uid != null ? uid : cuid;
    }

    public String setPrivateRecordUID(String cuid, String uid) {
        if (cuid == null || uid == null)
            throw new NullPointerException();

        lazyLoadDefaultConfiguration();
        return privateRecordUIDs.put(cuid, uid);
    }

    public int[] setPrivateRecordKeys(String uid, int[] keys) {
        if (uid == null)
            throw new NullPointerException();

        int[] tmp = keys.clone();
        Arrays.sort(tmp);
        lazyLoadDefaultConfiguration();
        return privateRecordKeys.put(uid, tmp);
    }

    public Attributes createRecord(Attributes dataset, Attributes fmi,
            String[] fileIDs) {
        String cuid = fmi.getString(Tag.MediaStorageSOPClassUID, null);
        RecordType type = getRecordType(cuid);
        return createRecord(type,
                type == RecordType.PRIVATE ? getPrivateRecordUID(cuid) : null,
                dataset, fmi, fileIDs);
    }

    public Attributes createRecord(RecordType type, String privRecUID,
                Attributes dataset, Attributes fmi, String[] fileIDs) {
        if (type == null)
            throw new NullPointerException("type");
        if (dataset == null)
            throw new NullPointerException("dataset");

        lazyLoadDefaultConfiguration();
        int[] keys = null;
        if (type == RecordType.PRIVATE) {
            if (privRecUID == null)
                throw new NullPointerException(
                        "privRecUID must not be null for type = PRIVATE");
            keys = privateRecordKeys.get(privRecUID);
        } else {
            if (privRecUID != null)
                throw new IllegalArgumentException(
                        "privRecUID must be null for type != PRIVATE");
        }
        if (keys == null)
            keys = recordKeys.get(type);
        Attributes rec = new Attributes(
                keys.length + (fileIDs != null ? 9 : 5));
        rec.setInt(Tag.OffsetOfTheNextDirectoryRecord, VR.UL, 0);
        rec.setInt(Tag.RecordInUseFlag, VR.US, IN_USE);
        rec.setInt(Tag.OffsetOfReferencedLowerLevelDirectoryEntity, VR.UL, 0);
        rec.setString(Tag.DirectoryRecordType, VR.CS, type.code());
        if (privRecUID != null)
            rec.setString(Tag.PrivateRecordUID, VR.UI, privRecUID);
        if (fileIDs != null) {
            rec.setString(Tag.ReferencedFileID, VR.CS, fileIDs);
            rec.setString(Tag.ReferencedSOPClassUIDInFile, VR.UI,
                    fmi.getString(Tag.MediaStorageSOPClassUID, null));
            rec.setString(Tag.ReferencedSOPInstanceUIDInFile, VR.UI,
                    fmi.getString(Tag.MediaStorageSOPInstanceUID, null));
            rec.setString(Tag.ReferencedTransferSyntaxUIDInFile, VR.UI,
                    fmi.getString(Tag.TransferSyntaxUID, null));
        }
        rec.addSelected(dataset, keys);
        Sequence contentSeq = (Sequence) dataset.getValue(Tag.ContentSequence);
        if (contentSeq != null)
            copyConceptMod(contentSeq, rec);
        return rec ;
    }

    private void copyConceptMod(Sequence srcSeq, Attributes rec) {
        Sequence dstSeq = null;
        for (Attributes item : srcSeq) {
            if ("HAS CONCEPT MOD"
                    .equals(item.getString(Tag.RelationshipType, null))) {
                if (dstSeq == null)
                    dstSeq = rec.newSequence(Tag.ContentSequence, 1);
                dstSeq.add(new Attributes(item, false));
            }
        }
    }
}
