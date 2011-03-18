package org.dcm4che.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

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
    public boolean add(Object frag) {
        add(size(), frag);
        return true;
    }

    @Override
    public void add(int index, Object frag) {
        super.add(index, frag != null ? frag : Value.EMPTY_BYTES);
    }

    @Override
    public boolean addAll(Collection<? extends Object> c) {
        return addAll(size(), c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Object> c) {
        for (Object o : c)
            add(index++, o);
        return !c.isEmpty();
    }

    @Override
    public void writeTo(DicomOutputStream dos, VR vr)
            throws IOException {
        for (Object frag : this)
            dos.writeAttribute(Tag.Item, vr, frag, null);
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
    public int getEncodedLength(EncodeOptions encOpts, VR vr) {
        return -1;
    }

    @Override
    public byte[] toBytes(VR vr, boolean bigEndian) throws IOException {
        throw new UnsupportedOperationException();
    }
}
