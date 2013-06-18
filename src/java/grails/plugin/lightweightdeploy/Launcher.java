package grails.plugin.lightweightdeploy;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import grails.plugin.lightweightdeploy.logging.RequestLoggingFactory;
import grails.plugin.lightweightdeploy.logging.ServerLoggingFactory;
import java.io.File;
import java.io.IOException;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Based heavily on code from Burt Beckwith's standalone plugin and Codehale's Dropwizard.
 */
public class Launcher {
    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    private static final String EXTERNAL_CONNECTOR_NAME = "external";
    private static final String INTERNAL_CONNECTOR_NAME = "internal";
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
		deleteExplodedOnShutdown(war);

		System.setProperty("org.eclipse.jetty.xml.XmlParser.NotValidating", "true");

		Server server = configureJetty(war);

		startJetty(server);
	}

	protected Server configureJetty(War war) throws IOException {
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

        return server;
	}

    protected Handler configureExternal(Server server, War war) throws IOException {
        ExternalConnectorFactory externalConnectorFactory = new ExternalConnectorFactory(this.configuration,
                                                                                         this.healthCheckRegistry,
                                                                                         this.metricsRegistry);
        AbstractConnector externalConnector = externalConnectorFactory.build();
        server.addConnector(externalConnector);

        return createApplicationContext(war.getDirectory().getPath() + "/" + WAR_EXPLODED_SUBDIR);
    }

    protected Handler configureInternal(Server server) {
        logger.info("Configuring admin connector");

        addConnector(server, configureInternalConnector());

        return configureAdminContext();
    }

    protected AbstractConnector configureInternalConnector() {
        final SocketConnector connector = new SocketConnector();
        connector.setPort(this.configuration.getAdminPort());
        connector.setName("internal");
        connector.setThreadPool(new QueuedThreadPool(8));
        connector.setName(INTERNAL_CONNECTOR_NAME);
        return connector;
    }

	protected void startJetty(Server server) {
		try {
			server.start();
			logger.info("Startup complete. Server running on " + this.configuration.getPort());
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error loading Jetty: " + e.getMessage());
			System.exit(1);
		}
	}

    protected Handler configureAdminContext() {
        final ServletContextHandler handler = new ServletContextHandler();
        configureInternalServlets(handler);
        handler.setConnectorNames(new String[]{INTERNAL_CONNECTOR_NAME});
        handler.getServletContext().setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY,healthCheckRegistry);
        handler.getServletContext().setAttribute(MetricsServlet.METRICS_REGISTRY, metricsRegistry);
        return handler;
    }

    protected void configureInternalServlets(ServletContextHandler handler) {
        handler.addServlet(new ServletHolder(new AdminServlet()), "/*");
    }

	protected Handler createApplicationContext(String webAppRoot) throws IOException {
		WebAppContext context = new ExternalContext(webAppRoot, getMetricsRegistry(), getHealthCheckRegistry());

        //bind this context to the external connector
        context.setConnectorNames(new String[] {EXTERNAL_CONNECTOR_NAME});

        configureExternalServlets(context);

		return context;
	}

    /**
     * Override point for subclasses
     */
    protected void configureExternalServlets(WebAppContext context) {
    }

    protected void addConnector(Server server, AbstractConnector connector) {
        connector.setMaxIdleTime(200 * 1000);
        connector.setLowResourcesMaxIdleTime(0);
        connector.setRequestBufferSize(16 * 1024);
        connector.setRequestHeaderSize(6 * 1024);
        connector.setResponseBufferSize(32 * 1024);
        connector.setResponseHeaderSize(6 * 1024);

        server.addConnector(connector);
    }

	protected void deleteExplodedOnShutdown(War war) {
        final File warDirectory = war.getDirectory();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Utils.deleteDir(warDirectory);
			}
		});
	}

    protected static void verifyArgs(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Requires 1 argument, which is the path to the config.yml file");
        }
    }
}
