package grails.plugin.lightweightdeploy

import ch.qos.logback.classic.Level
import org.junit.Test

import static org.junit.Assert.*

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
        assertEquals("app.domain.com", configuration.sslConfiguration.keyStoreAlias)
    }

    @Test
    void shouldSetKeystorePathFromConfig() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        Configuration configuration = new Configuration(config)
        assertEquals("/etc/pki/tls/jks/test.jks", configuration.sslConfiguration.keyStorePath)
    }

    @Test
    void shouldSetKeystorePasswordFromConfig() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        Configuration configuration = new Configuration(config)
        assertEquals("password", configuration.sslConfiguration.keyStorePassword)
    }

    @Test
    void serverConsoleLoggingThresholdShouldDefaultToAll() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        attachServerConsoleLoggingConfig(config).console.remove("threshold")
        Configuration configuration = new Configuration(config)
        assertEquals(Level.ALL, configuration.serverLogConfiguration.consoleConfiguration.threshold)
    }

    @Test
    void ifServerLoggingConsoleSetInConfigThenFileLoggingShouldBeSetToTrue() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerConsoleLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertTrue(configuration.isServerLoggingEnabled())
    }

    @Test
    void serverConsoleLoggingThresholdShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerConsoleLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(Level.DEBUG, configuration.serverLogConfiguration.consoleConfiguration.threshold)
    }

    @Test
    void serverConsoleLoggingTimezoneShouldDefaultToLocal() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerConsoleLoggingConfig(config)
        config.logging.console.remove("timeZone")
        Configuration configuration = new Configuration(config)
        assertEquals(TimeZone.default, configuration.serverLogConfiguration.consoleConfiguration.timeZone)
    }

    @Test
    void serverConsoleLoggingTimezoneShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerConsoleLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(TimeZone.getTimeZone("GMT+10"), configuration.serverLogConfiguration.consoleConfiguration.timeZone)
    }

    @Test
    void serverConsoleLoggingFormatShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerConsoleLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals("[%d{ISO8601}] %m%n", configuration.serverLogConfiguration.consoleConfiguration.logFormat.get())
    }

    @Test
    void serverFileLoggingThresholdShouldDefaultToAll() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        attachServerFileLoggingConfig(config).file.remove("threshold")
        Configuration configuration = new Configuration(config)
        assertEquals(Level.ALL, configuration.serverLogConfiguration.fileConfiguration.threshold)
    }

    @Test
    void serverFileLoggingRootLevelShouldDefaultToInfo() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        attachServerFileLoggingConfig(config).remove("rootLevel")
        Configuration configuration = new Configuration(config)
        assertEquals(Level.INFO, configuration.serverLogConfiguration.rootLevel)
    }

    @Test
    void serverFileLoggingLoggersIsNotRequired() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        attachServerFileLoggingConfig(config).remove("loggers")
        Configuration configuration = new Configuration(config)
        assertEquals(0, configuration.serverLogConfiguration.loggers.size())
    }

    @Test
    void serverFileLoggingLoggersCanBeEmpty() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        attachServerFileLoggingConfig(config).loggers.clear()
        Configuration configuration = new Configuration(config)
        assertEquals(0, configuration.serverLogConfiguration.loggers.size())
    }

    @Test(expected = IllegalArgumentException)
    void serverFileLoggingShouldNotAcceptLoggersInsideFileConfiguration() {
        Map<String, ? extends Object> config = defaultConfig()
        attachServerFileLoggingConfig(config)
        config.logging.file.put("loggers", config.logging.loggers)
        new Configuration(config)
    }

    @Test(expected = IllegalArgumentException)
    void serverFileLoggingShouldNotAcceptRootLevelInsideFileConfiguration() {
        Map<String, ? extends Object> config = defaultConfig()
        attachServerFileLoggingConfig(config)
        config.logging.file.put("rootLevel", config.logging.rootLevel)
        new Configuration(config)
    }

    @Test
    void ifServerLoggingFileSetInConfigThenFileLoggingShouldBeSetToTrue() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerFileLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertTrue(configuration.isServerLoggingEnabled())
    }

    @Test
    void serverFileLoggingThresholdShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerFileLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(Level.DEBUG, configuration.serverLogConfiguration.fileConfiguration.threshold)
    }

    @Test
    void serverFileLoggingRootLevelShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerFileLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(Level.WARN, configuration.serverLogConfiguration.rootLevel)
    }

    @Test
    void serverFileLoggingLoggersShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerFileLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(Level.INFO, configuration.serverLogConfiguration.loggers."foo")
        assertEquals(Level.ERROR, configuration.serverLogConfiguration.loggers."bar.baz")
    }

    @Test
    void serverFileLoggingFileShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerFileLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals("/app/logs/server.log", configuration.serverLogConfiguration.fileConfiguration.currentLogFilename)
    }

    @Test
    void serverFileLoggingTimezoneShouldDefaultToLocal() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerFileLoggingConfig(config)
        config.logging.file.remove("timeZone")
        Configuration configuration = new Configuration(config)
        assertEquals(TimeZone.default, configuration.serverLogConfiguration.fileConfiguration.timeZone)
    }

    @Test
    void serverFileLoggingTimezoneShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerFileLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(TimeZone.getTimeZone("GMT+10"), configuration.serverLogConfiguration.fileConfiguration.timeZone)
    }

    @Test
    void serverFileLoggingFormatShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerFileLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals("[%d{ISO8601}] %m%n", configuration.serverLogConfiguration.fileConfiguration.logFormat.get())
    }

    @Test
    void requestLoggingThresholdShouldDefaultToAll() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        attachRequestLoggingConfig(config).file.remove("threshold")
        Configuration configuration = new Configuration(config)
        assertEquals(Level.ALL, configuration.requestLogConfiguration.fileConfiguration.threshold)
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
        assertEquals(Level.ALL, configuration.requestLogConfiguration.fileConfiguration.threshold)
    }

    @Test
    void requestLoggingFileShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachRequestLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals("/app/logs/request.log", configuration.requestLogConfiguration.fileConfiguration.currentLogFilename)
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
        config.http.requestLog = [
                file: [
                        threshold: Level.ALL.levelStr,
                        currentLogFilename: "/app/logs/request.log",
                        timeZone: "GMT+10"
                ]]
        config.http.requestLog
    }

    protected Map<String, Object> attachServerFileLoggingConfig(Map<String, Map<String, Object>> config) {
        config.logging = [
                rootLevel: Level.WARN.levelStr,
                loggers: [
                        "foo": Level.INFO.levelStr,
                        "bar.baz": Level.ERROR.levelStr
                ],
                file: [
                        threshold: Level.DEBUG.levelStr,
                        currentLogFilename: "/app/logs/server.log",
                        timeZone: "GMT+10",
                        logFormat: "[%d{ISO8601}] %m%n"]]
        config.logging
    }

    protected Map<String, Object> attachServerConsoleLoggingConfig(Map<String, Map<String, Object>> config) {
        config.logging = [
                rootLevel: Level.WARN.levelStr,
                loggers: [
                        "foo": Level.INFO.levelStr,
                        "bar.baz": Level.ERROR.levelStr
                ],
                console: [
                        threshold: Level.DEBUG.levelStr,
                        timeZone: "GMT+10",
                        logFormat: "[%d{ISO8601}] %m%n"]]
        config.logging
    }

}
