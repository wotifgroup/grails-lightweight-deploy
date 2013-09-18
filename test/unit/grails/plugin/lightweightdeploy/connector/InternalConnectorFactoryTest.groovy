package grails.plugin.lightweightdeploy.connector

import grails.plugin.lightweightdeploy.Configuration
import org.eclipse.jetty.server.AbstractConnector
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.junit.Test

import static junit.framework.Assert.assertEquals

class InternalConnectorFactoryTest {

    @Test
    void onlyEverOneConnector() {
        assertEquals(1, getConnectors(defaultConfig()).size())
    }

    @Test
    void adminPortShouldBeSetFromConfiguration() {
        assertEquals(1234, getConnector(defaultConfig()).port)
    }

    @Test
    void threadPoolShouldBeQueueThreaded() {
        assertEquals(QueuedThreadPool.class, getConnector(defaultConfig()).threadPool.class)
        assertEquals(8, getConnector(defaultConfig()).threadPool._maxThreads)
    }

    private AbstractConnector getConnector(Configuration config) {
        getConnectors(config).iterator().next()
    }

    private Set<? extends AbstractConnector> getConnectors(Configuration config) {
        new InternalConnectorFactory(config).build()
    }


    private Configuration defaultConfig() {
        new Configuration([http: [adminPort: 1234]])
    }

}
