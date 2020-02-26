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

import org.dcm4che3.data.Tag;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;

/**
 *
 * @author Homero Cardoso de Almeida <homero.cardosodealmeida@agfa.com>
 */
public class UniqueKeyCheckFailureCollectorTest {
    private static final String MISSING_PATIENTID_MESSAGE = "Missing Attribute: (0010,0020)";
    private static final String MISSING_PATIENTID_SERIESUID_MESSAGE = "Missing Attributes: (0010,0020), (0020,000E)";
    private static final String INVALID_MODALITY_MESSAGE = "Invalid Attribute: (0008,0060) - \"AA\"";
    private static final String INVALID_MODALITY_LATERALITY_MESSAGE = "Invalid Attributes: (0008,0060) - \"AA\", (0020,0060) - \"C\"";
    private static final String MISSING_PATIENTID_INVALID_LATERALITY_MESSAGE = "Missing Attribute: (0010,0020); Invalid Attribute: (0020,0060) - \"C\"";

    private UniqueKeyCheckFailureCollector collector;

    @Before
    public void setup() {
        collector = new UniqueKeyCheckFailureCollector();
    }

    @Test
    public void getFailureMessage_includesMissingTags_whenOneTagAdded() {
        addMissing(Tag.PatientID);

        assertThat("Message does not have correct tag",
                collector.getFailureMessage(), equalTo(MISSING_PATIENTID_MESSAGE));
    }

    @Test
    public void getFailureMessage_includesMissingTags_whenMultipleTagsAdded() {
        addMissing(Tag.PatientID);
        addMissing(Tag.SeriesInstanceUID);

        assertThat("Message does not have all tags",
                collector.getFailureMessage(), equalTo(MISSING_PATIENTID_SERIESUID_MESSAGE));
    }

    @Test
    public void getFailureMessage_includesInvalidTags_whenOneTagAdded() {
        addInvalid(Tag.Modality, "AA");

        assertThat("Message does not have correct tag",
                collector.getFailureMessage(), equalTo(INVALID_MODALITY_MESSAGE));
    }

    @Test
    public void getFailureMessage_includesInvalidTags_whenMultipleTagsAdded() {
        addInvalid(Tag.Modality, "AA");
        addInvalid(Tag.Laterality, "C");

        assertThat("Message does not have correct tag",
                collector.getFailureMessage(), equalTo(INVALID_MODALITY_LATERALITY_MESSAGE));
    }

    @Test
    public void getFailureMessage_includesMissingAndInvalidTags_whenMultipleTagsAdded() {
        addMissing(Tag.PatientID);
        addInvalid(Tag.Laterality, "C");

        assertThat("Message does not have correct tag",
                collector.getFailureMessage(), equalTo(MISSING_PATIENTID_INVALID_LATERALITY_MESSAGE));
    }

    @Test
    public void getTags_includesMissingAndInvalidTags_WhenMultipleTagsAdded() {
        addMissing(Tag.StudyInstanceUID);
        addMissing(Tag.PatientID);
        addInvalid(Tag.Laterality, "C");
        addInvalid(Tag.Modality, "AA");

        assertThat("List of tags should contain all added elements",
                IntStream.of(collector.getTags()).boxed().toArray(),
                arrayContainingInAnyOrder(
                        Tag.StudyInstanceUID,
                        Tag.PatientID,
                        Tag.Laterality,
                        Tag.Modality
                ));
    }

    private void addMissing(int tag) {
        collector.add(new UniqueKeyCheckFailure(
                UniqueKeyCheckFailure.FailureType.MISSING_ATTRIBUTE,
                tag,
                null
        ));
    }

    private void addInvalid(int tag, String value) {
        collector.add(new UniqueKeyCheckFailure(
                UniqueKeyCheckFailure.FailureType.INVALID_ATTRIBUTE,
                tag,
                value
        ));
    }
}
