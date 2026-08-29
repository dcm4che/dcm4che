package org.dcm4che3.util;

import static org.junit.Assert.*;
import org.junit.Test;

public class PropertyTest {

    @Test
    public void testPropertyStringObject() {
        Property p = new Property("name", "value");
        assertEquals("name", p.getName());
        assertEquals("value", p.getValue());
    }

    @Test
    public void testPropertyString() {
        Property p = new Property("name=value");
        assertEquals("name", p.getName());
        assertEquals("value", p.getValue());
        
        p = new Property("flag=true");
        assertEquals(Boolean.TRUE, p.getValue());
        
        p = new Property("num=12.3");
        assertEquals(12.3, (Double) p.getValue(), 0.0);
    }

    @Test
    public void testEqualsHashCode() {
        Property p1 = new Property("name", "value");
        Property p2 = new Property("name", "value");
        Property p3 = new Property("name", "other");
        
        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testToString() {
        Property p = new Property("name", "value");
        assertEquals("name=value", p.toString());
    }

    public static class MockBean {
        private String stringVal;
        private boolean booleanVal;
        private int intVal;
        private double doubleVal;

        public void setStringVal(String stringVal) { this.stringVal = stringVal; }
        public void setBooleanVal(boolean booleanVal) { this.booleanVal = booleanVal; }
        public void setIntVal(int intVal) { this.intVal = intVal; }
        public void setDoubleVal(double doubleVal) { this.doubleVal = doubleVal; }
    }

    @Test
    public void testSetAt() {
        MockBean bean = new MockBean();
        
        new Property("stringVal", "test").setAt(bean);
        assertEquals("test", bean.stringVal);
        
        new Property("booleanVal", true).setAt(bean);
        assertTrue(bean.booleanVal);
        
        new Property("intVal", 123).setAt(bean);
        assertEquals(123, bean.intVal);
        
        new Property("doubleVal", 12.3).setAt(bean);
        assertEquals(12.3, bean.doubleVal, 0.0);
    }

    @Test
    public void testValueOfArray() {
        String[] ss = {"a=1", "b=true", "c=val"};
        Property[] props = Property.valueOf(ss);
        assertEquals(3, props.length);
        assertEquals(1.0, (Double) props[0].getValue(), 0.0);
        assertEquals(Boolean.TRUE, props[1].getValue());
        assertEquals("val", props[2].getValue());
    }

    @Test
    public void testGetFrom() {
        Property[] props = {new Property("a", 1.0), new Property("b", "val")};
        assertEquals(1.0, Property.getFrom(props, "a", 0.0), 0.0);
        assertEquals("val", Property.getFrom(props, "b", "def"));
        assertEquals("def", Property.getFrom(props, "c", "def"));
    }
}
