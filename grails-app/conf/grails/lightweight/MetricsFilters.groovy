package grails.lightweight

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Timer
import grails.plugin.lightweightdeploy.ExternalContext
import org.codehaus.groovy.grails.web.context.ServletContextHolder

import static com.codahale.metrics.MetricRegistry.name

class MetricsFilters {

    public static final ThreadLocal<Timer.Context> timerContextThreadLocal = new ThreadLocal<Timer.Context>()

    def filters = {
        all(controller:'*', action:'*') {
            before = {
                try {
                    Timer timer = metricRegistry.timer(name(controllerName, actionName?:"defaultAction"))
                    Timer.Context context = timer.time();
                    timerContextThreadLocal.set(context)
                } catch (Throwable e) {
                    log.warn("There was an error starting measurement", e)
                }
            }
            afterView = { Exception e ->
                try {
                    if (timerContextThreadLocal.get() != null) {
                        timerContextThreadLocal.get().stop()
                        timerContextThreadLocal.remove()
                    }
                } catch (Throwable metricsE) {
                    log.warn("There was an error stopping measurement", e)
                }
            }
        }
    }

    protected MetricRegistry getMetricRegistry() {
        ServletContextHolder.servletContext.getAttribute(ExternalContext.METRICS_REGISTRY_SERVLET_ATTRIBUTE)
    }
}
