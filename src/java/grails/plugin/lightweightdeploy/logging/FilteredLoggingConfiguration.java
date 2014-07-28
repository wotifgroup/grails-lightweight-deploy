package grails.plugin.lightweightdeploy.logging;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * Extends the DropWizard-like log config to allow direction of certain log output to certain appenders.
 * <p/>
 * Wraps another appender and filters the log messages it receives.
 * If 'includes' is specified, only logs on loggers whose name is in that comma-separated list will be allowed.
 * If 'excludes' is specified, any logs on loggers whose name is in that comma-separated list will be excluded.
 */
public class FilteredLoggingConfiguration extends AbstractLoggingConfiguration {

    private AbstractLoggingConfiguration appender;
    private Set<String> inclusions = new HashSet<String>();
    private Set<String> exclusions = new HashSet<String>();

    public FilteredLoggingConfiguration(Map<String, ?> config) {
        super(config);

        if (config.containsKey("appender")) {
            Map<String, ?> appenderConfig = (Map<String, ?>) config.get("appender");
            setAppender(LoggingConfiguration.createAppenderConfiguration(appenderConfig));
        } else {
            throw new IllegalArgumentException("Must provide an 'appender' for a filtered appender");
        }

        if (config.containsKey("includes")) {
            List<String> inclusions = (List<String>) config.get("includes");
            for (String inclusion : inclusions) {
                addInclusion(inclusion);
            }
        }
        if (config.containsKey("excludes")) {
            List<String> exclusions = (List<String>) config.get("excludes");
            for (String exclusion : exclusions) {
                addExclusion(exclusion);
            }
        }
    }

    public AbstractLoggingConfiguration getAppender() {
        return appender;
    }

    public Set<String> getInclusions() {
        return unmodifiableSet(inclusions);
    }

    public Set<String> getExclusions() {
        return unmodifiableSet(exclusions);
    }

    public void setAppender(AbstractLoggingConfiguration appender) {
        this.appender = appender;
    }

    public void addInclusion(String s) {
        inclusions.add(s);
    }

    public void addExclusion(String s) {
        exclusions.add(s);
    }

}
