package org.dcm4che3.imageio.plugins.dcm;

import org.dcm4che3.imageio.metadata.DicomImageAccessor;
import org.w3c.dom.Node;

import javax.imageio.metadata.IIOMetadata;

/**
 * Abstract class which all DicomImageAccessor implementations should implement to
 * @author Andrew Cowan (awcowan@gmail.com)
 */
public abstract class DicomImageAccessorMetaData extends IIOMetadata implements DicomImageAccessor {

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public Node getAsTree(String formatName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void mergeTree(String formatName, Node root) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }
}
