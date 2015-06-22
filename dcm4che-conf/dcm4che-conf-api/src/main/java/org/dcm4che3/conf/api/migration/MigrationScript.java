package org.dcm4che3.conf.api.migration;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;

public interface MigrationScript {
    String NO_VERSION = "-NO_VERSION-";

    void migrate(MigrationContext migrationContext) throws ConfigurationException;

    static class MigrationContext {
        private String fromVersion;
        private String toVersion;
        private Configuration configuration;
        private DicomConfiguration dicomConfiguration;

        public MigrationContext() {
        }

        public MigrationContext(String fromVersion, String toVersion, Configuration configuration, DicomConfiguration dicomConfiguration) {
            this.fromVersion = fromVersion;
            this.toVersion = toVersion;
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
    }
}
