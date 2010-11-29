package org.dcm4che.data;

public class EncodeOptions {

    public static final EncodeOptions DEFAULTS =
            new EncodeOptions(false, true, false, true, false);

    private final boolean groupLength;
    private final boolean undefSeqLength;
    private final boolean undefEmptySeqLength;
    private final boolean undefItemLength;
    private final boolean undefEmptyItemLength;
    
    public EncodeOptions(boolean groupLength,
            boolean undefSeqLength, boolean undefEmptySeqLength,
            boolean undefItemLength, boolean undefEmptyItemLength) {
        if (undefEmptySeqLength && !undefSeqLength)
            throw new IllegalArgumentException();
        if (undefEmptyItemLength && !undefItemLength)
            throw new IllegalArgumentException();
        this.groupLength = groupLength;
        this.undefSeqLength = undefSeqLength;
        this.undefEmptySeqLength = undefEmptySeqLength;
        this.undefItemLength = undefItemLength;
        this.undefEmptyItemLength = undefEmptyItemLength;
    }

    public final boolean isGroupLength() {
        return groupLength;
    }

    public final boolean isUndefSequenceLength() {
        return undefSeqLength;
    }

    public final boolean isUndefEmptySequenceLength() {
        return undefEmptySeqLength;
    }

    public final boolean isUndefItemLength() {
        return undefItemLength;
    }

    public final boolean isUndefEmptyItemLength() {
        return undefEmptyItemLength;
    }

    public final boolean needCalcLength() {
        return groupLength || !undefSeqLength || !undefItemLength;
    }
}
