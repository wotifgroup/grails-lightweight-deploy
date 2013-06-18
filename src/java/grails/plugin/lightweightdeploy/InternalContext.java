package grails.plugin.lightweightdeploy;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class InternalContext extends ServletContextHandler {

    public InternalContext(HealthCheckRegistry healthCheckRegistry, MetricRegistry metricRegistry) {
        super();

        getServletContext().setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, healthCheckRegistry);
        getServletContext().setAttribute(MetricsServlet.METRICS_REGISTRY, metricRegistry);
    }
}
