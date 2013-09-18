package grails.plugin.lightweightdeploy.connector;

import com.google.common.collect.Sets;
import grails.plugin.lightweightdeploy.Configuration;
import grails.plugin.lightweightdeploy.Launcher;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.util.Set;

public class InternalConnectorFactory extends AbstractConnectorFactory {

    public static final String INTERNAL_CONNECTOR_NAME = "internal";

    public InternalConnectorFactory(Configuration configuration) {
        super(configuration);
    }

    @Override
    public Set<? extends AbstractConnector> build() {
        final SocketConnector connector = new SocketConnector();
        connector.setPort(getConfiguration().getAdminPort());
        connector.setThreadPool(new QueuedThreadPool(8));
        connector.setName(INTERNAL_CONNECTOR_NAME);
        return Sets.newHashSet(connector);
    }

}
