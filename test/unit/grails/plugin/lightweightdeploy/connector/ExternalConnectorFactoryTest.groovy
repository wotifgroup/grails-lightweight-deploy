package grails.plugin.lightweightdeploy.connector

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.jetty8.InstrumentedSelectChannelConnector
import com.codahale.metrics.jetty8.InstrumentedSslSocketConnector
import grails.plugin.lightweightdeploy.Configuration
import org.eclipse.jetty.server.AbstractConnector
import org.eclipse.jetty.server.ssl.SslSocketConnector
import org.junit.Test

import static junit.framework.Assert.assertEquals
import static junit.framework.Assert.assertTrue

class ExternalConnectorFactoryTest {

    @Test
    void oneConnectorShouldBeAddedForSingleMode() {
        assertEquals(1, getConnectors(defaultConfig(true, false)).size())
        assertEquals(1, getConnectors(defaultConfig(false, false)).size())
    }

    @Test
    void twoConnectorsShouldBeAddedForMixedMode() {
        def connectors = getConnectors(defaultConfig(true, true)).asList()
        assertEquals(2, connectors.size())
        def first = connectors.get(0)
        def second = connectors.get(1)
        // verify there is a connector on each port
        assertTrue((first.port == 1235 && second.port == 1234) || (first.port == 1234 && second.port == 1235))
    }

    @Test
    void httpsPortShouldBeSetFromConfiguration() {
        assertEquals(1234,
                getSingleConnector(defaultConfig(true)).port)
    }

    @Test
    void httpPortShouldBeSetFromConfiguration() {
        assertEquals(1234,
                getSingleConnector(defaultConfig(true)).port)
    }

    @Test
    void httpsShouldBeInstrumented() {
        assertEquals(InstrumentedSslSocketConnector.class,
                getSingleConnector(defaultConfig(true)).getClass())
    }

    @Test
    void httpShouldBeInstrumented() {
        assertEquals(InstrumentedSelectChannelConnector.class,
                getSingleConnector(defaultConfig(false)).getClass())
    }

    @Test
    void httpsShouldHaveDefaultValuesSet() {
        testDefaultValues(getSingleConnector(defaultConfig(true)))
    }

    @Test
    void httpShouldHaveDefaultValuesSet() {
        testDefaultValues(getSingleConnector(defaultConfig(false)))
    }

    @Test
    void sslShouldHaveSslPropertiesSet() {
        SslSocketConnector connector = (SslSocketConnector) getSingleConnector(defaultConfig(true))
        assertEquals("app.domain.com", connector.getSslContextFactory().getCertAlias())
        assertEquals("/etc/pki/tls/jks/test.jks", connector.getSslContextFactory().getKeyStorePath())
        assertEquals("password", connector.getSslContextFactory()._keyStorePassword._pw)
    }

    void testDefaultValues(AbstractConnector connector) {
        assertEquals(200 * 1000, connector.getMaxIdleTime())
        assertEquals(0, connector.getLowResourcesMaxIdleTime())
        assertEquals(16 * 1024, connector.getRequestBufferSize())
        assertEquals(6 * 1024, connector.getRequestHeaderSize())
        assertEquals(32 * 1024, connector.getResponseBufferSize())
        assertEquals(6 * 1024, connector.getResponseHeaderSize())
    }

    private AbstractConnector getSingleConnector(Configuration config) {
        getConnectors(config).iterator().next()
    }

    private Set<? extends AbstractConnector> getConnectors(Configuration config) {
        new ExternalConnectorFactory(config, new MetricRegistry()).build()
    }

    protected Configuration defaultConfig(boolean isSsl, boolean isMixedMode = false) {
        def map = [http: [port: 1234]]
        if (isSsl) {
            map.http.ssl = [keyStore: "/etc/pki/tls/jks/test.jks",
                    keyStorePassword: "password",
                    certAlias: "app.domain.com"]
            if (isMixedMode) {
                map.http.ssl.put("port", 1235)
            }
        }
        new Configuration(map)
    }
}
