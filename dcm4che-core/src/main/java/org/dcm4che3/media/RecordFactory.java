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
 * Java(TM), hosted at https://github.com/dcm4che.
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

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.ContentHandlerAdapter;
import org.dcm4che3.util.ResourceLocator;
import org.xml.sax.SAXException;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
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
            loadConfiguration(ResourceLocator.getResource(
                    "org/dcm4che3/media/RecordFactory.xml", this.getClass()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void loadConfiguration(String uri)
            throws ParserConfigurationException, SAXException, IOException {
        Attributes attrs = parseXML(uri);
        Sequence sq = attrs.getSequence(Tag.DirectoryRecordSequence);
        if (sq == null)
            throw new IllegalArgumentException(
                    "Missing Directory Record Sequence in " + uri);

        EnumMap<RecordType, int[]> recordKeys = new EnumMap<RecordType, int[]>(
                RecordType.class);
        HashMap<String, RecordType> recordTypes = new HashMap<String, RecordType>(
                134);
        HashMap<String, String> privateRecordUIDs = new HashMap<String, String>();
        HashMap<String, int[]> privateRecordKeys = new HashMap<String, int[]>();
        for (Attributes item : sq) {
            RecordType type = RecordType.forCode(item.getString(
                    Tag.DirectoryRecordType, null));
            String privuid = type == RecordType.PRIVATE ? item.getString(
                    Tag.PrivateRecordUID, null) : null;
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
                if (privateRecordKeys.put(privuid, keys) != null)
                    throw new IllegalArgumentException(
                            "Duplicate Private Record UID: " + privuid);
            } else {
                if (recordKeys.put(type, keys) != null)
                    throw new IllegalArgumentException(
                            "Duplicate Record Type: " + type);
            }
        }
        EnumSet<RecordType> missingTypes = EnumSet.allOf(RecordType.class);
        missingTypes.removeAll(recordKeys.keySet());
        if (!missingTypes.isEmpty())
            throw new IllegalArgumentException("Missing Record Types: "
                    + missingTypes);
        this.recordTypes = recordTypes;
        this.recordKeys = recordKeys;
        this.privateRecordUIDs = privateRecordUIDs;
        this.privateRecordKeys = privateRecordKeys;
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

    public int[] getRecordKeys(RecordType type) {
        lazyLoadDefaultConfiguration();
        return recordKeys.get(type);
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
        Attributes rec = new Attributes(keys.length + (fileIDs != null ? 9 : 5));
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
        rec.addSelected(dataset, keys, 0, keys.length);
        Sequence contentSeq = dataset.getSequence(Tag.ContentSequence);
        if (contentSeq != null)
            copyConceptMod(contentSeq, rec);
        return rec;
    }

    private void copyConceptMod(Sequence srcSeq, Attributes rec) {
        Sequence dstSeq = null;
        for (Attributes item : srcSeq) {
            if ("HAS CONCEPT MOD".equals(item.getString(Tag.RelationshipType,
                    null))) {
                if (dstSeq == null)
                    dstSeq = rec.newSequence(Tag.ContentSequence, 1);
                dstSeq.add(new Attributes(item, false));
            }
        }
    }
}
