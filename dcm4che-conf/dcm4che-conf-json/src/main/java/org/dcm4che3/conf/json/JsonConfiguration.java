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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2013
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

package org.dcm4che3.conf.json;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.net.*;
import org.dcm4che3.util.StringUtils;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Nov 2015
 */
public class JsonConfiguration {

    private boolean extended = true;
    private final List<JsonConfigurationExtension> extensions = new ArrayList<>();

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public void addJsonConfigurationExtension(JsonConfigurationExtension ext) {
        extensions.add(ext);
    }

    public boolean removeJsonConfigurationExtension(JsonConfigurationExtension ext) {
        if (!extensions.remove(ext))
            return false;

        return true;
    }

    public <T extends JsonConfigurationExtension> T getJsonConfigurationExtension(Class<T> clazz) {
        for (JsonConfigurationExtension extension : extensions) {
            if (clazz.isAssignableFrom(extension.getClass()))
                return (T) extension;
        }
        return null;
    }

    public void writeTo(DeviceInfo deviceInfo, JsonGenerator gen) {
        JsonWriter writer = new JsonWriter(gen);
        gen.writeStartObject();
        gen.write("dicomDeviceName", deviceInfo.getDeviceName());
        writer.writeNotNull("dicomDescription", deviceInfo.getDescription());
        writer.writeNotNull("dicomManufacturer", deviceInfo.getManufacturer());
        writer.writeNotNull("dicomManufacturerModelName", deviceInfo.getManufacturerModelName());
        writer.writeNotEmpty("dicomSoftwareVersion", deviceInfo.getSoftwareVersions());
        writer.writeNotNull("dicomStationName", deviceInfo.getStationName());
        writer.writeNotEmpty("dicomInstitutionName", deviceInfo.getInstitutionNames());
        writer.writeNotEmpty("dicomInstitutionDepartmentName", deviceInfo.getInstitutionalDepartmentNames());
        writer.writeNotEmpty("dicomPrimaryDeviceType", deviceInfo.getPrimaryDeviceTypes());
        gen.write("dicomInstalled", deviceInfo.getInstalled());
        gen.writeEnd();
    }

    public void writeTo(ApplicationEntityInfo aetInfo, JsonGenerator gen) {
        JsonWriter writer = new JsonWriter(gen);
        gen.writeStartObject();
        writer.writeNotNull("dicomDeviceName", aetInfo.getDeviceName());
        writer.writeNotNull("dicomAETitle", aetInfo.getAeTitle());
        writer.writeNotEmpty("dcmOtherAETitle", aetInfo.getOtherAETitle());
        writer.writeNotNull("dicomDescription", aetInfo.getDescription());
        gen.write("dicomAssociationInitiator", aetInfo.getAssociationInitiator());
        gen.write("dicomAssociationAcceptor", aetInfo.getAssociationAcceptor());
        writer.writeNotEmpty("dicomApplicationCluster", aetInfo.getApplicationCluster());
        writer.writeNotNull("dicomInstalled", aetInfo.getInstalled());
        gen.writeEnd();
    }

    public void writeTo(Device device, JsonGenerator gen) {
        JsonWriter writer = new JsonWriter(gen);
        writer.writeStartObject();
        writer.writeNotNull("dicomDeviceName", device.getDeviceName());
        writer.writeNotNull("dicomDescription", device.getDescription());
        writer.writeNotNull("dicomManufacturer", device.getManufacturer());
        writer.writeNotNull("dicomManufacturerModelName", device.getManufacturerModelName());
        writer.writeNotEmpty("dicomSoftwareVersion", device.getSoftwareVersions());
        writer.writeNotNull("dicomStationName", device.getStationName());
        writer.writeNotNull("dicomDeviceSerialNumber", device.getDeviceSerialNumber());
        writer.writeNotNull("dicomIssuerOfPatientID", device.getIssuerOfPatientID());
        writer.writeNotNull("dicomIssuerOfAccessionNumber", device.getIssuerOfAccessionNumber());
        writer.writeNotNull("dicomOrderPlacerIdentifier", device.getOrderPlacerIdentifier());
        writer.writeNotNull("dicomOrderFillerIdentifier", device.getOrderFillerIdentifier());
        writer.writeNotNull("dicomIssuerOfAdmissionID", device.getIssuerOfAdmissionID());
        writer.writeNotNull("dicomIssuerOfServiceEpisodeID", device.getIssuerOfServiceEpisodeID());
        writer.writeNotNull("dicomIssuerOfContainerIdentifier", device.getIssuerOfContainerIdentifier());
        writer.writeNotNull("dicomIssuerOfSpecimenIdentifier", device.getIssuerOfSpecimenIdentifier());
        writer.writeNotEmpty("dicomInstitutionName", device.getInstitutionNames());
        writer.writeNotEmpty("dicomInstitutionCode", device.getInstitutionCodes());
        writer.writeNotEmpty("dicomInstitutionAddress", device.getInstitutionAddresses());
        writer.writeNotEmpty("dicomInstitutionDepartmentName", device.getInstitutionalDepartmentNames());
        writer.writeNotEmpty("dicomPrimaryDeviceType", device.getPrimaryDeviceTypes());
        writer.writeNotEmpty("dicomRelatedDeviceReference", device.getRelatedDeviceRefs());
        writer.writeNotEmpty("dicomAuthorizedNodeCertificateReference", device.getAuthorizedNodeCertificateRefs());
        writer.writeNotEmpty("dicomThisNodeCertificateReference", device.getThisNodeCertificateRefs());
        writer.write("dicomInstalled", device.isInstalled());
        writeConnectionsTo(device, writer);
        writeApplicationAEsTo(device, writer);

        if (extended) {
            gen.writeStartObject("dcmDevice");
            writer.writeNotDef("dcmLimitOpenAssociations", device.getLimitOpenAssociations(), 0);
            writer.writeNotNull("dcmTrustStoreURL", device.getTrustStoreURL());
            writer.writeNotNull("dcmTrustStoreType", device.getTrustStoreType());
            writer.writeNotNull("dcmTrustStorePin", device.getTrustStorePin());
            writer.writeNotNull("dcmTrustStorePinProperty", device.getTrustStorePinProperty());
            writer.writeNotNull("dcmKeyStoreURL", device.getKeyStoreURL());
            writer.writeNotNull("dcmKeyStoreType", device.getKeyStoreType());
            writer.writeNotNull("dcmKeyStorePin", device.getKeyStorePin());
            writer.writeNotNull("dcmKeyStorePinProperty", device.getKeyStorePinProperty());
            writer.writeNotNull("dcmKeyStoreKeyPin", device.getKeyStoreKeyPin());
            writer.writeNotNull("dcmKeyStoreKeyPinProperty", device.getKeyStoreKeyPinProperty());
            writer.writeNotNull("dcmTimeZoneOfDevice", device.getTimeZoneOfDevice());
            gen.writeEnd();
            for (JsonConfigurationExtension ext : extensions)
                ext.storeTo(device, writer);
        }
        gen.writeEnd();
    }

    public Device loadDeviceFrom(JsonParser parser, ConfigurationDelegate config)
            throws ConfigurationException {
        Device device = new Device();
        JsonReader reader = new JsonReader(parser);
        reader.next();
        reader.expect(JsonParser.Event.START_OBJECT);
        while (reader.next() == JsonParser.Event.KEY_NAME) {
            switch (reader.getString()) {
                case "dicomDeviceName":
                    device.setDeviceName(reader.stringValue());
                    break;
                case "dicomDescription":
                    device.setDescription(reader.stringValue());
                    break;
                case "dicomManufacturer":
                    device.setManufacturer(reader.stringValue());
                    break;
                case "dicomManufacturerModelName":
                    device.setManufacturerModelName(reader.stringValue());
                    break;
                case "dicomSoftwareVersion":
                    device.setSoftwareVersions(reader.stringArray());
                    break;
                case "dicomStationName":
                    device.setStationName(reader.stringValue());
                    break;
                case "dicomDeviceSerialNumber":
                    device.setDeviceSerialNumber(reader.stringValue());
                    break;
                case "dicomIssuerOfPatientID":
                    device.setIssuerOfPatientID(reader.issuerValue());
                    break;
                case "dicomIssuerOfAccessionNumber":
                    device.setIssuerOfAccessionNumber(reader.issuerValue());
                    break;
                case "dicomOrderPlacerIdentifier":
                    device.setOrderPlacerIdentifier(reader.issuerValue());
                    break;
                case "dicomOrderFillerIdentifier":
                    device.setOrderFillerIdentifier(reader.issuerValue());
                    break;
                case "dicomIssuerOfAdmissionID":
                    device.setIssuerOfAdmissionID(reader.issuerValue());
                    break;
                case "dicomIssuerOfServiceEpisodeID":
                    device.setIssuerOfServiceEpisodeID(reader.issuerValue());
                    break;
                case "dicomIssuerOfContainerIdentifier":
                    device.setIssuerOfContainerIdentifier(reader.issuerValue());
                    break;
                case "dicomIssuerOfSpecimenIdentifier":
                    device.setIssuerOfSpecimenIdentifier(reader.issuerValue());
                    break;
                case "dicomInstitutionName":
                    device.setInstitutionNames(reader.stringArray());
                    break;
                case "dicomInstitutionCode":
                    device.setInstitutionCodes(reader.codeArray());
                    break;
                case "dicomInstitutionAddress":
                    device.setInstitutionAddresses(reader.stringArray());
                    break;
                case "dicomInstitutionDepartmentName":
                    device.setInstitutionalDepartmentNames(reader.stringArray());
                    break;
                case "dicomPrimaryDeviceType":
                    device.setPrimaryDeviceTypes(reader.stringArray());
                    break;
                case "dicomRelatedDeviceReference":
                    device.setRelatedDeviceRefs(reader.stringArray());
                    break;
                case "dicomAuthorizedNodeCertificateReference":
                    for (String ref : reader.stringArray())
                        device.setAuthorizedNodeCertificates(ref);
                    break;
                case "dicomThisNodeCertificateReference":
                    for (String ref : reader.stringArray())
                        device.setThisNodeCertificates(ref);
                    break;
                case "dicomInstalled":
                    device.setInstalled(reader.booleanValue());
                    break;
                case "dcmDevice":
                    reader.next();
                    reader.expect(JsonParser.Event.START_OBJECT);
                    while (reader.next() == JsonParser.Event.KEY_NAME) {
                        switch (reader.getString()) {
                            case "dcmLimitOpenAssociations":
                                device.setLimitOpenAssociations(reader.intValue());
                                break;
                            case "dcmTrustStoreURL":
                                device.setTrustStoreURL(reader.stringValue());
                                break;
                            case "dcmTrustStoreType":
                                device.setTrustStoreType(reader.stringValue());
                                break;
                            case "dcmTrustStorePin":
                                device.setTrustStorePin(reader.stringValue());
                                break;
                            case "dcmTrustStorePinProperty":
                                device.setTrustStorePinProperty(reader.stringValue());
                                break;
                            case "dcmKeyStoreURL":
                                device.setKeyStoreURL(reader.stringValue());
                                break;
                            case "dcmKeyStoreType":
                                device.setKeyStoreType(reader.stringValue());
                                break;
                            case "dcmKeyStorePin":
                                device.setKeyStorePin(reader.stringValue());
                                break;
                            case "dcmKeyStorePinProperty":
                                device.setKeyStorePinProperty(reader.stringValue());
                                break;
                            case "dcmKeyStoreKeyPin":
                                device.setKeyStoreKeyPin(reader.stringValue());
                                break;
                            case "dcmKeyStoreKeyPinProperty":
                                device.setKeyStoreKeyPinProperty(reader.stringValue());
                                break;
                            case "dcmTimeZoneOfDevice":
                                device.setTimeZoneOfDevice(reader.timeZoneValue());
                                break;
                            default:
                                reader.skipUnknownProperty();
                        }
                    }
                    reader.expect(JsonParser.Event.END_OBJECT);
                    break;
                case "dicomNetworkConnection":
                    loadConnections(device, reader);
                    break;
                case "dicomNetworkAE":
                    loadApplicationEntities(device, reader);
                    break;
                default:
                    if (!loadDeviceExtension(device, reader, config))
                        reader.skipUnknownProperty();
            }
        }
        reader.expect(JsonParser.Event.END_OBJECT);
        if (device.getDeviceName() == null)
            throw new JsonParsingException("Missing property: dicomDeviceName", reader.getLocation());
        return device;
    }


    private boolean loadDeviceExtension(Device device, JsonReader reader, ConfigurationDelegate config)
            throws ConfigurationException {
        for (JsonConfigurationExtension ext : extensions)
            if (ext.loadDeviceExtension(device, reader, config))
                return true;
        return false;
    }

    private void writeConnectionsTo(Device device, JsonWriter writer) {
        List<Connection> conns = device.listConnections();
        if (conns.isEmpty())
            return;

        writer.writeStartArray("dicomNetworkConnection");
        for (Connection conn : conns)
            writeTo(conn, writer);
        writer.writeEnd();
    }

    private void writeTo(Connection conn, JsonWriter writer) {
        writer.writeStartObject();
        writer.writeNotNull("cn", conn.getCommonName());
        writer.writeNotNull("dicomHostname", conn.getHostname());
        writer.writeNotDef("dicomPort", conn.getPort(), Connection.NOT_LISTENING);
        writer.writeNotEmpty("dicomTLSCipherSuite", conn.getTlsCipherSuites());
        writer.writeNotNull("dicomInstalled", conn.getInstalled());
        if (extended) {
            writer.writeStartObject("dcmNetworkConnection");
            writer.writeNotNull("dcmProtocol",
                    StringUtils.nullify(conn.getProtocol(), Connection.Protocol.DICOM));
            writer.writeNotNull("dcmHTTPProxy", conn.getHttpProxy());
            writer.writeNotEmpty("dcmBlacklistedHostname", conn.getBlacklist());
            writer.writeNotDef("dcmTCPBacklog", conn.getBacklog(), Connection.DEF_BACKLOG);
            writer.writeNotDef("dcmTCPConnectTimeout",
                    conn.getConnectTimeout(), Connection.NO_TIMEOUT);
            writer.writeNotDef("dcmAARQTimeout",
                    conn.getRequestTimeout(), Connection.NO_TIMEOUT);
            writer.writeNotDef("dcmAAACTimeout",
                    conn.getAcceptTimeout(), Connection.NO_TIMEOUT);
            writer.writeNotDef("dcmARRPTimeout",
                    conn.getReleaseTimeout(), Connection.NO_TIMEOUT);
            writer.writeNotDef("dcmResponseTimeout",
                    conn.getResponseTimeout(), Connection.NO_TIMEOUT);
            writer.writeNotDef("dcmRetrieveTimeout",
                    conn.getRetrieveTimeout(), Connection.NO_TIMEOUT);
            writer.writeNotDef("dcmIdleTimeout", conn.getIdleTimeout(), Connection.NO_TIMEOUT);
            writer.writeNotDef("dcmTCPCloseDelay",
                    conn.getSocketCloseDelay(), Connection.DEF_SOCKETDELAY);
            writer.writeNotDef("dcmTCPSendBufferSize",
                    conn.getSendBufferSize(), Connection.DEF_BUFFERSIZE);
            writer.writeNotDef("dcmTCPReceiveBufferSize",
                    conn.getReceiveBufferSize(), Connection.DEF_BUFFERSIZE);
            writer.writeNotDef("dcmTCPNoDelay", conn.isTcpNoDelay(), true);
            writer.writeNotNull("dcmBindAddress", conn.getBindAddress());
            writer.writeNotNull("dcmClientBindAddress", conn.getClientBindAddress());
            writer.writeNotDef("dcmSendPDULength",
                    conn.getSendPDULength(), Connection.DEF_MAX_PDU_LENGTH);
            writer.writeNotDef("dcmReceivePDULength",
                    conn.getReceivePDULength(), Connection.DEF_MAX_PDU_LENGTH);
            writer.writeNotDef("dcmMaxOpsPerformed",
                    conn.getMaxOpsPerformed(), Connection.SYNCHRONOUS_MODE);
            writer.writeNotDef("dcmMaxOpsInvoked",
                    conn.getMaxOpsInvoked(), Connection.SYNCHRONOUS_MODE);
            writer.writeNotDef("dcmPackPDV", conn.isPackPDV(), true);
            if (conn.isTls()) {
                writer.writeNotEmpty("dcmTLSProtocol", conn.getTlsProtocols());
                writer.writeNotDef("dcmTLSNeedClientAuth", conn.isTlsNeedClientAuth(), true);
            }
            writer.writeEnd();
        }
        writer.writeEnd();
    }

    private void loadConnections(Device device, JsonReader reader) {
        reader.next();
        reader.expect(JsonParser.Event.START_ARRAY);
        while (reader.next() == JsonParser.Event.START_OBJECT) {
            Connection conn = new Connection();
            loadFrom(conn, reader);
            device.addConnection(conn);
        }
        reader.expect(JsonParser.Event.END_ARRAY);
    }

    private void loadFrom(Connection conn, JsonReader reader) {
        while (reader.next() == JsonParser.Event.KEY_NAME) {
            switch (reader.getString()) {
                case "cn":
                    conn.setCommonName(reader.stringValue());
                    break;
                case "dicomHostname":
                    conn.setHostname(reader.stringValue());
                    break;
                case "dicomPort":
                    conn.setPort(reader.intValue());
                    break;
                case "dicomTLSCipherSuite":
                    conn.setTlsCipherSuites(reader.stringArray());
                    break;
                case "dicomInstalled":
                    conn.setInstalled(reader.booleanValue());
                    break;
                case "dcmNetworkConnection":
                    reader.next();
                    reader.expect(JsonParser.Event.START_OBJECT);
                    while (reader.next() == JsonParser.Event.KEY_NAME) {
                        switch (reader.getString()) {
                            case "dcmProtocol":
                                conn.setProtocol(Connection.Protocol.valueOf(reader.stringValue()));
                                break;
                            case "dcmHTTPProxy":
                                conn.setHttpProxy(reader.stringValue());
                                break;
                            case "dcmBlacklistedHostname":
                                conn.setBlacklist(reader.stringArray());
                                break;
                            case "dcmTCPBacklog":
                                conn.setBacklog(reader.intValue());
                                break;
                            case "dcmTCPConnectTimeout":
                                conn.setConnectTimeout(reader.intValue());
                                break;
                            case "dcmAARQTimeout":
                                conn.setRequestTimeout(reader.intValue());
                                break;
                            case "dcmAAACTimeout":
                                conn.setAcceptTimeout(reader.intValue());
                                break;
                            case "dcmARRPTimeout":
                                conn.setReleaseTimeout(reader.intValue());
                                break;
                            case "dcmResponseTimeout":
                                conn.setResponseTimeout(reader.intValue());
                                break;
                            case "dcmRetrieveTimeout":
                                conn.setRetrieveTimeout(reader.intValue());
                                break;
                            case "dcmIdleTimeout":
                                conn.setIdleTimeout(reader.intValue());
                                break;
                            case "dcmTCPCloseDelay":
                                conn.setSocketCloseDelay(reader.intValue());
                                break;
                            case "dcmTCPSendBufferSize":
                                conn.setSendBufferSize(reader.intValue());
                                break;
                            case "dcmTCPReceiveBufferSize":
                                conn.setReceiveBufferSize(reader.intValue());
                                break;
                            case "dcmTCPNoDelay":
                                conn.setTcpNoDelay(reader.booleanValue());
                                break;
                            case "dcmBindAddress":
                                conn.setBindAddress(reader.stringValue());
                                break;
                            case "dcmClientBindAddress":
                                conn.setClientBindAddress(reader.stringValue());
                                break;
                            case "dcmTLSNeedClientAuth":
                                conn.setTlsNeedClientAuth(reader.booleanValue());
                                break;
                            case "dcmTLSProtocol":
                                conn.setTlsProtocols(reader.stringArray());
                                break;
                            case "dcmSendPDULength":
                                conn.setSendPDULength(reader.intValue());
                                break;
                            case "dcmReceivePDULength":
                                conn.setReceivePDULength(reader.intValue());
                                break;
                            case "dcmMaxOpsPerformed":
                                conn.setMaxOpsPerformed(reader.intValue());
                                break;
                            case "dcmMaxOpsInvoked":
                                conn.setMaxOpsInvoked(reader.intValue());
                                break;
                            case "dcmPackPDV":
                                conn.setPackPDV(reader.booleanValue());
                                break;
                            default:
                                reader.skipUnknownProperty();
                        }
                    }
                    reader.expect(JsonParser.Event.END_OBJECT);
                    break;
                default:
                    reader.skipUnknownProperty();
            }
        }
        reader.expect(JsonParser.Event.END_OBJECT);
    }

    private void writeApplicationAEsTo(Device device, JsonWriter writer) {
        Collection<ApplicationEntity> aes = device.getApplicationEntities();
        if (aes.isEmpty())
            return;

        List<Connection> conns = device.listConnections();
        writer.writeStartArray("dicomNetworkAE");
        for (ApplicationEntity ae : aes)
            writeTo(ae, conns, writer);
        writer.writeEnd();
    }
    
    private void writeTo(ApplicationEntity ae, List<Connection> conns, JsonWriter writer) {
        writer.writeStartObject();
        writer.writeNotNull("dicomAETitle", ae.getAETitle());
        writer.writeNotNull("dicomDescription", ae.getDescription());
        writer.writeNotEmpty("dicomApplicationCluster", ae.getApplicationClusters());
        writer.writeNotEmpty("dicomPreferredCallingAETitle", ae.getPreferredCallingAETitles());
        writer.writeNotEmpty("dicomPreferredCalledAETitle", ae.getPreferredCalledAETitles());
        writer.write("dicomAssociationInitiator", ae.isAssociationInitiator());
        writer.write("dicomAssociationAcceptor", ae.isAssociationAcceptor());
        writer.writeConnRefs(conns, ae.getConnections());
        writer.writeNotEmpty("dicomSupportedCharacterSet", ae.getSupportedCharacterSets());
        writer.writeNotNull("dicomInstalled", ae.getInstalled());
        writeTransferCapabilitiesTo(ae, writer);
        if (extended) {
            writer.writeStartObject("dcmNetworkAE");
            writer.writeNotEmpty("dcmAcceptedCallingAETitle", ae.getAcceptedCallingAETitles());
            writer.writeNotEmpty("dcmOtherAETitle", ae.getOtherAETitles());
            writer.writeNotEmpty("dcmMasqueradeCallingAETitle", ae.getMasqueradeCallingAETitles());
            writer.writeEnd();
            for (JsonConfigurationExtension ext : extensions)
                ext.storeTo(ae, writer);
        }
        writer.writeEnd();
    }

    private void loadApplicationEntities(Device device, JsonReader reader) {
        reader.next();
        reader.expect(JsonParser.Event.START_ARRAY);
        while (reader.next() == JsonParser.Event.START_OBJECT) {
            ApplicationEntity ae = new ApplicationEntity();
            loadFrom(ae, reader, device);
            device.addApplicationEntity(ae);
        }
        reader.expect(JsonParser.Event.END_ARRAY);
    }

    private void loadFrom(ApplicationEntity ae, JsonReader reader, Device device) {
        List<Connection> conns = device.listConnections();
        while (reader.next() == JsonParser.Event.KEY_NAME) {
            switch (reader.getString()) {
                case "dicomAETitle":
                    ae.setAETitle(reader.stringValue());
                    break;
                case "dicomDescription":
                    ae.setDescription(reader.stringValue());
                    break;
                case "dicomApplicationCluster":
                    ae.setApplicationClusters(reader.stringArray());
                    break;
                case "dicomPreferredCallingAETitle":
                    ae.setPreferredCallingAETitles(reader.stringArray());
                    break;
                case "dicomPreferredCalledAETitle":
                    ae.setPreferredCalledAETitles(reader.stringArray());
                    break;
                case "dicomAssociationInitiator":
                    ae.setAssociationInitiator(reader.booleanValue());
                    break;
                case "dicomAssociationAcceptor":
                    ae.setAssociationAcceptor(reader.booleanValue());
                    break;
                case "dicomSupportedCharacterSet":
                    ae.setSupportedCharacterSets(reader.stringArray());
                    break;
                case "dicomInstalled":
                    ae.setInstalled(reader.booleanValue());
                    break;
                case "dicomNetworkConnectionReference":
                    for (String connRef : reader.stringArray())
                        ae.addConnection(conns.get(JsonReader.toConnectionIndex(connRef)));
                    break;
                case "dcmNetworkAE":
                    reader.next();
                    reader.expect(JsonParser.Event.START_OBJECT);
                    while (reader.next() == JsonParser.Event.KEY_NAME) {
                        switch (reader.getString()) {
                            case "dcmAcceptedCallingAETitle":
                                ae.setAcceptedCallingAETitles(reader.stringArray());
                                break;
                            case "dcmOtherAETitle":
                                ae.setOtherAETitles(reader.stringArray());
                                break;
                            case "dcmMasqueradeCallingAETitle":
                                ae.setMasqueradeCallingAETitles(reader.stringArray());
                                break;
                            default:
                                reader.skipUnknownProperty();
                        }
                    }
                    reader.expect(JsonParser.Event.END_OBJECT);
                    break;
                case "dicomTransferCapability":
                    loadTransferCapabilities(ae, reader);
                    break;
                default:
                    if (!loadApplicationEntityExtension(device, ae, reader))
                        reader.skipUnknownProperty();
            }
        }
        reader.expect(JsonParser.Event.END_OBJECT);
        if (ae.getAETitle() == null)
            throw new JsonParsingException("Missing property: dicomAETitle", reader.getLocation());
    }

    private boolean loadApplicationEntityExtension(Device device, ApplicationEntity ae, JsonReader reader) {
        for (JsonConfigurationExtension ext : extensions)
            if (ext.loadApplicationEntityExtension(device, ae, reader))
                return true;
        return false;
    }

    private void writeTransferCapabilitiesTo(ApplicationEntity ae, JsonWriter writer) {
        writer.writeStartArray("dicomTransferCapability");
        for (TransferCapability tc : ae.getTransferCapabilities())
            writeTo(tc, writer);
        writer.writeEnd();

    }
    
    private void writeTo(TransferCapability tc, JsonWriter writer) {
        writer.writeStartObject();
        writer.writeNotNull("cn", tc.getCommonName());
        writer.writeNotNull("dicomSOPClass", tc.getSopClass());
        writer.writeNotNull("dicomTransferRole", tc.getRole().toString());
        writer.writeNotEmpty("dicomTransferSyntax", tc.getTransferSyntaxes());
        if (extended) {
            EnumSet<QueryOption> queryOpts = tc.getQueryOptions();
            StorageOptions storageOpts = tc.getStorageOptions();
            if (queryOpts != null || storageOpts != null) {
                writer.writeStartObject("dcmTransferCapability");
                if (queryOpts != null) {
                    writer.writeNotDef("dcmRelationalQueries",
                            queryOpts.contains(QueryOption.RELATIONAL), false);
                    writer.writeNotDef("dcmCombinedDateTimeMatching",
                            queryOpts.contains(QueryOption.DATETIME), false);
                    writer.writeNotDef("dcmFuzzySemanticMatching",
                            queryOpts.contains(QueryOption.FUZZY), false);
                    writer.writeNotDef("dcmTimezoneQueryAdjustment",
                            queryOpts.contains(QueryOption.TIMEZONE), false);
                }
                if (storageOpts != null) {
                    writer.write("dcmStorageConformance", storageOpts.getLevelOfSupport().ordinal());
                    writer.write("dcmDigitalSignatureSupport", storageOpts.getDigitalSignatureSupport().ordinal());
                    writer.write("dcmDataElementCoercion", storageOpts.getElementCoercion().ordinal());
                }
                writer.writeEnd();
            }
        }
        writer.writeEnd();
    }

    private void loadTransferCapabilities(ApplicationEntity ae, JsonReader reader) {
        reader.next();
        reader.expect(JsonParser.Event.START_ARRAY);
        while (reader.next() == JsonParser.Event.START_OBJECT) {
            TransferCapability tc = new TransferCapability();
            loadFrom(tc, reader);
            ae.addTransferCapability(tc);
        }
        reader.expect(JsonParser.Event.END_ARRAY);
    }

    private void loadFrom(TransferCapability tc, JsonReader reader) {
        while (reader.next() == JsonParser.Event.KEY_NAME) {
            switch (reader.getString()) {
                case "cn":
                    tc.setCommonName(reader.stringValue());
                    break;
                case "dicomSOPClass":
                    tc.setSopClass(reader.stringValue());
                    break;
                case "dicomTransferRole":
                    tc.setRole(TransferCapability.Role.valueOf(reader.stringValue()));
                    break;
                case "dicomTransferSyntax":
                    tc.setTransferSyntaxes(reader.stringArray());
                    break;
                case "dcmTransferCapability":
                    EnumSet<QueryOption> queryOpts = null;
                    StorageOptions storageOpts = null;
                    reader.next();
                    reader.expect(JsonParser.Event.START_OBJECT);
                    while (reader.next() == JsonParser.Event.KEY_NAME) {
                        switch (reader.getString()) {
                            case "dcmRelationalQueries":
                                if (reader.booleanValue()) {
                                    if (queryOpts == null)
                                        queryOpts = EnumSet.noneOf(QueryOption.class);
                                    queryOpts.add(QueryOption.RELATIONAL);
                                }
                                break;
                            case "dcmCombinedDateTimeMatching":
                                if (reader.booleanValue()) {
                                    if (queryOpts == null)
                                        queryOpts = EnumSet.noneOf(QueryOption.class);
                                    queryOpts.add(QueryOption.DATETIME);
                                }
                                break;
                            case "dcmFuzzySemanticMatching":
                                if (reader.booleanValue()) {
                                    if (queryOpts == null)
                                        queryOpts = EnumSet.noneOf(QueryOption.class);
                                    queryOpts.add(QueryOption.FUZZY);
                                }
                                break;
                            case "dcmTimezoneQueryAdjustment":
                                if (reader.booleanValue()) {
                                    if (queryOpts == null)
                                        queryOpts = EnumSet.noneOf(QueryOption.class);
                                    queryOpts.add(QueryOption.TIMEZONE);
                                }
                                break;
                            case "dcmStorageConformance":
                                if (storageOpts == null)
                                    storageOpts = new StorageOptions();
                                storageOpts.setLevelOfSupport(
                                        StorageOptions.LevelOfSupport.valueOf(reader.intValue()));
                                break;
                            case "dcmDigitalSignatureSupport":
                                if (storageOpts == null)
                                    storageOpts = new StorageOptions();
                                storageOpts.setDigitalSignatureSupport(
                                        StorageOptions.DigitalSignatureSupport.valueOf(reader.intValue()));
                                break;
                            case "dcmDataElementCoercion":
                                if (storageOpts == null)
                                    storageOpts = new StorageOptions();
                                storageOpts.setElementCoercion(
                                        StorageOptions.ElementCoercion.valueOf(reader.intValue()));
                                break;
                            default:
                                reader.skipUnknownProperty();
                        }
                    }
                    reader.expect(JsonParser.Event.END_OBJECT);
                    tc.setQueryOptions(queryOpts);
                    tc.setStorageOptions(storageOpts);
                    break;
                default:
                    reader.skipUnknownProperty();
            }
        }
        reader.expect(JsonParser.Event.END_OBJECT);
    }

}
