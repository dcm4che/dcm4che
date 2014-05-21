<?xml version="1.0" encoding="UTF-8"?>
<!-- ***** BEGIN LICENSE BLOCK *****
   - Version: MPL 1.1/GPL 2.0/LGPL 2.1
   -
   - The contents of this file are subject to the Mozilla Public License Version
   - 1.1 (the "License"); you may not use this file except in compliance with
   - the License. You may obtain a copy of the License at
   - http://www.mozilla.org/MPL/
   -
   - Software distributed under the License is distributed on an "AS IS" basis,
   - WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
   - for the specific language governing rights and limitations under the
   - License.
   -
   - The Original Code is part of dcm4che, an implementation of DICOM(TM) in
   - Java(TM), hosted at https://github.com/gunterze/dcm4che.
   -
   - The Initial Developer of the Original Code is
   - Agfa Healthcare.
   - Portions created by the Initial Developer are Copyright (C) 2011-2014
   - the Initial Developer. All Rights Reserved.
   -
   - Contributor(s):
   - Gunter Zeilinger <gunterze@gmail.com>
   -
   - Alternatively, the contents of this file may be used under the terms of
   - either the GNU General Public License Version 2 or later (the "GPL"), or
   - the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
   - in which case the provisions of the GPL or the LGPL are applicable instead
   - of those above. If you wish to allow use of your version of this file only
   - under the terms of either the GPL or the LGPL, and not to allow others to
   - use your version of this file under the terms of the MPL, indicate your
   - decision by deleting the provisions above and replace them with the notice
   - and other provisions required by the GPL or the LGPL. If you do not delete
   - the provisions above, a recipient may use your version of this file under
   - the terms of any one of the MPL, the GPL or the LGPL.
   -
   - ***** END LICENSE BLOCK *****  -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template name="dataelementsEx">
    <el tag="00020017" keyword="SendingApplicationEntityTitle" vr="AE" vm="1">Sending Application Entity Title</el>
    <el tag="00020018" keyword="ReceivingApplicationEntityTitle" vr="AE" vm="1">Receiving Application Entity Title</el>
    <el tag="00180013" keyword="ContrastBolusT1Relaxivity" vr="FL" vm="1">Contrast/Bolus T1 Relaxivity</el>
    <el tag="00189518" keyword="PrimaryPositionerIncrementSign" vr="SS" vm="1">Primary Positioner Increment Sign</el>
    <el tag="00189519" keyword="SecondaryPositionerIncrementSign" vr="SS" vm="1">Secondary Positioner Increment Sign</el>
    <el tag="00189541" keyword="DetectorPositionSequence" vr="SQ" vm="1">Detector Position Sequence</el>
    <el tag="00189542" keyword="XRayAcquisitionDoseSequence" vr="SQ" vm="1">X-Ray Acquisition Dose Sequence</el>
    <el tag="00189543" keyword="XRaySourceIsocenterPrimaryAngle" vr="FD" vm="1">X-Ray Source Isocenter Primary Angle</el>
    <el tag="00189544" keyword="XRaySourceIsocenterSecondaryAngle" vr="FD" vm="1">X-Ray Source Isocenter Secondary Angle</el>
    <el tag="00189545" keyword="IsocenterPrimaryAngle" vr="FD" vm="1">Breast Support Isocenter Primary Angle</el>
    <el tag="00189546" keyword="IsocenterSecondaryAngle" vr="FD" vm="1">Breast Support Isocenter Secondary Angle</el>
    <el tag="00189547" keyword="BreastSupportXPositionToIsocenter" vr="FD" vm="1">Breast Support X Position to Isocenter</el>
    <el tag="00189548" keyword="BreastSupportYPositionToIsocenter" vr="FD" vm="1">Breast Support Y Position to Isocenter</el>
    <el tag="00189549" keyword="BreastSupportZPositionToIsocenter" vr="FD" vm="1">Breast Support Z Position to Isocenter</el>
    <el tag="00189550" keyword="DetectorIsocenterPrimaryAngle" vr="FD" vm="1">Detector Isocenter Primary Angle</el>
    <el tag="00189551" keyword="DetectorIsocenterSecondaryAngle" vr="FD" vm="1">Detector Isocenter Secondary Angle</el>
    <el tag="00189552" keyword="DetectorXPositionToIsocenter" vr="FD" vm="1">Detector X Position to Isocenter</el>
    <el tag="00189553" keyword="DetectorYPositionToIsocenter" vr="FD" vm="1">Detector Y Position to Isocenter</el>
    <el tag="00189554" keyword="DetectorZPositionToIsocenter" vr="FD" vm="1">Detector Z Position to Isocenter</el>
    <el tag="00189555" keyword="XRayGridSequence" vr="SQ" vm="1">X-Ray Grid Sequence</el>
    <el tag="00189556" keyword="XRayFilterSequence" vr="SQ" vm="1">X-Ray Filter Sequence</el>
    <el tag="00189557" keyword="DetectorActiveAreaTLHCPosition" vr="FD" vm="3">Detector Active Area TLHC Position</el>
    <el tag="00189558" keyword="DetectorActiveAreaOrientation" vr="FD" vm="6">Detector Active Area Orientation</el>
    <el tag="00189559" keyword="PositionerPrimaryAngleDirection" vr="CS" vm="1">Positioner Primary Angle Direction</el>
    <el tag="0020930B" keyword="VolumeToTransducerRelationship" vr="CS" vm="1">Volume to Transducer Relationship</el>
  </xsl:template>

</xsl:stylesheet>