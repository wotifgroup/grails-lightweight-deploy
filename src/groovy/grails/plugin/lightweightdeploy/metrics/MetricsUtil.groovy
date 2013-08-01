package grails.plugin.lightweightdeploy.metrics

import com.codahale.metrics.MetricRegistry
import grails.plugin.lightweightdeploy.ExternalContext
import grails.util.Environment
import org.codehaus.groovy.grails.web.context.ServletContextHolder

class MetricsUtil {

    /**
     * The MetricRegistry to use when Environment == TEST
     */
    private static MetricRegistry testMetricRegistry

    public static MetricRegistry getMetricRegistry() {
        if (Environment.current == Environment.PRODUCTION) {
            ServletContextHolder.servletContext.getAttribute(ExternalContext.METRICS_REGISTRY_SERVLET_ATTRIBUTE)
        } else {
            //Not all test types properly bootstrap the servletContext as of Grails 2.2.3. Therefore, in test mode, use
            //a consistent instance
            if (!this.testMetricRegistry) {
                this.testMetricRegistry = new MetricRegistry()
            }
            this.testMetricRegistry
        }
    }
}
