/*
 * *** BEGIN LICENSE BLOCK *****
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
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2015
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
 * *** END LICENSE BLOCK *****
 */

package org.dcm4che3.net.service;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.Status;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Aug 2015
 */
public enum QueryRetrieveLevel2 {
    PATIENT(Tag.PatientID, VR.LO),
    STUDY(Tag.StudyInstanceUID, VR.UI),
    SERIES(Tag.SeriesInstanceUID, VR.UI),
    IMAGE(Tag.SOPInstanceUID, VR.UI);

    private static Logger LOG = LoggerFactory.getLogger(QueryRetrieveLevel2.class);
    private static ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();
    private final int uniqueKey;
    private final VR vrOfUniqueKey;

    QueryRetrieveLevel2(int uniqueKey, VR vrOfUniqueKey) {
        this.uniqueKey = uniqueKey;
        this.vrOfUniqueKey = vrOfUniqueKey;
    }

    public int uniqueKey() {
        return uniqueKey;
    }

    public VR vrOfUniqueKey() {
        return vrOfUniqueKey;
    }

    public static QueryRetrieveLevel2 validateQueryIdentifier(
            Attributes keys, EnumSet<QueryRetrieveLevel2> levels, boolean relational)
            throws DicomServiceException {
        return validateIdentifier(keys, levels, relational, false, true);
    }

    public static QueryRetrieveLevel2 validateQueryIdentifier(
            Attributes keys, EnumSet<QueryRetrieveLevel2> levels, boolean relational, boolean lenient)
            throws DicomServiceException {
        return validateIdentifier(keys, levels, relational, lenient, true);
    }

    public static QueryRetrieveLevel2 validateRetrieveIdentifier(
            Attributes keys, EnumSet<QueryRetrieveLevel2> levels, boolean relational)
            throws DicomServiceException {
        return validateIdentifier(keys, levels, relational, false,false);
    }

    public static QueryRetrieveLevel2 validateRetrieveIdentifier(
            Attributes keys, EnumSet<QueryRetrieveLevel2> levels, boolean relational, boolean lenient)
            throws DicomServiceException {
        return validateIdentifier(keys, levels, relational, lenient,false);
    }

    private static QueryRetrieveLevel2 validateIdentifier(
            Attributes keys, EnumSet<QueryRetrieveLevel2> levels, boolean relational, boolean lenient, boolean query)
            throws DicomServiceException {
        String value = keys.getString(Tag.QueryRetrieveLevel);
        if (value == null)
            throw missingAttribute(Tag.QueryRetrieveLevel);

        QueryRetrieveLevel2 level;
        try {
            level = QueryRetrieveLevel2.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw invalidAttributeValue(Tag.QueryRetrieveLevel, value);
        }
        if (!levels.contains(level))
            throw invalidAttributeValue(Tag.QueryRetrieveLevel, value);

        for (QueryRetrieveLevel2 level2 : levels) {
            if (level2 == level) {
                level.checkUniqueKey(keys, query, false, level != QueryRetrieveLevel2.PATIENT);
                break;
            }
            level2.checkUniqueKey(keys, relational, lenient, false);
        }

        return level;
    }

    private void checkUniqueKey(Attributes keys, boolean optional, boolean lenient, boolean multiple)
            throws DicomServiceException {
        String[] ids = keys.getStrings(uniqueKey);
        if (!multiple && ids != null && ids.length > 1)
            throw invalidAttributeValue(uniqueKey, StringUtils.concat(ids, '\\'));
        if (ids == null || ids.length == 0 || ids[0].indexOf('*') >= 0 || ids[0].indexOf('?') >= 0) {
            if (!optional)
                if (lenient)
                    LOG.info("Missing or wildcard " + DICT.keywordOf(uniqueKey) + " " + TagUtils.toString(uniqueKey)
                            + " in Query/Retrieve Identifier");
                else
                    throw ids == null || ids.length == 0
                            ? missingAttribute(uniqueKey)
                            : invalidAttributeValue(uniqueKey, ids[0]);
        }
    }

    private static DicomServiceException missingAttribute(int tag) {
        return identifierDoesNotMatchSOPClass(
                "Missing " + DICT.keywordOf(tag) + " " + TagUtils.toString(tag), tag);
    }

    private static DicomServiceException invalidAttributeValue(int tag, String value) {
        return identifierDoesNotMatchSOPClass(
                "Invalid " + DICT.keywordOf(tag) + " " + TagUtils.toString(tag) + " - " + value,
                tag);
    }

    private static DicomServiceException identifierDoesNotMatchSOPClass(String comment, int tag) {
        return new DicomServiceException(Status.IdentifierDoesNotMatchSOPClass, comment)
                .setOffendingElements(tag);
    }
}
