package grails.plugin.lightweightdeploy.application.healthcheck

import com.codahale.metrics.health.HealthCheckRegistry
import grails.plugin.lightweightdeploy.ExternalContext
import grails.util.Environment
import org.codehaus.groovy.grails.web.context.ServletContextHolder

class HealthCheckUtil {

    /**
     * The HealthCheckRegistry to use when Environment == TEST
     */
    private static HealthCheckRegistry testHealthCheckRegistry = null

    private HealthCheckUtil() {
    }

    public static HealthCheckRegistry getHealthCheckRegistry() {
        return getHealthCheckRegistry(Environment.getCurrent())
    }

    public static HealthCheckRegistry getHealthCheckRegistry(Environment environment) {
        if (Environment.PRODUCTION.equals(environment)) {
            return (HealthCheckRegistry) ServletContextHolder.servletContext.getAttribute(ExternalContext.HEALTH_CHECK_REGISTRY_SERVLET_ATTRIBUTE)
        } else {
            //Not all test types properly bootstrap the servletContext as of Grails 2.2.3. Therefore, in test mode, use
            //a consistent instance
            if (!testHealthCheckRegistry) {
                testHealthCheckRegistry = new HealthCheckRegistry()
            }
            return testHealthCheckRegistry
        }
    }
}
