package org.dcm4che3.conf.migration;


import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.api.migration.MigrationScript;
import org.dcm4che3.conf.core.DefaultBeanVitalizer;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.internal.BeanVitalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * @author Roman K
 */
public class MigrationRunner {

    public static final String MIGRATE_TO_VERSION_PROP = "org.dcm4che.conf.onStartupMigrateToVersion";
    private static Logger log = LoggerFactory
            .getLogger(MigrationRunner.class);

    public static final String METADATA_ROOT_PATH = "/dicomConfigurationRoot/metadataRoot/versioning";
    public static final String NO_VERSION = "NO_VERSION";

    private Configuration configuration;
    private Collection<MigrationScript> migrationScripts;
    private DicomConfiguration dicomConfiguration;

    public MigrationRunner() {
    }

    public MigrationRunner(Configuration configuration, Collection<MigrationScript> migrationScripts, DicomConfiguration dicomConfiguration) {
        this.configuration = configuration;
        this.migrationScripts = migrationScripts;
        this.dicomConfiguration = dicomConfiguration;
    }

    public void migrate() throws ConfigurationException {
        String property = System.getProperty(MIGRATE_TO_VERSION_PROP);
        if (property != null) {
            log.info("Dcm4che configuration init: migrating configuration to version "+property);
            migrateToVersion(property);
        } else
            log.warn("Dcm4che configuration init: Not running any migration - target configuration version is not specified (property " + MIGRATE_TO_VERSION_PROP + ")");
    }


    protected ConfigurationMetadata migrateToVersion(String toVersion) throws ConfigurationException {

        configuration.lock();

        BeanVitalizer beanVitalizer = new DefaultBeanVitalizer();

        Object metadataNode = configuration.getConfigurationNode(METADATA_ROOT_PATH, ConfigurationMetadata.class);
        ConfigurationMetadata configMetadata = null;
        if (metadataNode != null)
            configMetadata = beanVitalizer.newConfiguredInstance((Map<String, Object>) metadataNode, ConfigurationMetadata.class);
        else {
            configMetadata = new ConfigurationMetadata();
            configMetadata.setVersion(NO_VERSION);
        }
        String fromVersion = configMetadata.getVersion();

        // check if we need to run scripts at all
        if (fromVersion.equals(toVersion)) {
            log.info("Configuration migration is not needed - configuration version is already " + toVersion);
            return configMetadata;
        }

        MigrationScript.MigrationContext migrationContext = new MigrationScript.MigrationContext(fromVersion, toVersion, configuration, dicomConfiguration);

        // run all scripts
        for (MigrationScript migrationScript : migrationScripts) {
            log.info("Executing migration script "+migrationScript.getClass().getName());
            migrationScript.migrate(migrationContext);
        }

        // update version
        configMetadata.setVersion(toVersion);
        configuration.persistNode(METADATA_ROOT_PATH, beanVitalizer.createConfigNodeFromInstance(configMetadata), ConfigurationMetadata.class);

        return configMetadata;
    }


}

