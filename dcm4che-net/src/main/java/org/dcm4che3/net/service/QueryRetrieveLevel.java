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
 * Java(TM), hosted at https://github.com/dcm4che/dcm4che.
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

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IOD;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.ValidationResult;
import org.dcm4che3.net.Status;

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
}