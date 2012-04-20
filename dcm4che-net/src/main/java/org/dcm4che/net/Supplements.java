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
 * Portions created by the Initial Developer are Copyright (C) 2012
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

package org.dcm4che.net;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public abstract class Supplements {

    public static void supplementComposite(Attributes ds, Device device) {
        supplementValue(ds, Tag.Manufacturer, VR.LO, device.getManufacturer());
        supplementValue(ds, Tag.ManufacturerModelName, VR.LO, device.getManufacturerModelName());
        supplementValue(ds, Tag.StationName, VR.SH, device.getStationName());
        supplementValue(ds, Tag.DeviceSerialNumber, VR.LO, device.getDeviceSerialNumber());
        supplementIssuerOfPatientID(ds, device.getIssuerOfPatientID());
        supplementIssuer(ds, Tag.AccessionNumber,
                Tag.IssuerOfAccessionNumberSequence,
                device.getIssuerOfAccessionNumber());
        supplementIssuer(ds, Tag.PlacerOrderNumberImagingServiceRequest,
                Tag.OrderPlacerIdentifierSequence,
                device.getOrderPlacerIdentifier());
        supplementIssuer(ds, Tag.FillerOrderNumberImagingServiceRequest,
                Tag.OrderFillerIdentifierSequence,
                device.getOrderFillerIdentifier());
        supplementIssuer(ds, Tag.AdmissionID,
                Tag.IssuerOfAdmissionIDSequence,
                device.getIssuerOfAdmissionID());
        supplementIssuer(ds, Tag.ServiceEpisodeID,
                Tag.IssuerOfServiceEpisodeID,
                device.getIssuerOfServiceEpisodeID());
        supplementIssuer(ds, Tag.ContainerIdentifier,
                Tag.IssuerOfTheContainerIdentifierSequence,
                device.getIssuerOfContainerIdentifier());
        supplementIssuer(ds, Tag.SpecimenIdentifier,
                Tag.IssuerOfTheSpecimenIdentifierSequence,
                device.getIssuerOfSpecimenIdentifier());
        supplementValues(ds, Tag.SoftwareVersions, VR.LO, device.getSoftwareVersions());
        supplementValue(ds, Tag.InstitutionName, VR.LO, device.getInstitutionNames());
        supplementCode(ds, Tag.InstitutionCodeSequence, device.getInstitutionCodes());
        supplementValue(ds, Tag.InstitutionalDepartmentName, VR.LO,
        device.getInstitutionalDepartmentNames());
        Sequence rqSeq = ds.getSequence(Tag.RequestAttributesSequence);
        if (rqSeq != null)
            for (Attributes rq : rqSeq) {
                supplementIssuer(rq, Tag.AccessionNumber,
                        Tag.IssuerOfAccessionNumberSequence,
                        device.getIssuerOfAccessionNumber());
                supplementIssuer(rq, Tag.PlacerOrderNumberImagingServiceRequest,
                        Tag.OrderPlacerIdentifierSequence,
                        device.getOrderPlacerIdentifier());
                supplementIssuer(rq, Tag.FillerOrderNumberImagingServiceRequest,
                        Tag.OrderFillerIdentifierSequence,
                        device.getOrderFillerIdentifier());
            }
    }

    public static boolean supplementIssuerOfPatientID(Attributes ds, Issuer issuer) {
        if (issuer == null
                || !ds.containsValue(Tag.PatientID)
                || ds.containsValue(Tag.IssuerOfPatientID) 
                || ds.containsValue(Tag.IssuerOfPatientIDQualifiersSequence))
            return false;
        
        String localNamespaceEntityID = issuer.getLocalNamespaceEntityID();
        if (localNamespaceEntityID != null)
            ds.setString(Tag.IssuerOfPatientID, VR.LO, localNamespaceEntityID);
        String universalEntityID = issuer.getUniversalEntityID();
        if (universalEntityID != null) {
            Attributes item = new Attributes(ds.bigEndian(), 2);
            item.setString(Tag.UniversalEntityID, VR.UT, universalEntityID);
            item.setString(Tag.UniversalEntityIDType, VR.CS,
                    issuer.getUniversalEntityIDType());
            ds.newSequence(Tag.IssuerOfPatientIDQualifiersSequence, 1).add(item);
        }
        return true;
    }

    private static boolean supplementValue(Attributes ds, int tag, VR vr,
            String... values) {
        if (values.length == 0 || values[0] == null
                || ds.containsValue(tag))
            return false;

        ds.setString(tag, vr, values[0]);
        return true;
    }

    private static boolean supplementValues(Attributes ds, int tag, VR vr,
            String... values) {
        if (values.length == 0
                || ds.containsValue(tag))
            return false;

        ds.setString(tag, vr, values);
        return true;
    }

    public static boolean supplementIssuer(Attributes ds, int tag, int seqTag,
            Issuer issuer) {
        if (issuer == null
                || !ds.containsValue(tag)
                || ds.containsValue(seqTag))
            return false;

        Attributes item = new Attributes(ds.bigEndian(), 3);
        String localNamespaceEntityID = issuer.getLocalNamespaceEntityID();
        if (localNamespaceEntityID != null)
            item.setString(Tag.LocalNamespaceEntityID, VR.LO, localNamespaceEntityID);
        String universalEntityID = issuer.getUniversalEntityID();
        if (universalEntityID != null) {
            item.setString(Tag.UniversalEntityID, VR.UT, universalEntityID);
            item.setString(Tag.UniversalEntityIDType, VR.CS,
                    issuer.getUniversalEntityIDType());
        }
        ds.newSequence(seqTag, 1).add(item);
        return true;
    }

    public static boolean supplementCode(Attributes ds, int seqTag, Code... codes) {
        if (codes.length == 0 || codes[0] == null || ds.containsValue(seqTag))
            return false;

        Attributes item = new Attributes(ds.bigEndian(), 4);
        item.setString(Tag.CodeValue, VR.SH, codes[0].getCodeValue());
        item.setString(Tag.CodingSchemeDesignator, VR.SH,
                codes[0].getCodingSchemeDesignator());
        String version = codes[0].getCodingSchemeVersion();
        if (version != null)
            item.setString(Tag.CodingSchemeVersion, VR.SH, version);
        item.setString(Tag.CodeMeaning, VR.LO, codes[0].getCodeMeaning());
        ds.newSequence(seqTag, 1).add(item);
        return true;
    }

}
