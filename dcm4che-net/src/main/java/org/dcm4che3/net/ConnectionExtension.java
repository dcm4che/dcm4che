package org.dcm4che3.net;


/**
 * @author Roman K
 */
public class ConnectionExtension  {

    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void reconfigure(ConnectionExtension from) {
    }

    public Class<ConnectionExtension> getBaseClass() {
        return ConnectionExtension.class;
    }
}
