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

package org.dcm4che.net.service;

import org.dcm4che.data.Tag;
import org.dcm4che.net.Status;
import org.dcm4che.util.AttributesValidator;

public enum QueryRetrieveLevel {
    PATIENT {
        @Override
        public void validateQueryKeys(AttributesValidator validator,
                QueryRetrieveLevel rootLevel, boolean relational) throws DicomServiceException {
        }

        @Override
        public void validateRetrieveKeys(AttributesValidator validator,
                QueryRetrieveLevel rootLevel, boolean relational) throws DicomServiceException {
            validator.getType1String(Tag.StudyInstanceUID, 0, Integer.MAX_VALUE);
            check(validator);
        }

    },
    STUDY {
        @Override
        public void validateQueryKeys(AttributesValidator validator,
                QueryRetrieveLevel rootLevel, boolean relational) throws DicomServiceException {
            if (!relational && rootLevel == QueryRetrieveLevel.PATIENT)
                validator.getType1String(Tag.PatientID, 0, 1);
            else
                validator.getType3String(Tag.PatientID, 0, 1, null);
            check(validator);
        }

        @Override
        public void validateRetrieveKeys(AttributesValidator validator,
                QueryRetrieveLevel rootLevel, boolean relational) throws DicomServiceException {
            validator.getType1String(Tag.StudyInstanceUID, 0, Integer.MAX_VALUE);
            validateQueryKeys(validator, rootLevel, relational);
        }
    },
    SERIES {
        @Override
        public void validateQueryKeys(AttributesValidator validator,
                QueryRetrieveLevel rootLevel, boolean relational) throws DicomServiceException {
            if (!relational && rootLevel == QueryRetrieveLevel.PATIENT)
                validator.getType1String(Tag.PatientID, 0, 1);
            else
                validator.getType3String(Tag.PatientID, 0, 1, null);
            if (relational)
                validator.getType3String(Tag.StudyInstanceUID, 0, 1, null);
            else
                validator.getType1String(Tag.StudyInstanceUID, 0, 1);
            check(validator);
        }

        @Override
        public void validateRetrieveKeys(AttributesValidator validator,
                QueryRetrieveLevel rootLevel, boolean relational) throws DicomServiceException {
            validator.getType1String(Tag.SeriesInstanceUID, 0, Integer.MAX_VALUE);
            validateQueryKeys(validator, rootLevel, relational);
        }
    },
    IMAGE {
        @Override
        public void validateQueryKeys(AttributesValidator validator,
            QueryRetrieveLevel rootLevel, boolean relational) throws DicomServiceException {
            if (!relational && rootLevel == QueryRetrieveLevel.PATIENT)
                validator.getType1String(Tag.PatientID, 0, 1);
            else
                validator.getType3String(Tag.PatientID, 0, 1, null);
            if (relational) {
                validator.getType3String(Tag.StudyInstanceUID, 0, 1, null);
                validator.getType3String(Tag.SeriesInstanceUID, 0, 1, null);
            } else {
                validator.getType1String(Tag.StudyInstanceUID, 0, 1);
                validator.getType1String(Tag.SeriesInstanceUID, 0, 1);
            }
            check(validator);
        }

        @Override
        public void validateRetrieveKeys(AttributesValidator validator,
                QueryRetrieveLevel rootLevel, boolean relational) throws DicomServiceException {
            validator.getType1String(Tag.SOPInstanceUID, 0, Integer.MAX_VALUE);
            if (rootLevel != QueryRetrieveLevel.IMAGE)
                validateQueryKeys(validator, rootLevel, relational);
            else
                check(validator);
        }
    },
    FRAME {
        @Override
        public void validateQueryKeys(AttributesValidator validator,
                QueryRetrieveLevel rootLevel, boolean relational) throws DicomServiceException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void validateRetrieveKeys(AttributesValidator validator,
                QueryRetrieveLevel rootLevel, boolean relational) throws DicomServiceException {
            validator.getType1String(Tag.SOPInstanceUID, 0, 1);
            check(validator);
        }
    };

    public static QueryRetrieveLevel valueOf(AttributesValidator validator,
            String[] qrLevels) throws DicomServiceException {
        String qrLevel = validator.getType1String(Tag.QueryRetrieveLevel, 0, 1, qrLevels);
        check(validator);
        return QueryRetrieveLevel.valueOf(qrLevel);
    }

    public abstract void validateQueryKeys(AttributesValidator validator,
            QueryRetrieveLevel rootLevel, boolean relational) throws DicomServiceException;

    public abstract void validateRetrieveKeys(AttributesValidator validator,
            QueryRetrieveLevel rootLevel, boolean relational) throws DicomServiceException;

    private static void check(AttributesValidator validator)
            throws DicomServiceException {
        if (validator.hasOffendingElements())
            throw new DicomServiceException(
                    Status.IdentifierDoesNotMatchSOPClass,
                    validator.getErrorComment())
                .setOffendingElements(validator.getOffendingElements());
    }
}