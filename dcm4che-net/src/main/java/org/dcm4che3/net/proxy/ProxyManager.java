package org.dcm4che3.net.proxy;

import java.io.IOException;
import java.net.Socket;

/**
 * Define Service Provider Interface for proxy manager (authentication
 * processing)
 *
 * @author Amaury Pernette
 * 
 */
public interface ProxyManager {

    void doProxyHandshake(final Socket s, final String hostname, final int port, final String userauth,
            final int connectTimeout) throws IOException;

    String getProviderName();

    String getVersion();

}
