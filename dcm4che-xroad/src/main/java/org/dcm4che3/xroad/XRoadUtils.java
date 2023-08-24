/*
 * *** BEGIN LICENSE BLOCK *****
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
 * Portions created by the Initial Developer are Copyright (C) 2013-2021
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
 * *** END LICENSE BLOCK *****
 */

package org.dcm4che3.xroad;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.dict.archive.PrivateTag;
import org.dcm4che3.net.Device;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Holder;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Jun 2023
 */
public class XRoadUtils {

    private static final ObjectFactory factory = new ObjectFactory();

    public static RR441ResponseType rr441(XRoadAdapterPortType port, Map<String, String> props, RR441RequestType rq,
                                          Holder<RequestHash> requestHash)
            throws XRoadException {
        return rr441(port, rq,
                createXRoadClientIdentifierType(props),
                createXRoadServiceIdentifierType(props, "RR441"),
                props.getOrDefault("userId", "EE11111111111"),
                props.getOrDefault("id", ""),
                props.getOrDefault("protocolVersion", "4.0"),
                requestHash);
    }

    public static RR441ResponseType rr441(XRoadAdapterPortType port, RR441RequestType rq,
                                           XRoadClientIdentifierType clientIdentifierType,
                                           XRoadServiceIdentifierType serviceIdentifierType,
                                           String userID,
                                           String id,
                                           String protocolVersion,
                                           Holder<RequestHash> requestHash) {
        Holder<RR441RequestType> request = new Holder<>(rq);
        Holder<RR441ResponseType> response = new Holder<>();
        port.rr441(request,
                new Holder(clientIdentifierType),
                new Holder(serviceIdentifierType),
                new Holder(userID),
                new Holder(id),
                new Holder(protocolVersion),
                response,
                requestHash);
        return response.value;
    }

    public static RR441RequestType createRR441RequestType(Map<String, String> props, String patientID) {
        String cValjad = props.getOrDefault("rr441.cValjad", "1,2,6,7,9,10");
        return createRR441RequestType(cValjad, patientID);
    }

    public static RR441RequestType createRR441RequestType(String cValjad, String patientID) {
        RR441RequestType rq = factory.createRR441RequestType();
        rq.setCValjad(cValjad);
        rq.setCIsikukoodid(patientID);
        return rq;
    }

    public static Attributes toAttributes(String specificCharacterSet, RR441ResponseType rsp) {
        List<RR441ResponseType.TtIsikuid.TtIsikud> ttIsikudList = rsp.getTtIsikuid().getTtIsikud();
        if (ttIsikudList.isEmpty())
            return null;

        RR441ResponseType.TtIsikuid.TtIsikud ttIsikud = ttIsikudList.get(0);
        Attributes attrs = new Attributes();
        attrs.setString(Tag.SpecificCharacterSet, VR.CS,
                specificCharacterSet);
        attrs.setString(Tag.PatientName, VR.PN, patientName(ttIsikud));
        attrs.setString(Tag.PatientID, VR.LO, ttIsikud.getTtIsikudCIsikukood());
        attrs.setString(Tag.PatientSex, VR.CS, patientSex(ttIsikud.getTtIsikudCSugu()));
        attrs.setString(Tag.PatientBirthDate, VR.DA, patientBirthDate(ttIsikud.getTtIsikudCSynniaeg()));
        attrs.setString(PrivateTag.PrivateCreator, PrivateTag.XRoadPersonStatus, VR.CS,
                ttIsikud.getTtIsikudCIsStaatus());
        attrs.setString(PrivateTag.PrivateCreator, PrivateTag.XRoadDataStatus, VR.CS,
                ttIsikud.getTtIsikudCKirjeStaatus());
        return attrs;
    }

    private static String patientName(RR441ResponseType.TtIsikuid.TtIsikud ttIsikud) {
        return ttIsikud.getTtIsikudCPerenimi() + '^' + ttIsikud.getTtIsikudCEesnimi();
    }

    private static String patientBirthDate(String synniaeg) {
        if (synniaeg == null || synniaeg.length() != 10)
            return null;

        char[] data = new char[8];
        synniaeg.getChars(6, 10, data, 0);
        synniaeg.getChars(3, 5, data, 4);
        synniaeg.getChars(0, 2, data, 6);
        return new String(data);
    }

    private static String patientSex(String sugu) {
        if (sugu != null && sugu.length() == 1) {
            switch (sugu.charAt(0)) {
                case 'M':
                    return "M";
                case 'N':
                    return "F";
            }
        }
        return null;
    }

    public static void setEndpointAddress(Object port, String endpoint) {
        BindingProvider bindingProvider = (BindingProvider) port;
        Map<String, Object> reqCtx = bindingProvider.getRequestContext();
        reqCtx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
    }

    public static void setTlsClientParameters(XRoadAdapterPortType port, Device device,
                                              String tlsProtocol, String[] cipherSuites, boolean disableCNCheck)
            throws GeneralSecurityException, IOException {
        Client client = ClientProxy.getClient(port);
        HTTPConduit conduit = (HTTPConduit) client.getConduit();
        conduit.setTlsClientParameters(createTLSClientParameters(device, tlsProtocol, cipherSuites, disableCNCheck));
    }

    private static TLSClientParameters createTLSClientParameters(
            Device device, String tlsProtocol, String[] cipherSuites, boolean disableCNCheck)
            throws GeneralSecurityException, IOException {
        TLSClientParameters params = new TLSClientParameters();
        params.setKeyManagers(device.keyManagers());
        params.setTrustManagers(device.trustManagers());
        params.setSecureSocketProtocol(tlsProtocol);
        for (String cipherSuite : cipherSuites)
            params.getCipherSuites().add(cipherSuite.trim());
        params.setDisableCNCheck(disableCNCheck);
        return params;
    }

    private static XRoadClientIdentifierType createXRoadClientIdentifierType(Map<String, String> props) {
        XRoadClientIdentifierType type = createXRoadClientIdentifierType(
                props.getOrDefault("client.objectType", XRoadObjectType.SUBSYSTEM.name()),
                props.getOrDefault("client.xRoadInstance", "EE"),
                props.getOrDefault("client.memberClass", "NGO"),
                props.getOrDefault("client.memberCode", "90007945"),
                props.getOrDefault("client.subsystemCode", "mia"));
        return type;
    }

    public static XRoadClientIdentifierType createXRoadClientIdentifierType(String objectTypeName, String xRoadInstance
            , String memberClass, String memberCode, String subsystemCode) {
        XRoadClientIdentifierType type = factory.createXRoadClientIdentifierType();
        type.setObjectType(XRoadObjectType.valueOf(objectTypeName));
        type.setXRoadInstance(xRoadInstance);
        type.setMemberClass(memberClass);
        type.setMemberCode(memberCode);
        type.setSubsystemCode(subsystemCode);
        return type;
    }

    private static XRoadServiceIdentifierType createXRoadServiceIdentifierType(Map<String, String> props, String serviceCode) {
        return createXRoadServiceIdentifierType(serviceCode,
                props.getOrDefault("service.objectType", XRoadObjectType.SERVICE.name()),
                props.getOrDefault("service.xRoadInstance", "EE"),
                props.getOrDefault("service.memberClass", "GOV"),
                props.getOrDefault("service.memberCode", "70008440"),
                props.getOrDefault("service.subsystemCode", "rr"),
                props.getOrDefault("serviceVersion", "v1"));
    }

    public static XRoadServiceIdentifierType createXRoadServiceIdentifierType(String serviceCode, String objectTypeName,
                                                                            String xRoadInstance, String memberClass,
                                                                            String memberCode, String subsystemCode,
                                                                            String serviceVersion) {
        XRoadServiceIdentifierType type = factory.createXRoadServiceIdentifierType();
        type.setObjectType(XRoadObjectType.valueOf(objectTypeName));
        type.setXRoadInstance(xRoadInstance);
        type.setMemberClass(memberClass);
        type.setMemberCode(memberCode);
        type.setSubsystemCode(subsystemCode);
        type.setServiceCode(serviceCode);
        type.setServiceVersion(serviceVersion);
        return type;
    }
}
