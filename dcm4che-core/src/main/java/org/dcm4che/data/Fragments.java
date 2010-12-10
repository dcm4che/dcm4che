package org.dcm4che.data;

import java.util.ArrayList;

public class Fragments extends ArrayList<Object> {

    private static final long serialVersionUID = -6667210062541083610L;

    private final VR vr;
    private final boolean bigEndian;

    Fragments(VR vr, boolean bigEndian, int initialCapacity) {
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

    public int calcLength() {
        int len = 0;
        for (Object o : this) {
            len += 8;
            if (o != null)
                len += (((o instanceof BulkDataLocator) 
                            ? ((BulkDataLocator) o).length 
                            : ((byte[]) o).length)
                        + 1) & ~1;
        }
        return len;
    }

    @Override
    public String toString() {
        return "" + size() + " Fragments";
    }
}
