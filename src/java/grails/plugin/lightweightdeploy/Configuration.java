package grails.plugin.lightweightdeploy;

import grails.plugin.lightweightdeploy.connector.SslConfiguration;
import grails.plugin.lightweightdeploy.jmx.JmxConfiguration;
import grails.plugin.lightweightdeploy.logging.FileLoggingConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * Stores the configuration for the jetty server
 */
public class Configuration {

    private Integer port;
    private SslConfiguration sslConfiguration;
    private Integer adminPort;
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
            Map<String, String> sslConfig = (Map<String, String>) httpConfig.get("ssl");
            this.sslConfiguration = new SslConfiguration(sslConfig);
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
            this.requestLogConfiguration = new FileLoggingConfiguration(loggingConfig);
        }
    }

    protected void initServerLogging(Map<String, ?> config) {
        if (config.containsKey("logging")) {
            Map<String, ?> loggingConfig = (Map<String, ?>) config.get("logging");
            if (loggingConfig.containsKey("file")) {
                Map<String, ?> fileConfig = (Map<String, ?>) loggingConfig.get("file");
                this.serverLogConfiguration = new FileLoggingConfiguration(fileConfig);
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

    public boolean isMixedMode() {
        return isSsl() && sslConfiguration.getPort() != null && sslConfiguration.getPort() != port;
    }

    public boolean isSsl() {
        return (this.sslConfiguration != null);
    }

    public SslConfiguration getSslConfiguration() {
        return sslConfiguration;
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
               ", adminPort=" + adminPort +
               ", ssl=" + isSsl() +
               '}';
    }
}
