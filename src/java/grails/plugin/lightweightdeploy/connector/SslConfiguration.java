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

    public SslConfiguration(Map<String, String> sslConfig) throws IOException {
        this.keyStorePath = sslConfig.get("keyStore");
        this.keyStoreAlias = sslConfig.get("certAlias");

        if (sslConfig.containsKey("keyStorePassword")) {
            this.keyStorePassword = sslConfig.get("keyStorePassword");
        } else if (sslConfig.containsKey("keyStorePasswordPath")) {
            this.keyStorePassword = Files.toString(new File(sslConfig.get("keyStorePasswordPath")), Charsets.US_ASCII);
        }

        if (sslConfig.containsKey("port")) {
            this.port = Integer.valueOf(sslConfig.get("port"));
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
