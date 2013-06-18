package grails.plugin.lightweightdeploy.jmx;

public class JmxConfiguration {

    private int serverPort;
    private int registryPort;

    public JmxConfiguration(int registryPort, int serverPort) {
        this.registryPort = registryPort;
        this.serverPort = serverPort;
    }

    public int getRegistryPort() {
        return registryPort;
    }

    public int getServerPort() {
        return serverPort;
    }
}
