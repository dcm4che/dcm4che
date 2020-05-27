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
 * Portions created by the Initial Developer are Copyright (C) 2015-2019
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
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since May 2020
 */
public class ProcedureDiscontinuationReasons {
    public static final Code EquipmentFailure = new Code(
            "110501",
            "DCM",
            null,
            "Equipment failure");
    public static final Code DuplicateOrder = new Code(
            "110510",
            "DCM",
            null,
            "Duplicate order");
    public static final Code DiscontinuedForUnspecifiedReason = new Code(
            "110513",
            "DCM",
            null,
            "Discontinued for unspecified reason");
    public static final Code IncorrectWorklistEntrySelected = new Code(
            "110514",
            "DCM",
            null,
            "Incorrect worklist entry selected");
    public static final Code ObjectsIncorrectlyFormatted = new Code(
            "110521",
            "DCM",
            null,
            "Objects incorrectly formatted");
    public static final Code ObjectTypesNotSupported = new Code(
            "110522",
            "DCM",
            null,
            "Object Types not supported");
    public static final Code ObjectSetIncomplete = new Code(
            "110523",
            "DCM",
            null,
            "Object Set incomplete");
    public static final Code MediaFailure = new Code(
            "110524",
            "DCM",
            null,
            "Media Failure");
    public static final Code ResourcePreEmpted = new Code(
            "110526",
            "DCM",
            null,
            "Resource pre-empted");
    public static final Code ResourceInadequate = new Code(
            "110527",
            "DCM",
            null,
            "Resource inadequate");
    public static final Code DiscontinuedProcedureStepRescheduled = new Code(
            "110528",
            "DCM",
            null,
            "Discontinued Procedure Step rescheduled");
    public static final Code DiscontinuedProcedureStepReschedulingRecommended = new Code(
            "110529",
            "DCM",
            null,
            "Discontinued Procedure Step rescheduling recommended");
    public static final Code WorkitemAssignmentRejectedByAssignedResource = new Code(
            "110530",
            "DCM",
            null,
            "Workitem assignment rejected by assigned resource");
    public static final Code WorkitemExpired = new Code(
            "110533",
            "DCM",
            null,
            "Workitem expired");
    // TODO Include CID 9301 Modality PPS Discontinuation Reasons
    // TODO Include CID 60 Imaging Agent Administration Adverse Events
}