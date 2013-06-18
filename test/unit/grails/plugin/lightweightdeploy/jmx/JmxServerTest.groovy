package grails.plugin.lightweightdeploy.jmx

import org.junit.Test

import static junit.framework.Assert.assertEquals

class JmxServerTest {

    @Test
    void startShouldStartTheServer() {
        def jmxConfiguration = new JmxConfiguration(10000, 10001)
        def server = new JmxServer(jmxConfiguration)
        server.start()
        //this is the only way to check for actual run state
        assertEquals(1, server.connectorServer._connectorServer.state)
        server.stop()

    }

    @Test
    void stopShouldStopTheServer() {
        def jmxConfiguration = new JmxConfiguration(10000, 10001)
        def server = new JmxServer(jmxConfiguration)
        server.start()
        server.stop()
        assertEquals(2, server.connectorServer._connectorServer.state)
    }
}
