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
public class AnatomicRegion {
    public static final Code Abdomen = new Code("T-D4000", "SRT", null, "Abdomen");
    public static final Code AbdomenPelvis = new Code("R-FAB57", "SRT", null, "Abdomen and Pelvis");
    public static final Code AdrenalGland = new Code("T-B3000", "SRT", null, "Adrenal gland");
    public static final Code AnkleJoint = new Code("T-15750", "SRT", null, "Ankle joint");
    public static final Code Aorta = new Code("T-42000", "SRT", null, "Aorta");
    public static final Code Axilla = new Code("T-D8104", "SRT", null, "Axilla");
    public static final Code Back = new Code("T-D2100", "SRT", null, "Back");
    public static final Code Bladder = new Code("T-74000", "SRT", null, "Bladder");
    public static final Code Brain = new Code("T-A0100", "SRT", null, "Brain");
    public static final Code Breast = new Code("T-04000", "SRT", null, "Breast");
    public static final Code Bronchus = new Code("T-26000", "SRT", null, "Bronchus");
    public static final Code Buttock = new Code("T-D2600", "SRT", null, "Buttock");
    public static final Code Calcaneus = new Code("T-12770", "SRT", null, "Calcaneus");
    public static final Code CalfOfLeg = new Code("T-D9440", "SRT", null, "Calf of leg");
    public static final Code CarotidArtery = new Code("T-45010", "SRT", null, "Carotid Artery");
    public static final Code Cerebellum = new Code("T-A6000", "SRT", null, "Cerebellum");
    public static final Code CervicalSpine = new Code("T-11501", "SRT", null, "Cervical spine");
    public static final Code CervicoThoracicSpine = new Code("T-D00F7", "SRT", null, "Cervico-thoracic spine");
    public static final Code Cervix = new Code("T-83200", "SRT", null, "Cervix");
    public static final Code Cheek = new Code("T-D1206", "SRT", null, "Cheek");
    public static final Code Chest = new Code("T-D3000", "SRT", null, "Chest");
    public static final Code ChestAbdomen = new Code("R-FAB55", "SRT", null, "Chest and Abdomen");
    public static final Code ChestAbdomenPelvis = new Code("R-FAB56", "SRT", null, "Chest, Abdomen and Pelvis");
    public static final Code CircleOfWillis = new Code("T-45526", "SRT", null, "Circle of Willis");
    public static final Code Clavicle = new Code("T-12310", "SRT", null, "Clavicle");
    public static final Code Coccyx = new Code("T-11BF0", "SRT", null, "Coccyx");
    public static final Code Colon = new Code("T-59300", "SRT", null, "Colon");
    public static final Code Cornea = new Code("T-AA200", "SRT", null, "Cornea");
    public static final Code CoronaryArtery = new Code("T-43000", "SRT", null, "Coronary artery");
    public static final Code Duodenum = new Code("T-58200", "SRT", null, "Duodenum");
    public static final Code Ear = new Code("T-AB000", "SRT", null, "Ear");
    public static final Code ElbowJoint = new Code("T-15430", "SRT", null, "Elbow joint");
    public static final Code EntireBody = new Code("T-D0010", "SRT", null, "Entire body");
    public static final Code Esophagus = new Code("T-56000", "SRT", null, "Esophagus");
    public static final Code Extremity = new Code("T-D0300", "SRT", null, "Extremity");
    public static final Code Eye = new Code("T-AA000", "SRT", null, "Eye");
    public static final Code Eyelid = new Code("T-AA810", "SRT", null, "Eyelid");
    public static final Code Face = new Code("T-D1200", "SRT", null, "Face");
    public static final Code Femur = new Code("T-12710", "SRT", null, "Femur");
    public static final Code Finger = new Code("T-D8800", "SRT", null, "Finger");
    public static final Code Foot = new Code("T-D9700", "SRT", null, "Foot");
    public static final Code Gallbladder = new Code("T-63000", "SRT", null, "Gallbladder");
    public static final Code Hand = new Code("T-D8700", "SRT", null, "Hand");
    public static final Code Head = new Code("T-D1100", "SRT", null, "Head");
    public static final Code HeadNeck = new Code("T-D1000", "SRT", null, "Head and Neck");
    public static final Code Heart = new Code("T-32000", "SRT", null, "Heart");
    public static final Code HipJoint = new Code("T-15710", "SRT", null, "Hip joint");
    public static final Code Humerus = new Code("T-12410", "SRT", null, "Humerus");
    public static final Code Ileum = new Code("T-58600", "SRT", null, "Ileum");
    public static final Code Ilium = new Code("T-12340", "SRT", null, "Ilium");
    public static final Code InternalAuditoryCanal = new Code("T-AB959", "SRT", null, "Internal Auditory Canal");
    public static final Code JawRegion = new Code("T-D1213", "SRT", null, "Jaw region");
    public static final Code Jejunum = new Code("T-58400", "SRT", null, "Jejunum");
    public static final Code Kidney = new Code("T-71000", "SRT", null, "Kidney");
    public static final Code Knee = new Code("T-D9200", "SRT", null, "Knee");
    public static final Code Larynx = new Code("T-24100", "SRT", null, "Larynx");
    public static final Code Liver = new Code("T-62000", "SRT", null, "Liver");
    public static final Code LowerLeg = new Code("T-D9400", "SRT", null, "Lower leg");
    public static final Code LumbarSpine = new Code("T-11503", "SRT", null, "Lumbar spine");
    public static final Code LumboSacralSpine = new Code("T-D00F9", "SRT", null, "Lumbo-sacral spine");
    public static final Code Lung = new Code("T-28000", "SRT", null, "Lung");
    public static final Code Mandible = new Code("T-11180", "SRT", null, "Mandible");
    public static final Code Maxilla = new Code("T-11170", "SRT", null, "Maxilla");
    public static final Code Mediastinum = new Code("T-D3300", "SRT", null, "Mediastinum");
    public static final Code Mouth = new Code("T-51000", "SRT", null, "Mouth");
    public static final Code Neck = new Code("T-D1600", "SRT", null, "Neck");
    public static final Code NeckChest = new Code("R-FAB52", "SRT", null, "Neck and Chest");
    public static final Code NeckChestAbdomen = new Code("R-FAB53", "SRT", null, "Neck, Chest and Abdomen");
    public static final Code NeckChestAbdomenPelvis = new Code("R-FAB54", "SRT", null, "Neck, Chest, Abdomen and Pelvis");
    public static final Code Nose = new Code("T-21000", "SRT", null, "Nose");
    public static final Code OrbitalStructure = new Code("T-D14AE", "SRT", null, "Orbital structure");
    public static final Code Ovary = new Code("T-87000", "SRT", null, "Ovary");
    public static final Code Pancreas = new Code("T-65000", "SRT", null, "Pancreas");
    public static final Code ParotidGland = new Code("T-61100", "SRT", null, "Parotid gland");
    public static final Code Patella = new Code("T-12730", "SRT", null, "Patella");
    public static final Code Pelvis = new Code("T-D6000", "SRT", null, "Pelvis");
    public static final Code Penis = new Code("T-91000", "SRT", null, "Penis");
    public static final Code Pharynx = new Code("T-55000", "SRT", null, "Pharynx");
    public static final Code Prostate = new Code("T-9200B", "SRT", null, "Prostate");
    public static final Code Radius = new Code("T-12420", "SRT", null, "Radius");
    public static final Code RadiusUlna = new Code("T-12403", "SRT", null, "Radius and ulna");
    public static final Code Rectum = new Code("T-59600", "SRT", null, "Rectum");
    public static final Code Rib = new Code("T-11300", "SRT", null, "Rib");
    public static final Code Sacrum = new Code("T-11AD0", "SRT", null, "Sacrum");
    public static final Code Scalp = new Code("T-D1160", "SRT", null, "Scalp");
    public static final Code Scapula = new Code("T-12280", "SRT", null, "Scapula");
    public static final Code Sclera = new Code("T-AA110", "SRT", null, "Sclera");
    public static final Code Scrotum = new Code("T-98000", "SRT", null, "Scrotum");
    public static final Code Shoulder = new Code("T-D2220", "SRT", null, "Shoulder");
    public static final Code Skull = new Code("T-11100", "SRT", null, "Skull");
    public static final Code Spine = new Code("T-D04FF", "SRT", null, "Spine");
    public static final Code Spleen = new Code("T-C3000", "SRT", null, "Spleen");
    public static final Code Sternum = new Code("T-11210", "SRT", null, "Sternum");
    public static final Code Stomach = new Code("T-57000", "SRT", null, "Stomach");
    public static final Code SubmandibularGland = new Code("T-61300", "SRT", null, "Submandibular gland");
    public static final Code TemporomandibularJoint = new Code("T-15290", "SRT", null, "Temporomandibular joint");
    public static final Code Testis = new Code("T-94000", "SRT", null, "Testis");
    public static final Code Thigh = new Code("T-D9100", "SRT", null, "Thigh");
    public static final Code ThoracicSpine = new Code("T-11502", "SRT", null, "Thoracic spine");
    public static final Code ThoracoLumbarSpine = new Code("T-D00F8", "SRT", null, "Thoraco-lumbar spine");
    public static final Code Thumb = new Code("T-D8810", "SRT", null, "Thumb");
    public static final Code Thymus = new Code("T-C8000", "SRT", null, "Thymus");
    public static final Code Thyroid = new Code("T-B6000", "SRT", null, "Thyroid");
    public static final Code Tibia = new Code("T-12740", "SRT", null, "Tibia");
    public static final Code TibiaFibula = new Code("T-12701", "SRT", null, "Tibia and fibula");
    public static final Code Toe = new Code("T-D9800", "SRT", null, "Toe");
    public static final Code Tongue = new Code("T-53000", "SRT", null, "Tongue");
    public static final Code Trachea = new Code("T-25000", "SRT", null, "Trachea");
    public static final Code Ulna = new Code("T-12430", "SRT", null, "Ulna");
    public static final Code UpperArm = new Code("T-D8200", "SRT", null, "Upper arm");
    public static final Code Ureter = new Code("T-73000", "SRT", null, "Ureter");
    public static final Code Urethra = new Code("T-75000", "SRT", null, "Urethra");
    public static final Code Uterus = new Code("T-83000", "SRT", null, "Uterus");
    public static final Code Vagina = new Code("T-82000", "SRT", null, "Vagina");
    public static final Code Vulva = new Code("T-81000", "SRT", null, "Vulva");
    public static final Code WristJoint = new Code("T-15460", "SRT", null, "Wrist joint");
    public static final Code Zygoma = new Code("T-11166", "SRT", null, "Zygoma");

    private static final Map<String, Code> BODY_PART_EXAMINED = new HashMap<String, Code>();
    static {
        BODY_PART_EXAMINED.put("ABDOMEN", Abdomen);
        BODY_PART_EXAMINED.put("ABDOMENPELVIS", AbdomenPelvis);
        BODY_PART_EXAMINED.put("ADRENAL", AdrenalGland);
        BODY_PART_EXAMINED.put("ANKLE", AnkleJoint);
        BODY_PART_EXAMINED.put("AORTA", Aorta);
        BODY_PART_EXAMINED.put("AXILLA", Axilla);
        BODY_PART_EXAMINED.put("BACK", Back);
        BODY_PART_EXAMINED.put("BLADDER", Bladder);
        BODY_PART_EXAMINED.put("BRAIN", Brain);
        BODY_PART_EXAMINED.put("BREAST", Breast);
        BODY_PART_EXAMINED.put("BRONCHUS", Bronchus);
        BODY_PART_EXAMINED.put("BUTTOCK", Buttock);
        BODY_PART_EXAMINED.put("CALCANEUS", Calcaneus);
        BODY_PART_EXAMINED.put("CALF", CalfOfLeg);
        BODY_PART_EXAMINED.put("CAROTID", CarotidArtery);
        BODY_PART_EXAMINED.put("CEREBELLUM", Cerebellum);
        BODY_PART_EXAMINED.put("CSPINE", CervicalSpine);
        BODY_PART_EXAMINED.put("CTSPINE", CervicoThoracicSpine);
        BODY_PART_EXAMINED.put("CERVIX", Cervix);
        BODY_PART_EXAMINED.put("CHEEK", Cheek);
        BODY_PART_EXAMINED.put("CHEST", Chest);
        BODY_PART_EXAMINED.put("CHESTABDOMEN", ChestAbdomen);
        BODY_PART_EXAMINED.put("CHESTABDPELVIS", ChestAbdomenPelvis);
        BODY_PART_EXAMINED.put("CIRCLEOFWILLIS", CircleOfWillis);
        BODY_PART_EXAMINED.put("CLAVICLE", Clavicle);
        BODY_PART_EXAMINED.put("COCCYX", Coccyx);
        BODY_PART_EXAMINED.put("COLON", Colon);
        BODY_PART_EXAMINED.put("CORNEA", Cornea);
        BODY_PART_EXAMINED.put("CORONARYARTERY", CoronaryArtery);
        BODY_PART_EXAMINED.put("DUODENUM", Duodenum);
        BODY_PART_EXAMINED.put("EAR", Ear);
        BODY_PART_EXAMINED.put("ELBOW", ElbowJoint);
        BODY_PART_EXAMINED.put("WHOLEBODY", EntireBody);
        BODY_PART_EXAMINED.put("ESOPHAGUS", Esophagus);
        BODY_PART_EXAMINED.put("EXTREMITY", Extremity);
        BODY_PART_EXAMINED.put("EYE", Eye);
        BODY_PART_EXAMINED.put("EYELID", Eyelid);
        BODY_PART_EXAMINED.put("FACE", Face);
        BODY_PART_EXAMINED.put("FEMUR", Femur);
        BODY_PART_EXAMINED.put("FINGER", Finger);
        BODY_PART_EXAMINED.put("FOOT", Foot);
        BODY_PART_EXAMINED.put("GALLBLADDER", Gallbladder);
        BODY_PART_EXAMINED.put("HAND", Hand);
        BODY_PART_EXAMINED.put("HEAD", Head);
        BODY_PART_EXAMINED.put("HEADNECK", HeadNeck);
        BODY_PART_EXAMINED.put("HEART", Heart);
        BODY_PART_EXAMINED.put("HIP", HipJoint);
        BODY_PART_EXAMINED.put("HUMERUS", Humerus);
        BODY_PART_EXAMINED.put("ILEUM", Ileum);
        BODY_PART_EXAMINED.put("ILIUM", Ilium);
        BODY_PART_EXAMINED.put("IAC", InternalAuditoryCanal);
        BODY_PART_EXAMINED.put("JAW", JawRegion);
        BODY_PART_EXAMINED.put("JEJUNUM", Jejunum);
        BODY_PART_EXAMINED.put("KIDNEY", Kidney);
        BODY_PART_EXAMINED.put("KNEE", Knee);
        BODY_PART_EXAMINED.put("LARYNX", Larynx);
        BODY_PART_EXAMINED.put("LIVER", Liver);
        BODY_PART_EXAMINED.put("LEG", LowerLeg);
        BODY_PART_EXAMINED.put("LSPINE", LumbarSpine);
        BODY_PART_EXAMINED.put("LSSPINE", LumboSacralSpine);
        BODY_PART_EXAMINED.put("LUNG", Lung);
        BODY_PART_EXAMINED.put("JAW", Mandible);
        BODY_PART_EXAMINED.put("MAXILLA", Maxilla);
        BODY_PART_EXAMINED.put("MEDIASTINUM", Mediastinum);
        BODY_PART_EXAMINED.put("MOUTH", Mouth);
        BODY_PART_EXAMINED.put("NECK", Neck);
        BODY_PART_EXAMINED.put("NECKCHEST", NeckChest);
        BODY_PART_EXAMINED.put("NECKCHESTABDOMEN", NeckChestAbdomen);
        BODY_PART_EXAMINED.put("NECKCHESTABDPELV", NeckChestAbdomenPelvis);
        BODY_PART_EXAMINED.put("NOSE", Nose);
        BODY_PART_EXAMINED.put("ORBIT", OrbitalStructure);
        BODY_PART_EXAMINED.put("OVARY", Ovary);
        BODY_PART_EXAMINED.put("PANCREAS", Pancreas);
        BODY_PART_EXAMINED.put("PAROTID", ParotidGland);
        BODY_PART_EXAMINED.put("PATELLA", Patella);
        BODY_PART_EXAMINED.put("PELVIS", Pelvis);
        BODY_PART_EXAMINED.put("PENIS", Penis);
        BODY_PART_EXAMINED.put("PHARYNX", Pharynx);
        BODY_PART_EXAMINED.put("PROSTATE", Prostate);
        BODY_PART_EXAMINED.put("RADIUS", Radius);
        BODY_PART_EXAMINED.put("RADIUSULNA", RadiusUlna);
        BODY_PART_EXAMINED.put("RECTUM", Rectum);
        BODY_PART_EXAMINED.put("RIB", Rib);
        BODY_PART_EXAMINED.put("SSPINE", Sacrum);
        BODY_PART_EXAMINED.put("SCALP", Scalp);
        BODY_PART_EXAMINED.put("SCAPULA", Scapula);
        BODY_PART_EXAMINED.put("SCLERA", Sclera);
        BODY_PART_EXAMINED.put("SCROTUM", Scrotum);
        BODY_PART_EXAMINED.put("SHOULDER", Shoulder);
        BODY_PART_EXAMINED.put("SKULL", Skull);
        BODY_PART_EXAMINED.put("SPINE", Spine);
        BODY_PART_EXAMINED.put("SPLEEN", Spleen);
        BODY_PART_EXAMINED.put("STERNUM", Sternum);
        BODY_PART_EXAMINED.put("STOMACH", Stomach);
        BODY_PART_EXAMINED.put("SUBMANDIBULAR", SubmandibularGland);
        BODY_PART_EXAMINED.put("TMJ", TemporomandibularJoint);
        BODY_PART_EXAMINED.put("TESTIS", Testis);
        BODY_PART_EXAMINED.put("THIGH", Thigh);
        BODY_PART_EXAMINED.put("TSPINE", ThoracicSpine);
        BODY_PART_EXAMINED.put("TLSPINE", ThoracoLumbarSpine);
        BODY_PART_EXAMINED.put("THUMB", Thumb);
        BODY_PART_EXAMINED.put("THYMUS", Thymus);
        BODY_PART_EXAMINED.put("THYROID", Thyroid);
        BODY_PART_EXAMINED.put("TIBIA", Tibia);
        BODY_PART_EXAMINED.put("TIBIAFIBULA", TibiaFibula);
        BODY_PART_EXAMINED.put("TOE", Toe);
        BODY_PART_EXAMINED.put("TONGUE", Tongue);
        BODY_PART_EXAMINED.put("TRACHEA", Trachea);
        BODY_PART_EXAMINED.put("ULNA", Ulna);
        BODY_PART_EXAMINED.put("ARM", UpperArm);
        BODY_PART_EXAMINED.put("URETER", Ureter);
        BODY_PART_EXAMINED.put("URETHRA", Urethra);
        BODY_PART_EXAMINED.put("UTERUS", Uterus);
        BODY_PART_EXAMINED.put("VAGINA", Vagina);
        BODY_PART_EXAMINED.put("VULVA", Vulva);
        BODY_PART_EXAMINED.put("WRIST", WristJoint);
        BODY_PART_EXAMINED.put("ZYGOMA", Zygoma);
    }

    public static Code codeOf(String bodyPartExamined) {
        return BODY_PART_EXAMINED.get(bodyPartExamined);
    }

    public static Code addCode(Code code, String bodyPartExamined) {
        return BODY_PART_EXAMINED.put(bodyPartExamined, code);
    }

    public static Code removeCode(String bodyPartExamined) {
        return BODY_PART_EXAMINED.remove(bodyPartExamined);
    }
}
