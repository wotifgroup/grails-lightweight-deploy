package grails.plugin.lightweightdeploy.connector;

import com.codahale.metrics.Clock;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jetty8.InstrumentedSelectChannelConnector;
import com.codahale.metrics.jetty8.InstrumentedSslSocketConnector;
import grails.plugin.lightweightdeploy.Configuration;
import grails.plugin.lightweightdeploy.Launcher;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class ExternalConnectorFactory extends AbstractConnectorFactory {

    public static final String EXTERNAL_HTTP_CONNECTOR_NAME = "external";
    public static final String EXTERNAL_HTTPS_CONNECTOR_NAME = "external-ssl";

    private static final Logger logger = LoggerFactory.getLogger(ExternalConnectorFactory.class);

    private MetricRegistry metricRegistry;

    public ExternalConnectorFactory(Configuration configuration, MetricRegistry metricRegistry) {
        super(configuration);
        this.metricRegistry = metricRegistry;
    }

    @Override
    public Set<? extends AbstractConnector> build() {
        final Set<AbstractConnector> connectors = new HashSet<AbstractConnector>();

        if (getConfiguration().isMixedMode()) {
            connectors.add(configureExternalHttpsConnector());
            connectors.add(configureExternalHttpConnector());
        } else if (getConfiguration().isSsl()) {
            connectors.add(configureExternalHttpsConnector());
        } else {
            connectors.add(configureExternalHttpConnector());
        }

        // apply the default values to each connector
        for (AbstractConnector connector : connectors) {
            defaultValues(connector);
        }

        return connectors;
    }

    private AbstractConnector configureExternalHttpConnector() {
        logger.info("Creating http connector");

        final InstrumentedSelectChannelConnector connector = new InstrumentedSelectChannelConnector(
                this.metricRegistry,
                getConfiguration().getPort(),
                Clock.defaultClock());
        connector.setName(EXTERNAL_HTTP_CONNECTOR_NAME);
        connector.setUseDirectBuffers(true);

        return connector;
    }

    private AbstractConnector configureExternalHttpsConnector() {
        logger.info("Creating https connector");

        final SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setCertAlias(getConfiguration().getSslConfiguration().getKeyStoreAlias());
        sslContextFactory.setKeyStorePath(getConfiguration().getSslConfiguration().getKeyStorePath());
        sslContextFactory.setKeyStorePassword(getConfiguration().getSslConfiguration().getKeyStorePassword());

        final Integer port = getConfiguration().getSslConfiguration().getPort() != null ?
                getConfiguration().getSslConfiguration().getPort() :
                getConfiguration().getPort();

        final InstrumentedSslSocketConnector connector = new InstrumentedSslSocketConnector(
                this.metricRegistry,
                port,
                sslContextFactory,
                Clock.defaultClock());
        connector.setName(EXTERNAL_HTTPS_CONNECTOR_NAME);

        return connector;
    }
}
