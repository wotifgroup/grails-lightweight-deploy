package grails.plugin.lightweightdeploy.application.metrics

import com.codahale.metrics.MetricRegistry
import grails.plugin.lightweightdeploy.ExternalContext
import grails.util.Environment
import org.codehaus.groovy.grails.web.context.ServletContextHolder

class MetricsUtil {

    private static MetricRegistry fallbackMetricRegistry = null

    private MetricsUtil() {
    }

    public static MetricRegistry getMetricRegistry() {
        return servletMetricRegistry ?: getFallbackMetricRegistry()
    }

    /**
     * @deprecated use {@link #getMetricRegistry()} instead
     */
    @Deprecated
    public static MetricRegistry getMetricRegistry(Environment environment) {
        return metricRegistry
    }

    private static MetricRegistry getServletMetricRegistry() {
        return (MetricRegistry) ServletContextHolder.servletContext?.getAttribute(ExternalContext.METRICS_REGISTRY_SERVLET_ATTRIBUTE)
    }

    private synchronized static getFallbackMetricRegistry() {
        if (!fallbackMetricRegistry) {
            fallbackMetricRegistry = new MetricRegistry()
        }
        return fallbackMetricRegistry
    }

}
