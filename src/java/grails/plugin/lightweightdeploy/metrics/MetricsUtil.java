package grails.plugin.lightweightdeploy.metrics;

import com.codahale.metrics.MetricRegistry;
import grails.plugin.lightweightdeploy.ExternalContext;
import grails.util.Environment;
import org.codehaus.groovy.grails.web.context.ServletContextHolder;

class MetricsUtil {

    /**
     * The MetricRegistry to use when Environment == TEST
     */
    private static MetricRegistry testMetricRegistry = null;

    private MetricsUtil() {
    }

    public static MetricRegistry getMetricRegistry() {
        return getMetricRegistry(Environment.getCurrent());
    }

    public static MetricRegistry getMetricRegistry(Environment environment) {
        if (Environment.PRODUCTION.equals(environment)) {
            return (MetricRegistry) ServletContextHolder.getServletContext().getAttribute(ExternalContext.METRICS_REGISTRY_SERVLET_ATTRIBUTE);
        } else {
            //Not all test types properly bootstrap the servletContext as of Grails 2.2.3. Therefore, in test mode, use
            //a consistent instance
            if (testMetricRegistry == null) {
                testMetricRegistry = new MetricRegistry();
            }
            return testMetricRegistry;
        }
    }
}
