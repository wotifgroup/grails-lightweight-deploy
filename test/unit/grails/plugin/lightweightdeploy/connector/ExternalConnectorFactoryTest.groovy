package grails.plugin.lightweightdeploy.connector

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.health.HealthCheckRegistry
import com.codahale.metrics.jetty8.InstrumentedSelectChannelConnector
import com.codahale.metrics.jetty8.InstrumentedSslSocketConnector
import grails.plugin.lightweightdeploy.Configuration
import org.eclipse.jetty.server.AbstractConnector
import org.eclipse.jetty.server.ssl.SslSocketConnector
import org.junit.Test

import static junit.framework.Assert.assertEquals

class ExternalConnectorFactoryTest {

    @Test
    void httpsPortShouldBeSetFromConfiguration() {
        assertEquals(1234,
                     getConnector(defaultConfig(true)).port)
    }

    @Test
    void httpPortShouldBeSetFromConfiguration() {
        assertEquals(1234,
                     getConnector(defaultConfig(true)).port)
    }

    @Test
    void httpsShouldBeInstrumented() {
        assertEquals(InstrumentedSslSocketConnector.class,
                     getConnector(defaultConfig(true)).getClass())
    }

    @Test
    void httpShouldBeInstrumented() {
        assertEquals(InstrumentedSelectChannelConnector.class,
                     getConnector(defaultConfig(false)).getClass())
    }

    @Test
    void httpsShouldHaveDefaultValuesSet() {
        testDefaultValues(getConnector(defaultConfig(true)))
    }

    @Test
    void httpShouldHaveDefaultValuesSet() {
        testDefaultValues(getConnector(defaultConfig(false)))
    }

    @Test
    void sslShouldHaveSslPropertiesSet() {
        SslSocketConnector connector = (SslSocketConnector) getConnector(defaultConfig(true))
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

    protected AbstractConnector getConnector(Configuration config) {
        new ExternalConnectorFactory(config,
                                     new HealthCheckRegistry(),
                                     new MetricRegistry()).build()
    }

    protected Configuration defaultConfig(boolean isSsl) {
        def map = [http: [port: 1234]]
        if (isSsl) {
            map.http.ssl = [keyStore: "/etc/pki/tls/jks/test.jks",
                            keyStorePassword: "password",
                            certAlias: "app.domain.com"]
        }
        new Configuration(map)
    }
}
