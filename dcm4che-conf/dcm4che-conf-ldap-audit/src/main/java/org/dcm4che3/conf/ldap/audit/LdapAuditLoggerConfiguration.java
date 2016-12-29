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

package org.dcm4che3.conf.ldap.audit;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.ldap.LdapDicomConfigurationExtension;
import org.dcm4che3.conf.ldap.LdapUtils;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditLoggerDeviceExtension;
import org.dcm4che3.net.audit.AuditSuppressCriteria;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
public class LdapAuditLoggerConfiguration extends LdapDicomConfigurationExtension {

    private static final String CN_AUDIT_LOGGER = "cn=Audit Logger,";

    private static final Logger LOG = LoggerFactory.getLogger(
            LdapAuditLoggerConfiguration.class);

    @Override
    protected void storeChilds(String deviceDN, Device device) throws NamingException {
        AuditLoggerDeviceExtension auditLoggerExt = device.getDeviceExtension(AuditLoggerDeviceExtension.class);
        if (auditLoggerExt == null)
            return;

        for (AuditLogger auditLogger : auditLoggerExt.getAuditLoggers())
            store(deviceDN, auditLogger);
    }

    private void store(String deviceDN, AuditLogger logger)
            throws NamingException {
        String appDN = auditLoggerDN(logger.getCommonName(), deviceDN);
        config.createSubcontext(appDN,
                storeTo(logger, deviceDN, new BasicAttributes(true)));
        for (AuditSuppressCriteria criteria : logger.getAuditSuppressCriteriaList()) {
            config.createSubcontext(
                    LdapUtils.dnOf("cn", criteria.getCommonName(), appDN),
                    storeTo(criteria, new BasicAttributes(true)));
        }
    }

    private String auditLoggerDN(String name, String deviceDN) {
        return LdapUtils.dnOf("cn" , name, deviceDN);
    }

    private Attributes storeTo(AuditLogger logger, String deviceDN, Attributes attrs) {
        attrs.put(new BasicAttribute("objectclass", "dcmAuditLogger"));
        LdapUtils.storeNotNull(attrs, "cn", logger.getCommonName());
        LdapUtils.storeNotDef(attrs, "dcmAuditFacility",
                logger.getFacility().ordinal(), 10);
        LdapUtils.storeNotDef(attrs, "dcmAuditSuccessSeverity",
                logger.getSuccessSeverity().ordinal(), 5);
        LdapUtils.storeNotDef(attrs, "dcmAuditMinorFailureSeverity",
                logger.getMinorFailureSeverity().ordinal(), 4);
        LdapUtils.storeNotDef(attrs, "dcmAuditSeriousFailureSeverity",
                logger.getSeriousFailureSeverity().ordinal(), 3);
        LdapUtils.storeNotDef(attrs, "dcmAuditMajorFailureSeverity",
                logger.getMajorFailureSeverity().ordinal(), 2);
        LdapUtils.storeNotNull(attrs, "dcmAuditSourceID",
                logger.getAuditSourceID());
        LdapUtils.storeNotNull(attrs, "dcmAuditEnterpriseSiteID",
                logger.getAuditEnterpriseSiteID());
        LdapUtils.storeNotEmpty(attrs, "dcmAuditSourceTypeCode",
                logger.getAuditSourceTypeCodes());
        LdapUtils.storeNotNull(attrs, "dcmAuditApplicationName",
                logger.getApplicationName());
        LdapUtils.storeNotNull(attrs, "dcmAuditMessageID",
                StringUtils.nullify(logger.getMessageID(), AuditLogger.MESSAGE_ID));
        LdapUtils.storeNotNull(attrs, "dcmAuditMessageEncoding",
                StringUtils.nullify(logger.getEncoding(), "UTF-8"));
        LdapUtils.storeNotNull(attrs, "dcmAuditMessageSchemaURI",
                logger.getSchemaURI());
        LdapUtils.storeNotDef(attrs, "dcmAuditMessageBOM",
                logger.isIncludeBOM(), true);
        LdapUtils.storeNotDef(attrs, "dcmAuditMessageFormatXML",
                logger.isFormatXML(), false);
        LdapUtils.storeNotDef(attrs, "dcmAuditTimestampInUTC",
                logger.isTimestampInUTC(), false);
        LdapUtils.storeConnRefs(attrs, logger.getConnections(), deviceDN);
        LdapUtils.storeNotNull(attrs, "dcmAuditRecordRepositoryDeviceReference",
                config.deviceRef(logger.getAuditRecordRepositoryDeviceName()));
        LdapUtils.storeNotDef(attrs, "dcmAuditIncludeInstanceUID", 
                logger.isIncludeInstanceUID(), false);
        LdapUtils.storeNotNull(attrs, "dcmAuditLoggerSpoolDirectoryURI",
                logger.getSpoolDirectoryURI());
        LdapUtils.storeNotDef(attrs, "dcmAuditLoggerRetryInterval",
                logger.getRetryInterval(), 0);
        LdapUtils.storeNotNull(attrs, "dicomInstalled",
                logger.getInstalled());
        return attrs;
    }

    private Attributes storeTo(AuditSuppressCriteria criteria, BasicAttributes attrs) {
        attrs.put(new BasicAttribute("objectclass", "dcmAuditSuppressCriteria"));
        attrs.put(new BasicAttribute("cn", criteria.getCommonName()));
        LdapUtils.storeNotEmpty(attrs, "dcmAuditEventID",
                criteria.getEventIDsAsStringArray());
        LdapUtils.storeNotEmpty(attrs, "dcmAuditEventTypeCode",
                criteria.getEventTypeCodesAsStringArray());
        LdapUtils.storeNotEmpty(attrs, "dcmAuditEventActionCode",
                criteria.getEventActionCodes());
        LdapUtils.storeNotEmpty(attrs, "dcmAuditEventOutcomeIndicator",
                criteria.getEventOutcomeIndicators());
        LdapUtils.storeNotEmpty(attrs, "dcmAuditUserID",
                criteria.getUserIDs());
        LdapUtils.storeNotEmpty(attrs, "dcmAuditAlternativeUserID",
                criteria.getAlternativeUserIDs());
        LdapUtils.storeNotEmpty(attrs, "dcmAuditUserRoleIDCode",
                criteria.getUserRoleIDCodesAsStringArray());
        LdapUtils.storeNotEmpty(attrs, "dcmAuditNetworkAccessPointID",
                criteria.getNetworkAccessPointIDs());
        LdapUtils.storeNotNull(attrs, "dcmAuditUserIsRequestor",
                criteria.getUserIsRequestor());
        return attrs;
    }


    @Override
    protected void loadChilds(Device device, String deviceDN)
            throws NamingException, ConfigurationException {
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

    private AuditLogger loadAuditLogger(SearchResult sr, String deviceDN,
                                        Device device) throws NamingException, ConfigurationException {
        Attributes attrs = sr.getAttributes();
        AuditLogger auditLogger = new AuditLogger(LdapUtils.stringValue(attrs.get("cn"), null));
        loadFrom(auditLogger, attrs);
        for (String connDN : LdapUtils.stringArray(attrs.get("dicomNetworkConnectionReference")))
            auditLogger.addConnection(LdapUtils.findConnection(connDN, deviceDN, device));
        String arrDeviceDN = LdapUtils.stringValue(attrs.get("dcmAuditRecordRepositoryDeviceReference"), null);
        auditLogger.setAuditRecordRepositoryDevice(deviceDN.equals(arrDeviceDN)
                ? device
                : loadAuditRecordRepository(arrDeviceDN));
        loadAuditSuppressCriteria(auditLogger, auditLoggerDN(auditLogger.getCommonName(), deviceDN));
        return auditLogger;
    }

    private Device loadAuditRecordRepository(String arrDeviceRef) {
        try {
            return config.loadDevice(arrDeviceRef);
        } catch (ConfigurationException e) {
            LOG.info("Failed to load Audit Record Repository "
                    + arrDeviceRef + " referenced by Audit Logger", e);
            return null;
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
                LdapUtils.stringValue(attrs.get("dcmAuditMessageSchemaURI"), null));
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
    protected void mergeChilds(Device prev, Device device, String deviceDN)
            throws NamingException {
        AuditLoggerDeviceExtension prevAuditLoggerExt = prev.getDeviceExtension(AuditLoggerDeviceExtension.class);
        AuditLoggerDeviceExtension auditLoggerExt = device.getDeviceExtension(AuditLoggerDeviceExtension.class);

        if (prevAuditLoggerExt != null)
            for (String appName : prevAuditLoggerExt.getAuditLoggerNames()) {
                if (auditLoggerExt == null || !auditLoggerExt.containsAuditLogger(appName))
                    config.destroySubcontextWithChilds(auditLoggerDN(appName, deviceDN));
            }

        if (auditLoggerExt == null)
            return;

        for (AuditLogger logger : auditLoggerExt.getAuditLoggers()) {
            String appName = logger.getCommonName();
            if (prevAuditLoggerExt == null || !prevAuditLoggerExt.containsAuditLogger(appName)) {
                store(deviceDN, logger);
            } else
                merge(prevAuditLoggerExt.getAuditLogger(appName), logger, deviceDN);
        }
    }

    private void merge(AuditLogger prevLogger, AuditLogger logger, String deviceDN)
            throws NamingException {
        String appDN = auditLoggerDN(logger.getCommonName(), deviceDN);
        config.modifyAttributes(appDN, storeDiffs(prevLogger, logger, deviceDN, new ArrayList<ModificationItem>()));
        mergeAuditSuppressCriteria(prevLogger, logger, appDN);
    }

    private void mergeAuditSuppressCriteria(AuditLogger prevLogger,
            AuditLogger logger, String auditLoggerDN) throws NamingException {
        for (AuditSuppressCriteria prevCriteria : prevLogger.getAuditSuppressCriteriaList()) {
            String cn = prevCriteria.getCommonName();
            if (logger.findAuditSuppressCriteriaByCommonName(cn) == null)
                config.destroySubcontext(LdapUtils.dnOf("cn", cn, auditLoggerDN));
        }
        for (AuditSuppressCriteria criteria : logger.getAuditSuppressCriteriaList()) {
            String cn = criteria.getCommonName();
            String dn = LdapUtils.dnOf("cn", cn, auditLoggerDN);
            AuditSuppressCriteria prev = prevLogger.findAuditSuppressCriteriaByCommonName(cn);
            if (prev == null)
                config.createSubcontext(
                        dn,
                        storeTo(criteria, new BasicAttributes(true)));
            else
                config.modifyAttributes(dn, storeDiffs(prev, criteria, 
                        new ArrayList<ModificationItem>()));
        }
    }

    private List<ModificationItem> storeDiffs(AuditLogger a, AuditLogger b,
            String deviceDN, ArrayList<ModificationItem> mods) {
        LdapUtils.storeDiff(mods, "dcmAuditFacility",
                a.getFacility().ordinal(),
                b.getFacility().ordinal(),
                10);
        LdapUtils.storeDiff(mods, "dcmAuditSuccessSeverity",
                a.getSuccessSeverity().ordinal(),
                b.getSuccessSeverity().ordinal(),
                5);
        LdapUtils.storeDiff(mods, "dcmAuditMinorFailureSeverity",
                a.getMinorFailureSeverity().ordinal(),
                b.getMinorFailureSeverity().ordinal(),
                4);
        LdapUtils.storeDiff(mods, "dcmAuditSeriousFailureSeverity",
                a.getSeriousFailureSeverity().ordinal(),
                b.getSeriousFailureSeverity().ordinal(),
                3);
        LdapUtils.storeDiff(mods, "dcmAuditMajorFailureSeverity",
                a.getMajorFailureSeverity().ordinal(),
                b.getMajorFailureSeverity().ordinal(),
                2);
        LdapUtils.storeDiff(mods, "dcmAuditSourceID",
                a.getAuditSourceID(),
                b.getAuditSourceID());
        LdapUtils.storeDiff(mods, "dcmAuditEnterpriseSiteID",
                a.getAuditEnterpriseSiteID(),
                b.getAuditEnterpriseSiteID());
        LdapUtils.storeDiff(mods, "dcmAuditSourceTypeCode",
                a.getAuditSourceTypeCodes(),
                b.getAuditSourceTypeCodes());
        LdapUtils.storeDiff(mods, "dcmAuditApplicationName",
                a.getApplicationName(),
                b.getApplicationName());
        LdapUtils.storeDiff(mods, "dcmAuditMessageID",
                a.getMessageID(),
                b.getMessageID());
        LdapUtils.storeDiff(mods, "dcmAuditMessageEncoding",
                StringUtils.nullify(a.getEncoding(), "UTF-8"),
                StringUtils.nullify(b.getEncoding(), "UTF-8"));
        LdapUtils.storeDiff(mods, "dcmAuditMessageSchemaURI",
                a.getSchemaURI(),
                b.getSchemaURI());
        LdapUtils.storeDiff(mods, "dcmAuditMessageBOM",
                a.isIncludeBOM(),
                b.isIncludeBOM(),
                true);
        LdapUtils.storeDiff(mods, "dcmAuditMessageFormatXML",
                a.isFormatXML(),
                b.isFormatXML(),
                false);
        LdapUtils.storeDiff(mods, "dcmAuditTimestampInUTC",
                a.isTimestampInUTC(),
                b.isTimestampInUTC(),
                false);
        LdapUtils.storeDiff(mods, "dicomNetworkConnectionReference",
                a.getConnections(),
                b.getConnections(),
                deviceDN);
        LdapUtils.storeDiff(mods, "dcmAuditRecordRepositoryDeviceReference",
                arrDeviceRef(a),
                arrDeviceRef(b));
        LdapUtils.storeDiff(mods, "dcmAuditIncludeInstanceUID", 
                a.isIncludeInstanceUID(), 
                b.isIncludeInstanceUID(), 
                false);
        LdapUtils.storeDiff(mods, "dcmAuditLoggerSpoolDirectoryURI",
                a.getSpoolDirectoryURI(),
                b.getSpoolDirectoryURI());
        LdapUtils.storeDiff(mods, "dcmAuditLoggerRetryInterval", 
                a.getRetryInterval(), 
                b.getRetryInterval(), 
                0);
        LdapUtils.storeDiff(mods, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled());
        return mods;
    }

    private List<ModificationItem> storeDiffs(AuditSuppressCriteria a,
            AuditSuppressCriteria b, ArrayList<ModificationItem> mods) {
        LdapUtils.storeDiff(mods, "dcmAuditEventID",
                a.getEventIDsAsStringArray(),
                b.getEventIDsAsStringArray());
        LdapUtils.storeDiff(mods, "dcmAuditEventTypeCode",
                a.getEventTypeCodesAsStringArray(),
                b.getEventTypeCodesAsStringArray());
        LdapUtils.storeDiff(mods, "dcmAuditEventActionCode",
                a.getEventActionCodes(),
                b.getEventActionCodes());
        LdapUtils.storeDiff(mods, "dcmAuditEventOutcomeIndicator",
                a.getEventOutcomeIndicators(),
                b.getEventOutcomeIndicators());
        LdapUtils.storeDiff(mods, "dcmAuditUserID",
                a.getUserIDs(),
                b.getUserIDs());
        LdapUtils.storeDiff(mods, "dcmAuditAlternativeUserID",
                a.getAlternativeUserIDs(),
                b.getAlternativeUserIDs());
        LdapUtils.storeDiff(mods, "dcmAuditUserRoleIDCode",
                a.getUserRoleIDCodesAsStringArray(),
                b.getUserRoleIDCodesAsStringArray());
        LdapUtils.storeDiff(mods, "dcmAuditNetworkAccessPointID",
                a.getNetworkAccessPointIDs(),
                b.getNetworkAccessPointIDs());
        LdapUtils.storeDiff(mods, "dcmAuditUserIsRequestor",
                a.getUserIsRequestor(),
                b.getUserIsRequestor());
        return mods;
    }

    private String arrDeviceRef(AuditLogger a) {
        Device arrDevice = a.getAuditRecordRepositoryDevice();
        return arrDevice != null
                ? config.deviceRef(arrDevice.getDeviceName())
                : null;
    }

}
