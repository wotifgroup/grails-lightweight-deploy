package grails.plugin.lightweightdeploy.jmx;

import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;
import org.eclipse.jetty.jmx.ConnectorServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A jmx server, exposing the mbeans for this application over RMI.
 */
public class JmxServer {
    private static final Logger logger = LoggerFactory.getLogger(JmxServer.class);

    private static final String BEAN_NAME = "org.eclipse.jetty.jmx:name=rmiconnectorserver";

    private ConnectorServer connectorServer;

    public JmxServer(JmxConfiguration jmxConfiguration) {
        String uri = String.format("service:jmx:rmi://localhost:%d/jndi/rmi://localhost:%d/jmxrmi",
                                   jmxConfiguration.getServerPort(),
                                   jmxConfiguration.getRegistryPort());

        try {
            this.connectorServer = new ConnectorServer(new JMXServiceURL(uri), BEAN_NAME);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid JMX service URL", e);
        } catch (Exception e) {
            throw new RuntimeException("Unable to build JMX server.", e);
        }
    }

    public boolean isRunning() {
        return this.connectorServer.isRunning();
    }

    public void start() {
        logger.info("Starting the JMX server...");

        try {
            connectorServer.doStart();
        } catch (Exception e) {
            throw new RuntimeException("Error starting jmx server", e);
        }

        logger.info("Started the JMX server");
    }

    public void stop() {
        logger.info("Stopping the JMX server...");

        try {
            connectorServer.doStop();
            //have to manually unregister the bean, seemingly because of a bug in doStop()
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(BEAN_NAME));
        } catch (Exception e) {
            throw new RuntimeException("Error starting jmx server", e);
        }

        logger.info("Stopping the JMX server");
    }
}
