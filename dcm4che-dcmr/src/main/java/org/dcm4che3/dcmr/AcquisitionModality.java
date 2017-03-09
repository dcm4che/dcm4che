/*
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  J4Care.
 *  Portions created by the Initial Developer are Copyright (C) 2015-2017
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 */

package org.dcm4che3.dcmr;

import org.dcm4che3.data.Code;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Mar 2017
 */
public class AcquisitionModality {
    public static final Code Autorefraction = new Code("AR", "DCM", null, "Autorefraction");
    public static final Code BoneMineralDensitometry = new Code("BMD", "DCM", null, "Bone Mineral Densitometry");
    public static final Code UltrasoundBoneDensitometry = new Code("BDUS", "DCM", null, "Ultrasound Bone Densitometry");
    public static final Code CardiacElectrophysiology = new Code("EPS", "DCM", null, "Cardiac Electrophysiology");
    public static final Code ComputedRadiography = new Code("CR", "DCM", null, "Computed Radiography");
    public static final Code ComputedTomography = new Code("CT", "DCM", null, "Computed Tomography");
    public static final Code DigitalRadiography = new Code("DX", "DCM", null, "Digital Radiography");
    public static final Code Electrocardiography = new Code("ECG", "DCM", null, "Electrocardiography");
    public static final Code Endoscopy = new Code("ES", "DCM", null, "Endoscopy");
    public static final Code ExternalCameraPhotography  = new Code("XC", "DCM", null, "External-camera Photography");
    public static final Code GeneralMicroscopy = new Code("GM", "DCM", null, "General Microscopy");
    public static final Code HemodynamicWaveform = new Code("HD", "DCM", null, "Hemodynamic Waveform");
    public static final Code IntraOralRadiography  = new Code("IO", "DCM", null, "Intra-oral Radiography");
    public static final Code IntravascularOpticalCoherence  = new Code("IVOCT", "DCM", null, "Intravascular Optical Coherence Tomography");
    public static final Code IntravascularUltrasound = new Code("IVUS", "DCM", null, "Intravascular Ultrasound");
    public static final Code Keratometry = new Code("KER", "DCM", null, "Keratometry");
    public static final Code Lensometry = new Code("LEN", "DCM", null, "Lensometry");
    public static final Code MagneticResonance = new Code("MR", "DCM", null, "Magnetic Resonance");
    public static final Code Mammography = new Code("MG", "DCM", null, "Mammography");
    public static final Code NuclearMedicine = new Code("NM", "DCM", null, "Nuclear Medicine");
    public static final Code OphthalmicAxialMeasurements = new Code("OAM", "DCM", null, "Ophthalmic Axial Measurements");
    public static final Code OpticalCoherenceTomography = new Code("OCT", "DCM", null, "Optical Coherence Tomography");
    public static final Code OphthalmicMapping = new Code("OPM", "DCM", null, "Ophthalmic Mapping");
    public static final Code OphthalmicPhotography = new Code("OP", "DCM", null, "Ophthalmic Photography");
    public static final Code OphthalmicRefraction = new Code("OPR", "DCM", null, "Ophthalmic Refraction");
    public static final Code OphthalmicTomography = new Code("OPT", "DCM", null, "Ophthalmic Tomography");
    public static final Code OphthalmicVisualField = new Code("OPV", "DCM", null, "Ophthalmic Visual Field");
    public static final Code OpticalSurfaceScanner = new Code("OSS", "DCM", null, "Optical Surface Scanner");
    public static final Code PanoramicXRay = new Code("PX", "DCM", null, "Panoramic X-Ray");
    public static final Code PositronEmissionTomography = new Code("PT", "DCM", null, "Positron emission tomography");
    public static final Code Radiofluoroscopy = new Code("RF", "DCM", null, "Radiofluoroscopy");
    public static final Code RadiographicImaging = new Code("RG", "DCM", null, "Radiographic imaging");
    public static final Code SlideMicroscopy = new Code("SM", "DCM", null, "Slide Microscopy");
    public static final Code SubjectiveRefraction = new Code("SRF", "DCM", null, "Subjective Refraction");
    public static final Code Ultrasound = new Code("US", "DCM", null, "Ultrasound");
    public static final Code VisualAcuity = new Code("VA", "DCM", null, "Visual Acuity");
    public static final Code XRayAngiography  = new Code("XA", "DCM", null, "X-Ray Angiography");

    private static final Map<String,Code> MODALITIES = new HashMap<String,Code>(50);
    static {
        Code[] codes = {
            Autorefraction,
            BoneMineralDensitometry,
            UltrasoundBoneDensitometry,
            CardiacElectrophysiology,
            ComputedRadiography,
            ComputedTomography,
            DigitalRadiography,
            Electrocardiography,
            Endoscopy,
            ExternalCameraPhotography ,
            GeneralMicroscopy,
            HemodynamicWaveform,
            IntraOralRadiography ,
            IntravascularOpticalCoherence ,
            IntravascularUltrasound,
            Keratometry,
            Lensometry,
            MagneticResonance,
            Mammography,
            NuclearMedicine,
            OphthalmicAxialMeasurements,
            OpticalCoherenceTomography,
            OphthalmicMapping,
            OphthalmicPhotography,
            OphthalmicRefraction,
            OphthalmicTomography,
            OphthalmicVisualField,
            OpticalSurfaceScanner,
            PanoramicXRay,
            PositronEmissionTomography,
            Radiofluoroscopy,
            RadiographicImaging,
            SlideMicroscopy,
            SubjectiveRefraction,
            Ultrasound,
            VisualAcuity,
            XRayAngiography
        };
        for (Code code : codes) {
            MODALITIES.put(code.getCodeValue(), code);
        }

    }
    
    public static Code codeOf(String modality) {
        return MODALITIES.get(modality);
    }

    public static Code addCode(Code code) {
        return MODALITIES.put(code.getCodeValue(), code);
    }

    public static Code removeCode(String modality) {
        return MODALITIES.remove(modality);
    }
}
