<!--
  ~ **** BEGIN LICENSE BLOCK *****
  ~ Version: MPL 1.1/GPL 2.0/LGPL 2.1
  ~
  ~ The contents of this file are subject to the Mozilla Public License Version
  ~ 1.1 (the "License"); you may not use this file except in compliance with
  ~ the License. You may obtain a copy of the License at
  ~ http://www.mozilla.org/MPL/
  ~
  ~ Software distributed under the License is distributed on an "AS IS" basis,
  ~ WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  ~ for the specific language governing rights and limitations under the
  ~ License.
  ~
  ~ The Original Code is part of dcm4che, an implementation of DICOM(TM) in
  ~ Java(TM), hosted at https://github.com/dcm4che.
  ~
  ~ The Initial Developer of the Original Code is
  ~ J4Care.
  ~ Portions created by the Initial Developer are Copyright (C) 2015
  ~ the Initial Developer. All Rights Reserved.
  ~
  ~ Contributor(s):
  ~ See @authors listed below
  ~
  ~ Alternatively, the contents of this file may be used under the terms of
  ~ either the GNU General Public License Version 2 or later (the "GPL"), or
  ~ the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  ~ in which case the provisions of the GPL or the LGPL are applicable instead
  ~ of those above. If you wish to allow use of your version of this file only
  ~ under the terms of either the GPL or the LGPL, and not to allow others to
  ~ use your version of this file under the terms of the MPL, indicate your
  ~ decision by deleting the provisions above and replace them with the notice
  ~ and other provisions required by the GPL or the LGPL. If you do not delete
  ~ the provisions above, a recipient may use your version of this file under
  ~ the terms of any one of the MPL, the GPL or the LGPL.
  ~
  ~ **** END LICENSE BLOCK *****
  -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template name="cuid2name">
    <xsl:param name="cuid"/>
    <xsl:choose>
      <xsl:when test="not($cuid)">Missing SOP Class</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.1'">Computed Radiography Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.1.1'">Digital X-Ray Image - For Presentation</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.1.1.1'">Digital X-Ray Image - For Processing</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.1.2'">Digital Mammography X-Ray Image - For Presentation</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.1.2.1'">Digital Mammography X-Ray Image - For Processing</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.1.3'">Digital Intra-Oral X-Ray Image - For Presentation</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.1.3.1'">Digital Intra-Oral X-Ray Image - For Processing</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.2'">CT Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.2.1'">Enhanced CT Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.2.2'">Legacy Converted Enhanced CT Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.3'">Ultrasound Multi-frame Image (Retired)</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.3.1'">Ultrasound Multi-frame Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.4'">MR Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.4.1'">Enhanced MR Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.4.2'">MR Spectroscopy</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.4.3'">Enhanced MR Color Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.4.4'">Legacy Converted Enhanced MR Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.5'">Nuclear Medicine Image (Retired)</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.6'">Ultrasound Image (Retired)</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.6.1'">Ultrasound Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.6.2'">Enhanced US Volume</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.7'">Secondary Capture Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.7.1'">Multi-frame Single Bit Secondary Capture Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.7.2'">Multi-frame Grayscale Byte Secondary Capture Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.7.3'">Multi-frame Grayscale Word Secondary Capture Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.7.4'">Multi-frame True Color Secondary Capture Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.8'">Standalone Overlay (Retired)</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.9'">Standalone Curve (Retired)</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.9.1'">Waveform - Trial (Retired)</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.9.1.1'">12-lead ECG Waveform</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.9.1.2'">General ECG Waveform</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.9.1.3'">Ambulatory ECG Waveform</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.9.2.1'">Hemodynamic Waveform</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.9.3.1'">Cardiac Electrophysiology Waveform</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.9.4.1'">Basic Voice Audio Waveform</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.9.4.2'">General Audio Waveform</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.9.5.1'">Arterial Pulse Waveform</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.9.6.1'">Respiratory Waveform</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.10'">Standalone Modality LUT (Retired)</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.11'">Standalone VOI LUT (Retired)</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.11.1'">Grayscale Softcopy Presentation State SOP Class</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.11.2'">Color Softcopy Presentation State SOP Class</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.11.3'">Pseudo-Color Softcopy Presentation State SOP Class</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.11.4'">Blending Softcopy Presentation State SOP Class</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.11.5'">XA/XRF Grayscale Softcopy Presentation State</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.12.1'">X-Ray Angiographic Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.12.1.1'">Enhanced XA Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.12.2'">X-Ray Radiofluoroscopic Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.12.2.1'">Enhanced XRF Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.12.3'">X-Ray Angiographic Bi-Plane Image (Retired)</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.13.1.1'">X-Ray 3D Angiographic Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.13.1.2'">X-Ray 3D Craniofacial Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.13.1.3'">Breast Tomosynthesis Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.13.1.4'">Breast Projection X-Ray Image - For Presentation</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.13.1.5'">Breast Projection X-Ray Image - For Processing</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.14.1'">Intravascular Optical Coherence Tomography Image - For Presentation</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.14.2'">Intravascular Optical Coherence Tomography Image - For Processing</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.20'">Nuclear Medicine Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.30'">Parametric Map</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.66'">Raw Data</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.66.1'">Spatial Registration</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.66.2'">Spatial Fiducials</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.66.3'">Deformable Spatial Registration</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.66.4'">Segmentation</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.66.5'">Surface Segmentation</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.67'">Real World Value Mapping</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.68.1'">Surface Scan Mesh</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.68.2'">Surface Scan Point Cloud</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.77.1'">VL Image - Trial (Retired)</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.77.2'">VL Multi-frame Image - Trial (Retired)</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.77.1.1'">VL Endoscopic Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.77.1.1.1'">Video Endoscopic Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.77.1.2'">VL Microscopic Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.77.1.2.1'">Video Microscopic Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.77.1.3'">VL Slide-Coordinates Microscopic Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.77.1.4'">VL Photographic Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.77.1.4.1'">Video Photographic Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.77.1.5.1'">Ophthalmic Photography 8 Bit Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.77.1.5.2'">Ophthalmic Photography 16 Bit Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.77.1.5.3'">Stereometric Relationship</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.77.1.5.4'">Ophthalmic Tomography Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.77.1.5.5'">Wide Field Ophthalmic Photography Stereographic Projection Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.77.1.5.6'">Wide Field Ophthalmic Photography 3D Coordinates Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.77.1.6'">VL Whole Slide Microscopy Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.78.1'">Lensometry Measurements</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.78.2'">Autorefraction Measurements</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.78.3'">Keratometry Measurements</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.78.4'">Subjective Refraction Measurements</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.78.5'">Visual Acuity Measurements</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.78.6'">Spectacle Prescription Report</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.78.7'">Ophthalmic Axial Measurements</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.78.8'">Intraocular Lens Calculations</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.79.1'">Macular Grid Thickness and Volume Report</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.80.1'">Ophthalmic Visual Field Static Perimetry Measurements</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.81.1'">Ophthalmic Thickness Map</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.82.1'">Corneal Topography Map</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.88.1'">Text SR - Trial (Retired)</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.88.2'">Audio SR - Trial (Retired)</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.88.3'">Detail SR - Trial (Retired)</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.88.4'">Comprehensive SR - Trial (Retired)</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.88.11'">Basic Text SR</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.88.22'">Enhanced SR</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.88.33'">Comprehensive SR</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.88.34'">Comprehensive 3D SR</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.88.40'">Procedure Log</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.88.50'">Mammography CAD SR</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.88.59'">Key Object Selection Document</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.88.65'">Chest CAD SR</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.88.67'">X-Ray Radiation Dose SR</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.88.68'">Radiopharmaceutical Radiation Dose SR</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.88.69'">Colon CAD SR</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.88.70'">Implantation Plan SR</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.104.1'">Encapsulated PDF</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.104.2'">Encapsulated CDA</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.128'">Positron Emission Tomography Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.128.1'">Legacy Converted Enhanced PET Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.129'">Standalone PET Curve (Retired)</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.130'">Enhanced PET Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.131'">Basic Structured Display</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.481.1'">RT Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.481.2'">RT Dose</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.481.3'">RT Structure Set</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.481.4'">RT Beams Treatment Record</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.481.5'">RT Plan</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.481.6'">RT Brachy Treatment Record</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.481.7'">RT Treatment Summary Record</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.481.8'">RT Ion Plan</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.481.9'">RT Ion Beams Treatment Record</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.501.1'">DICOS CT Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.501.2.1'">DICOS Digital X-Ray Image - For Presentation</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.501.2.2'">DICOS Digital X-Ray Image - For Processing</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.501.3'">DICOS Threat Detection Report</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.501.4'">DICOS 2D AIT</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.501.5'">DICOS 3D AIT</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.501.6'">DICOS Quadrupole Resonance (QR)</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.601.1'">Eddy Current Image</xsl:when>
      <xsl:when test="$cuid='1.2.840.10008.5.1.4.1.1.601.2'">Eddy Current Multi-frame Image</xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="concat('Unknown SOP Class: ',$cuid)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
