package grails.plugin.lightweightdeploy.connector;

import com.codahale.metrics.Clock;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jetty8.InstrumentedSelectChannelConnector;
import com.codahale.metrics.jetty8.InstrumentedSslSocketConnector;
import grails.plugin.lightweightdeploy.Configuration;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalConnectorFactory extends AbstractConnectorFactory {
    private static final Logger logger = LoggerFactory.getLogger(ExternalConnectorFactory.class);

    private MetricRegistry metricRegistry;
    private HealthCheckRegistry healthCheckRegistry;

    public ExternalConnectorFactory(Configuration configuration, HealthCheckRegistry healthCheckRegistry, MetricRegistry metricRegistry) {
        super(configuration);
        this.healthCheckRegistry = healthCheckRegistry;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public AbstractConnector build() {
        AbstractConnector connector = null;
		if (getConfiguration().isSsl()) {
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
            getConfiguration().getPort(),
            Clock.defaultClock());
        connector.setName("external");
        connector.setUseDirectBuffers(true);
        return connector;
	}

	protected AbstractConnector configureExternalHttpsConnector() {
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setCertAlias(getConfiguration().getSslConfiguration().getKeyStoreAlias());
        sslContextFactory.setKeyStorePath(getConfiguration().getSslConfiguration().getKeyStorePath());
        sslContextFactory.setKeyStorePassword(getConfiguration().getSslConfiguration().getKeyStorePassword());

        InstrumentedSslSocketConnector connector = new InstrumentedSslSocketConnector(
            this.metricRegistry,
            getConfiguration().getPort(),
            sslContextFactory,
            Clock.defaultClock());
        connector.setName("external");

        return connector;
	}
}
