package org.dcm4che3.conf.core.api;

/**
 * This base class represent a "root" {@link ConfigurableClass} that gets backed
 * by the storage and thus has a (JPA) version associated with it.  
 * 
 * @author Maciek Siemczyk (maciek.siemczyk@agfa.com)
 */
public abstract class StorageVersionedConfigurableClass {

    private long storageVersion;

    public long getStorageVersion() {

        return storageVersion;
    }

    public void setStorageVersion(long version) {

        this.storageVersion = version;
    }    
}
