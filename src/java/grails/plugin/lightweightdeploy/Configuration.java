package grails.plugin.lightweightdeploy;

import ch.qos.logback.classic.Level;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import grails.plugin.lightweightdeploy.jmx.JmxConfiguration;
import grails.plugin.lightweightdeploy.logging.FileLoggingConfiguration;
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
    private FileLoggingConfiguration serverLogConfiguration;
    private FileLoggingConfiguration requestLogConfiguration;
    private File workDir;
    private JmxConfiguration jmxConfiguration;

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
        initJmx(config);
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

    protected void initJmx(Map<String, ?> config) {
        if (config.containsKey("jmx")) {
            Map<String, ?> jmxConfig = (Map<String, ?>) config.get("jmx");
            Integer registryPort = (Integer) jmxConfig.get("registryPort");
            Integer serverPort = (Integer) jmxConfig.get("serverPort");
            if (registryPort == null || serverPort == null) {
                throw new IllegalArgumentException("Both server and registry port must be present for jmx");
            }
            this.jmxConfiguration = new JmxConfiguration(registryPort, serverPort);
        }
    }

    protected void initLogging(Map<String, ?> config) {
        initRequestLogging(config);
        initServerLogging(config);
        initWorkDir(config);
    }

    protected void initRequestLogging(Map<String, ?> config) {
        Map<String, ?> httpConfig = (Map<String, ?>) config.get("http");
        if (httpConfig.containsKey("requestLog")) {
            Map<String, String> loggingConfig = (Map<String, String>) ((Map<String, ?>) httpConfig.get("requestLog")).get("file");
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
                Map<String, ?> fileConfig = (Map<String, ?>) loggingConfig.get("file");
                this.serverLogConfiguration = new FileLoggingConfiguration(fileConfig.get("currentLogFilename").toString());
                if (fileConfig.containsKey("timeZone")) {
                    this.serverLogConfiguration.setLogFileTimeZone(TimeZone.getTimeZone(fileConfig.get("timeZone").toString()));
                }
                if (fileConfig.containsKey("threshold")) {
                    this.serverLogConfiguration.setLoggingThreshold(Level.toLevel(fileConfig.get("threshold").toString()));
                }
                if (fileConfig.containsKey("rootLevel")) {
                    this.serverLogConfiguration.setRootLevel(Level.toLevel(fileConfig.get("rootLevel").toString()));
                }
                if (fileConfig.containsKey("loggers")) {
                    for (Map.Entry<String, ?> entry : ((Map<String, ?>) fileConfig.get("loggers")).entrySet()) {
                        this.serverLogConfiguration.getLoggers().put(entry.getKey(), Level.toLevel(entry.getValue().toString()));
                    }
                }
            }
        }
    }

    protected void initWorkDir(Map<String, ?> config) {
        if (config.containsKey("workDir")) {
            this.workDir = new File((String) config.get("workDir"));
        } else {
            this.workDir = new File(System.getProperty("java.io.tmpdir"));
        }
    }

    public Integer getPort() {
        return port;
    }

    public Integer getAdminPort() {
        return adminPort;
    }

    public JmxConfiguration getJmxConfiguration() {
        return jmxConfiguration;
    }

    public boolean hasAdminPort() {
        return getAdminPort() != null;
    }

    public boolean isJmxEnabled() {
        return (this.jmxConfiguration != null);
    }

    public boolean isRequestLoggingEnabled() {
        return (this.requestLogConfiguration != null);
    }

    public boolean isServerLoggingEnabled() {
        return (this.serverLogConfiguration != null);
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

    public File getWorkDir() {
        return workDir;
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
