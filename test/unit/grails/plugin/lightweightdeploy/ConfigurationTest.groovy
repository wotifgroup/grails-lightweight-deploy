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
        assertEquals(1234, configuration.httpConfiguration.port.intValue())
        assertFalse(configuration.httpConfiguration.isSsl())
    }

    @Test
    void shouldAssumeHttpsIfSslBlock() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        Configuration configuration = new Configuration(config)
        assertEquals(1234, configuration.httpConfiguration.port.intValue())
        assertTrue(configuration.httpConfiguration.isSsl())
    }

    @Test
    void shouldSetKeystoreAliasFromConfig() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        Configuration configuration = new Configuration(config)
        assertEquals("app.domain.com", configuration.httpConfiguration.sslConfiguration.keyStoreAlias)
    }

    @Test
    void shouldSetKeystorePathFromConfig() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        Configuration configuration = new Configuration(config)
        assertEquals("/etc/pki/tls/jks/test.jks", configuration.httpConfiguration.sslConfiguration.keyStorePath)
    }

    @Test
    void shouldSetKeystorePasswordFromConfig() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        Configuration configuration = new Configuration(config)
        assertEquals("password", configuration.httpConfiguration.sslConfiguration.keyStorePassword)
    }

    @Test
    void serverConsoleLoggingThresholdShouldDefaultToAll() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        attachServerConsoleLoggingConfig(config).appenders[0].remove("threshold")
        Configuration configuration = new Configuration(config)
        assertEquals(Level.ALL, configuration.serverLogConfiguration.appenderConfigurations[0].threshold)
    }

    @Test
    void ifServerLoggingConsoleSetInConfigThenServerLoggingShouldBeSetToTrue() throws IOException {
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
        assertEquals(Level.DEBUG, configuration.serverLogConfiguration.appenderConfigurations[0].threshold)
    }

    @Test
    void serverConsoleLoggingTimezoneShouldDefaultToLocal() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerConsoleLoggingConfig(config).appenders[0].remove("timeZone")
        Configuration configuration = new Configuration(config)
        assertEquals(TimeZone.default, configuration.serverLogConfiguration.appenderConfigurations[0].timeZone)
    }

    @Test
    void serverConsoleLoggingTimezoneShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerConsoleLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(TimeZone.getTimeZone("GMT+10"), configuration.serverLogConfiguration.appenderConfigurations[0].timeZone)
    }

    @Test
    void serverConsoleLoggingFormatShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerConsoleLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals("[%d{ISO8601}] %m%n", configuration.serverLogConfiguration.appenderConfigurations[0].logFormat.get())
    }

    @Test
    void serverFileLoggingThresholdShouldDefaultToAll() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        attachServerFileLoggingConfig(config).appenders[0].remove("threshold")
        Configuration configuration = new Configuration(config)
        assertEquals(Level.ALL, configuration.serverLogConfiguration.appenderConfigurations[0].threshold)
    }

    @Test
    void serverFileLoggingRootLevelShouldDefaultToInfo() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        attachServerFileLoggingConfig(config).remove("level")
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
        config.logging.appenders[0].put("loggers", config.logging.loggers)
        new Configuration(config)
    }

    @Test(expected = IllegalArgumentException)
    void serverFileLoggingShouldNotAcceptRootLevelInsideFileConfiguration() {
        Map<String, ? extends Object> config = defaultConfig()
        attachServerFileLoggingConfig(config)
        config.logging.appenders[0].put("rootLevel", config.logging.level)
        new Configuration(config)
    }

    @Test
    void ifServerLoggingFileSetInConfigThenServerLoggingShouldBeSetToTrue() throws IOException {
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
        assertEquals(Level.DEBUG, configuration.serverLogConfiguration.appenderConfigurations[0].threshold)
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
        assertEquals("/app/logs/server.log",
                configuration.serverLogConfiguration.appenderConfigurations[0].currentLogFilename)
    }

    @Test
    void serverFileLoggingTimezoneShouldDefaultToLocal() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerFileLoggingConfig(config).appenders[0].remove("timeZone")
        Configuration configuration = new Configuration(config)
        assertEquals(TimeZone.default, configuration.serverLogConfiguration.appenderConfigurations[0].timeZone)
    }

    @Test
    void serverFileLoggingTimezoneShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerFileLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(TimeZone.getTimeZone("GMT+10"),
                configuration.serverLogConfiguration.appenderConfigurations[0].timeZone)
    }

    @Test
    void serverFileLoggingFormatShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerFileLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals("[%d{ISO8601}] %m%n",
                configuration.serverLogConfiguration.appenderConfigurations[0].logFormat.get())
    }

    @Test
    void serverFilteredLoggingShouldBeSupported() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerFilteredLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertNotNull(configuration.serverLogConfiguration.appenderConfigurations[0])
    }

    @Test
    void serverFilteredLoggingShouldWrapAnotherAppender() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerFilteredLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals("[%d{ISO8601}] %m%n",
                configuration.serverLogConfiguration.appenderConfigurations[0].appender.logFormat.get())
    }

    @Test(expected = IllegalArgumentException)
    void serverFilteredLoggingMustWrapAnotherAppender() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerFilteredLoggingConfig(config).appenders[0].remove("appender")
        new Configuration(config)
    }

    @Test
    void serverFilteredLoggingShouldUseInclusionsFromFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerFilteredLoggingConfig(config).appenders[0].includes = ["foo"]
        Configuration configuration = new Configuration(config)
        assertEquals(["foo"].toSet(), configuration.serverLogConfiguration.appenderConfigurations[0].inclusions)
    }

    @Test
    void serverFilteredLoggingInclusionsShouldBeOptional() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerFilteredLoggingConfig(config).appenders[0].remove("includes")
        Configuration configuration = new Configuration(config)
        assertEquals([].toSet(), configuration.serverLogConfiguration.appenderConfigurations[0].inclusions)
    }

    @Test
    void serverFilteredLoggingShouldUseExclusionsFromFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerFilteredLoggingConfig(config).appenders[0].excludes = ["bar"]
        Configuration configuration = new Configuration(config)
        assertEquals(["bar"].toSet(), configuration.serverLogConfiguration.appenderConfigurations[0].exclusions)
    }

    @Test
    void serverFilteredLoggingExclusionsShouldBeOptional() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerFilteredLoggingConfig(config).appenders[0].remove("excludes")
        Configuration configuration = new Configuration(config)
        assertEquals([].toSet(), configuration.serverLogConfiguration.appenderConfigurations[0].exclusions)
    }

    @Test
    void requestLoggingThresholdShouldDefaultToAll() throws IOException {
        Map<String, ? extends Object> config = defaultConfig()
        attachRequestLoggingConfig(config).appenders[0].remove("threshold")
        Configuration configuration = new Configuration(config)
        assertEquals(Level.ALL, configuration.requestLogConfiguration.appenderConfigurations[0].threshold)
    }

    @Test
    void ifRequestLoggingFileSetInConfigThenFileLoggingShouldBeSetToTrue() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachRequestLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertTrue(configuration.requestLoggingEnabled)
    }

    @Test
    void requestLoggingThresholdShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachRequestLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(Level.ALL, configuration.requestLogConfiguration.appenderConfigurations[0].threshold)
    }

    @Test
    void requestLoggingFileShouldBeSetToValueInFile() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachRequestLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals("/app/logs/request.log",
                configuration.requestLogConfiguration.appenderConfigurations[0].currentLogFilename)
    }

    @Test
    void requestLoggingTimezoneShouldDefaultToLocal() throws IOException {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachRequestLoggingConfig(config).appenders[0].remove("timeZone")
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
    public void deprecatedServerFileLoggingFormatSupported() throws Exception {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachDeprecatedServerFileLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(1, configuration.serverLogConfiguration.appenderConfigurations.size())
    }

    @Test
    public void deprecatedServerConsoleLoggingFormatSupported() throws Exception {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachDeprecatedServerConsoleLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(1, configuration.serverLogConfiguration.appenderConfigurations.size())
    }

    @Test
    public void deprecatedRequestLoggingFormatSupported() throws Exception {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachDeprecatedRequestLoggingConfig(config)
        Configuration configuration = new Configuration(config)
        assertEquals(1, configuration.requestLogConfiguration.appenderConfigurations.size())
    }

    @Test
    public void multipleLoggersOfSameTypeSupported() throws Exception {
        Map<String, Map<String, Object>> config = defaultConfig()
        attachServerFileLoggingConfig(config).appenders[0].currentLogFilename = '/app/logs/server-foo.log'
        attachServerFileLoggingConfig(config).appenders[1].currentLogFilename = '/app/logs/server-bar.log'
        Configuration configuration = new Configuration(config)
        assertEquals(['/app/logs/server-foo.log', '/app/logs/server-bar.log'],
                configuration.serverLogConfiguration.appenderConfigurations*.currentLogFilename)
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

    @Test
    void minThreadsShouldDefault() {
        Configuration configuration = new Configuration(defaultConfig())
        assertEquals(8, configuration.httpConfiguration.minThreads)
    }

    @Test
    void maxThreadsShouldDefault() {
        Configuration configuration = new Configuration(defaultConfig())
        assertEquals(128, configuration.httpConfiguration.maxThreads)
    }

    @Test
    void minThreadsShouldBeSetIfPresent() {
        Map<String, ? extends Object> config = defaultConfig()
        config.http.minThreads = 1000
        Configuration configuration = new Configuration(config)
        assertEquals(1000, configuration.httpConfiguration.minThreads)
    }

    @Test
    void maxThreadsShouldBeSetIfPresent() {
        Map<String, ? extends Object> config = defaultConfig()
        config.http.maxThreads = 10000
        Configuration configuration = new Configuration(config)
        assertEquals(10000, configuration.httpConfiguration.maxThreads)
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

    protected def attachDeprecatedRequestLoggingConfig(def config) {
        config.http.requestLog = [
                file: [
                        threshold: Level.ALL.levelStr,
                        currentLogFilename: "/app/logs/request.log",
                        timeZone: "GMT+10"
                ]]
        config.http.requestLog
    }

    protected def attachRequestLoggingConfig(def config) {
        config.http.requestLog = [
                appenders: [
                        [
                                type: "file",
                                threshold: Level.ALL.levelStr,
                                currentLogFilename: "/app/logs/request.log",
                                timeZone: "GMT+10"
                        ]
                ]]
        config.http.requestLog
    }

    protected Map<String, Object> attachDeprecatedServerFileLoggingConfig(Map<String, Map<String, Object>> config) {
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

    protected Map<String, Object> attachDeprecatedServerConsoleLoggingConfig(Map<String, Map<String, Object>> config) {
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

    protected Map<String, Object> attachServerFileLoggingConfig(Map<String, Map<String, Object>> config) {
        if (!config.logging) {
            attachServerLoggingConfig(config)
        }
        config.logging.appenders += [
                type: "file",
                threshold: Level.DEBUG.levelStr,
                currentLogFilename: "/app/logs/server.log",
                logFormat: "[%d{ISO8601}] %m%n",
                timeZone: "GMT+10"
        ]
        config.logging
    }

    protected Map<String, Object> attachServerConsoleLoggingConfig(Map<String, Map<String, Object>> config) {
        if (!config.logging) {
            attachServerLoggingConfig(config)
        }
        config.logging.appenders += [
                type: "console",
                threshold: Level.DEBUG.levelStr,
                timeZone: "GMT+10",
                logFormat: "[%d{ISO8601}] %m%n"
        ]
        config.logging
    }

    protected Map<String, Object> attachServerFilteredLoggingConfig(Map<String, Map<String, Object>> config) {
        if (!config.logging) {
            attachServerLoggingConfig(config)
        }
        config.logging.appenders += [
                type    : "filtered",
                includes: ["foo"],
                appender: [
                        type              : "file",
                        threshold         : Level.DEBUG.levelStr,
                        currentLogFilename: "/app/logs/server.log",
                        timeZone          : "GMT+10",
                        logFormat         : "[%d{ISO8601}] %m%n"
                ]
        ]
        config.logging
    }

    protected Map<String, Object> attachServerLoggingConfig(Map<String, Map<String, Object>> config) {
        config.logging = [
                level: Level.WARN.levelStr,
                loggers: [
                        "foo": Level.INFO.levelStr,
                        "bar.baz": Level.ERROR.levelStr
                ],
                appenders: []
        ]
        config.logging
    }

}
