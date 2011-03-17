package org.dcm4che.data;

public class EncodeOptions {

    private boolean groupLength = false;
    private boolean undefSeqLength = true;
    private boolean undefEmptySeqLength = false;
    private boolean undefItemLength = true;
    private boolean undefEmptyItemLength = false;

    public final boolean isGroupLength() {
        return groupLength;
    }

    public final void setGroupLength(boolean groupLength) {
        this.groupLength = groupLength;
    }

    public final boolean isUndefSequenceLength() {
        return undefSeqLength;
    }

    public final void setUndefSequenceLength(boolean undefLength) {
        this.undefSeqLength = undefLength;
        if (!undefLength)
            undefEmptySeqLength = false;
    }

    public final boolean isUndefEmptySequenceLength() {
        return undefEmptySeqLength;
    }

    public final void setUndefEmptySequenceLength(boolean undefLength) {
        this.undefEmptySeqLength = undefLength;
        if (undefLength)
            undefSeqLength = true;
    }

    public final boolean isUndefItemLength() {
        return undefItemLength;
    }

    public final void setUndefItemLength(boolean undefLength) {
        this.undefItemLength = undefLength;
        if (!undefLength)
            undefEmptyItemLength = false;
    }

    public final boolean isUndefEmptyItemLength() {
        return undefEmptyItemLength;
    }

    public final void setUndefEmptyItemLength(boolean undefLength) {
        this.undefEmptyItemLength = undefLength;
        if (undefLength)
            undefItemLength = true;
    }

    public final boolean needCalcLength() {
        return groupLength || !undefSeqLength || !undefItemLength;
    }
}
