package grails.plugin.lightweightdeploy.application.healthcheck

import com.codahale.metrics.health.HealthCheckRegistry
import grails.plugin.lightweightdeploy.ExternalContext
import grails.util.Environment
import org.codehaus.groovy.grails.web.context.ServletContextHolder

class HealthCheckUtil {

    private static HealthCheckRegistry fallbackHealthCheckRegistry = null

    private HealthCheckUtil() {
    }

    public static HealthCheckRegistry getHealthCheckRegistry() {
        return servletHealthCheckRegistry ?: getFallbackHealthCheckRegistry()
    }

    /**
     * @deprecated use {@link #getHealthCheckRegistry()} instead
     */
    @Deprecated
    public static HealthCheckRegistry getHealthCheckRegistry(Environment environment) {
        return healthCheckRegistry
    }

    private static getServletHealthCheckRegistry() {
        return (HealthCheckRegistry) ServletContextHolder.servletContext?.getAttribute(ExternalContext.HEALTH_CHECK_REGISTRY_SERVLET_ATTRIBUTE)
    }

    private synchronized static getFallbackHealthCheckRegistry() {
        if (!fallbackHealthCheckRegistry) {
            fallbackHealthCheckRegistry = new HealthCheckRegistry()
        }
        return fallbackHealthCheckRegistry
    }

}
