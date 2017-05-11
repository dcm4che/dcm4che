package org.dcm4che3.conf.api;

public class DicomConfigOptions {

    /**
     * If set to true, will not throw exceptions when a reference to a configuration object cannot be resolved.
     * Instead, such properties will be left null
     */
    private Boolean ignoreUnresolvedReferences;

    public Boolean getIgnoreUnresolvedReferences() {
        return ignoreUnresolvedReferences;
    }

    public void setIgnoreUnresolvedReferences(Boolean ignoreUnresolvedReferences) {
        this.ignoreUnresolvedReferences = ignoreUnresolvedReferences;
    }
}
