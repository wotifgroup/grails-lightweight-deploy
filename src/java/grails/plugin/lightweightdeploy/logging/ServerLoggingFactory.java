package grails.plugin.lightweightdeploy.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.jmx.JMXConfigurator;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import grails.plugin.lightweightdeploy.Configuration;
import java.lang.management.ManagementFactory;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import ch.qos.logback.classic.Level;


/**
 * Borrowed heavily from com.yammer.dropwizard.logging.LoggingFactory.
 */
public class ServerLoggingFactory {

    private final Configuration config;

    public ServerLoggingFactory(Configuration config) {
        this.config = config;
    }

    public void configure() {
        //detach current appenders;
        getCleanRoot();

        hijackJDKLogging();

        final Logger root = configureLevels();

        root.addAppender(AsyncAppender.wrap(LogbackFactory.buildFileAppender(this.config.getServerLogConfiguration(),
                                                                             root.getLoggerContext())));

        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            final ObjectName objectName = new ObjectName("grails.plugin.lightweightdeploy:type=Logging");
            if (!server.isRegistered(objectName)) {
                server.registerMBean(new JMXConfigurator(root.getLoggerContext(),
                                                         server,
                                                         objectName),
                                     objectName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void hijackJDKLogging() {
        //doesn't work in current version of jul-to-slf4j
//        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private Logger configureLevels() {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.getLoggerContext().reset();

        final LevelChangePropagator propagator = new LevelChangePropagator();
        propagator.setContext(root.getLoggerContext());
        propagator.setResetJUL(true);

        root.getLoggerContext().addListener(propagator);

        root.setLevel(config.getServerLogConfiguration().getRootLevel());

        for (Map.Entry<String, Level> entry : config.getServerLogConfiguration().getLoggers().entrySet()) {
            ((Logger) LoggerFactory.getLogger(entry.getKey())).setLevel(entry.getValue());
        }

        return root;
    }

    private static Logger getCleanRoot() {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.detachAndStopAllAppenders();
        return root;
    }
}
