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

package org.dcm4che3.conf.json.audit;

import jakarta.json.stream.JsonParser;
import org.dcm4che3.audit.AuditMessages;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.json.ConfigurationDelegate;
import org.dcm4che3.conf.json.JsonConfigurationExtension;
import org.dcm4che3.conf.json.JsonReader;
import org.dcm4che3.conf.json.JsonWriter;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditLoggerDeviceExtension;
import org.dcm4che3.net.audit.AuditSuppressCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Jan 2016
 */
public class JsonAuditLoggerConfiguration extends JsonConfigurationExtension {

    private static final Logger LOG = LoggerFactory.getLogger(JsonAuditLoggerConfiguration.class);

    @Override
    protected void storeTo(Device device, JsonWriter writer) {
        AuditLoggerDeviceExtension ext = device.getDeviceExtension(AuditLoggerDeviceExtension.class);
        if (ext == null)
            return;
        writer.writeStartArray("dcmAuditLogger");
        for (AuditLogger logger : ext.getAuditLoggers())
            writeTo(device, logger, writer);
        writer.writeEnd();
    }

    private void writeTo(Device device, AuditLogger auditLogger, JsonWriter writer) {
        writer.writeStartObject();
        writer.writeNotNullOrDef("cn", auditLogger.getCommonName(), null);
        writer.writeNotNullOrDef("dcmAuditRecordRepositoryDeviceName",
                auditLogger.getAuditRecordRepositoryDeviceNameNotNull(), null);
        writer.writeConnRefs(device.listConnections(), auditLogger.getConnections());
        writer.writeNotNull("dicomInstalled", auditLogger.getInstalled());
        writer.writeNotNullOrDef("dcmAuditSourceID", auditLogger.getAuditSourceID(), null);
        writer.writeNotNullOrDef("dcmAuditEnterpriseSiteID", auditLogger.getAuditEnterpriseSiteID(), null);
        writer.writeNotEmpty("dcmAuditSourceTypeCode", auditLogger.getAuditSourceTypeCodes());
        writer.writeNotNullOrDef("dcmAuditFacility", auditLogger.getFacility(), AuditLogger.Facility.authpriv);
        writer.writeNotNullOrDef("dcmAuditSuccessSeverity",
                auditLogger.getSuccessSeverity(), AuditLogger.Severity.notice);
        writer.writeNotNullOrDef("dcmAuditMinorFailureSeverity",
                auditLogger.getMinorFailureSeverity(), AuditLogger.Severity.warning);
        writer.writeNotNullOrDef("dcmAuditSeriousFailureSeverity",
                auditLogger.getSeriousFailureSeverity(), AuditLogger.Severity.err);
        writer.writeNotNullOrDef("dcmAuditMajorFailureSeverity",
                auditLogger.getMajorFailureSeverity(), AuditLogger.Severity.crit);
        writer.writeNotNullOrDef("dcmAuditApplicationName", auditLogger.getApplicationName(), null);
        writer.writeNotNullOrDef("dcmAuditMessageID", auditLogger.getMessageID(), AuditLogger.MESSAGE_ID);
        writer.writeNotNullOrDef("dcmAuditMessageEncoding", auditLogger.getEncoding(), "UTF-8");
        writer.writeNotDef("dcmAuditMessageBOM", auditLogger.isIncludeBOM(), true);
        writer.writeNotDef("dcmAuditTimestampInUTC", auditLogger.isTimestampInUTC(), false);
        writer.writeNotDef("dcmAuditMessageFormatXML", auditLogger.isFormatXML(), false);
        writer.writeNotNullOrDef("dcmAuditMessageSchemaURI", auditLogger.getSchemaURI(), AuditMessages.SCHEMA_URI);
        writer.writeNotDef("dcmAuditIncludeInstanceUID", auditLogger.isIncludeInstanceUID(), false);
        writer.writeNotNullOrDef("dcmAuditLoggerSpoolDirectoryURI", auditLogger.getSpoolDirectoryURI(), null);
        writer.writeNotDef("dcmAuditLoggerRetryInterval", auditLogger.getRetryInterval(), 0);
        writeAuditSuppressCriteriaList(writer, auditLogger.getAuditSuppressCriteriaList());
        writer.writeEnd();
    }


    protected void writeAuditSuppressCriteriaList (
            JsonWriter writer, List<AuditSuppressCriteria> list) {
        if (list.isEmpty())
            return;
        writer.writeStartArray("dcmAuditSuppressCriteria");
        for (AuditSuppressCriteria suppressCriteria : list) {
            writer.writeStartObject();
            writer.writeNotNullOrDef("cn", suppressCriteria.getCommonName(), null);
            writer.writeNotEmpty("dcmAuditEventID", suppressCriteria.getEventIDsAsStringArray());
            writer.writeNotEmpty("dcmAuditEventTypeCode", suppressCriteria.getEventTypeCodesAsStringArray());
            writer.writeNotEmpty("dcmAuditEventActionCode", suppressCriteria.getEventActionCodes());
            writer.writeNotEmpty("dcmAuditEventOutcomeIndicator", suppressCriteria.getEventOutcomeIndicators());
            writer.writeNotEmpty("dcmAuditUserID", suppressCriteria.getUserIDs());
            writer.writeNotEmpty("dcmAuditAlternativeUserID", suppressCriteria.getAlternativeUserIDs());
            writer.writeNotEmpty("dcmAuditUserRoleIDCode", suppressCriteria.getUserRoleIDCodesAsStringArray());
            writer.writeNotEmpty("dcmAuditNetworkAccessPointID", suppressCriteria.getNetworkAccessPointIDs());
            writer.writeNotNull("dcmAuditUserIsRequestor", suppressCriteria.getUserIsRequestor());
            writer.writeNotEmpty("dcmParticipantObjectTypeCodes",
                    suppressCriteria.getParticipantObjectTypeCodes());
            writer.writeNotEmpty("dcmParticipantObjectTypeCodeRoles",
                    suppressCriteria.getParticipantObjectTypeCodeRoles());
            writer.writeNotEmpty("dcmParticipantObjectDataLifeCycle",
                    suppressCriteria.getParticipantObjectDataLifeCycle());
            writer.writeEnd();
        }
        writer.writeEnd();
    }

    @Override
    public boolean loadDeviceExtension(Device device, JsonReader reader, ConfigurationDelegate config)
            throws ConfigurationException {
        if (!reader.getString().equals("dcmAuditLogger"))
            return false;

        AuditLoggerDeviceExtension ext = new AuditLoggerDeviceExtension();
        loadFrom(ext, reader, device.listConnections(), config);
        device.addDeviceExtension(ext);
        return true;
    }

    private void loadFrom(AuditLoggerDeviceExtension ext, JsonReader reader, List<Connection> conns,
                          ConfigurationDelegate config) throws ConfigurationException {
        reader.next();
        reader.expect(JsonParser.Event.START_ARRAY);
        while (reader.next() == JsonParser.Event.START_OBJECT) {
            AuditLogger logger = new AuditLogger();
            loadFrom(logger, reader, conns, config);
            reader.expect(JsonParser.Event.END_OBJECT);
            ext.addAuditLogger(logger);
        }
        reader.expect(JsonParser.Event.END_ARRAY);
    }

    private void loadFrom(AuditLogger logger, JsonReader reader, List<Connection> conns,
                          ConfigurationDelegate config) throws ConfigurationException {
        while (reader.next() == JsonParser.Event.KEY_NAME) {
            switch (reader.getString()) {
                case "cn":
                    logger.setCommonName(reader.stringValue());
                    break;
                case "dcmAuditRecordRepositoryDeviceName":
                    loadAuditRecordRepositoryDevice(logger, reader.stringValue(), config);
                    break;
                case "dicomNetworkConnectionReference":
                    for (String connRef : reader.stringArray())
                        logger.addConnection(conns.get(JsonReader.toConnectionIndex(connRef)));
                    break;
                case "dicomInstalled":
                    logger.setInstalled(reader.booleanValue());
                    break;
                case "dcmAuditSourceID":
                    logger.setAuditSourceID(reader.stringValue());
                    break;
                case "dcmAuditEnterpriseSiteID":
                    logger.setAuditEnterpriseSiteID(reader.stringValue());
                    break;
                case "dcmAuditSourceTypeCode":
                    logger.setAuditSourceTypeCodes(reader.stringArray());
                    break;
                case "dcmAuditFacility":
                    logger.setFacility(AuditLogger.Facility.valueOf(reader.stringValue()));
                    break;
                case "dcmAuditSuccessSeverity":
                    logger.setSuccessSeverity(AuditLogger.Severity.valueOf(reader.stringValue()));
                    break;
                case "dcmAuditMinorFailureSeverity":
                    logger.setMinorFailureSeverity(AuditLogger.Severity.valueOf(reader.stringValue()));
                    break;
                case "dcmAuditSeriousFailureSeverity":
                    logger.setSeriousFailureSeverity(AuditLogger.Severity.valueOf(reader.stringValue()));
                    break;
                case "dcmAuditMajorFailureSeverity":
                    logger.setMajorFailureSeverity((AuditLogger.Severity.valueOf(reader.stringValue())));
                    break;
                case "dcmAuditApplicationName":
                    logger.setApplicationName(reader.stringValue());
                    break;
                case "dcmAuditMessageID":
                    logger.setMessageID(reader.stringValue());
                    break;
                case "dcmAuditMessageEncoding":
                    logger.setEncoding(reader.stringValue());
                    break;
                case "dcmAuditMessageBOM":
                    logger.setIncludeBOM(reader.booleanValue());
                    break;
                case "dcmAuditTimestampInUTC":
                    logger.setTimestampInUTC(reader.booleanValue());
                    break;
                case "dcmAuditMessageFormatXML":
                    logger.setFormatXML(reader.booleanValue());
                    break;
                case "dcmAuditMessageSchemaURI":
                    logger.setSchemaURI(reader.stringValue());
                    break;
                case "dcmAuditIncludeInstanceUID":
                    logger.setIncludeInstanceUID(reader.booleanValue());
                    break;
                case "dcmAuditLoggerSpoolDirectoryURI":
                    logger.setSpoolDirectoryURI(reader.stringValue());
                    break;
                case "dcmAuditLoggerRetryInterval":
                    logger.setRetryInterval(reader.intValue());
                    break;
                case "dcmAuditSuppressCriteria":
                    loadAuditSuppressCriteriaListFrom(logger, reader);
                    break;
                default:
                    reader.skipUnknownProperty();
            }
        }
    }

    private static void loadAuditRecordRepositoryDevice(
            AuditLogger auditLogger, String arrDeviceName, ConfigurationDelegate config) {
        try {
            auditLogger.setAuditRecordRepositoryDevice(config.findDevice(arrDeviceName));
        } catch (ConfigurationException e) {
            LOG.info("Failed to load Audit Record Repository {} referenced by Audit Logger", arrDeviceName, e);
            auditLogger.setAuditRecordRepositoryDeviceName(arrDeviceName);
        }
    }

    private void loadAuditSuppressCriteriaListFrom(AuditLogger logger, JsonReader reader) {
        reader.next();
        reader.expect(JsonParser.Event.START_ARRAY);
        while (reader.next() == JsonParser.Event.START_OBJECT) {
            reader.expect(JsonParser.Event.START_OBJECT);
            AuditSuppressCriteria ct = new AuditSuppressCriteria("cn");
            while (reader.next() == JsonParser.Event.KEY_NAME) {
                switch (reader.getString()) {
                    case "cn":
                        ct.setCommonName(reader.stringValue());
                        break;
                    case "dcmAuditEventID":
                        ct.setEventIDsAsStringArray(reader.stringArray());
                        break;
                    case "dcmAuditEventTypeCode":
                        ct.setEventTypeCodesAsStringArray(reader.stringArray());
                        break;
                    case "dcmAuditEventActionCode":
                        ct.setEventActionCodes(reader.stringArray());
                        break;
                    case "dcmAuditEventOutcomeIndicator":
                        ct.setEventOutcomeIndicators(reader.stringArray());
                        break;
                    case "dcmAuditUserID":
                        ct.setUserIDs(reader.stringArray());
                        break;
                    case "dcmAuditAlternativeUserID":
                        ct.setAlternativeUserIDs(reader.stringArray());
                        break;
                    case "dcmAuditUserRoleIDCode":
                        ct.setUserRoleIDCodesAsStringArray(reader.stringArray());
                        break;
                    case "dcmAuditNetworkAccessPointID":
                        ct.setNetworkAccessPointIDs(reader.stringArray());
                        break;
                    case "dcmAuditUserIsRequestor":
                        ct.setUserIsRequestor(reader.booleanValue());
                        break;
                    case "dcmParticipantObjectTypeCodes":
                        ct.setParticipantObjectTypeCodes(reader.stringArray());
                        break;
                    case "dcmParticipantObjectTypeCodeRoles":
                        ct.setParticipantObjectTypeCodeRoles(reader.stringArray());
                        break;
                    case "dcmParticipantObjectDataLifeCycle":
                        ct.setParticipantObjectDataLifeCycle(reader.stringArray());
                        break;
                    default:
                        reader.skipUnknownProperty();
                }
            }
            reader.expect(JsonParser.Event.END_OBJECT);
            logger.addAuditSuppressCriteria(ct);
        }
        reader.expect(JsonParser.Event.END_ARRAY);
    }
}
