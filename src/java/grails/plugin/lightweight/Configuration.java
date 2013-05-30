package grails.plugin.lightweight;

import ch.qos.logback.classic.Level;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
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
                Map<String, String> fileConfig = (Map<String, String>) loggingConfig.get("file");
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

    public Level getLoggingThreshold() {
        return loggingThreshold;
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
