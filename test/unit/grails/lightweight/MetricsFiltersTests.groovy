package grails.lightweight

import com.codahale.metrics.MetricRegistry
import grails.plugin.lightweightdeploy.ExternalContext
import grails.test.mixin.Mock
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.junit.Test

import javax.servlet.ServletContext

@Mock(MetricsFilters)
class MetricsFiltersTests {

    @Test
    void controllerCallShouldBeTimed() {
        MetricRegistry metricRegistry = mockMetricRegistry()

        withFilters(controller: "test", action: 'testAction') {
            Thread.sleep(100)
        }
        assertEquals 1, metricRegistry.getTimers().get("test.testAction").getCount()
    }

    @Test
    void actionNameShouldDefault() {
        MetricRegistry metricRegistry = mockMetricRegistry()

        withFilters(controller: "test") {
            Thread.sleep(100)
        }
        assertEquals 1, metricRegistry.getTimers().get("test.defaultAction").getCount()
    }

    private MetricRegistry mockMetricRegistry() {
        MetricRegistry metricRegistry = new MetricRegistry()
        def servletContextMock = mockFor(ServletContext)
        servletContextMock.demand.getAttribute {String name ->
            assertEquals ExternalContext.METRICS_REGISTRY_SERVLET_ATTRIBUTE, name
            return metricRegistry
        }
        ServletContextHolder.servletContext = servletContextMock.createMock()
        metricRegistry
    }
}
