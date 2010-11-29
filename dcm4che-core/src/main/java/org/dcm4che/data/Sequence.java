package org.dcm4che.data;

import java.util.ArrayList;
import java.util.Collection;


public class Sequence extends ArrayList<Attributes> {

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

    int calcLength(boolean explicitVR, EncodeOptions encOpts) {
        int len = 0;
        for (Attributes item : this) {
            len += 8;
            if (item.isEmpty()) {
                if (encOpts.isUndefEmptyItemLength())
                    len += 8;
            } else {
                len += item.calcLength(explicitVR, encOpts);
                if (encOpts.isUndefItemLength())
                    len += 8;
            }
        }
        length = len;
        return len;
    }
}
