package grails.plugin.lightweightdeploy;

import grails.plugin.lightweightdeploy.connector.HttpConfiguration;
import grails.plugin.lightweightdeploy.connector.SslConfiguration;
import grails.plugin.lightweightdeploy.jmx.JmxConfiguration;
import grails.plugin.lightweightdeploy.logging.LoggingConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * Stores the configuration for the jetty server
 */
public class Configuration {

    private HttpConfiguration httpConfiguration;
    private SslConfiguration sslConfiguration;
    private LoggingConfiguration serverLogConfiguration;
    private LoggingConfiguration requestLogConfiguration;
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

        this.httpConfiguration = new HttpConfiguration(httpConfig);
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
            requestLogConfiguration = new LoggingConfiguration((Map<String, ?>) httpConfig.get("requestLog"));
        }
    }

    protected void initServerLogging(Map<String, ?> config) {
        if (config.containsKey("logging")) {
            serverLogConfiguration = new LoggingConfiguration((Map<String, ?>) config.get("logging"));
        }
    }

    protected void initWorkDir(Map<String, ?> config) {
        if (config.containsKey("workDir")) {
            this.workDir = new File((String) config.get("workDir"));
        } else {
            this.workDir = new File(System.getProperty("java.io.tmpdir"));
        }
    }

    public HttpConfiguration getHttpConfiguration() {
        return httpConfiguration;
    }

    public JmxConfiguration getJmxConfiguration() {
        return jmxConfiguration;
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

    public LoggingConfiguration getServerLogConfiguration() {
        return serverLogConfiguration;
    }

    public LoggingConfiguration getRequestLogConfiguration() {
        return requestLogConfiguration;
    }

    public File getWorkDir() {
        return workDir;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "port=" + httpConfiguration.getPort() +
                ", adminPort=" + httpConfiguration.getAdminPort() +
                ", ssl=" + httpConfiguration.isSsl() +
                '}';
    }

}
