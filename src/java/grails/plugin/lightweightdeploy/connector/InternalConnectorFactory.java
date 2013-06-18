package grails.plugin.lightweightdeploy.connector;

import grails.plugin.lightweightdeploy.Configuration;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class InternalConnectorFactory extends AbstractConnectorFactory {

    public InternalConnectorFactory(Configuration configuration) {
        super(configuration);
    }

    @Override
    public AbstractConnector build() {
        final SocketConnector connector = new SocketConnector();
        connector.setPort(getConfiguration().getAdminPort());
        connector.setThreadPool(new QueuedThreadPool(8));
        return connector;
    }
}
