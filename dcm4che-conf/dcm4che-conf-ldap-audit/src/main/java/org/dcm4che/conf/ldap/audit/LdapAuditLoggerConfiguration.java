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

package org.dcm4che.conf.ldap.audit;

import static org.dcm4che.conf.ldap.LdapDicomConfiguration.booleanValue;
import static org.dcm4che.conf.ldap.LdapDicomConfiguration.findConnection;
import static org.dcm4che.conf.ldap.LdapDicomConfiguration.intValue;
import static org.dcm4che.conf.ldap.LdapDicomConfiguration.storeConnRefs;
import static org.dcm4che.conf.ldap.LdapDicomConfiguration.storeDiff;
import static org.dcm4che.conf.ldap.LdapDicomConfiguration.storeNotDef;
import static org.dcm4che.conf.ldap.LdapDicomConfiguration.storeNotEmpty;
import static org.dcm4che.conf.ldap.LdapDicomConfiguration.storeNotNull;
import static org.dcm4che.conf.ldap.LdapDicomConfiguration.stringArray;
import static org.dcm4che.conf.ldap.LdapDicomConfiguration.stringValue;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;

import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.conf.ldap.LdapDicomConfigurationExtension;
import org.dcm4che.net.Device;
import org.dcm4che.net.audit.AuditLogger;
import org.dcm4che.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class LdapAuditLoggerConfiguration extends LdapDicomConfigurationExtension {

    private static final String CN_AUDIT_LOGGER = "cn=Audit Logger,";

    @Override
    protected void storeChilds(String deviceDN, Device device) throws NamingException {
        AuditLogger logger = device.getDeviceExtension(AuditLogger.class);
        if (logger != null)
            store(deviceDN, logger);
    }

    private void store(String deviceDN, AuditLogger logger)
            throws NamingException {
        config.createSubcontext(CN_AUDIT_LOGGER + deviceDN,
                storeTo(logger, deviceDN, new BasicAttributes(true)));
    }

    private Attributes storeTo(AuditLogger logger, String deviceDN, Attributes attrs) {
        attrs.put(new BasicAttribute("objectclass", "dcmAuditLogger"));
        storeNotDef(attrs, "dcmAuditFacility",
                logger.getFacility().ordinal(), 10);
        storeNotDef(attrs, "dcmAuditSuccessSeverity",
                logger.getSuccessSeverity().ordinal(), 5);
        storeNotDef(attrs, "dcmAuditMinorFailureSeverity",
                logger.getMinorFailureSeverity().ordinal(), 4);
        storeNotDef(attrs, "dcmAuditSeriousFailureSeverity",
                logger.getSeriousFailureSeverity().ordinal(), 3);
        storeNotDef(attrs, "dcmAuditMajorFailureSeverity",
                logger.getMajorFailureSeverity().ordinal(), 2);
        storeNotNull(attrs, "dcmAuditSourceID",
                logger.getAuditSourceID());
        storeNotNull(attrs, "dcmAuditEnterpriseSiteID",
                logger.getAuditEnterpriseSiteID());
        storeNotEmpty(attrs, "dcmAuditSourceTypeCode",
                logger.getAuditSourceTypeCodes());
        storeNotNull(attrs, "dcmAuditApplicationName",
                logger.getApplicationName());
        storeNotNull(attrs, "dcmAuditMessageID",
                StringUtils.nullify(logger.getMessageID(), AuditLogger.MESSAGE_ID));
        storeNotNull(attrs, "dcmAuditMessageEncoding",
                StringUtils.nullify(logger.getEncoding(), "UTF-8"));
        storeNotNull(attrs, "dcmAuditMessageSchemaURI",
                logger.getSchemaURI());
        storeNotDef(attrs, "dcmAuditMessageBOM",
                logger.isIncludeBOM(), true);
        storeNotDef(attrs, "dcmAuditMessageFormatXML",
                logger.isFormatXML(), false);
        storeNotDef(attrs, "dcmAuditTimestampInUTC",
                logger.isTimestampInUTC(), false);
        storeConnRefs(attrs, logger.getConnections(), deviceDN);
        storeNotNull(attrs, "dcmAuditRecordRepositoryDeviceReference",
                config.deviceRef(logger.getAuditRecordRepositoryDeviceName()));
        storeNotNull(attrs, "dicomInstalled",
                logger.getInstalled());
        return attrs;
    }

    @Override
    protected void loadChilds(Device device, String deviceDN)
            throws NamingException, ConfigurationException {
        Attributes attrs;
        try {
            attrs = config.getAttributes(CN_AUDIT_LOGGER + deviceDN);
        } catch (NameNotFoundException e) {
            return;
        }
        AuditLogger logger = new AuditLogger();
        loadFrom(logger, attrs);
        for (String connDN : stringArray(
                attrs.get("dicomNetworkConnectionReference")))
            logger.addConnection(
                    findConnection(connDN, deviceDN, device));
        String arrDeviceDN = stringValue(
                attrs.get("dcmAuditRecordRepositoryDeviceReference"), null);
        logger.setAuditRecordRepositoryDevice(deviceDN.equals(arrDeviceDN)
                ? device
                : config.loadDevice(arrDeviceDN));
        device.addDeviceExtension(logger);
    }

    private void loadFrom(AuditLogger logger, Attributes attrs) throws NamingException {
        logger.setFacility(AuditLogger.Facility.values()
                [intValue(attrs.get("dcmAuditFacility"), 10)]);
        logger.setSuccessSeverity(AuditLogger.Severity.values()
                [intValue(attrs.get("dcmAuditSuccessSeverity"), 5)]);
        logger.setMinorFailureSeverity(AuditLogger.Severity.values()
                [intValue(attrs.get("dcmAuditMinorFailureSeverity"), 4)]);
        logger.setSeriousFailureSeverity(AuditLogger.Severity.values()
                [intValue(attrs.get("dcmAuditSeriousFailureSeverity"), 3)]);
        logger.setMajorFailureSeverity(AuditLogger.Severity.values()
                [intValue(attrs.get("dcmAuditMajorFailureSeverity"), 2)]);
        logger.setAuditSourceID(stringValue(attrs.get("dcmAuditSourceID"), null));
        logger.setAuditEnterpriseSiteID(
                stringValue(attrs.get("dcmAuditEnterpriseSiteID"), null));
        logger.setAuditSourceTypeCodes(
                stringArray(attrs.get("dcmAuditSourceTypeCode")));
        logger.setApplicationName(
                stringValue(attrs.get("dcmAuditApplicationName"), null));
        logger.setMessageID(
                stringValue(attrs.get("dcmAuditMessageID"), AuditLogger.MESSAGE_ID));
        logger.setEncoding(
                stringValue(attrs.get("dcmAuditMessageEncoding"), "UTF-8"));
        logger.setSchemaURI(
                stringValue(attrs.get("dcmAuditMessageSchemaURI"), null));
        logger.setIncludeBOM(
                booleanValue(attrs.get("dcmAuditMessageBOM"), true));
        logger.setIncludeBOM(
                booleanValue(attrs.get("dcmAuditMessageFormatXML"), false));
        logger.setTimestampInUTC(
                booleanValue(attrs.get("dcmAuditTimestampInUTC"), false));
        logger.setInstalled(
                booleanValue(attrs.get("dicomInstalled"), null));
    }

    @Override
    protected void mergeChilds(Device prev, Device device, String deviceDN)
            throws NamingException {
        AuditLogger prevLogger = prev.getDeviceExtension(AuditLogger.class);
        AuditLogger logger = device.getDeviceExtension(AuditLogger.class);
        if (logger == null) {
            if (prevLogger != null)
                config.destroySubcontextWithChilds(CN_AUDIT_LOGGER + deviceDN);
            return;
        }
        if (prevLogger == null) {
            store(deviceDN, logger);
            return;
        }
        config.modifyAttributes(CN_AUDIT_LOGGER + deviceDN,
                storeDiffs(prevLogger, logger, deviceDN,
                        new ArrayList<ModificationItem>()));
    }

    private List<ModificationItem> storeDiffs(AuditLogger a, AuditLogger b,
            String deviceDN, ArrayList<ModificationItem> mods) {
        storeDiff(mods, "dcmAuditFacility",
                a.getFacility().ordinal(),
                b.getFacility().ordinal(),
                10);
        storeDiff(mods, "dcmAuditSuccessSeverity",
                a.getSuccessSeverity().ordinal(),
                b.getSuccessSeverity().ordinal(),
                5);
        storeDiff(mods, "dcmAuditMinorFailureSeverity",
                a.getMinorFailureSeverity().ordinal(),
                b.getMinorFailureSeverity().ordinal(),
                4);
        storeDiff(mods, "dcmAuditSeriousFailureSeverity",
                a.getSeriousFailureSeverity().ordinal(),
                b.getSeriousFailureSeverity().ordinal(),
                3);
        storeDiff(mods, "dcmAuditMajorFailureSeverity",
                a.getMajorFailureSeverity().ordinal(),
                b.getMajorFailureSeverity().ordinal(),
                2);
        storeDiff(mods, "dcmAuditSourceID",
                a.getAuditSourceID(),
                b.getAuditSourceID());
        storeDiff(mods, "dcmAuditEnterpriseSiteID",
                a.getAuditEnterpriseSiteID(),
                b.getAuditEnterpriseSiteID());
        storeDiff(mods, "dcmAuditSourceTypeCode",
                a.getAuditSourceTypeCodes(),
                b.getAuditSourceTypeCodes());
        storeDiff(mods, "dcmAuditApplicationName",
                a.getApplicationName(),
                b.getApplicationName());
        storeDiff(mods, "dcmAuditMessageID",
                a.getMessageID(),
                b.getMessageID());
        storeDiff(mods, "dcmAuditMessageEncoding",
                StringUtils.nullify(a.getEncoding(), "UTF-8"),
                StringUtils.nullify(b.getEncoding(), "UTF-8"));
        storeDiff(mods, "dcmAuditMessageSchemaURI",
                a.getSchemaURI(),
                b.getSchemaURI());
        storeDiff(mods, "dcmAuditMessageBOM",
                a.isIncludeBOM(),
                b.isIncludeBOM(),
                true);
        storeDiff(mods, "dcmAuditMessageFormatXML",
                a.isFormatXML(),
                b.isFormatXML(),
                false);
        storeDiff(mods, "dcmAuditTimestampInUTC",
                a.isTimestampInUTC(),
                b.isTimestampInUTC(),
                false);
        storeDiff(mods, "dicomNetworkConnectionReference",
                a.getConnections(),
                b.getConnections(),
                deviceDN);
        storeDiff(mods, "dcmAuditRecordRepositoryDeviceReference",
                config.deviceRef(a.getAuditRecordRepositoryDeviceName()),
                config.deviceRef(b.getAuditRecordRepositoryDeviceName()));
        storeDiff(mods, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled());
        return mods;
    }

}
