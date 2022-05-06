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
 * Portions created by the Initial Developer are Copyright (C) 2015-2019
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
package org.dcm4che3.audit.keycloak;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.ConfigurationNotFoundException;
import org.dcm4che3.conf.ldap.LdapDicomConfiguration;
import org.dcm4che3.conf.ldap.audit.LdapAuditLoggerConfiguration;
import org.dcm4che3.conf.ldap.audit.LdapAuditRecordRepositoryConfiguration;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditLoggerDeviceExtension;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Mar 2016
 */
public class Dcm4cheEventListenerProvider implements EventListenerProvider {
    private static final String LDAP_PROPERTIES = "ldap.properties";
    private static final String JBOSS_SERVER_CONFIG_DIR = "jboss.server.config.dir";
    private static final String APP_NAME_PROPERTY = "org.dcm4che.audit-keycloak.AppName";
    private static final String DEF_APP_NAME = "dcm4chee-arc";
    private static final String DEF_DEVICE_NAME = "dcm4chee-arc";
    private static final Logger LOG = LoggerFactory.getLogger(Dcm4cheEventListenerProvider.class);
    private final KeycloakSession keycloakSession;
    private final String suRole;

    public Dcm4cheEventListenerProvider(KeycloakSession keycloakSession, String suRole) {
        this.keycloakSession = keycloakSession;
        this.suRole = suRole;
    }


    @Override
    public void onEvent(Event event) {
        LOG.debug("Event: {}", event.getType());
        switch (event.getType()) {
            case LOGIN:
            case LOGIN_ERROR:
            case LOGOUT:
            case LOGOUT_ERROR:
            case UPDATE_PASSWORD:
            case UPDATE_PASSWORD_ERROR:
                auditLoggers().forEach(logger -> {
                    if (logger.isInstalled())
                        AuditAuth.audit(event, logger, keycloakSession, suRole);
                });
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        LOG.debug("Event: {} {}", adminEvent.getOperationType(), adminEvent.getResourceTypeAsString());
        auditLoggers().forEach(logger -> {
            if (logger.isInstalled())
                AdminEventsAuditService.audit(adminEvent, logger, keycloakSession);
        });
    }

    public void close() {

    }

    private static Collection<AuditLogger> auditLoggers() {
        try {
            return findDevice().getDeviceExtensionNotNull(AuditLoggerDeviceExtension.class).getAuditLoggers();
        } catch (Exception e) {
            LOG.warn("Failed to get audit logger", e);
            return Collections.emptyList();
        }
    }

    private static Device findDevice() throws ConfigurationException {
        String key = System.getProperty(APP_NAME_PROPERTY, DEF_APP_NAME) + ".DeviceName";
        String name = System.getProperty(key, DEF_DEVICE_NAME);
        try ( LdapDicomConfiguration conf = new LdapDicomConfiguration(loadProperties())) {
            conf.addDicomConfigurationExtension(new LdapAuditLoggerConfiguration());
            conf.addDicomConfigurationExtension(new LdapAuditRecordRepositoryConfiguration());
            return conf.findDevice(name);
        } catch (ConfigurationNotFoundException e) {
            LOG.error("Missing Configuration for Device '{}' - you may change the Device name by System Property '{}'",
                    name, key);
            throw e;
        }
    }

    private static Properties loadProperties() throws ConfigurationException {
        URL url;
        Path path = Paths.get(
                System.getProperty(JBOSS_SERVER_CONFIG_DIR),
                System.getProperty(APP_NAME_PROPERTY, DEF_APP_NAME),
                LDAP_PROPERTIES);
        try {
            url = path.toUri().toURL();
        } catch (MalformedURLException e1) {
            throw new AssertionError(e1);
        }
        Properties p = new Properties();
        try (InputStream stream = url.openStream()) {
            p.load(stream);
        } catch (IOException e) {
            LOG.error("Failed to load LDAP configuration from '{}' " +
                            "- you may change the sub-directory name by System Property '{}'",
                    url, APP_NAME_PROPERTY);
            throw new ConfigurationException(e);
        }
        return p;
    }
}
