package grails.plugin.lightweight;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigurationTest {

    @Test
    public void shouldAssumeHttpIfNoSslBlock() throws IOException {
        Map<String, Object> httpConfig = new HashMap<String, Object>();
        httpConfig.put("port", 1234);
        Configuration configuration = new Configuration(httpConfig);
        assertEquals(1234, configuration.getPort().intValue());
        assertFalse(configuration.isSsl());
    }

    @Test
    public void shouldAssumeHttpsIfSslBlock() throws IOException {
        Map<String, Object> httpConfig = buildHttpsConfig();
        Configuration configuration = new Configuration(httpConfig);
        assertEquals(1234, configuration.getPort().intValue());
        assertTrue(configuration.isSsl());
    }

    @Test
    public void shouldSetKeystoreAliasFromConfig() throws IOException {
        Map<String, Object> httpConfig = buildHttpsConfig();
        Configuration configuration = new Configuration(httpConfig);
        assertEquals("app.domain.com", configuration.getKeyStoreAlias());
    }

    @Test
    public void shouldSetKeystorePathFromConfig() throws IOException {
        Map<String, Object> httpConfig = buildHttpsConfig();
        Configuration configuration = new Configuration(httpConfig);
        assertEquals("/etc/pki/tls/jks/test.jks", configuration.getKeyStorePath());
    }

    @Test
    public void shouldSetKeystorePasswordFromConfig() throws IOException {
        Map<String, Object> httpConfig = buildHttpsConfig();
        Configuration configuration = new Configuration(httpConfig);
        assertEquals("password", configuration.getKeyStorePassword());
    }

    protected Map<String, Object> buildHttpsConfig() {
        Map<String, Object> httpsConfig = new HashMap<String, Object>();
        httpsConfig.put("port", 1234);
        Map<String, Object> sslConfig = new HashMap<String, Object>();
        sslConfig.put("keyStore", "/etc/pki/tls/jks/test.jks");
        sslConfig.put("keyStorePassword", "password");
        sslConfig.put("certAlias", "app.domain.com");
        httpsConfig.put("ssl", sslConfig);
        return httpsConfig;
    }

}
