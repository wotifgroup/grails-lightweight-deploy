package grails.plugin.lightweight

import ch.qos.logback.classic.Level
import org.junit.Test
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

public class ConfigurationTest {

    @Test
    public void shouldAssumeHttpIfNoSslBlock() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        config.http.remove("ssl")
        Configuration configuration = new Configuration(config)
        assertEquals(1234, configuration.getPort().intValue())
        assertFalse(configuration.isSsl())
    }

    @Test
    public void shouldAssumeHttpsIfSslBlock() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        Configuration configuration = new Configuration(config)
        assertEquals(1234, configuration.getPort().intValue())
        assertTrue(configuration.isSsl())
    }

    @Test
    public void shouldSetKeystoreAliasFromConfig() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        Configuration configuration = new Configuration(config)
        assertEquals("app.domain.com", configuration.getKeyStoreAlias())
    }

    @Test
    public void shouldSetKeystorePathFromConfig() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        Configuration configuration = new Configuration(config)
        assertEquals("/etc/pki/tls/jks/test.jks", configuration.getKeyStorePath())
    }

    @Test
    public void shouldSetKeystorePasswordFromConfig() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        Configuration configuration = new Configuration(config)
        assertEquals("password", configuration.getKeyStorePassword())
    }

    @Test
    public void serverLoggingThresholdShouldDefaultToInfo() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        attachServerLoggingConfig(config).file.remove("threshold")
        Configuration configuration = new Configuration(config)
        assertEquals(Level.INFO, configuration.serverLogConfiguration.threshold)
    }

    @Test
    public void ifServerLoggingFileSetInConfigThenFileLoggingShouldBeSetToTrue() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertTrue(configuration.isServerLoggingEnabled())
    }

    @Test
    public void serverloggingThresholdShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(Level.WARN, configuration.serverLogConfiguration.threshold)
    }

    @Test
    public void serverLoggingFileShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals("/app/logs/server.log", configuration.serverLogConfiguration.currentLogFilename)
    }

    @Test
    public void serverLoggingTimezoneShouldDefaultToLocal() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerLoggingConfig(config)
        config.logging.file.remove("timeZone")
        Configuration configuration = new Configuration(config)
        assertEquals(TimeZone.default, configuration.serverLogConfiguration.timeZone)
    }

    @Test
    public void serverLoggingTimezoneShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(TimeZone.getTimeZone("GMT+10"), configuration.serverLogConfiguration.timeZone)
    }
    
    @Test
    public void requestLoggingThresholdShouldDefaultToInfo() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        attachRequestLoggingConfig(config).file.remove("threshold")
        Configuration configuration = new Configuration(config)
        assertEquals(Level.INFO, configuration.requestLogConfiguration.threshold)
    }

    @Test
    public void ifRequestLoggingFileSetInConfigThenFileLoggingShouldBeSetToTrue() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachRequestLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertTrue(configuration.requestLoggingEnabled)
    }

    @Test
    public void requestloggingThresholdShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachRequestLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(Level.ALL, configuration.requestLogConfiguration.threshold)
    }

    @Test
    public void requestLoggingFileShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachRequestLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals("/app/logs/request.log", configuration.requestLogConfiguration.currentLogFilename)
    }

    @Test
    public void requestLoggingTimezoneShouldDefaultToLocal() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachRequestLoggingConfig(config).file.remove("timeZone")
        Configuration configuration = new Configuration(config)
        assertEquals(TimeZone.default, configuration.requestLogConfiguration.timeZone)
    }

    @Test
    public void requestLoggingTimezoneShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachRequestLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(TimeZone.getTimeZone("GMT+10"), configuration.requestLogConfiguration.timeZone)
    }

    @Test
    public void workDirShouldDefaultToTmpDir() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        Configuration configuration = new Configuration(config)
        assertEquals(new File(System.getProperty("java.io.tmpdir")), configuration.getWorkDir())
    }

    @Test
    public void workDirShouldBeConfigurable() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        config.workDir = "/apps/test"
        Configuration configuration = new Configuration(config)
        assertEquals(new File("/apps/test"), configuration.getWorkDir())
    }

    protected Map<String, Map<String, Object>> defaultConfig() {
        [http: [port: 1234,
                ssl: [keyStore: "/etc/pki/tls/jks/test.jks",
                      keyStorePassword: "password",
                      certAlias: "app.domain.com"]]]
    }

    protected def attachRequestLoggingConfig(def config) {
        config.http.requestLog = [file: [threshold: Level.ALL.levelStr,
                                         currentLogFilename: "/app/logs/request.log",
                                         timeZone: "GMT+10"]]
        config.http.requestLog
    }

    protected Map<String, Object> attachServerLoggingConfig(Map<String, Map<String, Object>> config) {
        config.logging = [file: [threshold: Level.WARN.levelStr,
                                 currentLogFilename: "/app/logs/server.log",
                                 timeZone: "GMT+10"]]
        config.logging
    }
}
