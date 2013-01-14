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

package org.dcm4che.conf.prefs.audit;

import static org.dcm4che.conf.prefs.PreferencesDicomConfiguration.booleanValue;
import static org.dcm4che.conf.prefs.PreferencesDicomConfiguration.storeConnRefs;
import static org.dcm4che.conf.prefs.PreferencesDicomConfiguration.storeDiff;
import static org.dcm4che.conf.prefs.PreferencesDicomConfiguration.storeDiffConnRefs;
import static org.dcm4che.conf.prefs.PreferencesDicomConfiguration.storeNotDef;
import static org.dcm4che.conf.prefs.PreferencesDicomConfiguration.storeNotEmpty;
import static org.dcm4che.conf.prefs.PreferencesDicomConfiguration.storeNotNull;
import static org.dcm4che.conf.prefs.PreferencesDicomConfiguration.stringArray;

import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.conf.prefs.PreferencesDicomConfigurationExtension;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.audit.AuditLogger;
import org.dcm4che.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class PreferencesAuditLoggerConfiguration
        extends PreferencesDicomConfigurationExtension {

    @Override
    protected void storeChilds(Device device, Preferences deviceNode) {
        AuditLogger logger =
                device.getDeviceExtension(AuditLogger.class);
        if (logger != null)
            storeTo(logger, deviceNode.node("dcmAuditLogger"));
    }

    private void storeTo(AuditLogger logger, Preferences prefs) {
        storeNotDef(prefs, "dcmAuditFacility",
                logger.getFacility().ordinal(), 10);
        storeNotDef(prefs, "dcmAuditSuccessSeverity",
                logger.getSuccessSeverity().ordinal(), 5);
        storeNotDef(prefs, "dcmAuditMinorFailureSeverity",
                logger.getMinorFailureSeverity().ordinal(), 4);
        storeNotDef(prefs, "dcmAuditSeriousFailureSeverity",
                logger.getSeriousFailureSeverity().ordinal(), 3);
        storeNotDef(prefs, "dcmAuditMajorFailureSeverity",
                logger.getMajorFailureSeverity().ordinal(), 2);
        storeNotNull(prefs, "dcmAuditSourceID",
                logger.getAuditSourceID());
        storeNotNull(prefs, "dcmAuditEnterpriseSiteID",
                logger.getAuditEnterpriseSiteID());
        storeNotEmpty(prefs, "dcmAuditSourceTypeCode",
                logger.getAuditSourceTypeCodes());
        storeNotNull(prefs, "dcmAuditApplicationName",
                logger.getApplicationName());
        storeNotNull(prefs, "dcmAuditMessageID",
                StringUtils.nullify(logger.getMessageID(), AuditLogger.MESSAGE_ID));
        storeNotNull(prefs, "dcmAuditMessageEncoding",
                StringUtils.nullify(logger.getEncoding(), "UTF-8"));
        storeNotNull(prefs, "dcmAuditMessageSchemaURI",
                logger.getSchemaURI());
        storeNotDef(prefs, "dcmAuditMessageBOM",
                logger.isIncludeBOM(), true);
        storeNotDef(prefs, "dcmAuditMessageFormatXML",
                logger.isFormatXML(), false);
        storeNotDef(prefs, "dcmAuditTimestampInUTC",
                logger.isTimestampInUTC(), false);
        storeConnRefs(prefs, logger.getConnections(),
                logger.getDevice().listConnections());
        storeNotNull(prefs, "dcmAuditRecordRepositoryDeviceReference",
                config.deviceRef(logger.getAuditRecordRepositoryDeviceName()));
        storeNotNull(prefs, "dicomInstalled", logger.getInstalled());
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
                    : config.loadDevice(arrDeviceRef));
        device.addDeviceExtension(logger);
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
                stringArray(prefs, "dcmAuditSourceTypeCode"));
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
        logger.setInstalled(booleanValue(prefs.get("dicomInstalled", null)));
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
        storeDiff(prefs, "dcmAuditFacility",
                a.getFacility().ordinal(),
                b.getFacility().ordinal(),
                10);
        storeDiff(prefs, "dcmAuditSuccessSeverity",
                a.getSuccessSeverity().ordinal(),
                b.getSuccessSeverity().ordinal(),
                5);
        storeDiff(prefs, "dcmAuditMinorFailureSeverity",
                a.getMinorFailureSeverity().ordinal(),
                b.getMinorFailureSeverity().ordinal(),
                4);
        storeDiff(prefs, "dcmAuditSeriousFailureSeverity",
                a.getSeriousFailureSeverity().ordinal(),
                b.getSeriousFailureSeverity().ordinal(),
                3);
        storeDiff(prefs, "dcmAuditMajorFailureSeverity",
                a.getMajorFailureSeverity().ordinal(),
                b.getMajorFailureSeverity().ordinal(),
                2);
        storeDiff(prefs, "dcmAuditSourceID",
                a.getAuditSourceID(),
                b.getAuditSourceID());
        storeDiff(prefs, "dcmAuditEnterpriseSiteID",
                a.getAuditEnterpriseSiteID(),
                b.getAuditEnterpriseSiteID());
        storeDiff(prefs, "dcmAuditSourceTypeCode",
                a.getAuditSourceTypeCodes(),
                b.getAuditSourceTypeCodes());
        storeDiff(prefs, "dcmAuditApplicationName",
                a.getApplicationName(),
                b.getApplicationName());
        storeDiff(prefs, "dcmAuditMessageID",
                a.getMessageID(),
                b.getMessageID());
        storeDiff(prefs, "dcmAuditMessageEncoding",
                StringUtils.nullify(a.getEncoding(), "UTF-8"),
                StringUtils.nullify(b.getEncoding(), "UTF-8"));
        storeDiff(prefs, "dcmAuditMessageSchemaURI",
                a.getSchemaURI(),
                b.getSchemaURI());
        storeDiff(prefs, "dcmAuditMessageBOM",
                a.isIncludeBOM(),
                b.isIncludeBOM(),
                true);
        storeDiff(prefs, "dcmAuditMessageFormatXML",
                a.isFormatXML(),
                b.isFormatXML(),
                false);
        storeDiff(prefs, "dcmAuditTimestampInUTC",
                a.isTimestampInUTC(),
                b.isTimestampInUTC(),
                false);
        storeDiffConnRefs(prefs, 
                a.getConnections(), a.getDevice().listConnections(), 
                b.getConnections(), b.getDevice().listConnections());
        storeDiff(prefs, "dcmAuditRecordRepositoryDeviceReference",
                config.deviceRef(a.getAuditRecordRepositoryDeviceName()),
                config.deviceRef(b.getAuditRecordRepositoryDeviceName()));
        storeDiff(prefs, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled());
    }
}
