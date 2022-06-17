/*
 * **** BEGIN LICENSE BLOCK *****
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
 * Portions created by the Initial Developer are Copyright (C) 2015-2018
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
 * **** END LICENSE BLOCK *****
 *
 */

package org.dcm4che3.dcmr;

import org.dcm4che3.data.Code;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Feb 2018
 */
public class DeIdentificationMethod {
    public static final Code BasicApplicationConfidentialityProfile = new Code("113100", "DCM", null, "Basic Application Confidentiality Profile");
    public static final Code CleanPixelDataOption = new Code("113101", "DCM", null, "Clean Pixel Data Option");
    public static final Code CleanRecognizableVisualFeaturesOption = new Code("113102", "DCM", null, "Clean Recognizable Visual Features Option");
    public static final Code CleanGraphicsOption = new Code("113103", "DCM", null, "Clean Graphics Option");
    public static final Code CleanStructuredContentOption = new Code("113104", "DCM", null, "Clean Structured Content Option");
    public static final Code CleanDescriptorsOption = new Code("113105", "DCM", null, "Clean Descriptors Option");
    public static final Code RetainLongitudinalTemporalInformationFullDatesOption = new Code("113106", "DCM", null, "Retain Longitudinal Temporal Information Full Dates Option");
    public static final Code RetainLongitudinalTemporalInformationModifiedDatesOption = new Code("113107", "DCM", null, "Retain Longitudinal Temporal Information Modified Dates Option");
    public static final Code RetainPatientCharacteristicsOption = new Code("113108", "DCM", null, "Retain Patient Characteristics Option");
    public static final Code RetainDeviceIdentityOption = new Code("113109", "DCM", null, "Retain Device Identity Option");
    public static final Code RetainUIDsOption = new Code("113110", "DCM", null, "Retain UIDs Option");
    public static final Code RetainSafePrivateOption = new Code("113111", "DCM", null, "Retain Safe Private Option");
    public static final Code RetainInstitutionIdentityOption = new Code("113112", "DCM", null, "Retain Institution Identity Option");
    public static final Code RetainPatientIDHashOption = new Code("113113", "99DCM4CHE", null, "Retain Patient ID Hash Option");

}
