package grails.plugin.lightweight;

import ch.qos.logback.classic.Level;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Files;
import grails.plugin.lightweight.logging.FileLoggingConfiguration;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TimeZone;
import org.yaml.snakeyaml.Yaml;

/**
 * Stores the configuration for the jetty server
 */
public class Configuration {

    private Integer port;
    private boolean ssl = false;
    private Integer adminPort;
    private String keyStorePath;
    private String keyStorePassword;
    private String keyStoreAlias;
    private Level baseLoggingThreshold = Level.INFO;
    private boolean serverLoggingEnabled = false;
    private FileLoggingConfiguration serverLogConfiguration;
    private boolean requestLoggingEnabled = false;
    private FileLoggingConfiguration requestLogConfiguration;

    public Configuration(Map<String, ?> config) throws IOException {
        init(config);
    }

    public Configuration(String ymlFilePath) throws IOException {
        Map<String, ?> config = (Map<String, ?>) new Yaml().load(new FileReader(new File(ymlFilePath)));
        init(config);
    }

    protected void init(Map<String, ?> config) throws IOException {
        initHttp(config);
        initLogging(config);
    }

    protected void initHttp(Map<String, ?> config) throws IOException {
        Map<String, ?> httpConfig = (Map<String, ?>) config.get("http");

        this.port = (Integer) httpConfig.get("port");
        if (httpConfig.containsKey("ssl")) {
            this.ssl = true;
            Map<String, String> sslConfig = (Map<String, String>) httpConfig.get("ssl");

            //configure SSL key store
            this.keyStorePath = sslConfig.get("keyStore");
            this.keyStoreAlias = sslConfig.get("certAlias");
            if (sslConfig.containsKey("keyStorePassword")) {
                this.keyStorePassword = sslConfig.get("keyStorePassword");
            } else if (sslConfig.containsKey("keyStorePasswordPath")) {
                this.keyStorePassword = Files.toString(new File(sslConfig.get("keyStorePasswordPath")), Charsets.US_ASCII);
            }
        }

        this.adminPort = null;
        if (httpConfig.containsKey("adminPort")) {
            this.adminPort = (Integer) httpConfig.get("adminPort");
        }
    }

    protected void initLogging(Map<String, ?> config) {
        initRequestLogging(config);
        initServerLogging(config);
    }

    protected void initRequestLogging(Map<String, ?> config) {
        Map<String, ?> httpConfig = (Map<String, ?>) config.get("http");
        if (httpConfig.containsKey("requestLog")) {
            Map<String, String> loggingConfig = (Map<String, String>) ((Map<String, ?>) httpConfig.get("requestLog")).get("file");
            this.requestLoggingEnabled = true;
            this.requestLogConfiguration = new FileLoggingConfiguration(loggingConfig.get("currentLogFilename"));
            if (loggingConfig.containsKey("timeZone")) {
                this.requestLogConfiguration.setLogFileTimeZone(TimeZone.getTimeZone(loggingConfig.get("timeZone")));
            }
            if (loggingConfig.containsKey("threshold")) {
                this.requestLogConfiguration.setLoggingThreshold(Level.toLevel(loggingConfig.get("threshold")));
            }
        }
    }

    protected void initServerLogging(Map<String, ?> config) {
        if (config.containsKey("logging")) {
            Map<String, ?> loggingConfig = (Map<String, ?>) config.get("logging");
            if (loggingConfig.containsKey("file")) {
                this.serverLoggingEnabled = true;
                Map<String, String> fileConfig = (Map<String, String>) loggingConfig.get("file");
                this.serverLogConfiguration = new FileLoggingConfiguration(fileConfig.get("currentLogFilename"));
                if (fileConfig.containsKey("timeZone")) {
                    this.serverLogConfiguration.setLogFileTimeZone(TimeZone.getTimeZone(fileConfig.get("timeZone")));
                }
                if (fileConfig.containsKey("threshold")) {
                    this.serverLogConfiguration.setLoggingThreshold(Level.toLevel(fileConfig.get("threshold")));
                }
            }
        }
    }

    public Level getBaseLoggingThreshold() {
        return baseLoggingThreshold;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getAdminPort() {
        return adminPort;
    }

    public boolean hasAdminPort() {
        return getAdminPort() != null;
    }

    public boolean isRequestLoggingEnabled() {
        return requestLoggingEnabled;
    }

    public boolean isServerLoggingEnabled() {
        return this.serverLoggingEnabled;
    }

    public boolean isSsl() {
        return ssl;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getKeyStoreAlias() {
        return keyStoreAlias;
    }

    public FileLoggingConfiguration getServerLogConfiguration() {
        return serverLogConfiguration;
    }

    public FileLoggingConfiguration getRequestLogConfiguration() {
        return requestLogConfiguration;
    }

    @Override
    public String toString() {
        return "Configuration{" +
               "port=" + port +
               ", ssl=" + ssl +
               ", adminPort=" + adminPort +
               ", keyStorePath='" + keyStorePath + '\'' +
               ", keyStoreAlias='" + keyStoreAlias + '\'' +
               '}';
    }
}
