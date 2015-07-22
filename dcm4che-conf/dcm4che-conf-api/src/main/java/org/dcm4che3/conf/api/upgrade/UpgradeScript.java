package org.dcm4che3.conf.api.upgrade;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;

import java.util.Map;
import java.util.Properties;

public interface UpgradeScript {
    String NO_VERSION = "-NO_VERSION-";

    void upgrade(UpgradeContext upgradeContext) throws ConfigurationException;

    class UpgradeContext {
        private String fromVersion;
        private String toVersion;
        private Properties properties;
        private Map<String,Object> scriptConfig;
        private Configuration configuration;
        private DicomConfiguration dicomConfiguration;

        public UpgradeContext() {
        }

        public UpgradeContext(String fromVersion, String toVersion, Properties properties, Map<String,Object> scriptConfig, Configuration configuration, DicomConfiguration dicomConfiguration) {
            this.fromVersion = fromVersion;
            this.toVersion = toVersion;
            this.properties = properties;
            this.scriptConfig = scriptConfig;
            this.configuration = configuration;
            this.dicomConfiguration = dicomConfiguration;
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
        
        public Map<String,Object> getScriptConfig() {
            return scriptConfig;
        }
    }
}
