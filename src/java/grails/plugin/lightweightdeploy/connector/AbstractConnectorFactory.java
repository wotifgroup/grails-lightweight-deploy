package grails.plugin.lightweightdeploy.connector;

import grails.plugin.lightweightdeploy.Configuration;
import org.eclipse.jetty.server.AbstractConnector;

import java.util.Set;

public abstract class AbstractConnectorFactory {

    private HttpConfiguration configuration;

    public AbstractConnectorFactory(HttpConfiguration configuration) {
        this.configuration = configuration;
    }

    public abstract Set<? extends AbstractConnector> build();

    protected void applyConfiguration(AbstractConnector connector) {
        connector.setAcceptorPriorityOffset(configuration.getAcceptorThreadPriorityOffset());
        connector.setAcceptors(configuration.getAcceptorThreads());
        connector.setAcceptQueueSize(configuration.getAcceptQueueSize());
        connector.setLowResourcesMaxIdleTime(configuration.getLowResourcesMaxIdleTime());
        connector.setMaxBuffers(configuration.getMaxBufferCount());
        connector.setMaxIdleTime(configuration.getMaxIdleTime());
        connector.setRequestBufferSize(configuration.getRequestBufferSize());
        connector.setRequestHeaderSize(configuration.getRequestHeaderBufferSize());
        connector.setResponseBufferSize(configuration.getResponseBufferSize());
        connector.setResponseHeaderSize(configuration.getResponseHeaderBufferSize());
        connector.setReuseAddress(configuration.isReuseAddress());

        // Use X-Forwarded-For header for origin IP
        connector.setForwarded(true);
    }

    protected HttpConfiguration getConfiguration() {
        return configuration;
    }

}
