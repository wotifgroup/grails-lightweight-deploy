package grails.lightweight

import com.codahale.metrics.MetricRegistry
import grails.plugin.lightweightdeploy.Launcher
import grails.test.mixin.*
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.junit.Test

import javax.servlet.ServletContext

@Mock(MetricsFilters)
class MetricsFiltersTests {

    @Test
    void controllerCallShouldBeTimed() {
        MetricRegistry metricRegistry = new MetricRegistry()
        def servletContextMock = mockFor(ServletContext)
        servletContextMock.demand.getAttribute {String name ->
            assertEquals Launcher.METRICS_REGISTRY_SERVLET_ATTRIBUTE, name
            return metricRegistry
        }
        ServletContextHolder.servletContext = servletContextMock.createMock()
        withFilters(controller: "test", action: 'testAction') {
            Thread.sleep(100)
        }
        assertEquals 1, metricRegistry.getTimers().get("test.testAction").getCount()
    }
}
