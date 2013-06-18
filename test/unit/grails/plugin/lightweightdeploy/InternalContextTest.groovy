package grails.plugin.lightweightdeploy

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.health.HealthCheckRegistry
import com.codahale.metrics.servlets.HealthCheckServlet
import com.codahale.metrics.servlets.MetricsServlet
import org.junit.Test

import static junit.framework.Assert.assertEquals

class InternalContextTest {

    @Test
    void metricRegistryAttributeShouldBeSet() {
        assertEquals(MetricRegistry.class, internalContext.servletContext.getAttribute(MetricsServlet.METRICS_REGISTRY).class)
    }

    @Test
    void healthCheckAttributeShouldBeSet() {
        assertEquals(HealthCheckRegistry.class, internalContext.servletContext.getAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY).class)
    }

    def getInternalContext() {
        new InternalContext(new HealthCheckRegistry(),
                            new MetricRegistry())
    }
}
