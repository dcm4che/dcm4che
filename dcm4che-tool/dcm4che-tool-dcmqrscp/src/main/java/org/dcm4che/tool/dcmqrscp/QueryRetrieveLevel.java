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

package org.dcm4che.tool.dcmqrscp;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.AttributesValidator;
import org.dcm4che.data.Tag;
import org.dcm4che.net.Status;
import org.dcm4che.net.service.DicomServiceException;

enum QueryRetrieveLevel {
    PATIENT {
        @Override
        void validate(AttributesValidator validator, boolean studyRoot, boolean relational,
                boolean retrieve) {
            if (retrieve)
                validateUniqueKey(validator, Tag.PatientID, false, 1);
        }
    },
    STUDY {
        @Override
        void validate(AttributesValidator validator, boolean studyRoot, boolean relational,
                boolean retrieve) {
            validateUniqueKey(validator, Tag.PatientID, relational || studyRoot, 1);
            if (retrieve)
                validateUniqueKey(validator, Tag.StudyInstanceUID, false, Integer.MAX_VALUE);
        }
    },
    SERIES {
        @Override
        void validate(AttributesValidator validator, boolean studyRoot, boolean relational,
                boolean retrieve) {
            validateUniqueKey(validator, Tag.PatientID, relational || studyRoot, 1);
            validateUniqueKey(validator, Tag.StudyInstanceUID, relational, 1);
            if (retrieve)
                validateUniqueKey(validator, Tag.SeriesInstanceUID, false, Integer.MAX_VALUE);
        }
    },
    IMAGE {
        @Override
        void validate(AttributesValidator validator, boolean studyRoot, boolean relational,
                boolean retrieve) {
            validateUniqueKey(validator, Tag.PatientID, relational || studyRoot, 1);
            validateUniqueKey(validator, Tag.StudyInstanceUID, relational, 1);
            validateUniqueKey(validator, Tag.SeriesInstanceUID, relational, 1);
            if (retrieve)
                validateUniqueKey(validator, Tag.SOPInstanceUID, false, Integer.MAX_VALUE);
        }
    };

    abstract void validate(AttributesValidator validator, boolean studyRoot, boolean relational,
            boolean retrieve);


    static void validateUniqueKey(AttributesValidator validator,
            int tag, boolean optional, int maxvm) {
        if (optional)
            validator.getType3String(tag, 0, maxvm, null);
        else
            validator.getType1String(tag, 0, maxvm);
    }

    public static void check(Attributes rq, AttributesValidator validator)
            throws DicomServiceException {
        if (validator.hasOffendingElements())
            throw new DicomServiceException(rq,
                    Status.IdentifierDoesNotMatchSOPClass,
                    validator.getErrorComment())
                .setOffendingElements(validator.getOffendingElements());
    }
}