package grails.plugin.lightweightdeploy.connector

import org.junit.Test

import static org.junit.Assert.*

public class HttpConfigurationTest {

    @Test
    void shouldAssumeHttpIfNoSslBlock() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        config.remove("ssl")
        HttpConfiguration configuration = new HttpConfiguration(config)
        assertEquals(1234, configuration.getPort().intValue())
        assertFalse(configuration.isSsl())
    }

    @Test
    void shouldAssumeHttpsIfSslBlock() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        HttpConfiguration configuration = new HttpConfiguration(config)
        assertEquals(1234, configuration.getPort().intValue())
        assertTrue(configuration.isSsl())
    }

    @Test
    void shouldSetKeystoreAliasFromConfig() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        HttpConfiguration configuration = new HttpConfiguration(config)
        assertEquals("app.domain.com", configuration.sslConfiguration.keyStoreAlias)
    }

    @Test
    void shouldSetKeystorePathFromConfig() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        HttpConfiguration configuration = new HttpConfiguration(config)
        assertEquals("/etc/pki/tls/jks/test.jks", configuration.sslConfiguration.keyStorePath)
    }

    @Test
    void shouldSetKeystorePasswordFromConfig() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        HttpConfiguration configuration = new HttpConfiguration(config)
        assertEquals("password", configuration.sslConfiguration.keyStorePassword)
    }

    @Test
    void minThreadsShouldDefault() {
        HttpConfiguration configuration = new HttpConfiguration(defaultConfig())
        assertEquals(8, configuration.minThreads)
    }

    @Test
    void maxThreadsShouldDefault() {
        HttpConfiguration configuration = new HttpConfiguration(defaultConfig())
        assertEquals(128, configuration.maxThreads)
    }

    @Test
    void minThreadsShouldBeSetIfPresent() {
        Map<String, ? extends Object> config = defaultConfig()
        config.minThreads = 1000
        HttpConfiguration configuration = new HttpConfiguration(config)
        assertEquals(1000, configuration.minThreads)
    }

    @Test
    void maxThreadsShouldBeSetIfPresent() {
        Map<String, ? extends Object> config = defaultConfig()
        config.maxThreads = 10000
        HttpConfiguration configuration = new HttpConfiguration(config)
        assertEquals(10000, configuration.maxThreads)
    }

    protected Map<String, Map<String, Object>> defaultConfig() {
        [port: 1234,
                ssl: [keyStore: "/etc/pki/tls/jks/test.jks",
                        keyStorePassword: "password",
                        certAlias: "app.domain.com"]]
    }

}
