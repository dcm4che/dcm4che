package org.dcm4che3.net.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.dcm4che3.util.Base64;

/**
 * Define basic proxy authentication processing.
 * This code come from old Connection.doProxyHandshake() method (before refactoring).
 *
 * @author Amaury Pernette
 * 
 */
public class BasicProxyManager implements ProxyManager {

	private static String PROVIDER_NAME = "org.dcm4che";
	private static String VERSION = "1.0";
	
	@Override
	public String getProviderName() {
		return PROVIDER_NAME;
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public void doProxyHandshake(Socket s, String hostname, int port,
			String userauth, int connectTimeout) throws IOException {

        StringBuilder request = new StringBuilder(128);
        request.append("CONNECT ")
                .append(hostname).append(':').append(port)
                .append(" HTTP/1.1\r\nHost: ")
                .append(hostname).append(':').append(port);
        if (userauth != null) {
            byte[] b = userauth.getBytes("UTF-8");
            char[] base64 = new char[(b.length + 2) / 3 * 4];
            Base64.encode(b, 0, b.length, base64, 0);
            request.append("\r\nProxy-Authorization: basic ")
                    .append(base64);
        }
        request.append("\r\n\r\n");
        OutputStream out = s.getOutputStream();
        out.write(request.toString().getBytes("US-ASCII"));
        out.flush();

        s.setSoTimeout(connectTimeout);
        @SuppressWarnings("resource")
        String response = new HTTPResponse(s).toString();
        s.setSoTimeout(0);
        if (!response.startsWith("HTTP/1.1 2"))
            throw new IOException("Unable to tunnel through " + s
                    + ". Proxy returns \"" + response + '\"');

	}

    private static class HTTPResponse extends ByteArrayOutputStream {

        private final String rsp;

        public HTTPResponse(Socket s) throws IOException {
            super(64);
            InputStream in = s.getInputStream();
            boolean eol = false;
            int b;
            while ((b = in.read()) != -1) {
                write(b);
                if (b == '\n') {
                    if (eol) {
                        rsp = new String(super.buf, 0, super.count, "US-ASCII");
                        return;
                    }
                    eol = true;
                } else if (b != '\r') {
                    eol = false;
                }
            }
            throw new IOException("Unexpected EOF from " + s);
        }

        @Override
        public String toString() {
            return rsp;
        }
    }
}
