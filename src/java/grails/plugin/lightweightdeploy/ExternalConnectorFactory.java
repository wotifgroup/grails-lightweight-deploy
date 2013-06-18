package grails.plugin.lightweightdeploy;

import com.codahale.metrics.Clock;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jetty8.InstrumentedSelectChannelConnector;
import com.codahale.metrics.jetty8.InstrumentedSslSocketConnector;
import grails.plugin.lightweightdeploy.logging.RequestLoggingFactory;
import java.io.File;
import java.io.IOException;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalConnectorFactory {
    private static final Logger logger = LoggerFactory.getLogger(ExternalConnectorFactory.class);

    private Configuration configuration;
    private MetricRegistry metricRegistry;
    private HealthCheckRegistry healthCheckRegistry;

    public ExternalConnectorFactory(Configuration configuration, HealthCheckRegistry healthCheckRegistry, MetricRegistry metricRegistry) {
        this.configuration = configuration;
        this.healthCheckRegistry = healthCheckRegistry;
        this.metricRegistry = metricRegistry;
    }

    public AbstractConnector build() {
        AbstractConnector connector = null;
		if (this.configuration.isSsl()) {
            logger.info("Creating https connector");
			connector = configureExternalHttpsConnector();
		} else {
            logger.info("Creating http connector");
		    connector = configureExternalHttpConnector();
        }

        defaultValues(connector);

        return connector;
    }

	protected AbstractConnector configureExternalHttpConnector() {
        InstrumentedSelectChannelConnector connector = new InstrumentedSelectChannelConnector(
            this.metricRegistry,
            this.configuration.getPort(),
            Clock.defaultClock());
        connector.setName("external");
        connector.setUseDirectBuffers(true);
        return connector;
	}

	protected AbstractConnector configureExternalHttpsConnector() {
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setCertAlias(this.configuration.getKeyStoreAlias());
        sslContextFactory.setKeyStorePath(this.configuration.getKeyStorePath());
        sslContextFactory.setKeyStorePassword(this.configuration.getKeyStorePassword());

        InstrumentedSslSocketConnector connector = new InstrumentedSslSocketConnector(
            this.metricRegistry,
            this.configuration.getPort(),
            sslContextFactory,
            Clock.defaultClock());
        connector.setName("external");

        return connector;
	}

    protected void defaultValues(AbstractConnector connector) {
        connector.setMaxIdleTime(200 * 1000);
        connector.setLowResourcesMaxIdleTime(0);
        connector.setRequestBufferSize(16 * 1024);
        connector.setRequestHeaderSize(6 * 1024);
        connector.setResponseBufferSize(32 * 1024);
        connector.setResponseHeaderSize(6 * 1024);
    }
}
