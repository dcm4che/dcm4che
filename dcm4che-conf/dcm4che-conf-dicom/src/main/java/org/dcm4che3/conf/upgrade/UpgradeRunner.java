/*
 * *** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2015
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */

package org.dcm4che3.conf.upgrade;


import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.api.internal.DicomConfigurationManager;
import org.dcm4che3.conf.api.upgrade.ScriptVersion;
import org.dcm4che3.conf.api.upgrade.UpgradeScript;
import org.dcm4che3.conf.core.DefaultBeanVitalizer;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.internal.BeanVitalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * @author Roman K
 */
@SuppressWarnings("unchecked")
public class UpgradeRunner {

    public static final String RUN_ALWAYS = "org.dcm4che.conf.upgrade.runAlways";
    private static Logger log = LoggerFactory
            .getLogger(UpgradeRunner.class);

    public static final String METADATA_ROOT_PATH = "/dicomConfigurationRoot/metadataRoot/versioning";

    private Collection<UpgradeScript> availableUpgradeScripts;
    private DicomConfigurationManager dicomConfigurationManager;
    private UpgradeSettings upgradeSettings;

    public UpgradeRunner() {
    }

    public UpgradeRunner(Collection<UpgradeScript> availableUpgradeScripts, DicomConfigurationManager dicomConfigurationManager, UpgradeSettings upgradeSettings) {
        this.availableUpgradeScripts = availableUpgradeScripts;
        this.dicomConfigurationManager = dicomConfigurationManager;
        this.upgradeSettings = upgradeSettings;
    }

    public void upgrade() {
        if (upgradeSettings == null) {
            log.info("Dcm4che configuration init: upgrade is not configured, no upgrade will be performed");
            return;
        }

        String toVersion = upgradeSettings.getUpgradeToVersion();
        if (toVersion != null) {
            upgradeToVersion(toVersion);
        } else
            log.warn("Dcm4che configuration init: target upgrade version is null. Upgrade will not be performed. Set the target config version in upgrade settings first.'");

    }


    protected void upgradeToVersion(final String toVersion) {
        dicomConfigurationManager.runBatch(new DicomConfiguration.DicomConfigBatch() {
            @Override
            public void run() {

                try {

                    Configuration configuration = dicomConfigurationManager.getConfigurationStorage();
                    configuration.lock();

                    BeanVitalizer beanVitalizer = new DefaultBeanVitalizer();

                    // load or initialize config metadata
                    Object metadataNode = configuration.getConfigurationNode(METADATA_ROOT_PATH, ConfigurationMetadata.class);
                    ConfigurationMetadata configMetadata = null;
                    if (metadataNode != null)
                        configMetadata = beanVitalizer.newConfiguredInstance((Map<String, Object>) metadataNode, ConfigurationMetadata.class);
                    else {
                        configMetadata = new ConfigurationMetadata();
                        configMetadata.setVersion(UpgradeScript.NO_VERSION);
                    }
                    String fromVersion = configMetadata.getVersion();

                    log.info("Dcm4che configuration init: upgrading configuration from version '{}' to version '{}'", fromVersion, toVersion);

                    Properties props = new Properties();
                    props.putAll(upgradeSettings.getProperties());

                    log.info("Config upgrade scripts specified in settings: {}", upgradeSettings.getUpgradeScriptsToRun());
                    log.info("Config upgrade scripts discovered in the deployment: {}", availableUpgradeScripts);

                    // run all scripts
                    for (String upgradeScriptName : upgradeSettings.getUpgradeScriptsToRun()) {

                        boolean found = false;

                        for (UpgradeScript script : availableUpgradeScripts) {
                            if (script.getClass().getName().startsWith(upgradeScriptName)) {
                                found = true;

                                // fetch upgradescript metadata
                                UpgradeScript.UpgradeScriptMetadata upgradeScriptMetadata = configMetadata.getMetadataOfUpgradeScripts().get(upgradeScriptName);
                                if (upgradeScriptMetadata == null) {
                                    upgradeScriptMetadata = new UpgradeScript.UpgradeScriptMetadata();
                                    configMetadata.getMetadataOfUpgradeScripts().put(upgradeScriptName, upgradeScriptMetadata);
                                }


                                // check if the script need to be executed
                                ScriptVersion currentScriptVersionAnno = script.getClass().getAnnotation(ScriptVersion.class);

                                String currentscriptVersion;
                                if (currentScriptVersionAnno == null) {
                                    currentscriptVersion = UpgradeScript.NO_VERSION;
                                    log.warn("Upgrade script '{}' does not have @ScriptVersion defined - using default '{}'",
                                            script.getClass().getName(),
                                            currentscriptVersion);
                                } else {
                                    currentscriptVersion = currentScriptVersionAnno.value();
                                }

                                if (upgradeScriptMetadata.getLastVersionExecuted() != null
                                        && upgradeScriptMetadata.getLastVersionExecuted().compareTo(currentscriptVersion) >= 0) {
                                    log.info("Upgrade script '{}' is skipped because current version '{}' is older than the last executed one ('{}')",
                                            script.getClass().getName(),
                                            currentscriptVersion,
                                            upgradeScriptMetadata.getLastVersionExecuted());
                                    continue;
                                }

                                log.info("Executing upgrade script '{}' (this version '{}', last executed version '{}')",
                                        script.getClass().getName(),
                                        currentscriptVersion,
                                        upgradeScriptMetadata.getLastVersionExecuted());


                                // collect pieces and prepare context
                                Map<String, Object> scriptConfig = (Map<String, Object>) upgradeSettings.getUpgradeConfig().get(upgradeScriptName);
                                UpgradeScript.UpgradeContext upgradeContext = new UpgradeScript.UpgradeContext(
                                        fromVersion, toVersion, props, scriptConfig, configuration, dicomConfigurationManager, upgradeScriptMetadata);

                                script.upgrade(upgradeContext);

                                // set last executed version from the annotation of the upgrade script if present
                                upgradeScriptMetadata.setLastVersionExecuted(currentscriptVersion);

                            }
                        }

                        if (!found)
                            throw new ConfigurationException("Upgrade script '" + upgradeScriptName + "' not found in the deployment");
                    }

                    // update version
                    configMetadata.setVersion(toVersion);

                    // persist updated metadata
                    configuration.persistNode(METADATA_ROOT_PATH, beanVitalizer.createConfigNodeFromInstance(configMetadata), ConfigurationMetadata.class);
                } catch (ConfigurationException e) {
                    throw new RuntimeException("Error while running the upgrade", e);
                }
            }
        });

        log.info("Configuration upgrade completed successfully");
    }


}

