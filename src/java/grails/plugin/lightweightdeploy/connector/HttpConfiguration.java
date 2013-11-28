////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2013, Wotif.com. All rights reserved.
//
// This is unpublished proprietary source code of Wotif.com.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
////////////////////////////////////////////////////////////////////////////////
package grails.plugin.lightweightdeploy.connector;

import java.io.IOException;
import java.util.Map;

public class HttpConfiguration {

    private int port = 8080;

    private Integer adminPort;

    private int minThreads = 8;

    private int maxThreads = 128;

    private SslConfiguration sslConfiguration;

    public HttpConfiguration(final Map<String, ?> httpConfig) throws IOException {
        this.port = (Integer) httpConfig.get("port");
        if (httpConfig.containsKey("adminPort")) {
            this.adminPort = (Integer) httpConfig.get("adminPort");
        }
        if (httpConfig.containsKey("minThreads")) {
            this.minThreads = (Integer) httpConfig.get("minThreads");
        }
        if (httpConfig.containsKey("maxThreads")) {
            this.maxThreads = (Integer) httpConfig.get("maxThreads");
        }
        if (httpConfig.containsKey("ssl")) {
            Map<String, ?> sslConfig = (Map<String, ?>) httpConfig.get("ssl");
            this.sslConfiguration = new SslConfiguration(sslConfig);
        }
    }

    public int getPort() {
        return port;
    }

    public Integer getAdminPort() {
        return adminPort;
    }

    public int getMinThreads() {
        return minThreads;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public boolean hasAdminPort() {
        return getAdminPort() != null;
    }

    public SslConfiguration getSslConfiguration() {
        return sslConfiguration;
    }

    public boolean isMixedMode() {
        return isSsl() && sslConfiguration.getPort() != null && sslConfiguration.getPort() != port;
    }

    public boolean isSsl() {
        return (getSslConfiguration() != null);
    }

}
