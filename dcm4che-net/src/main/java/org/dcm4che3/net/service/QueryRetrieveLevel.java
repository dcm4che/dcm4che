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

package org.dcm4che3.net.service;

import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IOD;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.ValidationResult;
import org.dcm4che3.net.Status;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public enum QueryRetrieveLevel {
    PATIENT,
    STUDY,
    SERIES,
    IMAGE,
    FRAME {
        @Override
        protected IOD queryKeysIOD(QueryRetrieveLevel rootLevel,
                boolean relational) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected IOD retrieveKeysIOD(QueryRetrieveLevel rootLevel,
                boolean relational) {
            return IMAGE.retrieveKeysIOD(rootLevel, relational);
        }
    };

    private static final int[] UNIQUE_KEYS = {
            Tag.PatientID,
            Tag.StudyInstanceUID,
            Tag.SeriesInstanceUID,
            Tag.SOPInstanceUID
    };

    private static final VR[] UNIQUE_KEYS_VR = { VR.LO, VR.UI, VR.UI, VR.UI };
    private static final int[] UNIQUE_KEYS_VM = { 1, -1, -1, -1 };

    private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();
    private static final Logger LOG = LoggerFactory.getLogger(QueryRetrieveLevel.class);

    public static QueryRetrieveLevel valueOf(Attributes attrs,
            String[] qrLevels) throws DicomServiceException {
        ValidationResult result = new ValidationResult();
        attrs.validate(new IOD.DataElement(Tag.QueryRetrieveLevel, VR.LO,
                IOD.DataElementType.TYPE_1, 1, 1, 0).setValues(qrLevels),
                result);
        check(result);
        return QueryRetrieveLevel.valueOf(attrs.getString(Tag.QueryRetrieveLevel));
    }

    public void validateQueryKeys(Attributes attrs,
            QueryRetrieveLevel rootLevel, boolean relational)
            throws DicomServiceException {
        check(attrs.validate(queryKeysIOD(rootLevel, relational)));
    }

    public void validateRetrieveKeys(Attributes attrs,
            QueryRetrieveLevel rootLevel, boolean relational)
            throws DicomServiceException {
        check(attrs.validate(retrieveKeysIOD(rootLevel, relational)));
    }

    public static QueryRetrieveLevel validateRetrieveIdentifier(Attributes keys, Set<QueryRetrieveLevel> qrLevels,
            boolean relational, boolean lenient) throws DicomServiceException {
        return validateIdentifier(keys, qrLevels, relational, lenient);
    }

    private static QueryRetrieveLevel validateIdentifier(Attributes keys, Set<QueryRetrieveLevel> qrLevels,
             boolean relational, boolean lenient) throws DicomServiceException {
        String value = keys.getString(Tag.QueryRetrieveLevel);
        if (Objects.isNull(value)) {
            throw missingAttribute(Tag.QueryRetrieveLevel);
        }

        QueryRetrieveLevel level;
        try {
            level = QueryRetrieveLevel.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw invalidAttributeValue(Tag.QueryRetrieveLevel, value);
        }

        if (!qrLevels.contains(level)) {
            throw invalidAttributeValue(Tag.QueryRetrieveLevel, value);
        }

        UniqueKeyCheckFailureCollector collector = new UniqueKeyCheckFailureCollector();
        for (QueryRetrieveLevel it : qrLevels) {
            int keyIndex = Math.min(it.ordinal(), UNIQUE_KEYS.length - 1);
            int key = UNIQUE_KEYS[keyIndex];
            boolean multiple = UNIQUE_KEYS_VM[keyIndex] == -1;

            boolean required = it == level;
            checkUniqueKey(key, keys, !required && relational, !required && lenient, multiple)
                .ifPresent(collector::add);
        }

        if (!collector.isEmpty()) {
            throw new DicomServiceException(Status.IdentifierDoesNotMatchSOPClass,
                    collector.getFailureMessage())
                    .setOffendingElements(collector.getTags());
        }
        return level;
    }

    protected IOD queryKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
        if (compareTo(rootLevel) < 0)
            throw new IllegalArgumentException("rootLevel:" + rootLevel);

        IOD iod = new IOD();
        for (int i = 0; i < rootLevel.ordinal(); i++) {
            iod.add(new IOD.DataElement(UNIQUE_KEYS[i], UNIQUE_KEYS_VR[i],
                    IOD.DataElementType.TYPE_3, 1, 1, 0));
        }
        int ordinal = ordinal();
        IOD.DataElementType type = relational ? IOD.DataElementType.TYPE_3 : IOD.DataElementType.TYPE_1;
        for (int i = rootLevel.ordinal(); i < ordinal; i++) {
            iod.add(new IOD.DataElement(UNIQUE_KEYS[i], UNIQUE_KEYS_VR[i], type, 1, 1, 0));
        }
        for (int i = ordinal + 1; i < UNIQUE_KEYS.length; i++) {
            iod.add(new IOD.DataElement(UNIQUE_KEYS[i], VR.UI, IOD.DataElementType.TYPE_0,  -1, -1, 0));
        }
        return iod;
    }

    protected IOD retrieveKeysIOD(QueryRetrieveLevel rootLevel, boolean relational) {
        if (compareTo(rootLevel) < 0)
            throw new IllegalArgumentException("rootLevel:" + rootLevel);

        IOD iod = new IOD();
        for (int i = 0; i < rootLevel.ordinal(); i++) {
            iod.add(new IOD.DataElement(UNIQUE_KEYS[i], UNIQUE_KEYS_VR[i],
                    IOD.DataElementType.TYPE_0, -1, -1, 0));
        }
        int ordinal = ordinal();
        IOD.DataElementType type = relational ? IOD.DataElementType.TYPE_3 : IOD.DataElementType.TYPE_1;
        for (int i = rootLevel.ordinal(); i < ordinal; i++) {
            iod.add(new IOD.DataElement(UNIQUE_KEYS[i], UNIQUE_KEYS_VR[i], type, 1, 1, 0));
        }
        iod.add(new IOD.DataElement(UNIQUE_KEYS[ordinal], UNIQUE_KEYS_VR[ordinal],
                IOD.DataElementType.TYPE_1, UNIQUE_KEYS_VM[ordinal], UNIQUE_KEYS_VM[ordinal], 0));
        for (int i = ordinal + 1; i < UNIQUE_KEYS.length; i++) {
            iod.add(new IOD.DataElement(UNIQUE_KEYS[i], VR.UI, IOD.DataElementType.TYPE_0, -1, -1, 0));
        }
        return iod;
    }

    private static void check(ValidationResult result) throws DicomServiceException {
        if (!result.isValid())
            throw new DicomServiceException(
                    Status.IdentifierDoesNotMatchSOPClass,
                    result.getErrorComment())
                .setOffendingElements(result.getOffendingElements());
    }

    private static Optional<UniqueKeyCheckFailure> checkUniqueKey(int key, Attributes attributes, boolean optional,
                                                                  boolean lenient, boolean multiple) {
        UniqueKeyCheckFailure failure = null;
        String[] ids = attributes.getStrings(key);
        if (Objects.isNull(ids) || ids.length == 0) {
            if (!optional) {
                if (lenient) {
                    LOG.info("Missing %s %s in Query/Retrieve Identifier");
                } else {
                    failure = new UniqueKeyCheckFailure(UniqueKeyCheckFailure.FailureType.MISSING_ATTRIBUTE, key, null);
                }
            }
        } else if (!multiple && ids.length > 1) {
            failure = new UniqueKeyCheckFailure(UniqueKeyCheckFailure.FailureType.INVALID_ATTRIBUTE, key,
                    StringUtils.concat(ids, '\\'));
        }
        return Optional.ofNullable(failure);
    }

    private static DicomServiceException missingAttribute(int missingTag) {
        final String message = MessageFormat.format("Missing {0} {1}", DICT.keywordOf(missingTag),
                TagUtils.toString(missingTag));
        return identifierDoesNotMatchSOPClass(message, missingTag);
    }

    private static DicomServiceException invalidAttributeValue(int tag, String value) {
        final String message = MessageFormat.format("Invalid {0} {1} - {2}", DICT.keywordOf(tag),
                TagUtils.toString(tag), value);
        return identifierDoesNotMatchSOPClass(message, Tag.QueryRetrieveLevel);
    }

    private static DicomServiceException identifierDoesNotMatchSOPClass(String comment, int tag) {
        return new DicomServiceException(Status.IdentifierDoesNotMatchSOPClass, comment)
                .setOffendingElements(tag);
    }

}