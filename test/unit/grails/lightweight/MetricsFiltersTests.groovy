package grails.lightweight

import com.codahale.metrics.MetricRegistry
import grails.plugin.lightweightdeploy.metrics.MetricsUtil
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

    @Test
    void controllerNameShouldDefault() {
        MetricRegistry metricRegistry = mockMetricRegistry()

        new MetricsFilters().startTimer(null, null)
        assertNotNull metricRegistry.getTimers().get("noController.defaultAction")
    }

    /**
     * No metricRegistry when just using run-app.
     */
    @Test
    void shouldNotFailIfNoMetricRegistry() {
        def servletContextMock = mockFor(ServletContext)
        servletContextMock.demand.getAttribute {String name ->
            null
        }
        ServletContextHolder.servletContext = servletContextMock.createMock()

        new MetricsFilters().startTimer("test", "testAction")
    }

    private MetricRegistry mockMetricRegistry() {
        MetricRegistry metricRegistry = new MetricRegistry()
        MetricsUtil.metaClass.'static'.getMetricRegistry = { -> metricRegistry }
        metricRegistry
    }
}
