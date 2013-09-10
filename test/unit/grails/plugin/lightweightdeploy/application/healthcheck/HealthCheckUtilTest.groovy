package grails.plugin.lightweightdeploy.application.healthcheck

import com.codahale.metrics.health.HealthCheckRegistry
import grails.plugin.lightweightdeploy.ExternalContext
import grails.util.Environment
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.junit.Test

import javax.servlet.ServletContext

import static groovy.util.GroovyTestCase.assertEquals

class HealthCheckUtilTest {

    @Test
    void healthCheckRegistryShouldBeReturnedFromServletContext() {
        final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry()
        ServletContextHolder.servletContext = [
                getAttribute: { String attributeName ->
                    if (ExternalContext.HEALTH_CHECK_REGISTRY_SERVLET_ATTRIBUTE.equals(attributeName)) {
                        return healthCheckRegistry
                    } else {
                        return null
                    }
                }
        ] as ServletContext
        assertEquals(healthCheckRegistry, HealthCheckUtil.getHealthCheckRegistry(Environment.PRODUCTION))
    }
}
