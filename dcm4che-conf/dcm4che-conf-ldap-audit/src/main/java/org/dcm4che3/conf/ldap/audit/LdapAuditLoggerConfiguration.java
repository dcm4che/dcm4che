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
 * Java(TM), hosted at https://github.com/dcm4che.
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

package org.dcm4che3.conf.ldap.audit;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import org.dcm4che3.audit.AuditMessages;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.ldap.LdapDicomConfigurationExtension;
import org.dcm4che3.conf.api.ConfigurationChanges;
import org.dcm4che3.conf.ldap.LdapUtils;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditLoggerDeviceExtension;
import org.dcm4che3.net.audit.AuditSuppressCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
public class LdapAuditLoggerConfiguration extends LdapDicomConfigurationExtension {

    private static final Logger LOG = LoggerFactory.getLogger(
            LdapAuditLoggerConfiguration.class);

    @Override
    protected void storeChilds(ConfigurationChanges diffs, String deviceDN, Device device) throws NamingException {
        AuditLoggerDeviceExtension auditLoggerExt = device.getDeviceExtension(AuditLoggerDeviceExtension.class);
        if (auditLoggerExt == null)
            return;

        for (AuditLogger auditLogger : auditLoggerExt.getAuditLoggers())
            store(diffs, deviceDN, auditLogger);
    }

    private void store(ConfigurationChanges diffs, String deviceDN, AuditLogger logger)
            throws NamingException {
        String appDN = auditLoggerDN(logger.getCommonName(), deviceDN);
        ConfigurationChanges.ModifiedObject ldapObj =
                ConfigurationChanges.addModifiedObject(diffs, appDN, ConfigurationChanges.ChangeType.C);
        config.createSubcontext(appDN,
                storeTo(ConfigurationChanges.nullifyIfNotVerbose(diffs, ldapObj),
                        logger, deviceDN, new BasicAttributes(true)));
        for (AuditSuppressCriteria criteria : logger.getAuditSuppressCriteriaList()) {
            String dn = LdapUtils.dnOf("cn", criteria.getCommonName(), appDN);
            ConfigurationChanges.ModifiedObject ldapObj1 =
                    ConfigurationChanges.addModifiedObjectIfVerbose(diffs, dn, ConfigurationChanges.ChangeType.C);
            config.createSubcontext(dn, storeTo(ldapObj1, criteria, new BasicAttributes(true)));
        }
    }

    private String auditLoggerDN(String name, String deviceDN) {
        return LdapUtils.dnOf("cn" , name, deviceDN);
    }

    private Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj, AuditLogger logger, String deviceDN, Attributes attrs) {
        attrs.put(new BasicAttribute("objectclass", "dcmAuditLogger"));
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "cn", logger.getCommonName(), null);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmAuditFacility",
                logger.getFacility().ordinal(), 10);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmAuditSuccessSeverity",
                logger.getSuccessSeverity().ordinal(), 5);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmAuditMinorFailureSeverity",
                logger.getMinorFailureSeverity().ordinal(), 4);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmAuditSeriousFailureSeverity",
                logger.getSeriousFailureSeverity().ordinal(), 3);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmAuditMajorFailureSeverity",
                logger.getMajorFailureSeverity().ordinal(), 2);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmAuditSourceID",
                logger.getAuditSourceID(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmAuditEnterpriseSiteID",
                logger.getAuditEnterpriseSiteID(), null);
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmAuditSourceTypeCode",
                logger.getAuditSourceTypeCodes());
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmAuditApplicationName",
                logger.getApplicationName(), null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmAuditMessageID",
                logger.getMessageID(), AuditLogger.MESSAGE_ID);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmAuditMessageEncoding",
                logger.getEncoding(), "UTF-8");
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmAuditMessageSchemaURI",
                logger.getSchemaURI(), AuditMessages.SCHEMA_URI);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmAuditMessageBOM",
                logger.isIncludeBOM(), true);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmAuditMessageFormatXML",
                logger.isFormatXML(), false);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmAuditTimestampInUTC",
                logger.isTimestampInUTC(), false);
        LdapUtils.storeConnRefs(ldapObj, attrs, logger.getConnections(), deviceDN);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmAuditRecordRepositoryDeviceReference",
                config.deviceRef(logger.getAuditRecordRepositoryDeviceNameNotNull()), null);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmAuditIncludeInstanceUID",
                logger.isIncludeInstanceUID(), false);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmAuditLoggerSpoolDirectoryURI",
                logger.getSpoolDirectoryURI(), null);
        LdapUtils.storeNotDef(ldapObj, attrs, "dcmAuditLoggerRetryInterval",
                logger.getRetryInterval(), 0);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomInstalled",
                logger.getInstalled(), null);
        return attrs;
    }

    private Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj, AuditSuppressCriteria criteria, BasicAttributes attrs) {
        attrs.put(new BasicAttribute("objectclass", "dcmAuditSuppressCriteria"));
        attrs.put(new BasicAttribute("cn", criteria.getCommonName()));
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmAuditEventID",
                criteria.getEventIDsAsStringArray());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmAuditEventTypeCode",
                criteria.getEventTypeCodesAsStringArray());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmAuditEventActionCode",
                criteria.getEventActionCodes());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmAuditEventOutcomeIndicator",
                criteria.getEventOutcomeIndicators());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmAuditUserID",
                criteria.getUserIDs());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmAuditAlternativeUserID",
                criteria.getAlternativeUserIDs());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmAuditUserRoleIDCode",
                criteria.getUserRoleIDCodesAsStringArray());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmAuditNetworkAccessPointID",
                criteria.getNetworkAccessPointIDs());
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmAuditUserIsRequestor",
                criteria.getUserIsRequestor(), null);
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmParticipantObjectTypeCodes",
                criteria.getParticipantObjectTypeCodes());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "getParticipantObjectTypeCodeRoles",
                criteria.getParticipantObjectTypeCodeRoles());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "getParticipantObjectDataLifeCycle",
                criteria.getParticipantObjectDataLifeCycle());
        return attrs;
    }


    @Override
    protected void loadChilds(Device device, String deviceDN) throws NamingException {
        NamingEnumeration<SearchResult> ne =
                config.search(deviceDN, "(objectclass=dcmAuditLogger)");
        try {
            if (!ne.hasMore())
                return;

            AuditLoggerDeviceExtension ext = new AuditLoggerDeviceExtension();
            device.addDeviceExtension(ext);
            do {
                ext.addAuditLogger(
                        loadAuditLogger(ne.next(), deviceDN, device));
            } while (ne.hasMore());
        } finally {
            LdapUtils.safeClose(ne);
        }
    }

    private AuditLogger loadAuditLogger(SearchResult sr, String deviceDN, Device device)
            throws NamingException {
        Attributes attrs = sr.getAttributes();
        AuditLogger auditLogger = new AuditLogger(LdapUtils.stringValue(attrs.get("cn"), null));
        loadFrom(auditLogger, attrs);
        for (String connDN : LdapUtils.stringArray(attrs.get("dicomNetworkConnectionReference")))
            auditLogger.addConnection(LdapUtils.findConnection(connDN, deviceDN, device));
        String arrDeviceDN = LdapUtils.stringValue(attrs.get("dcmAuditRecordRepositoryDeviceReference"), null);
        if (deviceDN.equals(arrDeviceDN)) {
            auditLogger.setAuditRecordRepositoryDevice(device);
        } else {
            loadAuditRecordRepositoryDevice(auditLogger, arrDeviceDN);
        }
       loadAuditSuppressCriteria(auditLogger, auditLoggerDN(auditLogger.getCommonName(), deviceDN));
        return auditLogger;
    }

    private void loadAuditRecordRepositoryDevice(AuditLogger auditLogger, String arrDeviceDN) {
        try {
            auditLogger.setAuditRecordRepositoryDevice(config.loadDevice(arrDeviceDN));
        } catch (ConfigurationException e) {
            LOG.info("Failed to load Audit Record Repository {} referenced by Audit Logger", arrDeviceDN, e);
            auditLogger.setAuditRecordRepositoryDeviceName(
                    LdapUtils.cutAttrValueFromDN(arrDeviceDN, "dicomDeviceName"));
        }
    }

    private void loadAuditSuppressCriteria(AuditLogger auditLogger, String auditLoggerDN)
            throws NamingException {
        NamingEnumeration<SearchResult> ne = 
                config.search(auditLoggerDN, "(objectclass=dcmAuditSuppressCriteria)");
        try {
            while (ne.hasMore()) {
                SearchResult sr = ne.next();
                Attributes attrs = sr.getAttributes();
                AuditSuppressCriteria criteria =
                        new AuditSuppressCriteria((String) attrs.get("cn").get());
                criteria.setEventIDsAsStringArray(LdapUtils.stringArray(
                        attrs.get("dcmAuditEventID")));
                criteria.setEventTypeCodesAsStringArray(LdapUtils.stringArray(
                        attrs.get("dcmAuditEventTypeCode")));
                criteria.setEventActionCodes(LdapUtils.stringArray(
                        attrs.get("dcmAuditEventActionCode")));
                criteria.setEventOutcomeIndicators(LdapUtils.stringArray(
                        attrs.get("dcmAuditEventOutcomeIndicator")));
                criteria.setUserIDs(LdapUtils.stringArray(
                        attrs.get("dcmAuditUserID")));
                criteria.setAlternativeUserIDs(LdapUtils.stringArray(
                        attrs.get("dcmAuditAlternativeUserID")));
                criteria.setUserRoleIDCodesAsStringArray(LdapUtils.stringArray(
                        attrs.get("dcmAuditUserRoleIDCode")));
                criteria.setNetworkAccessPointIDs(LdapUtils.stringArray(
                        attrs.get("dcmAuditNetworkAccessPointID")));
                criteria.setUserIsRequestor(LdapUtils.booleanValue(
                        attrs.get("dcmAuditUserIsRequestor"), null));
                criteria.setParticipantObjectTypeCodes(LdapUtils.stringArray(
                        attrs.get("dcmParticipantObjectTypeCodes")));
                criteria.setParticipantObjectTypeCodeRoles(LdapUtils.stringArray(
                        attrs.get("dcmParticipantObjectTypeCodeRoles")));
                criteria.setParticipantObjectDataLifeCycle(LdapUtils.stringArray(
                        attrs.get("dcmParticipantObjectDataLifeCycle")));
                auditLogger.addAuditSuppressCriteria(criteria);
            }
        } finally {
            LdapUtils.safeClose(ne);
        }
    }

    private void loadFrom(AuditLogger logger, Attributes attrs) throws NamingException {
        logger.setFacility(AuditLogger.Facility.values()
                [LdapUtils.intValue(attrs.get("dcmAuditFacility"), 10)]);
        logger.setSuccessSeverity(AuditLogger.Severity.values()
                [LdapUtils.intValue(attrs.get("dcmAuditSuccessSeverity"), 5)]);
        logger.setMinorFailureSeverity(AuditLogger.Severity.values()
                [LdapUtils.intValue(attrs.get("dcmAuditMinorFailureSeverity"), 4)]);
        logger.setSeriousFailureSeverity(AuditLogger.Severity.values()
                [LdapUtils.intValue(attrs.get("dcmAuditSeriousFailureSeverity"), 3)]);
        logger.setMajorFailureSeverity(AuditLogger.Severity.values()
                [LdapUtils.intValue(attrs.get("dcmAuditMajorFailureSeverity"), 2)]);
        logger.setAuditSourceID(LdapUtils.stringValue(attrs.get("dcmAuditSourceID"), null));
        logger.setAuditEnterpriseSiteID(
                LdapUtils.stringValue(attrs.get("dcmAuditEnterpriseSiteID"), null));
        logger.setAuditSourceTypeCodes(
                LdapUtils.stringArray(attrs.get("dcmAuditSourceTypeCode")));
        logger.setApplicationName(
                LdapUtils.stringValue(attrs.get("dcmAuditApplicationName"), null));
        logger.setMessageID(
                LdapUtils.stringValue(attrs.get("dcmAuditMessageID"), AuditLogger.MESSAGE_ID));
        logger.setEncoding(
                LdapUtils.stringValue(attrs.get("dcmAuditMessageEncoding"), "UTF-8"));
        logger.setSchemaURI(
                LdapUtils.stringValue(attrs.get("dcmAuditMessageSchemaURI"), AuditMessages.SCHEMA_URI));
        logger.setIncludeBOM(
                LdapUtils.booleanValue(attrs.get("dcmAuditMessageBOM"), true));
        logger.setFormatXML(
                LdapUtils.booleanValue(attrs.get("dcmAuditMessageFormatXML"), false));
        logger.setTimestampInUTC(
                LdapUtils.booleanValue(attrs.get("dcmAuditTimestampInUTC"), false));
        logger.setIncludeInstanceUID(
                LdapUtils.booleanValue(attrs.get("dcmAuditIncludeInstanceUID"), false));
        logger.setSpoolDirectoryURI(
                LdapUtils.stringValue(attrs.get("dcmAuditLoggerSpoolDirectoryURI"), null));
        logger.setRetryInterval(
                LdapUtils.intValue(attrs.get("dcmAuditLoggerRetryInterval"), 0));
        logger.setInstalled(
                LdapUtils.booleanValue(attrs.get("dicomInstalled"), null));
    }

    @Override
    protected void mergeChilds(ConfigurationChanges diffs, Device prev, Device device, String deviceDN)
            throws NamingException {
        AuditLoggerDeviceExtension prevAuditLoggerExt = prev.getDeviceExtension(AuditLoggerDeviceExtension.class);
        AuditLoggerDeviceExtension auditLoggerExt = device.getDeviceExtension(AuditLoggerDeviceExtension.class);

        if (prevAuditLoggerExt != null)
            for (String appName : prevAuditLoggerExt.getAuditLoggerNames()) {
                if (auditLoggerExt == null || !auditLoggerExt.containsAuditLogger(appName)) {
                    String dn = auditLoggerDN(appName, deviceDN);
                    config.destroySubcontextWithChilds(dn);
                    ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.D);
                }
            }

        if (auditLoggerExt == null)
            return;

        for (AuditLogger logger : auditLoggerExt.getAuditLoggers()) {
            String appName = logger.getCommonName();
            if (prevAuditLoggerExt == null || !prevAuditLoggerExt.containsAuditLogger(appName)) {
                store(diffs, deviceDN, logger);
            }
            else
                merge(diffs, prevAuditLoggerExt.getAuditLogger(appName), logger, deviceDN);
        }
    }

    private void merge(ConfigurationChanges diffs, AuditLogger prevLogger, AuditLogger logger, String deviceDN)
            throws NamingException {
        String appDN = auditLoggerDN(logger.getCommonName(), deviceDN);
        ConfigurationChanges.ModifiedObject ldapObj =
                ConfigurationChanges.addModifiedObject(diffs, appDN, ConfigurationChanges.ChangeType.U);
        config.modifyAttributes(appDN,
                storeDiffs(ldapObj, prevLogger, logger, deviceDN, new ArrayList<ModificationItem>()));
        ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
        mergeAuditSuppressCriteria(diffs, prevLogger, logger, appDN);
    }

    private void mergeAuditSuppressCriteria(ConfigurationChanges diffs, AuditLogger prevLogger,
                                            AuditLogger logger, String auditLoggerDN) throws NamingException {
        for (AuditSuppressCriteria prevCriteria : prevLogger.getAuditSuppressCriteriaList()) {
            String cn = prevCriteria.getCommonName();
            if (logger.findAuditSuppressCriteriaByCommonName(cn) == null) {
                String dn = LdapUtils.dnOf("cn", cn, auditLoggerDN);
                config.destroySubcontext(dn);
                ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.D);
            }
        }
        for (AuditSuppressCriteria criteria : logger.getAuditSuppressCriteriaList()) {
            String cn = criteria.getCommonName();
            String dn = LdapUtils.dnOf("cn", cn, auditLoggerDN);
            AuditSuppressCriteria prev = prevLogger.findAuditSuppressCriteriaByCommonName(cn);
            if (prev == null) {
                ConfigurationChanges.ModifiedObject ldapObj =
                        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.C);
                config.createSubcontext(dn,
                        storeTo(ConfigurationChanges.nullifyIfNotVerbose(diffs, ldapObj),
                                criteria, new BasicAttributes(true)));
            } else {
                ConfigurationChanges.ModifiedObject ldapObj =
                        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.U);
                config.modifyAttributes(dn, storeDiffs(ldapObj, prev, criteria,
                        new ArrayList<ModificationItem>()));
                ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
            }
        }
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, AuditLogger a, AuditLogger b,
                                              String deviceDN, ArrayList<ModificationItem> mods) {
        LdapUtils.storeDiff(ldapObj, mods, "dcmAuditFacility",
                a.getFacility().ordinal(),
                b.getFacility().ordinal(),
                10);
        LdapUtils.storeDiff(ldapObj, mods, "dcmAuditSuccessSeverity",
                a.getSuccessSeverity().ordinal(),
                b.getSuccessSeverity().ordinal(),
                5);
        LdapUtils.storeDiff(ldapObj, mods, "dcmAuditMinorFailureSeverity",
                a.getMinorFailureSeverity().ordinal(),
                b.getMinorFailureSeverity().ordinal(),
                4);
        LdapUtils.storeDiff(ldapObj, mods, "dcmAuditSeriousFailureSeverity",
                a.getSeriousFailureSeverity().ordinal(),
                b.getSeriousFailureSeverity().ordinal(),
                3);
        LdapUtils.storeDiff(ldapObj, mods, "dcmAuditMajorFailureSeverity",
                a.getMajorFailureSeverity().ordinal(),
                b.getMajorFailureSeverity().ordinal(),
                2);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmAuditSourceID",
                a.getAuditSourceID(),
                b.getAuditSourceID(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmAuditEnterpriseSiteID",
                a.getAuditEnterpriseSiteID(),
                b.getAuditEnterpriseSiteID(), null);
        LdapUtils.storeDiff(ldapObj, mods, "dcmAuditSourceTypeCode",
                a.getAuditSourceTypeCodes(),
                b.getAuditSourceTypeCodes());
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmAuditApplicationName",
                a.getApplicationName(),
                b.getApplicationName(), null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmAuditMessageID",
                a.getMessageID(),
                b.getMessageID(),
                AuditLogger.MESSAGE_ID);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmAuditMessageEncoding",
                a.getEncoding(),
                b.getEncoding(),
                "UTF-8");
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmAuditMessageSchemaURI",
                a.getSchemaURI(),
                b.getSchemaURI(),
                AuditMessages.SCHEMA_URI);
        LdapUtils.storeDiff(ldapObj, mods, "dcmAuditMessageBOM",
                a.isIncludeBOM(),
                b.isIncludeBOM(),
                true);
        LdapUtils.storeDiff(ldapObj, mods, "dcmAuditMessageFormatXML",
                a.isFormatXML(),
                b.isFormatXML(),
                false);
        LdapUtils.storeDiff(ldapObj, mods, "dcmAuditTimestampInUTC",
                a.isTimestampInUTC(),
                b.isTimestampInUTC(),
                false);
        LdapUtils.storeDiff(ldapObj, mods, "dicomNetworkConnectionReference",
                a.getConnections(),
                b.getConnections(),
                deviceDN);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmAuditRecordRepositoryDeviceReference",
                config.deviceRef(a.getAuditRecordRepositoryDeviceNameNotNull()),
                config.deviceRef(b.getAuditRecordRepositoryDeviceNameNotNull()), null);
        LdapUtils.storeDiff(ldapObj, mods, "dcmAuditIncludeInstanceUID",
                a.isIncludeInstanceUID(), 
                b.isIncludeInstanceUID(), 
                false);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmAuditLoggerSpoolDirectoryURI",
                a.getSpoolDirectoryURI(),
                b.getSpoolDirectoryURI(), null);
        LdapUtils.storeDiff(ldapObj, mods, "dcmAuditLoggerRetryInterval",
                a.getRetryInterval(), 
                b.getRetryInterval(), 
                0);
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled(), null);
        return mods;
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, AuditSuppressCriteria a,
                                              AuditSuppressCriteria b, ArrayList<ModificationItem> mods) {
        LdapUtils.storeDiff(ldapObj, mods, "dcmAuditEventID",
                a.getEventIDsAsStringArray(),
                b.getEventIDsAsStringArray());
        LdapUtils.storeDiff(ldapObj, mods, "dcmAuditEventTypeCode",
                a.getEventTypeCodesAsStringArray(),
                b.getEventTypeCodesAsStringArray());
        LdapUtils.storeDiff(ldapObj, mods, "dcmAuditEventActionCode",
                a.getEventActionCodes(),
                b.getEventActionCodes());
        LdapUtils.storeDiff(ldapObj, mods, "dcmAuditEventOutcomeIndicator",
                a.getEventOutcomeIndicators(),
                b.getEventOutcomeIndicators());
        LdapUtils.storeDiff(ldapObj, mods, "dcmAuditUserID",
                a.getUserIDs(),
                b.getUserIDs());
        LdapUtils.storeDiff(ldapObj, mods, "dcmAuditAlternativeUserID",
                a.getAlternativeUserIDs(),
                b.getAlternativeUserIDs());
        LdapUtils.storeDiff(ldapObj, mods, "dcmAuditUserRoleIDCode",
                a.getUserRoleIDCodesAsStringArray(),
                b.getUserRoleIDCodesAsStringArray());
        LdapUtils.storeDiff(ldapObj, mods, "dcmAuditNetworkAccessPointID",
                a.getNetworkAccessPointIDs(),
                b.getNetworkAccessPointIDs());
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmAuditUserIsRequestor",
                a.getUserIsRequestor(),
                b.getUserIsRequestor(), null);
        LdapUtils.storeDiff(ldapObj, mods, "dcmParticipantObjectTypeCodes",
                a.getParticipantObjectTypeCodes(),
                b.getParticipantObjectTypeCodes());
        LdapUtils.storeDiff(ldapObj, mods, "dcmParticipantObjectTypeCodeRoles",
                a.getParticipantObjectTypeCodeRoles(),
                b.getParticipantObjectTypeCodeRoles());
        LdapUtils.storeDiff(ldapObj, mods, "dcmParticipantObjectDataLifeCycle",
                a.getParticipantObjectDataLifeCycle(),
                b.getParticipantObjectDataLifeCycle());
        return mods;
    }

}
