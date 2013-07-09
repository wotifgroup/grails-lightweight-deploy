package grails.plugin.lightweightdeploy

import ch.qos.logback.classic.Level
import org.junit.Test
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

public class ConfigurationTest {

    @Test
    void shouldAssumeHttpIfNoSslBlock() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        config.http.remove("ssl")
        Configuration configuration = new Configuration(config)
        assertEquals(1234, configuration.getPort().intValue())
        assertFalse(configuration.isSsl())
    }

    @Test
    void shouldAssumeHttpsIfSslBlock() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        Configuration configuration = new Configuration(config)
        assertEquals(1234, configuration.getPort().intValue())
        assertTrue(configuration.isSsl())
    }

    @Test
    void shouldSetKeystoreAliasFromConfig() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        Configuration configuration = new Configuration(config)
        assertEquals("app.domain.com", configuration.getKeyStoreAlias())
    }

    @Test
    void shouldSetKeystorePathFromConfig() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        Configuration configuration = new Configuration(config)
        assertEquals("/etc/pki/tls/jks/test.jks", configuration.getKeyStorePath())
    }

    @Test
    void shouldSetKeystorePasswordFromConfig() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        Configuration configuration = new Configuration(config)
        assertEquals("password", configuration.getKeyStorePassword())
    }

    @Test
    void serverLoggingThresholdShouldDefaultToAll() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        attachServerLoggingConfig(config).file.remove("threshold")
        Configuration configuration = new Configuration(config)
        assertEquals(Level.ALL, configuration.serverLogConfiguration.threshold)
    }

    @Test
    void serverLoggingRootLevelShouldDefaultToInfo() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        attachServerLoggingConfig(config).file.remove("rootLevel")
        Configuration configuration = new Configuration(config)
        assertEquals(Level.INFO, configuration.serverLogConfiguration.rootLevel)
    }

    @Test
    void serverLoggingLoggersIsNotRequired() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        attachServerLoggingConfig(config).file.remove("loggers")
        Configuration configuration = new Configuration(config)
        assertEquals(0, configuration.serverLogConfiguration.loggers.size())
    }

    @Test
    void serverLoggingLoggersCanBeEmpty() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        attachServerLoggingConfig(config).file.loggers.clear()
        Configuration configuration = new Configuration(config)
        assertEquals(0, configuration.serverLogConfiguration.loggers.size())
    }

    @Test
    void ifServerLoggingFileSetInConfigThenFileLoggingShouldBeSetToTrue() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertTrue(configuration.isServerLoggingEnabled())
    }

    @Test
    void serverLoggingThresholdShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(Level.DEBUG, configuration.serverLogConfiguration.threshold)
    }

    @Test
    void serverLoggingRootLevelShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(Level.WARN, configuration.serverLogConfiguration.rootLevel)
    }

    @Test
    void serverLoggingLoggersShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(Level.INFO, configuration.serverLogConfiguration.loggers."foo")
        assertEquals(Level.ERROR, configuration.serverLogConfiguration.loggers."bar.baz")
    }

    @Test
    void serverLoggingFileShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals("/app/logs/server.log", configuration.serverLogConfiguration.currentLogFilename)
    }

    @Test
    void serverLoggingTimezoneShouldDefaultToLocal() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerLoggingConfig(config)
        config.logging.file.remove("timeZone")
        Configuration configuration = new Configuration(config)
        assertEquals(TimeZone.default, configuration.serverLogConfiguration.timeZone)
    }

    @Test
    void serverLoggingTimezoneShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(TimeZone.getTimeZone("GMT+10"), configuration.serverLogConfiguration.timeZone)
    }
    
    @Test
    void requestLoggingThresholdShouldDefaultToAll() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        attachRequestLoggingConfig(config).file.remove("threshold")
        Configuration configuration = new Configuration(config)
        assertEquals(Level.ALL, configuration.requestLogConfiguration.threshold)
    }

    @Test
    void ifRequestLoggingFileSetInConfigThenFileLoggingShouldBeSetToTrue() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachRequestLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertTrue(configuration.requestLoggingEnabled)
    }

    @Test
    void requestloggingThresholdShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachRequestLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(Level.ALL, configuration.requestLogConfiguration.threshold)
    }

    @Test
    void requestLoggingFileShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachRequestLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals("/app/logs/request.log", configuration.requestLogConfiguration.currentLogFilename)
    }

    @Test
    void requestLoggingTimezoneShouldDefaultToLocal() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachRequestLoggingConfig(config).file.remove("timeZone")
        Configuration configuration = new Configuration(config)
        assertEquals(TimeZone.default, configuration.requestLogConfiguration.timeZone)
    }

    @Test
    void requestLoggingTimezoneShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachRequestLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(TimeZone.getTimeZone("GMT+10"), configuration.requestLogConfiguration.timeZone)
    }

    @Test
    void workDirShouldDefaultToTmpDir() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        Configuration configuration = new Configuration(config)
        assertEquals(new File(System.getProperty("java.io.tmpdir")), configuration.getWorkDir())
    }

    @Test
    void workDirShouldBeConfigurable() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        config.workDir = "/apps/test"
        Configuration configuration = new Configuration(config)
        assertEquals(new File("/apps/test"), configuration.getWorkDir())
    }
    
    @Test
    void jmxShouldBeDisabledIfConfigOmitted() {
        Map<String, ? extends Object> config = defaultConfig()
        assertFalse(new Configuration(config).isJmxEnabled())
    }

    @Test
    void jmxShouldBeEnabledIfPresent() {
        Map<String, ? extends Object> config = defaultConfig()
        attachJmxConfig(config)
        assertTrue(new Configuration(config).isJmxEnabled())
    }

    @Test
    void jmxPortsShouldBeSetIfPresent() {
        Map<String, ? extends Object> config = defaultConfig()
        attachJmxConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(1234, configuration.jmxConfiguration.serverPort)
        assertEquals(2345, configuration.jmxConfiguration.registryPort)
    }

    @Test(expected = IllegalArgumentException)
    void serverPortMustBePresentForJmx() {
        Map<String, ? extends Object> config = defaultConfig()
        attachJmxConfig(config).remove("serverPort")
        new Configuration(config)
    }

    @Test(expected = IllegalArgumentException)
    void registryPortMustBePresentForJmx() {
        Map<String, ? extends Object> config = defaultConfig()
        attachJmxConfig(config).remove("registryPort")
        new Configuration(config)
    }

    protected Map<String, Map<String, Object>> defaultConfig() {
        [http: [port: 1234,
                ssl: [keyStore: "/etc/pki/tls/jks/test.jks",
                      keyStorePassword: "password",
                      certAlias: "app.domain.com"]]]
    }

    protected def attachJmxConfig(def config) {
        config.jmx = [serverPort: 1234,
                      registryPort: 2345]
        config.jmx
    }

    protected def attachRequestLoggingConfig(def config) {
        config.http.requestLog = [file: [threshold: Level.ALL.levelStr,
                                         currentLogFilename: "/app/logs/request.log",
                                         timeZone: "GMT+10"]]
        config.http.requestLog
    }

    protected Map<String, Object> attachServerLoggingConfig(Map<String, Map<String, Object>> config) {
        config.logging = [
                file: [
                    threshold: Level.DEBUG.levelStr,
                    rootLevel: Level.WARN.levelStr,
                    loggers: [
                        "foo": Level.INFO.levelStr,
                        "bar.baz": Level.ERROR.levelStr
                    ],
                    currentLogFilename: "/app/logs/server.log",
                    timeZone: "GMT+10"]]
        config.logging
    }
}
