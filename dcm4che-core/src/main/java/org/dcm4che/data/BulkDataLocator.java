package org.dcm4che.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.dcm4che.io.DicomOutputStream;
import org.dcm4che.util.StreamUtils;


public class BulkDataLocator implements Value {

    private static final int BULK_DATA_LOCATOR = 0xffff;

    public final String uri;
    public final String transferSyntax;
    public final long offset;
    public final int length;
    public final boolean deleteOnFinalize;

    public BulkDataLocator(String uri, String transferSyntax, long offset,
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
        this.offset = offset;
        this.length = length;
        this.deleteOnFinalize = deleteOnFinalize;
    }

    protected void finalize() throws Throwable {
        if (deleteOnFinalize)
            new File(new URI(uri)).delete();
    }

    @Override
    public boolean isEmpty() {
        return length == 0;
    }

    @Override
    public String toString() {
        return "BulkDataLocator[uri=" +  uri 
                + ", tsuid=" + transferSyntax
                + ", pos=" + offset
                + ", len=" + length + "]";
    }

    public InputStream openStream() throws IOException {
        try {
            return new URI(uri).toURL().openStream();
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void writeTo(DicomOutputStream dos, int tag, VR vr) throws IOException {
        if (dos.isIncludeBulkDataLocator() && !deleteOnFinalize) {
            dos.writeHeader(tag, vr, BULK_DATA_LOCATOR);
            dos.writeBulkDataLocator(this);
        } else {
            int swapBytes = vr.numEndianBytes();
            int padlen = length & 1;
            dos.writeHeader(tag, vr, length + padlen);
            InputStream in = openStream();
            try {
                StreamUtils.skipFully(in, offset);
                boolean bigEndian = dos.isBigEndian();
                if (swapBytes != 1 
                        && (transferSyntax.equals(UID.ExplicitVRBigEndian)
                                ? !bigEndian : dos.isBigEndian()))
                    StreamUtils.copy(in, dos, length, swapBytes);
                else
                    StreamUtils.copy(in, dos, length);
            } finally {
                in.close();
            }
            if (padlen > 0)
                dos.write(vr.paddingByte());
        }
    }

    @Override
    public int calcLength(boolean explicitVR, EncodeOptions encOpts, VR vr) {
        return (length + 1) & ~1;
    }

    @Override
    public byte[] toBytes(VR vr, boolean bigEndian) throws IOException {
        if (length == 0)
            return EMPTY_BYTES;

        InputStream in = openStream();
        try {
            StreamUtils.skipFully(in, offset);
            byte[] b = new byte[length];
            StreamUtils.readFully(in, b, 0, b.length);
            if (transferSyntax.equals(UID.ExplicitVRBigEndian) ? !bigEndian
                    : bigEndian) {
                vr.toggleEndian(b, false);
            }
            return b;
        } finally {
            in.close();
        }

    }
}
