package grails.plugin.lightweightdeploy;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.codahale.metrics.servlets.AdminServlet;
import grails.plugin.lightweightdeploy.connector.ExternalConnectorFactory;
import grails.plugin.lightweightdeploy.connector.InternalConnectorFactory;
import grails.plugin.lightweightdeploy.jmx.JmxServer;
import grails.plugin.lightweightdeploy.logging.RequestLoggingFactory;
import grails.plugin.lightweightdeploy.logging.ServerLoggingFactory;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Based heavily on code from Burt Beckwith's standalone plugin and Codehale's Dropwizard.
 */
public class Launcher {

    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    /**
     * The directory under the exploded dir which stores the war
     */
    private static final String WAR_EXPLODED_SUBDIR = "war";

    private Configuration configuration;
    private MetricRegistry metricsRegistry;
    private HealthCheckRegistry healthCheckRegistry;

    /**
     * Start the server.
     */
    public static void main(String[] args) throws IOException {
        verifyArgs(args);
        final Launcher launcher = new Launcher(args[0]);
        launcher.start();
    }

    public Launcher(String configYmlPath) throws IOException {
        this(new Configuration(configYmlPath));
    }

    public Launcher(Configuration configuration) {
        this.configuration = configuration;
        logger.info("Using configuration: " + this.configuration);

        this.metricsRegistry = new MetricRegistry();
        this.healthCheckRegistry = new HealthCheckRegistry();

        configureLogging();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public HealthCheckRegistry getHealthCheckRegistry() {
        return healthCheckRegistry;
    }

    public MetricRegistry getMetricsRegistry() {
        return metricsRegistry;
    }

    protected void configureLogging() {
        if (this.configuration.isServerLoggingEnabled()) {
            ServerLoggingFactory loggingFactory = new ServerLoggingFactory(this.configuration);
            loggingFactory.configure();
        }
    }

    protected void start() throws IOException {
        War war = new War(this.configuration.getWorkDir());

        Server server = configureJetty(war);

        startJetty(server);
    }

    protected Server configureJetty(War war) throws IOException {
        System.setProperty("org.eclipse.jetty.xml.XmlParser.NotValidating", "true");

        Server server = new Server();

        HandlerCollection handlerCollection = new HandlerCollection();
        handlerCollection.addHandler(configureExternal(server, war));
        if (this.configuration.hasAdminPort()) {
            handlerCollection.addHandler(configureInternal(server));
        }
        if (this.configuration.isRequestLoggingEnabled()) {
            RequestLoggingFactory requestLoggingFactory = new RequestLoggingFactory(this.configuration);
            handlerCollection.addHandler(requestLoggingFactory.configure());
        }
        server.setHandler(handlerCollection);

        if (this.configuration.isJmxEnabled()) {
            JmxServer jmxServer = new JmxServer(this.configuration.getJmxConfiguration());
            jmxServer.start();
        }

        return server;
    }

    protected Handler configureExternal(Server server, War war) throws IOException {
        logger.info("Configuring external connector(s)");

        final ExternalConnectorFactory connectorFactory = new ExternalConnectorFactory(configuration, metricsRegistry);
        for (AbstractConnector externalConnector : connectorFactory.build()) {
            server.addConnector(externalConnector);
        }

        return createExternalContext(server, war.getDirectory().getPath() + "/" + WAR_EXPLODED_SUBDIR);
    }

    protected Handler configureInternal(Server server) {
        logger.info("Configuring admin connector");

        final InternalConnectorFactory connectorFactory = new InternalConnectorFactory(getConfiguration());
        for (AbstractConnector externalConnector : connectorFactory.build()) {
            server.addConnector(externalConnector);
        }

        return createInternalContext(server);
    }

    protected void startJetty(Server server) {
        try {
            server.start();
            logger.info("Startup complete. Server running on " + this.configuration.getPort());
        } catch (Exception e) {
            logger.error("Error starting jetty. Exiting JVM.", e);
            System.exit(1);
        }
    }

    protected Handler createInternalContext(Server server) {

        final ServletContextHandler handler = new InternalContext(getHealthCheckRegistry(), getMetricsRegistry());

        //bind this context to the external connector
        handler.setConnectorNames(getConnectorNames(server));

        configureInternalServlets(handler);

        return handler;
    }

    protected void configureInternalServlets(ServletContextHandler handler) {
        handler.addServlet(new ServletHolder(new AdminServlet()), "/*");
    }

    protected Handler createExternalContext(Server server, String webAppRoot) throws IOException {
        final WebAppContext handler = new ExternalContext(webAppRoot, getMetricsRegistry(), getHealthCheckRegistry());

        //bind this context to the external connector
        handler.setConnectorNames(getConnectorNames(server));

        configureExternalServlets(handler);

        return handler;
    }

    private static String[] getConnectorNames(Server server) {
        String[] connectorNames = new String[server.getConnectors().length];
        for (int i = 0; i < server.getConnectors().length; i++) {
            connectorNames[i] = server.getConnectors()[i].getName();
        }
        return connectorNames;
    }

    protected void configureHealthChecks() {
        HealthCheckRegistry healthCheckRegistry = getHealthCheckRegistry();
        healthCheckRegistry.register("threadDeadlock", new ThreadDeadlockHealthCheck());
    }

    /**
     * Override point for subclasses
     */
    protected void configureExternalServlets(WebAppContext context) {
        configureHealthChecks();
    }

    protected static void verifyArgs(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Requires 1 argument, which is the path to the config.yml file");
        }
    }

}
