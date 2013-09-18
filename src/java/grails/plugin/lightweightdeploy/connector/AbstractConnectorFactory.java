package grails.plugin.lightweightdeploy.connector;

import grails.plugin.lightweightdeploy.Configuration;
import org.eclipse.jetty.server.AbstractConnector;

import java.util.Set;

public abstract class AbstractConnectorFactory {

    private Configuration configuration;

    public AbstractConnectorFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    public abstract Set<? extends AbstractConnector> build();

    protected void defaultValues(AbstractConnector connector) {
        connector.setMaxIdleTime(200 * 1000);
        connector.setLowResourcesMaxIdleTime(0);
        connector.setRequestBufferSize(16 * 1024);
        connector.setRequestHeaderSize(6 * 1024);
        connector.setResponseBufferSize(32 * 1024);
        connector.setResponseHeaderSize(6 * 1024);
    }

    protected Configuration getConfiguration() {
        return configuration;
    }

}
