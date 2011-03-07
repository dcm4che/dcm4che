package org.dcm4che.data;

import java.io.IOException;

import org.dcm4che.io.DicomOutputStream;

public interface Value {
    
    final byte[] EMPTY_BYTES = {};
    final Value NULL = new Value() {

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void writeTo(DicomOutputStream dos, int tag, VR vr)
                throws IOException {
            if (vr == VR.SQ
                    && dos.getEncodeOptions().isUndefEmptySequenceLength()) {
                dos.writeHeader(tag, vr, -1);
                dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
            } else {
                dos.writeHeader(tag, vr, 0);
            }
        }

        @Override
        public int calcLength(boolean explicitVR, EncodeOptions encOpts, VR vr) {
             return vr == VR.SQ && encOpts.isUndefEmptySequenceLength() ? 8 : 0 ;
        }

        @Override
        public String toString() {
            return "";
        }

        @Override
        public byte[] toBytes(VR vr, boolean bigEndian) {
            return EMPTY_BYTES;
        }
    };

    boolean isEmpty();

    byte[] toBytes(VR vr, boolean bigEndian) throws IOException;

    void writeTo(DicomOutputStream dos, int tag, VR vr) throws IOException;

    int calcLength(boolean explicitVR, EncodeOptions encOpts, VR vr);

}
