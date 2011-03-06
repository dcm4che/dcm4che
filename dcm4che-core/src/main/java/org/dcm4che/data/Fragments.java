package org.dcm4che.data;

import java.io.IOException;
import java.util.ArrayList;

import org.dcm4che.io.DicomOutputStream;

public class Fragments extends ArrayList<Object> implements Value {

    private static final long serialVersionUID = -6667210062541083610L;

    private final VR vr;
    private final boolean bigEndian;

    public Fragments(VR vr, boolean bigEndian, int initialCapacity) {
        super(initialCapacity);
        this.vr = vr;
        this.bigEndian = bigEndian;
    }

    public final VR vr() {
        return vr;
    }

    public final boolean bigEndian() {
        return bigEndian;
    }

    @Override
    public String toString() {
        return "" + size() + " Fragments";
    }

    @Override
    public void writeTo(DicomOutputStream dos, int tag, VR vr)
            throws IOException {
        dos.writeHeader(tag, vr, -1);
        for (Object frag : this)
            if (frag instanceof Value)
                ((Value) frag).writeTo(dos, Tag.Item, vr);
            else
                dos.writeAttribute(Tag.Item, this.vr(), (byte[]) frag);
        dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
    }

    @Override
    public int calcLength(boolean explicitVR, EncodeOptions encOpts, VR vr) {
        int len = 0;
        for (Object frag : this) {
            len += 8;
            if (frag instanceof Value)
                len += ((Value) frag).calcLength(explicitVR, encOpts, vr);
            else
                len += (((byte[]) frag).length + 1) & ~1;
        }
        return len;
    }

    @Override
    public byte[] toBytes(VR vr, boolean bigEndian) throws IOException {
        throw new UnsupportedOperationException();
    }
}
