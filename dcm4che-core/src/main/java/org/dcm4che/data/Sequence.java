package org.dcm4che.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.dcm4che.io.DicomOutputStream;


public class Sequence extends ArrayList<Attributes> implements Value {

    private static final long serialVersionUID = 7062970085409148066L;

    private final Attributes parent;
    private int length;

    Sequence(Attributes parent, int initialCapacity) {
        super(initialCapacity);
        this.parent = parent;
    }

    public final Attributes getParent() {
        return parent;
    }

    public final int getLength() {
        return length;
    }

    public void trimToSize(boolean recursive) {
        super.trimToSize();
        if (recursive)
            for (Attributes attrs: this)
                attrs.trimToSize(recursive);
    }

    @Override
    public boolean add(Attributes attrs) {
        return super.add(attrs.setParent(parent));
    }

    @Override
    public void add(int index, Attributes attrs) {
        super.add(index, attrs.setParent(parent));
    }

    @Override
    public boolean addAll(Collection<? extends Attributes> c) {
        setParent(c);
        return super.addAll(c);
    }

    private void setParent(Collection<? extends Attributes> c) {
        for (Attributes attrs : c)
            if (!attrs.isRoot())
                throw new IllegalStateException(
                    "Item already contained by Sequence");
        for (Attributes attrs : c)
            attrs.setParent(parent);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Attributes> c) {
        setParent(c);
        return super.addAll(index, c);
    }

    @Override
    public void clear() {
        for (Attributes attrs: this)
            attrs.setParent(null);
        super.clear();
    }

    @Override
    public Attributes remove(int index) {
        return super.remove(index).setParent(null);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Attributes && super.remove(o)) {
            ((Attributes) o).setParent(null);
            return true;
        }
        return false;
    }

    @Override
    public Attributes set(int index, Attributes attrs) {
        return super.set(index, attrs.setParent(parent));
    }

    @Override
    public String toString() {
        return "" + size() + " Items";
    }

    @Override
    public int calcLength(boolean explicitVR, EncodeOptions encOpts, VR vr) {
        int len = 0;
        for (Attributes item : this) {
            len += 8 + item.calcLength(explicitVR, encOpts);
            if (item.isEmpty() ? encOpts.isUndefEmptyItemLength()
                               : encOpts.isUndefItemLength())
                len += 8;
        }
        if (isEmpty() ? encOpts.isUndefEmptySequenceLength()
                      : encOpts.isUndefSequenceLength())
            len += 8;
        length = len;
        return len;
    }

    @Override
    public void writeTo(DicomOutputStream dos, int tag, VR vr)
            throws IOException {
        EncodeOptions encOpts = dos.getEncodeOptions();
        int len = isEmpty()
                ? encOpts.isUndefEmptySequenceLength() ? -1 : 0
                : encOpts.isUndefSequenceLength() ? -1 : length;
        dos.writeHeader(tag, VR.SQ, len);
        for (Attributes item : this)
            item.writeItemTo(dos);
        if (len != -1)
            dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
    }

    @Override
    public byte[] toBytes(VR vr, boolean bigEndian) throws IOException {
        throw new UnsupportedOperationException();
    }
}
