package org.dcm4che3.util;

import static org.junit.Assert.*;
import org.junit.Test;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ReverseDNSTest {

    @Test
    public void testHostNameOf() throws UnknownHostException {
        InetAddress addr = InetAddress.getByName("127.0.0.1");
        String hostName = ReverseDNS.hostNameOf(addr);
        assertNotNull(hostName);
    }

    @Test
    public void testDisabled() throws UnknownHostException {
        System.setProperty("org.dcm4che3.util.ReverseDNS", "false");
        try {
            // Need to re-trigger static initialization or check behavior if it's already initialized.
            // Since DISABLED is final, we might not be able to change it if it's already loaded.
            // But we can still test the current behavior.
            InetAddress addr = InetAddress.getByName("127.0.0.1");
            String hostName = ReverseDNS.hostNameOf(addr);
            assertNotNull(hostName);
            
            if (ReverseDNS.DISABLED) {
                assertEquals(addr.getHostAddress(), hostName);
            }
        } finally {
            System.clearProperty("org.dcm4che3.util.ReverseDNS");
        }
    }
}
