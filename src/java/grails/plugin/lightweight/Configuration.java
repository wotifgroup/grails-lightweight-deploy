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
public class Configuration implements FileLoggingConfiguration {

    private Integer port;
    private boolean ssl = false;
    private Integer adminPort;
    private String keyStorePath;
    private String keyStorePassword;
    private String keyStoreAlias;
    private boolean fileLoggingEnabled = false;
    private String logFilePath;
    private TimeZone logFileTimeZone = TimeZone.getDefault();
    private Level loggingThreshold = Level.INFO;

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
        if (config.containsKey("logging")) {
            Map<String, ?> loggingConfig = (Map<String, ?>) config.get("logging");
            if (loggingConfig.containsKey("file")) {
                this.fileLoggingEnabled = true;
                Map<String, String> fileConfig = (Map<String, String>) loggingConfig.get("file");
                this.logFilePath = fileConfig.get("currentLogFilename");
                if (fileConfig.containsKey("timeZone")) {
                    this.logFileTimeZone = TimeZone.getTimeZone(fileConfig.get("timeZone"));
                }
                if (fileConfig.containsKey("threshold")) {
                    this.loggingThreshold = Level.toLevel(fileConfig.get("threshold"));
                }
            }
        }
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

    public boolean isFileLoggingEnabled() {
        return this.fileLoggingEnabled;
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

    public Level getThreshold() {
        return loggingThreshold;
    }

    @Override
    public TimeZone getTimeZone() {
        return this.logFileTimeZone;
    }

    @Override
    public boolean isArchive() {
        //not currently supported because of bug in logback's file rolling.
        return false;
    }

    @Override
    public String getCurrentLogFilename() {
        return this.logFilePath;
    }

    @Override
    public String getArchivedLogFilenamePattern() {
        //not currently supported because of bug in logback's file rolling.
        return null;
    }

    @Override
    public int getArchivedFileCount() {
        //not currently supported because of bug in logback's file rolling.
        return 0;
    }

    @Override
    public Optional<String> getLogFormat() {
        //TODO: support custom log format
        return Optional.absent();
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
