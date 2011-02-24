package org.dcm4che.data;

interface ValueType {

    int numEndianBytes();

    byte[] toggleEndian(byte[] b, boolean preserve);

    byte[] toBytes(Object val, SpecificCharacterSet cs);

    String toString(Object val, boolean bigEndian, int valueIndex, String defVal);

    Object toStrings(Object val, boolean bigEndian, SpecificCharacterSet cs);

    int toInt(Object val, boolean bigEndian, int valueIndex, int defVal);

    int[] toInts(Object val, boolean bigEndian);

    float toFloat(Object val, boolean bigEndian, int valueIndex, float defVal);

    float[] toFloats(Object val, boolean bigEndian);

    double toDouble(Object val, boolean bigEndian, int valueIndex,
            double defVal);

    double[] toDoubles(Object val, boolean bigEndian);

    Object toValue(byte[] b);

    Object toValue(String s, boolean bigEndian);

    Object toValue(String[] ss, boolean bigEndian);

    Object toValue(int[] is, boolean bigEndian);

    Object toValue(float[] fs, boolean bigEndian);

    Object toValue(double[] ds, boolean bigEndian);

    boolean prompt(Object val, boolean bigEndian, SpecificCharacterSet cs,
            int maxChars, StringBuilder sb);

}
