package grails.plugin.lightweightdeploy.connector

import grails.plugin.lightweightdeploy.Configuration
import org.eclipse.jetty.server.AbstractConnector
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.junit.Test

import static junit.framework.Assert.assertEquals

class InternalConnectorFactoryTest {

    @Test
    void adminPortShouldBeSetFromConfiguration() {
        assertEquals(1234, getConnector(defaultConfig()).port)
    }

    @Test
    void threadPoolShouldBeQueueThreaded() {
        assertEquals(QueuedThreadPool.class, getConnector(defaultConfig()).threadPool.class)
        assertEquals(8, getConnector(defaultConfig()).threadPool._maxThreads)
    }

    protected AbstractConnector getConnector(Configuration config) {
        new InternalConnectorFactory(config).build()
    }

    protected Configuration defaultConfig() {
        new Configuration([http: [adminPort: 1234]])
    }
}
