package grails.plugin.lightweightdeploy.metrics

import com.codahale.metrics.MetricRegistry
import grails.plugin.lightweightdeploy.ExternalContext
import grails.util.Environment
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.junit.Test

import javax.servlet.ServletContext

import static groovy.util.GroovyTestCase.assertEquals

class MetricsUtilTest {

    @Test
    void metricsRegistryShouldBeReturnedFromServletContext() {
        final MetricRegistry metricRegistry = new MetricRegistry()
        ServletContextHolder.servletContext = [
            getAttribute: { String attributeName ->
                if (ExternalContext.METRICS_REGISTRY_SERVLET_ATTRIBUTE.equals(attributeName)) {
                    return metricRegistry
                } else {
                    return null
                }
            }
        ] as ServletContext
        assertEquals(metricRegistry, MetricsUtil.getMetricRegistry(Environment.PRODUCTION))
    }
}
