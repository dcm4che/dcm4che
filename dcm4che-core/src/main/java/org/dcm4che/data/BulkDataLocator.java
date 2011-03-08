package org.dcm4che.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.dcm4che.io.DicomOutputStream;
import org.dcm4che.util.StreamUtils;


public class BulkDataLocator implements Value {

    public final String uri;
    public final String transferSyntax;
    public final long offset;
    public final int length;

    public BulkDataLocator(String uri, String transferSyntax, long offset,
            int length) {
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
    public int calcLength(boolean explicitVR, EncodeOptions encOpts, VR vr) {
        return getEncodedLength();
    }

    @Override
    public int getEncodedLength() {
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
            if (transferSyntax.equals(UID.ExplicitVRBigEndian) 
                    ? !bigEndian
                    : bigEndian) {
                vr.toggleEndian(b, false);
            }
            return b;
        } finally {
            in.close();
        }

    }

    @Override
    public void writeTo(DicomOutputStream dos, VR vr) throws IOException {
        InputStream in = openStream();
        try {
            StreamUtils.skipFully(in, offset);
            if (transferSyntax.equals(UID.ExplicitVRBigEndian)
                    ? !dos.isBigEndian()
                    : dos.isBigEndian())
                StreamUtils.copy(in, dos, length, vr.numEndianBytes());
            else
                StreamUtils.copy(in, dos, length);
            if ((length & 1) != 0)
                dos.write(vr.paddingByte());
        } finally {
            in.close();
        }
    }

    public void serializeTo(ObjectOutputStream oos) throws IOException {
        oos.writeInt(length);
        oos.writeLong(offset);
        oos.writeUTF(uri);
        oos.writeUTF(transferSyntax);
    }

    public static Value deserializeFrom(ObjectInputStream ois)
            throws IOException {
        int len = ois.readInt();
        long off = ois.readLong();
        String uri = ois.readUTF();
        String tsuid = ois.readUTF();
        return new BulkDataLocator(uri, tsuid, off, len);
    }
}
