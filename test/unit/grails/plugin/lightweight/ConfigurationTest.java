package grails.plugin.lightweight;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.Level;
import java.util.TimeZone;
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
        assertEquals(Level.INFO, configuration.getThreshold());
    }

    @Test
    public void ifLoggingFileSetInConfigThenFileLoggingShouldBeSetToTrue() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig();
        attachLoggingConfig(config);
        Configuration configuration = new Configuration(config);
        assertTrue(configuration.isFileLoggingEnabled());
    }

    @Test
    public void loggingThresholdShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig();
        attachLoggingConfig(config);
        Configuration configuration = new Configuration(config);
        assertEquals(Level.WARN, configuration.getThreshold());
    }

    @Test
    public void loggingFileShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig();
        attachLoggingConfig(config);
        Configuration configuration = new Configuration(config);
        assertEquals("/app/logs/server.log", configuration.getCurrentLogFilename());
    }

    @Test
    public void timezoneShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig();
        attachLoggingConfig(config);
        Configuration configuration = new Configuration(config);
        assertEquals(TimeZone.getTimeZone("GMT+10"), configuration.getTimeZone());
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

    protected void attachLoggingConfig(Map<String, Map<String, Object>> config) {
        Map<String, Object> loggingConfig = new HashMap<String, Object>();
        Map<String, Object> loggingFileConfig = new HashMap<String, Object>();
        loggingFileConfig.put("threshold", Level.WARN.levelStr);
        loggingFileConfig.put("currentLogFilename", "/app/logs/server.log");
        loggingFileConfig.put("timeZone", "GMT+10");
        loggingConfig.put("file", loggingFileConfig);
        config.put("logging", loggingConfig);
    }
}
