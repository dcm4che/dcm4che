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
package org.dcm4che3.audit.keycloak;

import org.dcm4che3.util.StringUtils;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Mar 2016
 */
public class Dcm4cheEventListenerProviderFactory implements EventListenerProviderFactory {
    private static final String[] JBOSS_PROPERTIES = {
            "jboss.server.config",
            "jboss.server.data",
            "jboss.server.log",
            "jboss.server.temp",
    };

    static void addJBossDirURLSystemProperties() {
        String quarkusHomeDir = System.getProperty("kc.home.dir");
        if (quarkusHomeDir != null) {
            try {
                quarkusHomeDir = new File(quarkusHomeDir).getCanonicalPath() + '/';
            } catch (IOException ignore) {
            }
            String quarkusDataDir = quarkusHomeDir + "data/";
            System.setProperty("jboss.server.data.dir", quarkusDataDir);
            System.setProperty("jboss.server.config.dir", quarkusHomeDir + "conf/");
            System.setProperty("jboss.server.log.dir", quarkusDataDir + "log/");
            System.setProperty("jboss.server.temp.dir", quarkusDataDir + "tmp/");
            System.setProperty("org.dcm4che.audit-keycloak.AppName", "keycloak");
            System.setProperty("com.sun.jndi.ldap.object.disableEndpointIdentification",
                    StringUtils.replaceSystemProperties("${env.LDAP_DISABLE_HOSTNAME_VERIFICATION:true}"));
            System.setProperty("org.dcm4che.audit-keycloak.AppName", "keycloak");
            System.setProperty("keycloak.DeviceName",
                    StringUtils.replaceSystemProperties("${env.KEYCLOAK_DEVICE_NAME:keycloak}"));
            System.setProperty("super-user-role",
                    StringUtils.replaceSystemProperties("${env.SUPER_USER_ROLE:root}"));
        }
        for (String key : JBOSS_PROPERTIES) {
            String url = new File(System.getProperty(key + ".dir")).toURI().toString();
            System.setProperty(key + ".url", url.substring(0, url.length()-1));
        }
    }

    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {
        return new Dcm4cheEventListenerProvider(keycloakSession, System.getProperty("super-user-role"));
    }

    @Override
    public void init(Config.Scope scope) {
        addJBossDirURLSystemProperties();
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "dcm4che-audit";
    }
}
