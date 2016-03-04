package org.dcm4che3.conf.core.api;

/**
 * Denotes a UUID conflict in configuration
 */
public class DuplicateUUIDException extends  ConfigurationException{

    private static final long serialVersionUID = -7681606202001756448L;

    String duplicateUUID;
    Path existingNodePath;
    Path nodeBeingPersistedPath;

    public DuplicateUUIDException(String duplicateUUID, Path existingNodePath, Path nodeBeingPersistedPath) {

        super("Detected duplicate UUID in configuration: " + duplicateUUID + " " +
                "\n           Existing object path: " + existingNodePath +
                "\n Path of object being persisted: " + nodeBeingPersistedPath);

        this.duplicateUUID = duplicateUUID;
        this.existingNodePath = existingNodePath;
        this.nodeBeingPersistedPath = nodeBeingPersistedPath;
    }

    public String getDuplicateUUID() {
        return duplicateUUID;
    }

    public Path getExistingNodePath() {
        return existingNodePath;
    }

    public Path getNodeBeingPersistedPath() {
        return nodeBeingPersistedPath;
    }
}
