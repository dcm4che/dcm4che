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

import org.dcm4che3.net.*;
import org.dcm4che3.util.StringUtils;

import javax.json.stream.JsonGenerator;
import javax.naming.directory.BasicAttribute;
import java.util.*;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
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

    public void writeTo(DeviceInfo deviceInfo, JsonGenerator gen) {
        gen.writeStartObject();
        gen.write("dicomDeviceName", deviceInfo.getDeviceName());
        JsonConfiguration.writeNotNullTo("dicomDescription", deviceInfo.getDescription(), gen);
        JsonConfiguration.writeNotNullTo("dicomManufacturer", deviceInfo.getManufacturer(), gen);
        JsonConfiguration.writeNotNullTo("dicomManufacturerModelName", deviceInfo.getManufacturerModelName(), gen);
        JsonConfiguration.writeNotEmptyTo("dicomSoftwareVersion", deviceInfo.getSoftwareVersions(), gen);
        JsonConfiguration.writeNotNullTo("dicomStationName", deviceInfo.getStationName(), gen);
        JsonConfiguration.writeNotEmptyTo("dicomInstitutionDepartmentName",
                deviceInfo.getInstitutionalDepartmentNames(), gen);
        JsonConfiguration.writeNotEmptyTo("dicomPrimaryDeviceType", deviceInfo.getPrimaryDeviceTypes(), gen);
        gen.write("dicomInstalled", deviceInfo.getInstalled());
        gen.writeEnd();
    }

    public void writeTo(Device device, JsonGenerator gen) {
        gen.writeStartObject();
        gen.write("dicomDeviceName", device.getDeviceName());
        JsonConfiguration.writeNotNullTo("dicomDescription", device.getDescription(), gen);
        JsonConfiguration.writeNotNullTo("dicomManufacturer", device.getManufacturer(), gen);
        JsonConfiguration.writeNotNullTo("dicomManufacturerModelName", device.getManufacturerModelName(), gen);
        JsonConfiguration.writeNotEmptyTo("dicomSoftwareVersion", device.getSoftwareVersions(), gen);
        JsonConfiguration.writeNotNullTo("dicomStationName", device.getStationName(), gen);
        JsonConfiguration.writeNotNullTo("dicomDeviceSerialNumber", device.getDeviceSerialNumber(), gen);
        JsonConfiguration.writeNotNullTo("dicomIssuerOfPatientID", device.getIssuerOfPatientID(), gen);
        JsonConfiguration.writeNotNullTo("dicomIssuerOfAccessionNumber", device.getIssuerOfAccessionNumber(), gen);
        JsonConfiguration.writeNotNullTo("dicomOrderPlacerIdentifier", device.getOrderPlacerIdentifier(), gen);
        JsonConfiguration.writeNotNullTo("dicomOrderFillerIdentifier", device.getOrderFillerIdentifier(), gen);
        JsonConfiguration.writeNotNullTo("dicomIssuerOfAdmissionID", device.getIssuerOfAdmissionID(), gen);
        JsonConfiguration.writeNotNullTo("dicomIssuerOfServiceEpisodeID", device.getIssuerOfServiceEpisodeID(), gen);
        JsonConfiguration.writeNotNullTo("dicomIssuerOfContainerIdentifier",
                device.getIssuerOfContainerIdentifier(), gen);
        JsonConfiguration.writeNotNullTo("dicomIssuerOfSpecimenIdentifier",
                device.getIssuerOfSpecimenIdentifier(), gen);
        JsonConfiguration.writeNotEmptyTo("dicomInstitutionName",
                device.getInstitutionNames(), gen);
        JsonConfiguration.writeNotEmptyTo("dicomInstitutionCode",
                device.getInstitutionCodes(), gen);
        JsonConfiguration.writeNotEmptyTo("dicomInstitutionAddress",
                device.getInstitutionAddresses(), gen);
        JsonConfiguration.writeNotEmptyTo("dicomInstitutionDepartmentName",
                device.getInstitutionalDepartmentNames(), gen);
        JsonConfiguration.writeNotEmptyTo("dicomPrimaryDeviceType",
                device.getPrimaryDeviceTypes(), gen);
        JsonConfiguration.writeNotEmptyTo("dicomRelatedDeviceReference",
                device.getRelatedDeviceRefs(), gen);
        JsonConfiguration.writeNotEmptyTo("dicomAuthorizedNodeCertificateReference",
                device.getAuthorizedNodeCertificateRefs(), gen);
        JsonConfiguration.writeNotEmptyTo("dicomThisNodeCertificateReference",
                device.getThisNodeCertificateRefs(), gen);
        gen.write("dicomInstalled", device.isInstalled());
        writeConnectionsTo(device, gen);
        writeApplicationAEsTo(device, gen);

        if (extended) {
            gen.writeStartObject("dcmDevice");
            JsonConfiguration.writeNotDefTo("dcmLimitOpenAssociations", device.getLimitOpenAssociations(), 0, gen);
            JsonConfiguration.writeNotNullTo("dcmTrustStoreURL", device.getTrustStoreURL(), gen);
            JsonConfiguration.writeNotNullTo("dcmTrustStoreType", device.getTrustStoreType(), gen);
            JsonConfiguration.writeNotNullTo("dcmTrustStorePin", device.getTrustStorePin(), gen);
            JsonConfiguration.writeNotNullTo("dcmTrustStorePinProperty", device.getTrustStorePinProperty(), gen);
            JsonConfiguration.writeNotNullTo("dcmKeyStoreURL", device.getKeyStoreURL(), gen);
            JsonConfiguration.writeNotNullTo("dcmKeyStoreType", device.getKeyStoreType(), gen);
            JsonConfiguration.writeNotNullTo("dcmKeyStorePin", device.getKeyStorePin(), gen);
            JsonConfiguration.writeNotNullTo("dcmKeyStorePinProperty", device.getKeyStorePinProperty(), gen);
            JsonConfiguration.writeNotNullTo("dcmKeyStoreKeyPin", device.getKeyStoreKeyPin(), gen);
            JsonConfiguration.writeNotNullTo("dcmKeyStoreKeyPinProperty", device.getKeyStoreKeyPinProperty(), gen);
            JsonConfiguration.writeNotNullTo("dcmTimeZoneOfDevice", device.getTimeZoneOfDevice(), gen);
            gen.writeEnd();
            for (JsonConfigurationExtension ext : extensions)
                ext.storeTo(device, gen);
        }
        gen.writeEnd();
    }

    private void writeConnectionsTo(Device device, JsonGenerator gen) {
        List<Connection> conns = device.listConnections();
        if (conns.isEmpty())
            return;
        
        gen.writeStartArray("dicomNetworkConnection");
        for (Connection conn : conns)
            writeTo(conn, gen);
        gen.writeEnd();
    }

    private void writeTo(Connection conn, JsonGenerator gen) {
        gen.writeStartObject();
        JsonConfiguration.writeNotNullTo("cn", conn.getCommonName(), gen);
        JsonConfiguration.writeNotNullTo("dicomHostname", conn.getHostname(), gen);
        JsonConfiguration.writeNotDefTo("dicomPort", conn.getPort(), Connection.NOT_LISTENING, gen);
        JsonConfiguration.writeNotEmptyTo("dicomTLSCipherSuite", conn.getTlsCipherSuites(), gen);
        JsonConfiguration.writeNotNullTo("dicomInstalled", conn.getInstalled(), gen);
        if (extended) {
            gen.writeStartObject("dcmNetworkConnection");
            JsonConfiguration.writeNotNullTo("dcmProtocol",
                    StringUtils.nullify(conn.getProtocol(), Connection.Protocol.DICOM), gen);
            JsonConfiguration.writeNotNullTo("dcmHTTPProxy", conn.getHttpProxy(), gen);
            JsonConfiguration.writeNotEmptyTo("dcmBlacklistedHostname", conn.getBlacklist(), gen);
            JsonConfiguration.writeNotDefTo("dcmTCPBacklog", conn.getBacklog(), Connection.DEF_BACKLOG, gen);
            JsonConfiguration.writeNotDefTo("dcmTCPConnectTimeout",
                    conn.getConnectTimeout(), Connection.NO_TIMEOUT, gen);
            JsonConfiguration.writeNotDefTo("dcmAARQTimeout",
                    conn.getRequestTimeout(), Connection.NO_TIMEOUT, gen);
            JsonConfiguration.writeNotDefTo("dcmAAACTimeout",
                    conn.getAcceptTimeout(), Connection.NO_TIMEOUT, gen);
            JsonConfiguration.writeNotDefTo("dcmARRPTimeout",
                    conn.getReleaseTimeout(), Connection.NO_TIMEOUT, gen);
            JsonConfiguration.writeNotDefTo("dcmResponseTimeout",
                    conn.getResponseTimeout(), Connection.NO_TIMEOUT, gen);
            JsonConfiguration.writeNotDefTo("dcmRetrieveTimeout",
                    conn.getRetrieveTimeout(), Connection.NO_TIMEOUT, gen);
            JsonConfiguration.writeNotDefTo("dcmIdleTimeout", conn.getIdleTimeout(), Connection.NO_TIMEOUT, gen);
            JsonConfiguration.writeNotDefTo("dcmTCPCloseDelay",
                    conn.getSocketCloseDelay(), Connection.DEF_SOCKETDELAY, gen);
            JsonConfiguration.writeNotDefTo("dcmTCPSendBufferSize",
                    conn.getSendBufferSize(), Connection.DEF_BUFFERSIZE, gen);
            JsonConfiguration.writeNotDefTo("dcmTCPReceiveBufferSize",
                    conn.getReceiveBufferSize(), Connection.DEF_BUFFERSIZE, gen);
            JsonConfiguration.writeNotDefTo("dcmTCPNoDelay", conn.isTcpNoDelay(), true, gen);
            JsonConfiguration.writeNotNullTo("dcmBindAddress", conn.getBindAddress(), gen);
            JsonConfiguration.writeNotNullTo("dcmClientBindAddress", conn.getClientBindAddress(), gen);
            JsonConfiguration.writeNotDefTo("dcmSendPDULength",
                    conn.getSendPDULength(), Connection.DEF_MAX_PDU_LENGTH, gen);
            JsonConfiguration.writeNotDefTo("dcmReceivePDULength",
                    conn.getReceivePDULength(), Connection.DEF_MAX_PDU_LENGTH, gen);
            JsonConfiguration.writeNotDefTo("dcmMaxOpsPerformed",
                    conn.getMaxOpsPerformed(), Connection.SYNCHRONOUS_MODE, gen);
            JsonConfiguration.writeNotDefTo("dcmMaxOpsInvoked",
                    conn.getMaxOpsInvoked(), Connection.SYNCHRONOUS_MODE, gen);
            JsonConfiguration.writeNotDefTo("dcmPackPDV", conn.isPackPDV(), true, gen);
            if (conn.isTls()) {
                JsonConfiguration.writeNotEmptyTo("dcmTLSProtocol", conn.getTlsProtocols(), gen);
                JsonConfiguration.writeNotDefTo("dcmTLSNeedClientAuth", conn.isTlsNeedClientAuth(), true, gen);
            }
            gen.writeEnd();
        }
        gen.writeEnd();
    }

    private void writeApplicationAEsTo(Device device, JsonGenerator gen) {
        Collection<ApplicationEntity> aes = device.getApplicationEntities();
        if (aes.isEmpty())
            return;

        List<Connection> conns = device.listConnections();
        gen.writeStartArray("dicomNetworkAE");
        for (ApplicationEntity ae : aes)
            writeTo(ae, conns, gen);
        gen.writeEnd();
    }

    private void writeTo(ApplicationEntity ae, List<Connection> conns, JsonGenerator gen) {
        gen.writeStartObject();
        gen.write("dicomAETitle", ae.getAETitle());
        JsonConfiguration.writeNotNullTo("dicomDescription", ae.getDescription(), gen);
        JsonConfiguration.writeNotEmptyTo("dicomApplicationCluster", ae.getApplicationClusters(), gen);
        JsonConfiguration.writeNotEmptyTo("dicomPreferredCallingAETitle", ae.getPreferredCallingAETitles(), gen);
        JsonConfiguration.writeNotEmptyTo("dicomPreferredCalledAETitle", ae.getPreferredCalledAETitles(), gen);
        gen.write("dicomAssociationInitiator", ae.isAssociationInitiator());
        gen.write("dicomAssociationAcceptor", ae.isAssociationAcceptor());
        writeConnRefs(ae, conns, gen);
        JsonConfiguration.writeNotEmptyTo("dicomSupportedCharacterSet", ae.getSupportedCharacterSets(), gen);
        JsonConfiguration.writeNotNullTo("dicomInstalled", ae.getInstalled(), gen);
        writeTransferCapabilitiesTo(ae, gen);
        if (extended) {
            gen.writeStartObject("dcmNetworkAE");
            JsonConfiguration.writeNotEmptyTo("dcmAcceptedCallingAETitle", ae.getAcceptedCallingAETitles(), gen);
            JsonConfiguration.writeNotEmptyTo("dcmOtherAETitle", ae.getOtherAETitles(), gen);
            for (JsonConfigurationExtension ext : extensions)
                ext.storeTo(ae, gen);
            gen.writeEnd();
        }
        gen.writeEnd();
    }

    private void writeTransferCapabilitiesTo(ApplicationEntity ae, JsonGenerator gen) {
        gen.writeStartArray("dicomTransferCapability");
        for (TransferCapability tc : ae.getTransferCapabilities())
            writeTo(tc, gen);
        gen.writeEnd();
        
    }

    private void writeTo(TransferCapability tc, JsonGenerator gen) {
        gen.writeStartObject();
        JsonConfiguration.writeNotNullTo("cn", tc.getCommonName(), gen);
        gen.write("dicomSOPClass", tc.getSopClass());
        gen.write("dicomTransferRole", tc.getRole().toString());
        gen.writeStartArray("dicomTransferSyntax");
        for (String ts : tc.getTransferSyntaxes())
            gen.write(ts);
        gen.writeEnd();
        if (extended) {
            EnumSet<QueryOption> queryOpts = tc.getQueryOptions();
            StorageOptions storageOpts = tc.getStorageOptions();
            if (queryOpts != null || storageOpts != null) {
                gen.writeStartObject("dcmTransferCapability");
                if (queryOpts != null) {
                    JsonConfiguration.writeNotDefTo("dcmRelationalQueries",
                            queryOpts.contains(QueryOption.RELATIONAL), false, gen);
                    JsonConfiguration.writeNotDefTo("dcmCombinedDateTimeMatching",
                            queryOpts.contains(QueryOption.DATETIME), false, gen);
                    JsonConfiguration.writeNotDefTo("dcmFuzzySemanticMatching",
                            queryOpts.contains(QueryOption.FUZZY), false, gen);
                    JsonConfiguration.writeNotDefTo("dcmTimezoneQueryAdjustment",
                            queryOpts.contains(QueryOption.TIMEZONE), false, gen);
                }
                if (storageOpts != null) {
                    gen.write("dcmStorageConformance", storageOpts.getLevelOfSupport().ordinal());
                    gen.write("dcmDigitalSignatureSupport", storageOpts.getDigitalSignatureSupport().ordinal());
                    gen.write("dcmDataElementCoercion", storageOpts.getElementCoercion().ordinal());
                }
                gen.writeEnd();
            }
        }
        gen.writeEnd();
    }

    private void writeConnRefs(ApplicationEntity ae, List<Connection> conns, JsonGenerator gen) {
        gen.writeStartArray("dicomNetworkConnectionReference");
        for (Connection conn : ae.getConnections())
            gen.write("/dicomNetworkConnection/" + conns.indexOf(conn));
        gen.writeEnd();
    }

    public static void writeNotNullTo(String name, Object value, JsonGenerator gen) {
        if (value != null)
            gen.write(name, value.toString());
    }

    public static void writeNotNullTo(String name, Boolean value, JsonGenerator gen) {
        if (value != null)
            gen.write(name, value.booleanValue());
    }

    private static void writeNotNullTo(String name, TimeZone value, JsonGenerator gen) {
        if (value != null)
            gen.write(name, value.getID());
    }

    public static void writeNotEmptyTo(String name, Object[] values, JsonGenerator gen) {
        if (values.length != 0) {
            gen.writeStartArray(name);
            for (Object value : values)
                gen.write(value.toString());
            gen.writeEnd();
        }
    }

    public static void writeNotDefTo(String name, int value, int defVal, JsonGenerator gen) {
        if (value != defVal)
            gen.write(name, value);
    }

    public static void writeNotDefTo(String name, boolean value, boolean defVal, JsonGenerator gen) {
        if (value != defVal)
            gen.write(name, value);
    }

}
