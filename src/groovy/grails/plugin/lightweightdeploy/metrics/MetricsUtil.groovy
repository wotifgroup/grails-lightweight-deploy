package grails.plugin.lightweightdeploy.metrics

import com.codahale.metrics.MetricRegistry
import grails.plugin.lightweightdeploy.ExternalContext
import org.codehaus.groovy.grails.web.context.ServletContextHolder

class MetricsUtil {

    public static MetricRegistry getMetricRegistry() {
        ServletContextHolder.servletContext.getAttribute(ExternalContext.METRICS_REGISTRY_SERVLET_ATTRIBUTE)
    }
}
