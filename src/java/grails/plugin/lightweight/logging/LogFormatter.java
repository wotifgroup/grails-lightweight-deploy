package grails.plugin.lightweight.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import java.util.TimeZone;

/**
 * Borrowed heavily from com.yammer.dropwizard.logging.LogFormatter.
 */
public class LogFormatter extends PatternLayout {

    public LogFormatter(LoggerContext context, TimeZone timeZone) {
        super();
        setOutputPatternAsHeader(false);
        getDefaultConverterMap().put("ex", PrefixedThrowableProxyConverter.class.getName());
        getDefaultConverterMap().put("xEx", PrefixedExtendedThrowableProxyConverter.class.getName());
        setPattern("%-5p [%d{ISO8601," + timeZone.getID() + "}] %c: %m%n%xEx");
        setContext(context);
    }
}
