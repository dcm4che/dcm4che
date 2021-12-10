package org.dcm4che3.conf.core.api;

import java.io.Serializable;

/**
 * This base class represent a "root" {@link ConfigurableClass} that gets backed
 * by the storage and thus has a (JPA) version associated with it.  
 * 
 * @author Maciek Siemczyk (maciek.siemczyk@agfa.com)
 * 
 * @see Serializable
 */
public abstract class StorageVersionedConfigurableClass implements Serializable {
    
    private static final long serialVersionUID = -4117224687761026222L;
    
    private long storageVersion;

    public long getStorageVersion() {

        return storageVersion;
    }

    public void setStorageVersion(long version) {

        this.storageVersion = version;
    }    
}
