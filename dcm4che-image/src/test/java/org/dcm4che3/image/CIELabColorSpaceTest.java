package org.dcm4che3.image;

import org.junit.Test;
import java.awt.color.ColorSpace;
import java.io.*;

import static org.junit.Assert.*;

public class CIELabColorSpaceTest {

    private static final float EPSILON = 0.001f;

    @Test
    public void testGetInstance() {
        CIELabColorSpace instance1 = CIELabColorSpace.getInstance();
        CIELabColorSpace instance2 = CIELabColorSpace.getInstance();
        assertNotNull(instance1);
        assertSame(instance1, instance2);
        assertEquals(ColorSpace.TYPE_Lab, instance1.getType());
        assertEquals(3, instance1.getNumComponents());
    }

    @Test
    public void testGetMaxValue() {
        CIELabColorSpace instance = CIELabColorSpace.getInstance();
        assertEquals(100.0f, instance.getMaxValue(0), EPSILON);
        assertEquals(127.0f, instance.getMaxValue(1), EPSILON);
        assertEquals(127.0f, instance.getMaxValue(2), EPSILON);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMaxValueInvalidIndexLow() {
        CIELabColorSpace.getInstance().getMaxValue(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMaxValueInvalidIndexHigh() {
        CIELabColorSpace.getInstance().getMaxValue(3);
    }

    @Test
    public void testGetMinValue() {
        CIELabColorSpace instance = CIELabColorSpace.getInstance();
        assertEquals(0.0f, instance.getMinValue(0), EPSILON);
        assertEquals(-128.0f, instance.getMinValue(1), EPSILON);
        assertEquals(-128.0f, instance.getMinValue(2), EPSILON);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMinValueInvalidIndexLow() {
        CIELabColorSpace.getInstance().getMinValue(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMinValueInvalidIndexHigh() {
        CIELabColorSpace.getInstance().getMinValue(3);
    }

    @Test
    public void testFromCIEXYZ() {
        CIELabColorSpace instance = CIELabColorSpace.getInstance();
        
        // Test with X=1.0, Y=1.0, Z=1.0 -> Lab=[100, 0, 0]
        // f(1.0) = 1.0
        // L = 116 * 1.0 - 16 = 100
        // a = 500 * (1.0 - 1.0) = 0
        // b = 200 * (1.0 - 1.0) = 0
        float[] xyz = {1.0f, 1.0f, 1.0f};
        float[] lab = instance.fromCIEXYZ(xyz);
        assertEquals(100.0f, lab[0], EPSILON);
        assertEquals(0.0f, lab[1], EPSILON);
        assertEquals(0.0f, lab[2], EPSILON);

        // Test with some other values
        // X=0.2, Y=0.2, Z=0.2
        xyz = new float[]{0.2f, 0.2f, 0.2f};
        lab = instance.fromCIEXYZ(xyz);
        // f(0.2) = cbrt(0.2) = 0.5848
        // L = 116 * 0.5848 - 16 = 51.8368
        // a = 500 * (f(0.2) - f(0.2)) = 0
        // b = 200 * (f(0.2) - f(0.2)) = 0
        assertEquals(51.837f, lab[0], 0.01f);
        assertEquals(0.0f, lab[1], 0.01f);
        assertEquals(0.0f, lab[2], 0.01f);
    }

    @Test
    public void testToCIEXYZ() {
        CIELabColorSpace instance = CIELabColorSpace.getInstance();
        
        // Lab=[100, 0, 0] -> XYZ=[0.9642, 1.0000, 0.8249] (D50)
        float[] lab = {100.0f, 0.0f, 0.0f};
        float[] xyz = instance.toCIEXYZ(lab);
        // l = (100 + 16)/116 = 1
        // fInv(1) = 1*1*1 = 1
        // X = fInv(1 + 0/500) = 1
        // Y = fInv(1) = 1
        // Z = fInv(1 - 0/200) = 1
        // Note: The implementation doesn't seem to apply white point scaling in toCIEXYZ/fromCIEXYZ, 
        // it just uses the raw f/fInv functions on the components.
        // Wait, fInv(1) = 1.0. So it returns {1.0, 1.0, 1.0} for {100, 0, 0}.
        assertEquals(1.0f, xyz[0], EPSILON);
        assertEquals(1.0f, xyz[1], EPSILON);
        assertEquals(1.0f, xyz[2], EPSILON);
    }

    @Test
    public void testRoundTripXYZ() {
        CIELabColorSpace instance = CIELabColorSpace.getInstance();
        float[] originalXYZ = {0.5f, 0.5f, 0.5f};
        float[] lab = instance.fromCIEXYZ(originalXYZ);
        float[] backXYZ = instance.toCIEXYZ(lab);
        
        assertArrayEquals(originalXYZ, backXYZ, EPSILON);
    }

    @Test
    public void testRoundTripRGB() {
        CIELabColorSpace instance = CIELabColorSpace.getInstance();
        float[] originalRGB = {0.75f, 0.5f, 0.25f};
        float[] lab = instance.fromRGB(originalRGB);
        float[] backRGB = instance.toRGB(lab);
        
        assertArrayEquals(originalRGB, backRGB, EPSILON);
    }

    @Test
    public void testSmallValuesXYZ() {
        CIELabColorSpace instance = CIELabColorSpace.getInstance();
        // Values below DELTA_3 = 216.0 / 24389.0 ≈ 0.008856
        float[] smallXYZ = {0.005f, 0.005f, 0.005f};
        float[] lab = instance.fromCIEXYZ(smallXYZ);
        
        // f(0.005) = 0.005 / (108/841) + 4/29 = 0.005 * 841 / 108 + 4/29 = 0.038935 + 0.137931 = 0.176866
        // L = 116 * 0.176866 - 16 = 20.516 - 16 = 4.516
        assertEquals(4.516f, lab[0], 0.01f);
        assertEquals(0.0f, lab[1], 0.01f);
        assertEquals(0.0f, lab[2], 0.01f);
        
        float[] backXYZ = instance.toCIEXYZ(lab);
        assertArrayEquals(smallXYZ, backXYZ, EPSILON);
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        CIELabColorSpace instance = CIELabColorSpace.getInstance();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(instance);
        oos.close();
        
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CIELabColorSpace deserialized = (CIELabColorSpace) ois.readObject();
        
        assertSame(instance, deserialized);
    }
}
