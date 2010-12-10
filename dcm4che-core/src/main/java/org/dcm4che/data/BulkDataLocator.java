package org.dcm4che.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;


public class BulkDataLocator {

    public final String uri;
    public final String transferSyntax;
    public final long position;
    public final int length;
    public final boolean deleteOnFinalize;

    public BulkDataLocator(String uri, String transferSyntax, long position,
            int length, boolean deleteOnFinalize) {
        try {
            new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("uri: " + uri);
        }
        if (transferSyntax == null)
            throw new NullPointerException("transferSyntax");
        this.uri = uri;
        this.transferSyntax = transferSyntax;
        this.position = position;
        this.length = length;
        this.deleteOnFinalize = deleteOnFinalize;
    }

    protected void finalize() throws Throwable {
        if (deleteOnFinalize)
            new File(new URI(uri)).delete();
    }

    public String toString() {
        return "BulkDataLocator[uri=" +  uri 
                + ", tsuid=" + transferSyntax
                + ", pos=" + position
                + ", len=" + length + "]";
    }

    public InputStream openStream() throws IOException {
        try {
            return new URI(uri).toURL().openStream();
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }
}
