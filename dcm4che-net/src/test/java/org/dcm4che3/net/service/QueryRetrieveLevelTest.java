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
 * Portions created by the Initial Developer are Copyright (C) 2020
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

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.EnumSet;
import java.util.Set;

/**
 *
 * @author Homero Cardoso de Almeida <homero.cardosodealmeida@agfa.com>
 */
public class QueryRetrieveLevelTest {
    private Attributes attributes;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        attributes = new Attributes();
    }

    @Test
    public void studyRootQuery_withMissingStudyInstanceUID_isInvalid()
            throws DicomServiceException {
        thrown.expect(DicomServiceException.class);
        thrown.expectMessage("Missing Attribute: (0020,000D)");

        attributes.setString(Tag.QueryRetrieveLevel, VR.LO, QueryRetrieveLevel.STUDY.name());

        validate(false, false, EnumSet.of(
                QueryRetrieveLevel.STUDY,
                QueryRetrieveLevel.SERIES,
                QueryRetrieveLevel.IMAGE
        ));
    }

    @Test
    public void seriesQueryWithStudyRoot_withMissingStudyInstanceUID_isInvalid()
            throws DicomServiceException {
        thrown.expect(DicomServiceException.class);
        thrown.expectMessage("Missing Attribute: (0020,000D)");

        attributes.setString(Tag.QueryRetrieveLevel, VR.LO, QueryRetrieveLevel.SERIES.name());
        attributes.setString(Tag.SeriesInstanceUID, VR.UI, "1.1.1.1.1111");

        validate(false, false, EnumSet.of(
                QueryRetrieveLevel.STUDY,
                QueryRetrieveLevel.SERIES,
                QueryRetrieveLevel.IMAGE
        ));
    }

    @Test
    public void studyQueryWithPatientRoot_withMissingStudyInstanceUID_isInvalid()
            throws DicomServiceException {
        thrown.expect(DicomServiceException.class);
        thrown.expectMessage("Missing Attribute: (0020,000D)");

        attributes.setString(Tag.QueryRetrieveLevel, VR.LO, QueryRetrieveLevel.STUDY.name());
        attributes.setString(Tag.PatientID, VR.LO, "111111");

        validate(false, false, EnumSet.of(
                QueryRetrieveLevel.PATIENT,
                QueryRetrieveLevel.STUDY,
                QueryRetrieveLevel.SERIES,
                QueryRetrieveLevel.IMAGE
        ));
    }

    @Test
    public void relationalStudyQueryWithPatientRoot_withMissingPatientID_isValid()
            throws DicomServiceException {

        attributes.setString(Tag.QueryRetrieveLevel, VR.LO, QueryRetrieveLevel.STUDY.name());
        attributes.setString(Tag.StudyInstanceUID, VR.UI, "1.1.1.1");

        validate(true, false, EnumSet.of(
                QueryRetrieveLevel.PATIENT,
                QueryRetrieveLevel.STUDY,
                QueryRetrieveLevel.SERIES,
                QueryRetrieveLevel.IMAGE
        ));
    }

    @Test(expected = DicomServiceException.class)
    public void seriesQueryWithPatientRoot_withMissingStudyInstanceUIDAndPatientID_isInvalid()
            throws DicomServiceException {

        attributes.setString(Tag.QueryRetrieveLevel, VR.LO, QueryRetrieveLevel.SERIES.name());
        attributes.setString(Tag.SeriesInstanceUID, VR.UI, "1.1.1.1.1111");

        validate(false, false, EnumSet.of(
                QueryRetrieveLevel.PATIENT,
                QueryRetrieveLevel.STUDY,
                QueryRetrieveLevel.SERIES,
                QueryRetrieveLevel.IMAGE
        ));
    }

    @Test
    public void relationalSeriesQueryWithPatientRoot_withMissingStudyInstanceUIDAndPatientID_isValid()
            throws DicomServiceException {

        attributes.setString(Tag.QueryRetrieveLevel, VR.LO, QueryRetrieveLevel.SERIES.name());
        attributes.setString(Tag.SeriesInstanceUID, VR.UI, "1.1.1.1.1111");

        validate(true, false, EnumSet.of(
                QueryRetrieveLevel.PATIENT,
                QueryRetrieveLevel.STUDY,
                QueryRetrieveLevel.SERIES,
                QueryRetrieveLevel.IMAGE
        ));
    }

    public void validate(boolean relational, boolean lenient, Set<QueryRetrieveLevel> levels)
            throws DicomServiceException {
        QueryRetrieveLevel.validateRetrieveIdentifier(attributes, levels, relational, lenient);
    }

}
