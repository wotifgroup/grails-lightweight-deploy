package grails.plugin.lightweight;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.Level;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigurationTest {

    @Test
    public void shouldAssumeHttpIfNoSslBlock() throws IOException {
        Map<String, ? extends Object> config = defaultConfig();
        ((Map) config.get("http")).remove("ssl");
        Configuration configuration = new Configuration(config);
        assertEquals(1234, configuration.getPort().intValue());
        assertFalse(configuration.isSsl());
    }

    @Test
    public void shouldAssumeHttpsIfSslBlock() throws IOException {
        Map<String, ? extends Object> config = defaultConfig();
        Configuration configuration = new Configuration(config);
        assertEquals(1234, configuration.getPort().intValue());
        assertTrue(configuration.isSsl());
    }

    @Test
    public void shouldSetKeystoreAliasFromConfig() throws IOException {
        Map<String, ? extends Object> config = defaultConfig();
        Configuration configuration = new Configuration(config);
        assertEquals("app.domain.com", configuration.getKeyStoreAlias());
    }

    @Test
    public void shouldSetKeystorePathFromConfig() throws IOException {
        Map<String, ? extends Object> config = defaultConfig();
        Configuration configuration = new Configuration(config);
        assertEquals("/etc/pki/tls/jks/test.jks", configuration.getKeyStorePath());
    }

    @Test
    public void shouldSetKeystorePasswordFromConfig() throws IOException {
        Map<String, ? extends Object> config = defaultConfig();
        Configuration configuration = new Configuration(config);
        assertEquals("password", configuration.getKeyStorePassword());
    }

    @Test
    public void loggingThresholdShouldDefaultToInfo() throws IOException {
        Map<String, ? extends Object> config = defaultConfig();
        Configuration configuration = new Configuration(config);
        assertEquals(Level.INFO, configuration.getLoggingThreshold());
    }

    @Test
    public void loggingThresholdShouldBeSetToValueInFile() throws IOException {
        Map<String, Object> loggingConfig = new HashMap<String, Object>();
        Map<String, Object> loggingFileConfig = new HashMap<String, Object>();
        loggingFileConfig.put("threshold", Level.WARN.levelStr);
        loggingConfig.put("file", loggingFileConfig);
        Map<String, Map<String, Object>> config = defaultConfig();
        config.put("logging", loggingConfig);
        Configuration configuration = new Configuration(config);
        assertEquals(Level.WARN, configuration.getLoggingThreshold());
    }

    protected Map<String, Map<String, Object>> defaultConfig() {
        Map<String, Object> httpsConfig = new HashMap<String, Object>();
        httpsConfig.put("port", 1234);
        Map<String, Object> sslConfig = new HashMap<String, Object>();
        sslConfig.put("keyStore", "/etc/pki/tls/jks/test.jks");
        sslConfig.put("keyStorePassword", "password");
        sslConfig.put("certAlias", "app.domain.com");
        httpsConfig.put("ssl", sslConfig);
        Map<String, Map<String, Object>> config = new HashMap<String, Map<String, Object>>();
        config.put("http", httpsConfig);
        return config;
    }

}
