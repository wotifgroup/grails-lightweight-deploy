package grails.plugin.lightweightdeploy;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.codahale.metrics.jetty8.InstrumentedHandler;
import com.codahale.metrics.jetty8.InstrumentedQueuedThreadPool;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.servlets.AdminServlet;
import com.google.common.base.Strings;
import grails.plugin.lightweightdeploy.connector.ExternalConnectorFactory;
import grails.plugin.lightweightdeploy.connector.GzipConfiguration;
import grails.plugin.lightweightdeploy.connector.HttpConfiguration;
import grails.plugin.lightweightdeploy.connector.InternalConnectorFactory;
import grails.plugin.lightweightdeploy.connector.SessionsConfiguration;
import grails.plugin.lightweightdeploy.jetty.BiDiGzipFilter;
import grails.plugin.lightweightdeploy.jmx.JmxServer;
import grails.plugin.lightweightdeploy.logging.RequestLoggingFactory;
import grails.plugin.lightweightdeploy.logging.ServerLoggingFactory;
import grails.plugin.lightweightdeploy.logging.StartupShutdownLogger;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.EnumSet;

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
    private StartupShutdownLogger startupShutdownLogger;

    /**
     * Start the server.
     */
    public static void main(String[] args) {
        try {
            verifyArgs(args);
            new Launcher(args[0]).start();
        } catch (Throwable e) {
            System.err.println("Failure launching application");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public Launcher(String configYmlPath) throws IOException {
        this(new Configuration(configYmlPath));
    }

    public Launcher(Configuration configuration) {
        this.configuration = configuration;
        logger.info("Using configuration: " + this.configuration);

        this.metricsRegistry = configureMetricRegistry();
        this.healthCheckRegistry = new HealthCheckRegistry();
        this.startupShutdownLogger = new StartupShutdownLogger(logger, configuration.getAppName());

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

    protected MetricRegistry configureMetricRegistry() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        metricRegistry.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        metricRegistry.register("jvm.gc", new GarbageCollectorMetricSet());
        metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet());
        metricRegistry.register("jvm.threads", new ThreadStatesGaugeSet());

        if (getConfiguration().isJmxEnabled()) {
            JmxReporter.forRegistry(metricRegistry).build().start();
        }

        return metricRegistry;
    }

    protected void configureLogging() {
        if (this.configuration.isServerLoggingEnabled()) {
            ServerLoggingFactory loggingFactory = new ServerLoggingFactory(this.configuration);
            loggingFactory.configure();
        }
    }

    protected void start() throws Exception {
        War war = new War(this.configuration.getWorkDir());

        Server server = configureJetty(war);

        startJetty(server);
    }

    protected Server configureJetty(War war) throws IOException {
        System.setProperty("org.eclipse.jetty.xml.XmlParser.NotValidating", "true");

        final Server server = createServer();

        HandlerCollection handlerCollection = new HandlerCollection();
        handlerCollection.addHandler(configureExternal(server, war));
        if (this.configuration.getHttpConfiguration().hasAdminPort()) {
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

    protected Server createServer() {
        final Server server = new Server();

        // Add our the instrumented thread pool
        server.setThreadPool(createThreadPool());

        // Don't send Date and Server headers
        server.setSendDateHeader(false);
        server.setSendServerVersion(false);

        // Allow a grace period during shutdown
        server.setStopAtShutdown(true);
        server.setGracefulShutdown(2000);

        // Add the shutdown message
        server.addLifeCycleListener(startupShutdownLogger);

        return server;
    }

    protected Handler configureExternal(Server server, War war) throws IOException {
        logger.info("Configuring external connector(s)");

        final HttpConfiguration httpConfiguration = configuration.getHttpConfiguration();
        final ExternalConnectorFactory connectorFactory = new ExternalConnectorFactory(httpConfiguration, metricsRegistry);
        for (AbstractConnector externalConnector : connectorFactory.build()) {
            server.addConnector(externalConnector);
        }

        return createExternalContext(server, war.getDirectory().getPath() + "/" + WAR_EXPLODED_SUBDIR, httpConfiguration.getContextPath());
    }

    protected Handler configureInternal(Server server) {
        logger.info("Configuring admin connector");

        final InternalConnectorFactory connectorFactory = new InternalConnectorFactory(getConfiguration().getHttpConfiguration());
        for (AbstractConnector externalConnector : connectorFactory.build()) {
            server.addConnector(externalConnector);
        }

        return createInternalContext(server);
    }

    protected void startJetty(Server server) throws Exception {
        try {
            server.start();
            logger.info("Startup complete. Server running on " + this.configuration.getHttpConfiguration().getPort());
        } catch (Exception e) {
            logger.error("Error starting jetty. Exiting JVM.", e);
            server.stop();
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

    protected Handler createExternalContext(Server server, String webAppRoot, String contextPath) throws IOException {
        final WebAppContext handler = new ExternalContext(webAppRoot, getMetricsRegistry(), getHealthCheckRegistry(), contextPath);

        // Enable sessions support if required
        final SessionsConfiguration sessionsConfiguration = configuration.getHttpConfiguration().getSessionsConfiguration();
        if (sessionsConfiguration.isEnabled()) {
            final HashSessionIdManager idManager = new HashSessionIdManager();
            if (!Strings.isNullOrEmpty(sessionsConfiguration.getWorkerName())) {
                idManager.setWorkerName(sessionsConfiguration.getWorkerName());
            }

            // Assumes ExternalContext extends WebAppContext which configures sessions by default
            handler.getSessionHandler().getSessionManager().setSessionIdManager(idManager);
        } else {
            handler.setSessionHandler(null);
        }

        //bind this context to the external connector
        handler.setConnectorNames(getConnectorNames(server));

        configureExternalServlets(handler);

        // Optionally support GZip requests/responses
        configureGzip(handler);

        // Instrument our handler
        final Handler instrumented = new InstrumentedHandler(metricsRegistry, handler);

        return instrumented;
    }

    private void configureGzip(ServletContextHandler handler) {
        GzipConfiguration gzipConfiguration = configuration.getHttpConfiguration().getGzipConfiguration();

        if (gzipConfiguration.isEnabled()) {
            Filter filter = buildGzipFilter(gzipConfiguration);
            FilterHolder holder = new FilterHolder(filter);
            handler.addFilter(holder, "/*", EnumSet.allOf(DispatcherType.class));
        }
    }

    private Filter buildGzipFilter(GzipConfiguration gzipConfiguration) {
        BiDiGzipFilter filter = new BiDiGzipFilter();
        filter.setMinGzipSize(gzipConfiguration.getMinimumEntitySize());
        filter.setBufferSize(gzipConfiguration.getBufferSize());
        filter.setDeflateCompressionLevel(gzipConfiguration.getDeflateCompressionLevel());
        if (gzipConfiguration.getExcludedUserAgents() != null) {
            filter.setExcludedAgents(gzipConfiguration.getExcludedUserAgents());
        }
        if (gzipConfiguration.getCompressedMimeTypes() != null) {
            filter.setMimeTypes(gzipConfiguration.getCompressedMimeTypes());
        }
        if (gzipConfiguration.getIncludedMethods() != null) {
            filter.setMethods(gzipConfiguration.getIncludedMethods());
        }
        if (gzipConfiguration.getExcludedUserAgentPatterns() != null) {
            filter.setExcludedAgentPatterns(gzipConfiguration.getExcludedUserAgentPatterns());
        }
        filter.setVary(gzipConfiguration.getVary());
        filter.setDeflateNoWrap(gzipConfiguration.isGzipCompatibleDeflation());
        return filter;
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

    protected ThreadPool createThreadPool() {
        final InstrumentedQueuedThreadPool pool = new InstrumentedQueuedThreadPool(metricsRegistry);
        pool.setMinThreads(this.configuration.getHttpConfiguration().getMinThreads());
        pool.setMaxThreads(this.configuration.getHttpConfiguration().getMaxThreads());
        return pool;
    }

}
