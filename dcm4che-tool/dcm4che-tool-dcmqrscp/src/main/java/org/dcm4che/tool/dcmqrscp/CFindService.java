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
import org.dcm4che.data.UID;
import org.dcm4che.media.DicomDirReader;
import org.dcm4che.net.Association;
import org.dcm4che.net.DimseRSP;
import org.dcm4che.net.Status;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.net.service.AbstractCFindService;
import org.dcm4che.net.service.DicomServiceException;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
class CFindService extends AbstractCFindService {

    private final Main main;
    private final DicomDirReader reader;

    public CFindService(Main main) {
        super(main.getDevice(),
                UID.PatientRootQueryRetrieveInformationModelFIND,
                UID.StudyRootQueryRetrieveInformationModelFIND,
                UID.PatientStudyOnlyQueryRetrieveInformationModelFINDRetired);
        this.main = main;
        this.reader = main.getDicomDirReader();
    }

    @Override
    protected DimseRSP doCFind(Association as, PresentationContext pc,
            Attributes rq, Attributes keys, Attributes rsp)
            throws DicomServiceException {
        AttributesValidator validator = new AttributesValidator(keys);
        String level = validator.getType1String(Tag.QueryRetrieveLevel, 0,
                "PATIENT", "STUDY", "SERIES", "IMAGE");
        if (validator.hasOffendingElements())
            throw new DicomServiceException(rq,
                    Status.IdentifierDoesNotMatchSOPClass,
                    validator.getErrorComment())
                .setOffendingElements(Tag.QueryRetrieveLevel);
        switch (level.charAt(1)) {
        case 'A':
            return new CFindRSP.Patient(main, keys, rsp);
        case 'T':
            return new CFindRSP.Study(main, keys, rsp);
        case 'E':
            return new CFindRSP.Series(main, keys, rsp);
        case 'M':
            return new CFindRSP.Instance(main, keys, rsp);
        }
        throw new AssertionError();
    }

}
