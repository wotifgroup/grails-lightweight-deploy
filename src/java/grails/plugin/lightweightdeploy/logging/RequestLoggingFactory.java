package grails.plugin.lightweightdeploy.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import com.codahale.metrics.Clock;
import grails.plugin.lightweightdeploy.Configuration;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.slf4j.LoggerFactory;

public class RequestLoggingFactory {

    private final Configuration config;

    public RequestLoggingFactory(Configuration config) {
        this.config = config;
    }

    public Handler configure() {

        final Logger logger = (Logger) LoggerFactory.getLogger("http.request");
        logger.setAdditive(false);

        final LoggerContext context = logger.getLoggerContext();

        final AppenderAttachableImpl<ILoggingEvent> appenders = new AppenderAttachableImpl<ILoggingEvent>();

        final RequestLogLayout layout = new RequestLogLayout();
        layout.start();

        for (OutputStreamAppender<ILoggingEvent> appender : LogbackFactory.buildAppenders(config.getRequestLogConfiguration(),
                context)) {
            appender.stop();
            appender.setLayout(layout);
            appender.start();
            appenders.addAppender(appender);
        }

        final RequestLogHandler handler = new RequestLogHandler();
        handler.setRequestLog(new AsyncRequestLog(Clock.defaultClock(),
                appenders,
                config.getRequestLogConfiguration().getTimeZone()));

        return handler;

    }

}
