package org.dcm4che3.conf.api.upgrade;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.core.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

/**
 * Implement this interface to cover
 * <ul>
 * <li>transformation of configuration classes structure between releases</li>
 * <li>conditional default config initialization</li>
 * <li>migration of legacy configuration</li>
 * </ul>
 * <p/>
 * Mark the implemented class with
 * <code>@ org.dcm4che3.conf.api.upgrade.ScriptVersion</code>
 * annotation to allow the upgrade runner to detect whether the script needs to be re-executed.
 */
public interface UpgradeScript {
    String NO_VERSION = "-NO_VERSION-";

    void upgrade(UpgradeContext upgradeContext) throws ConfigurationException;

    class UpgradeContext {

        private static final Logger log = LoggerFactory.getLogger(UpgradeContext.class);

        private String fromVersion;
        private String toVersion;
        private Properties properties;
        private Map<String, Object> scriptConfig;
        private Configuration configuration;
        private DicomConfiguration dicomConfiguration;
        private UpgradeScriptMetadata upgradeScriptMetadata;
        private ConfigurationMetadata configMetaData;

        public UpgradeContext() {
        }

        public UpgradeContext(String fromVersion, String toVersion, Properties properties, Map<String, Object> scriptConfig, Configuration configuration, DicomConfiguration dicomConfiguration) {
            this(fromVersion, toVersion, properties, scriptConfig, configuration, dicomConfiguration, null, null);
        }

        public UpgradeContext(String fromVersion, String toVersion, Properties properties, Map<String, Object> scriptConfig, Configuration configuration, DicomConfiguration dicomConfiguration, UpgradeScriptMetadata upgradeScriptMetadata) {
            this(fromVersion, toVersion, properties, scriptConfig, configuration, dicomConfiguration, upgradeScriptMetadata, null);
        }

        public UpgradeContext(String fromVersion, String toVersion, Properties properties, Map<String, Object> scriptConfig, Configuration configuration, DicomConfiguration dicomConfiguration, UpgradeScriptMetadata upgradeScriptMetadata, ConfigurationMetadata configMetaData) {
            this.fromVersion = fromVersion;
            this.toVersion = toVersion;
            this.properties = properties;
            this.scriptConfig = scriptConfig;
            this.configuration = configuration;
            this.dicomConfiguration = dicomConfiguration;
            this.upgradeScriptMetadata = upgradeScriptMetadata;
            this.configMetaData = configMetaData;
        }


        public Object getFromVersion() {
            return fromVersion;
        }

        public Object getToVersion() {
            return toVersion;
        }

        public Configuration getConfiguration() {
            return configuration;
        }

        public DicomConfiguration getDicomConfiguration() {
            return dicomConfiguration;
        }

        public Properties getProperties() {
            return properties;
        }

        public Map<String, Object> getScriptConfig() {
            return scriptConfig;
        }

        public UpgradeScriptMetadata getUpgradeScriptMetadata() {
            return upgradeScriptMetadata;
        }

        /**
         * Allows to "fork" another script from the currently executed script.<br><br>
         * This feature allows to refactor an existing script (script <b>A</b>) by extracting a part of it into a new script (script <b>B</b>). <br><br>
         * To facilitate a simpler design, this method will pre-set some version for that new script (<b>B</b>), so the new script will "think"
         * that it's not the first time it's executed.
         * This covers the case when upgrading from an older version of <b>A</b> (when A+B was still a single script), so <b>B</b> will not think that it's a "clean installation" <br><br>
         * <b>CAUTION:</b> It is important that <b>B</b> appears <i>after</i> <b>A</b> in upgrade-settings.json
         * </b>
         *
         * @param forkedScript  The <b>B</b> script.
         * @param scriptVersion The version to set for the <b>B</b> script.
         */
        public void forkScript(Class<? extends UpgradeScript> forkedScript, String scriptVersion) {

            String scriptClass = forkedScript.getName();

            UpgradeScriptMetadata upgradeScriptMetadata = configMetaData.getMetadataOfUpgradeScripts().get(scriptClass);

            if (upgradeScriptMetadata != null) {
                log.warn("Attempted to fork script [" + scriptClass + "] that has already been executed (last executed version " + upgradeScriptMetadata.getLastVersionExecuted() + ")");
                return;
            }

            UpgradeScriptMetadata scriptMetadata = new UpgradeScriptMetadata();
            scriptMetadata.setLastVersionExecuted(scriptVersion);

            configMetaData.getMetadataOfUpgradeScripts().put(scriptClass, scriptMetadata);
            log.info("Forked upgrade script [" + scriptClass + "], initialized with version " + scriptVersion);

        }
    }

    @ConfigurableClass
    class UpgradeScriptMetadata {

        /**
         * The version of this upgrade script when it was last time executed, taken from @ScriptVersion
         */
        @ConfigurableProperty(description = "The version of this upgrade script when it was last time executed")
        String lastVersionExecuted;

        public String getLastVersionExecuted() {
            return lastVersionExecuted;
        }

        public void setLastVersionExecuted(String lastVersionExecuted) {
            this.lastVersionExecuted = lastVersionExecuted;
        }
    }
}
