package org.dcm4che3.net;

import org.dcm4che3.conf.core.api.ConfigurableClassExtension;
import org.dcm4che3.conf.core.api.Parent;
import org.dcm4che3.conf.core.api.SetParentIntoField;

/**
 * @author Roman K
 */
public class ConnectionExtension extends ConfigurableClassExtension<ConnectionExtension> {

    @Parent
    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void reconfigure(ConnectionExtension from) {
    }

    @Override
    public Class<ConnectionExtension> getBaseClass() {
        return ConnectionExtension.class;
    }
}
