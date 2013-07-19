package grails.plugin.lightweightdeploy

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.health.HealthCheckRegistry
import com.codahale.metrics.servlet.InstrumentedFilter
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration
import org.eclipse.jetty.webapp.MetaInfConfiguration
import org.eclipse.jetty.webapp.TagLibConfiguration
import org.eclipse.jetty.webapp.WebInfConfiguration
import org.eclipse.jetty.webapp.WebXmlConfiguration
import org.junit.Test

import static junit.framework.Assert.assertEquals
import static junit.framework.Assert.assertTrue

class ExternalContextTest {

    @Test
    void defaultsDescriptorShouldBeSet() {
        assertTrue(externalContext.defaultsDescriptor.matches("/tmp/webdefault[0-9]+?\\.war"))
    }

    @Test
    void healthCheckAttributeShouldBeSet() {
        assertEquals(HealthCheckRegistry.class, externalContext.getAttribute(ExternalContext.HEALTH_CHECK_REGISTRY_SERVLET_ATTRIBUTE).class)
    }

    @Test
    void metricRegistryAttributeShouldBeSet() {
        assertEquals(MetricRegistry.class, externalContext.getAttribute(ExternalContext.METRICS_REGISTRY_SERVLET_ATTRIBUTE).class)
    }

    @Test
    void metricRegistryShouldBeSetForInstrumentedFilter() {
        assertEquals(MetricRegistry.class, externalContext.getAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE).class)
    }

    @Test
    void parentClassLoaderShouldBeUsedFirstToPreserveLogbackSettings() {
        assertTrue(externalContext.parentLoaderPriority)
    }

    @Test
    void requiredConfigurationsShouldBeSet() {
        def configurationClasses = externalContext.configurations.collect { it.class }
        assertTrue(configurationClasses.contains(WebInfConfiguration.class))
        assertTrue(configurationClasses.contains(WebXmlConfiguration.class))
        assertTrue(configurationClasses.contains(MetaInfConfiguration.class))
        assertTrue(configurationClasses.contains(JettyWebXmlConfiguration.class))
        assertTrue(configurationClasses.contains(TagLibConfiguration.class))
    }

    @Test
    void directoryListingShouldBeDisabled() {
        assertEquals("false", externalContext.getInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed"))
    }

    private ExternalContext getExternalContext() {
        new ExternalContext(".", new MetricRegistry(), new HealthCheckRegistry())
    }
}
