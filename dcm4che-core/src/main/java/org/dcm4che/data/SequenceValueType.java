package org.dcm4che.data;

enum SequenceValueType implements ValueType {
    SQ;

    @Override
    public int numEndianBytes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] toggleEndian(byte[] b, boolean preserve) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] toBytes(Object val, SpecificCharacterSet cs) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public String toString(Object val, boolean bigEndian, int valueIndex,
            String defVal) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public Object toStrings(Object val, boolean bigEndian,
            SpecificCharacterSet cs) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public int toInt(Object val, boolean bigEndian, int valueIndex,
            int defVal) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public int[] toInts(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public float toFloat(Object val, boolean bigEndian, int valueIndex,
            float defVal) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public float[] toFloats(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public double toDouble(Object val, boolean bigEndian, int valueIndex,
            double defVal) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public double[] toDoubles(Object val, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public Object toValue(byte[] b) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public Object toValue(String s, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public Object toValue(String[] ss, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public Object toValue(int[] is, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public Object toValue(float[] fs, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public Object toValue(double[] ds, boolean bigEndian) {
        throw new UnsupportedOperationException();
    } 

    @Override
    public boolean prompt(Object val, boolean bigEndian,
            SpecificCharacterSet cs, int maxChars, StringBuilder sb) {
        throw new UnsupportedOperationException();
    }
}
