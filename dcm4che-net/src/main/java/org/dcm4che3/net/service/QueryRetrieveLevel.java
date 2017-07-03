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

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IOD;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.ValidationResult;
import org.dcm4che3.net.Status;

public enum QueryRetrieveLevel {
    PATIENT {
        @Override
        protected IOD queryKeysIOD(QueryRetrieveLevel rootLevel,
                boolean relational) {
            IOD iod = new IOD();
            iod.add(new IOD.DataElement(Tag.StudyInstanceUID, VR.UI,
                            IOD.DataElementType.TYPE_0, -1, -1, 0));
            iod.add(new IOD.DataElement(Tag.SeriesInstanceUID, VR.UI,
                    IOD.DataElementType.TYPE_0, -1, -1, 0));
            iod.add(new IOD.DataElement(Tag.SOPInstanceUID, VR.UI,
                    IOD.DataElementType.TYPE_0, -1, -1, 0));
            return iod;
        }

        @Override
        protected IOD retrieveKeysIOD(QueryRetrieveLevel rootLevel,
                boolean relational) {
            IOD iod = queryKeysIOD(rootLevel, relational);
            iod.add(new IOD.DataElement(Tag.PatientID, VR.LO,
                    IOD.DataElementType.TYPE_1, 1, 1, 0));
            return iod;
        }

    },
    STUDY {
        @Override
        protected IOD queryKeysIOD(QueryRetrieveLevel rootLevel,
                boolean relational) {
            IOD iod = new IOD();
            iod.add(new IOD.DataElement(Tag.PatientID, VR.LO,
                    !relational && rootLevel == QueryRetrieveLevel.PATIENT
                        ? IOD.DataElementType.TYPE_1
                        : IOD.DataElementType.TYPE_3,
                    1, 1, 0));
            iod.add(new IOD.DataElement(Tag.SeriesInstanceUID, VR.UI,
                    IOD.DataElementType.TYPE_0, -1, -1, 0));
            iod.add(new IOD.DataElement(Tag.SOPInstanceUID, VR.UI,
                    IOD.DataElementType.TYPE_0, -1, -1, 0));
            return iod;
        }

        @Override
        protected IOD retrieveKeysIOD(QueryRetrieveLevel rootLevel,
                boolean relational) {
            IOD iod = queryKeysIOD(rootLevel, relational);
            iod.add(new IOD.DataElement(Tag.StudyInstanceUID, VR.UI,
                    IOD.DataElementType.TYPE_1, -1, -1, 0));
            return iod;
        }
    },
    SERIES {
        @Override
        protected IOD queryKeysIOD(QueryRetrieveLevel rootLevel,
                boolean relational) {
            IOD iod = new IOD();
            iod.add(new IOD.DataElement(Tag.PatientID, VR.LO,
                    !relational && rootLevel == QueryRetrieveLevel.PATIENT
                        ? IOD.DataElementType.TYPE_1
                        : IOD.DataElementType.TYPE_3,
                    1, 1, 0));
            iod.add(new IOD.DataElement(Tag.StudyInstanceUID, VR.UI,
                        !relational 
                        ? IOD.DataElementType.TYPE_1
                        : IOD.DataElementType.TYPE_3,
                    1, 1, 0));
            iod.add(new IOD.DataElement(Tag.SOPInstanceUID, VR.UI,
                    IOD.DataElementType.TYPE_0, -1, -1, 0));
            return iod;
        }

        @Override
        protected IOD retrieveKeysIOD(QueryRetrieveLevel rootLevel,
                boolean relational) {
            IOD iod = queryKeysIOD(rootLevel, relational);
            iod.add(new IOD.DataElement(Tag.SeriesInstanceUID, VR.UI,
                    IOD.DataElementType.TYPE_1, -1, -1, 0));
            return iod;
        }
    },
    IMAGE {
        @Override
        protected IOD queryKeysIOD(QueryRetrieveLevel rootLevel,
                boolean relational) {
            IOD iod = new IOD();
            iod.add(new IOD.DataElement(Tag.PatientID, VR.LO,
                    !relational && rootLevel == QueryRetrieveLevel.PATIENT
                        ? IOD.DataElementType.TYPE_1
                        : IOD.DataElementType.TYPE_3,
                    1, 1, 0));
            iod.add(new IOD.DataElement(Tag.StudyInstanceUID, VR.UI,
                        !relational 
                        ? IOD.DataElementType.TYPE_1
                        : IOD.DataElementType.TYPE_3,
                    1, 1, 0));
            iod.add(new IOD.DataElement(Tag.SeriesInstanceUID, VR.UI,
                        !relational 
                        ? IOD.DataElementType.TYPE_1
                        : IOD.DataElementType.TYPE_3,
                    1, 1, 0));
            return iod;
        }

        @Override
        protected IOD retrieveKeysIOD(QueryRetrieveLevel rootLevel,
                boolean relational) {
            IOD iod = queryKeysIOD(rootLevel, relational);
            iod.add(new IOD.DataElement(Tag.SOPInstanceUID, VR.UI,
                    IOD.DataElementType.TYPE_1, -1, -1, 0));
            return iod;
        }
    },
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

    protected abstract IOD queryKeysIOD(QueryRetrieveLevel rootLevel, boolean relational);

    protected abstract IOD retrieveKeysIOD(QueryRetrieveLevel rootLevel, boolean relational);

    private static void check(ValidationResult result) throws DicomServiceException {
        if (!result.isValid())
            throw new DicomServiceException(
                    Status.IdentifierDoesNotMatchSOPClass,
                    result.getErrorComment())
                .setOffendingElements(result.getOffendingElements());
    }
}