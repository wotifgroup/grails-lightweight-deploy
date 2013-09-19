package grails.plugin.lightweightdeploy.connector;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class SslConfiguration {

    private Integer port;
    private String keyStorePath;
    private String keyStorePassword;
    private String keyStoreAlias;

    public SslConfiguration(Map<String, ?> sslConfig) throws IOException {
        this.keyStorePath = (String) sslConfig.get("keyStore");
        this.keyStoreAlias = (String) sslConfig.get("certAlias");

        if (sslConfig.containsKey("keyStorePassword")) {
            this.keyStorePassword = (String) sslConfig.get("keyStorePassword");
        } else if (sslConfig.containsKey("keyStorePasswordPath")) {
            this.keyStorePassword = Files.toString(new File((String) sslConfig.get("keyStorePasswordPath")), Charsets.US_ASCII);
        }

        if (sslConfig.containsKey("port")) {
            this.port = (Integer) sslConfig.get("port");
        }
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

    public Integer getPort() {
        return port;
    }
}
