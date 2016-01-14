package org.dcm4che3.conf.json.audit;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.json.ConfigurationDelegate;
import org.dcm4che3.conf.json.JsonConfigurationExtension;
import org.dcm4che3.conf.json.JsonReader;
import org.dcm4che3.conf.json.JsonWriter;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditSuppressCriteria;

import javax.json.stream.JsonParser;
import java.util.List;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jan 2016
 */
public class JsonAuditLoggerConfiguration extends JsonConfigurationExtension {
    @Override
    protected void storeTo(Device device, JsonWriter writer) {
        AuditLogger auditLogger = device.getDeviceExtension(AuditLogger.class);
        if (auditLogger == null)
            return;

        writer.writeStartObject("dcmAuditLogger");
        writer.writeNotNull("dcmAuditRecordRepositoryDeviceName",
                auditLogger.getAuditRecordRepositoryDevice().getDeviceName());
        writer.writeConnRefs(device.listConnections(), auditLogger.getConnections());
        writer.writeNotNull("dicomInstalled", auditLogger.getInstalled());
        writer.writeNotNull("dcmAuditSourceID", auditLogger.getAuditSourceID());
        writer.writeNotNull("dcmAuditEnterpriseSiteID", auditLogger.getAuditEnterpriseSiteID());
        writer.writeNotEmpty("dcmAuditSourceTypeCode", auditLogger.getAuditSourceTypeCodes());
        writer.writeNotNull("dcmAuditFacility", auditLogger.getFacility());
        writer.writeNotNull("dcmAuditSuccessSeverity", auditLogger.getSuccessSeverity());
        writer.writeNotNull("dcmAuditMinorFailureSeverity", auditLogger.getMinorFailureSeverity());
        writer.writeNotNull("dcmAuditSeriousFailureSeverity", auditLogger.getSeriousFailureSeverity());
        writer.writeNotNull("dcmAuditMajorFailureSeverity", auditLogger.getMajorFailureSeverity());
        writer.writeNotNull("dcmAuditApplicationName", auditLogger.getApplicationName());
        writer.writeNotNull("dcmAuditMessageID", auditLogger.getMessageID());
        writer.writeNotNull("dcmAuditMessageEncoding", auditLogger.getEncoding());
        writer.writeNotNull("dcmAuditMessageBOM", auditLogger.isIncludeBOM());
        writer.writeNotNull("dcmAuditTimestampInUTC", auditLogger.isTimestampInUTC());
        writer.writeNotNull("dcmAuditMessageFormatXML", auditLogger.isFormatXML());
        writer.writeNotNull("dcmAuditMessageSchemaURI", auditLogger.getSchemaURI());
        writer.writeNotNull("dcmAuditIncludeInstanceUID", auditLogger.isIncludeInstanceUID());
        writer.writeNotNull("dcmAuditLoggerSpoolDirectoryURI", auditLogger.getSpoolDirectoryURI());
        writer.writeNotNull("dcmAuditLoggerRetryInterval", auditLogger.getRetryInterval());
        writeAuditSuppressCriteriaList(writer, auditLogger.getAuditSuppressCriteriaList());
        writer.writeEnd();
    }


    protected void writeAuditSuppressCriteriaList (
            JsonWriter writer, List<AuditSuppressCriteria> list) {
        writer.writeStartArray("dcmAuditSuppressCriteria");
        for (AuditSuppressCriteria suppressCriteria : list) {
            writer.writeStartObject();
            writer.writeNotNull("cn", suppressCriteria.getCommonName());
            writer.writeNotEmpty("dcmAuditEventID", suppressCriteria.getEventIDsAsStringArray());
            writer.writeNotEmpty("dcmAuditEventTypeCode", suppressCriteria.getEventTypeCodesAsStringArray());
            writer.writeNotEmpty("dcmAuditEventActionCode", suppressCriteria.getEventActionCodes());
            writer.writeNotEmpty("dcmAuditEventOutcomeIndicator", suppressCriteria.getEventOutcomeIndicators());
            writer.writeNotEmpty("dcmAuditUserID", suppressCriteria.getUserIDs());
            writer.writeNotEmpty("dcmAuditAlternativeUserID", suppressCriteria.getAlternativeUserIDs());
            writer.writeNotEmpty("dcmAuditUserRoleIDCode", suppressCriteria.getUserRoleIDCodes());
            writer.writeNotEmpty("dcmAuditNetworkAccessPointID", suppressCriteria.getNetworkAccessPointIDs());
            writer.writeNotNull("dcmAuditUserIsRequestor", suppressCriteria.getUserIsRequestor());
            writer.writeEnd();
        }
        writer.writeEnd();
    }

    @Override
    public boolean loadDeviceExtension(Device device, JsonReader reader, ConfigurationDelegate config)
            throws ConfigurationException {
        if (!reader.getString().equals("dcmAuditLogger"))
            return false;

        reader.next();
        reader.expect(JsonParser.Event.START_OBJECT);
        AuditLogger logger = new AuditLogger();
        loadFrom(logger, reader, device.listConnections(), config);
        device.addDeviceExtension(logger);
        reader.expect(JsonParser.Event.END_OBJECT);
        return true;
    }

    private void loadFrom(AuditLogger logger, JsonReader reader, List<Connection> conns,
                          ConfigurationDelegate config) throws ConfigurationException {
        while (reader.next() == JsonParser.Event.KEY_NAME) {
            switch (reader.getString()) {
                case "dcmAuditRecordRepositoryDeviceName":
                    logger.setAuditRecordRepositoryDevice(config.findDevice(reader.stringValue()));
                    break;
                case "dicomNetworkConnectionReference":
                    for (String connRef : reader.stringArray())
                        logger.addConnection(conns.get(JsonReader.toConnectionIndex(connRef)));
                    break;
                case "dicomInstalled":
                    logger.setInstalled(reader.booleanValue());
                    break;
                //TODO
                default:
                    reader.skipUnknownProperty();
            }
        }
    }
}
