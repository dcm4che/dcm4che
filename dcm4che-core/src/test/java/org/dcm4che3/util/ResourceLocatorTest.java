package org.dcm4che3.util;

import static org.junit.Assert.*;
import org.junit.Test;
import java.net.URL;

public class ResourceLocatorTest {

    @Test
    public void testResourceURL() {
        // Test with a known resource (this class itself)
        String name = "org/dcm4che3/util/ResourceLocatorTest.class";
        String url = ResourceLocator.resourceURL(name);
        assertNotNull(url);
        assertTrue(url.contains(name));
    }

    @Test
    public void testResourceURLNotFound() {
        assertNull(ResourceLocator.resourceURL("non/existent/resource"));
    }

    @Test
    public void testGetResourceWithObject() {
        String name = "org/dcm4che3/util/ResourceLocator.class";
        String url = ResourceLocator.getResource(name, this);
        assertNotNull(url);
    }

    @Test
    public void testGetResourceWithClass() {
        String name = "org/dcm4che3/util/ResourceLocator.class";
        String url = ResourceLocator.getResource(name, ResourceLocator.class);
        assertNotNull(url);
    }

    @Test
    public void testGetResourceURL() {
        String name = "org/dcm4che3/util/ResourceLocator.class";
        URL url = ResourceLocator.getResourceURL(name, ResourceLocator.class);
        assertNotNull(url);
        assertTrue(url.toString().contains(name));
    }

    @Test
    public void testGetResourceURLWithNull() {
        // Should fall back to context classloader or system resource
        String name = "org/dcm4che3/util/ResourceLocator.class";
        URL url = ResourceLocator.getResourceURL(name, null);
        assertNotNull(url);
    }
}
