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
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.conf.prefs.audit;

import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.prefs.PreferencesDicomConfigurationExtension;
import org.dcm4che3.conf.prefs.PreferencesUtils;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
public class PreferencesAuditLoggerConfiguration
        extends PreferencesDicomConfigurationExtension {

    private static final Logger LOG = LoggerFactory.getLogger(
            PreferencesAuditLoggerConfiguration.class);

    @Override
    protected void storeChilds(Device device, Preferences deviceNode) {
        AuditLogger logger =
                device.getDeviceExtension(AuditLogger.class);
        if (logger != null)
            storeTo(logger, deviceNode.node("dcmAuditLogger"));
    }

    private void storeTo(AuditLogger logger, Preferences prefs) {
        PreferencesUtils.storeNotDef(prefs, "dcmAuditFacility",
                logger.getFacility().ordinal(), 10);
        PreferencesUtils.storeNotDef(prefs, "dcmAuditSuccessSeverity",
                logger.getSuccessSeverity().ordinal(), 5);
        PreferencesUtils.storeNotDef(prefs, "dcmAuditMinorFailureSeverity",
                logger.getMinorFailureSeverity().ordinal(), 4);
        PreferencesUtils.storeNotDef(prefs, "dcmAuditSeriousFailureSeverity",
                logger.getSeriousFailureSeverity().ordinal(), 3);
        PreferencesUtils.storeNotDef(prefs, "dcmAuditMajorFailureSeverity",
                logger.getMajorFailureSeverity().ordinal(), 2);
        PreferencesUtils.storeNotNull(prefs, "dcmAuditSourceID",
                logger.getAuditSourceID());
        PreferencesUtils.storeNotNull(prefs, "dcmAuditEnterpriseSiteID",
                logger.getAuditEnterpriseSiteID());
        PreferencesUtils.storeNotEmpty(prefs, "dcmAuditSourceTypeCode",
                logger.getAuditSourceTypeCodes());
        PreferencesUtils.storeNotNull(prefs, "dcmAuditApplicationName",
                logger.getApplicationName());
        PreferencesUtils.storeNotNull(prefs, "dcmAuditMessageID",
                StringUtils.nullify(logger.getMessageID(), AuditLogger.MESSAGE_ID));
        PreferencesUtils.storeNotNull(prefs, "dcmAuditMessageEncoding",
                StringUtils.nullify(logger.getEncoding(), "UTF-8"));
        PreferencesUtils.storeNotNull(prefs, "dcmAuditMessageSchemaURI",
                logger.getSchemaURI());
        PreferencesUtils.storeNotDef(prefs, "dcmAuditMessageBOM",
                logger.isIncludeBOM(), true);
        PreferencesUtils.storeNotDef(prefs, "dcmAuditMessageFormatXML",
                logger.isFormatXML(), false);
        PreferencesUtils.storeNotDef(prefs, "dcmAuditTimestampInUTC",
                logger.isTimestampInUTC(), false);
        PreferencesUtils.storeConnRefs(prefs, logger.getConnections(),
                logger.getDevice().listConnections());
        PreferencesUtils.storeNotNull(prefs, "dcmAuditRecordRepositoryDeviceReference",
                config.deviceRef(logger.getAuditRecordRepositoryDeviceName()));
        PreferencesUtils.storeNotDef(prefs, "dcmAuditIncludeInstanceUID", 
                logger.isIncludeInstanceUID(), false);
        PreferencesUtils.storeNotNull(prefs, "dicomInstalled", logger.getInstalled());
    }

    @Override
    protected void loadChilds(Device device, Preferences deviceNode)
            throws BackingStoreException, ConfigurationException {
        if (!deviceNode.nodeExists("dcmAuditLogger"))
            return;
        
        List<Connection> devConns = device.listConnections();
        Preferences loggerNode = deviceNode.node("dcmAuditLogger");
        AuditLogger logger = new AuditLogger();
        loadFrom(logger, loggerNode);
        int n = loggerNode.getInt("dicomNetworkConnectionReference.#", 0);
        for (int i = 0; i < n; i++) {
            logger.addConnection(devConns.get(
                    loggerNode.getInt("dicomNetworkConnectionReference." + (i+1), 0) - 1));
        }
        String arrDeviceRef =
                loggerNode.get("dcmAuditRecordRepositoryDeviceReference", null);
        if (arrDeviceRef == null)
            throw new ConfigurationException("Missing dcmAuditRecordRepositoryDeviceReference");

        logger.setAuditRecordRepositoryDevice(
                arrDeviceRef.equals(config.deviceRef(device.getDeviceName()))
                    ? device
                    : loadAuditRecordRepository(arrDeviceRef));
        device.addDeviceExtension(logger);
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

    private void loadFrom(AuditLogger logger, Preferences prefs) {
        logger.setFacility(AuditLogger.Facility.values()
                [prefs.getInt("dcmAuditFacility", 10)]);
        logger.setSuccessSeverity(AuditLogger.Severity.values()
                [prefs.getInt("dcmAuditSuccessSeverity", 5)]);
        logger.setMinorFailureSeverity(AuditLogger.Severity.values()
                [prefs.getInt("dcmAuditMinorFailureSeverity", 4)]);
        logger.setSeriousFailureSeverity(AuditLogger.Severity.values()
                [prefs.getInt("dcmAuditSeriousFailureSeverity", 3)]);
        logger.setMajorFailureSeverity(AuditLogger.Severity.values()
                [prefs.getInt("dcmAuditMajorFailureSeverity", 2)]);
        logger.setAuditSourceID(prefs.get("dcmAuditSourceID", null));
        logger.setAuditEnterpriseSiteID(
                prefs.get("dcmAuditEnterpriseSiteID", null));
        logger.setAuditSourceTypeCodes(
                PreferencesUtils.stringArray(prefs, "dcmAuditSourceTypeCode"));
        logger.setApplicationName(
                prefs.get("dcmAuditApplicationName", null));
        logger.setMessageID(
                prefs.get("dcmAuditMessageID", AuditLogger.MESSAGE_ID));
        logger.setEncoding(
                prefs.get("dcmAuditMessageEncoding", "UTF-8"));
        logger.setSchemaURI(
                prefs.get("dcmAuditMessageSchemaURI", null));
        logger.setIncludeBOM(
                prefs.getBoolean("dcmAuditMessageBOM", true));
        logger.setFormatXML(
                prefs.getBoolean("dcmAuditMessageFormatXML", false));
        logger.setTimestampInUTC(
                prefs.getBoolean("dcmAuditTimestampInUTC", false));
        logger.setIncludeInstanceUID(
                prefs.getBoolean("dcmAuditIncludeInstanceUID", false));
        logger.setInstalled(PreferencesUtils.booleanValue(prefs.get("dicomInstalled", null)));
    }

    @Override
    protected void mergeChilds(Device prev, Device device, Preferences deviceNode)
            throws BackingStoreException {
        AuditLogger prevLogger =
                prev.getDeviceExtension(AuditLogger.class);
        AuditLogger logger =
                device.getDeviceExtension(AuditLogger.class);
        if (logger == null && prevLogger == null)
            return;
        
        Preferences arrNode = deviceNode.node("dcmAuditLogger");
        if (logger == null)
            arrNode.removeNode();
        else if (prevLogger == null)
            storeTo(logger, arrNode);
        else
            storeDiffs(arrNode, prevLogger, logger);
    }

    private void storeDiffs(Preferences prefs, AuditLogger a, AuditLogger b) {
        PreferencesUtils.storeDiff(prefs, "dcmAuditFacility",
                a.getFacility().ordinal(),
                b.getFacility().ordinal(),
                10);
        PreferencesUtils.storeDiff(prefs, "dcmAuditSuccessSeverity",
                a.getSuccessSeverity().ordinal(),
                b.getSuccessSeverity().ordinal(),
                5);
        PreferencesUtils.storeDiff(prefs, "dcmAuditMinorFailureSeverity",
                a.getMinorFailureSeverity().ordinal(),
                b.getMinorFailureSeverity().ordinal(),
                4);
        PreferencesUtils.storeDiff(prefs, "dcmAuditSeriousFailureSeverity",
                a.getSeriousFailureSeverity().ordinal(),
                b.getSeriousFailureSeverity().ordinal(),
                3);
        PreferencesUtils.storeDiff(prefs, "dcmAuditMajorFailureSeverity",
                a.getMajorFailureSeverity().ordinal(),
                b.getMajorFailureSeverity().ordinal(),
                2);
        PreferencesUtils.storeDiff(prefs, "dcmAuditSourceID",
                a.getAuditSourceID(),
                b.getAuditSourceID());
        PreferencesUtils.storeDiff(prefs, "dcmAuditEnterpriseSiteID",
                a.getAuditEnterpriseSiteID(),
                b.getAuditEnterpriseSiteID());
        PreferencesUtils.storeDiff(prefs, "dcmAuditSourceTypeCode",
                a.getAuditSourceTypeCodes(),
                b.getAuditSourceTypeCodes());
        PreferencesUtils.storeDiff(prefs, "dcmAuditApplicationName",
                a.getApplicationName(),
                b.getApplicationName());
        PreferencesUtils.storeDiff(prefs, "dcmAuditMessageID",
                a.getMessageID(),
                b.getMessageID());
        PreferencesUtils.storeDiff(prefs, "dcmAuditMessageEncoding",
                StringUtils.nullify(a.getEncoding(), "UTF-8"),
                StringUtils.nullify(b.getEncoding(), "UTF-8"));
        PreferencesUtils.storeDiff(prefs, "dcmAuditMessageSchemaURI",
                a.getSchemaURI(),
                b.getSchemaURI());
        PreferencesUtils.storeDiff(prefs, "dcmAuditMessageBOM",
                a.isIncludeBOM(),
                b.isIncludeBOM(),
                true);
        PreferencesUtils.storeDiff(prefs, "dcmAuditMessageFormatXML",
                a.isFormatXML(),
                b.isFormatXML(),
                false);
        PreferencesUtils.storeDiff(prefs, "dcmAuditTimestampInUTC",
                a.isTimestampInUTC(),
                b.isTimestampInUTC(),
                false);
        PreferencesUtils.storeDiffConnRefs(prefs, 
                a.getConnections(), a.getDevice().listConnections(), 
                b.getConnections(), b.getDevice().listConnections());
        PreferencesUtils.storeDiff(prefs, "dcmAuditRecordRepositoryDeviceReference",
                arrDeviceRef(a),
                arrDeviceRef(b));
        PreferencesUtils.storeDiff(prefs, "dcmAuditIncludeInstanceUID", 
                a.isIncludeInstanceUID(), 
                b.isIncludeInstanceUID(),
                false);
        PreferencesUtils.storeDiff(prefs, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled());
    }

    private String arrDeviceRef(AuditLogger a) {
        Device arrDevice = a.getAuditRecordRepositoryDevice();
        return arrDevice != null
                ? config.deviceRef(arrDevice.getDeviceName())
                : null;
    }
}
