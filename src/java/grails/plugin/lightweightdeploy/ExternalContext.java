package grails.plugin.lightweightdeploy;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlet.InstrumentedFilter;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import javax.servlet.DispatcherType;

import grails.plugin.lightweightdeploy.servlets.ThreadNameFilter;
import grails.plugin.lightweightdeploy.servlets.RequestAndAccessCorrelationFilter;

import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.TagLibConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

public class ExternalContext extends WebAppContext {

    public static final String METRICS_REGISTRY_SERVLET_ATTRIBUTE = "metricsRegistry";
    public static final String HEALTH_CHECK_REGISTRY_SERVLET_ATTRIBUTE = "healthCheckRegistry";

    public ExternalContext(String webAppRoot,
                           MetricRegistry metricsRegistry,
                           HealthCheckRegistry healthCheckRegistry,
                           String contextPath) throws IOException {
        super(webAppRoot, contextPath);

        setAttribute(METRICS_REGISTRY_SERVLET_ATTRIBUTE, metricsRegistry);
        setAttribute(HEALTH_CHECK_REGISTRY_SERVLET_ATTRIBUTE, healthCheckRegistry);

        setAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE, metricsRegistry);
        addFilter(ThreadNameFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        addFilter(RequestAndAccessCorrelationFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        addFilter(InstrumentedFilter.class, "/*", EnumSet.allOf(DispatcherType.class));

        setConfigurations(new org.eclipse.jetty.webapp.Configuration[]{new WebInfConfiguration(),
                                                                       new WebXmlConfiguration(),
                                                                       new MetaInfConfiguration(),
                                                                       new JettyWebXmlConfiguration(),
                                                                       new TagLibConfiguration()});

        // Jetty requires a 'defaults descriptor' on the filesystem
		setDefaultsDescriptor(extractWebdefaultXml().getPath());

        //ensure the logback settings we've already configured are re-used in the app.
        setParentLoaderPriority(true);

        //disable the default directory listing
        setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
    }

	protected File extractWebdefaultXml() throws IOException {
		InputStream embeddedWebdefault = getClass().getClassLoader().getResourceAsStream("webdefault.xml");
		File temp = File.createTempFile("webdefault", ".war").getAbsoluteFile();
		temp.getParentFile().mkdirs();
		temp.deleteOnExit();
		ByteStreams.copy(embeddedWebdefault, new FileOutputStream(temp));
		return temp;
	}
}
