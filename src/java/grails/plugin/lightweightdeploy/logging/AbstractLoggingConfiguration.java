package grails.plugin.lightweightdeploy.logging;

import ch.qos.logback.classic.Level;
import com.google.common.base.Optional;

import java.util.Map;
import java.util.TimeZone;

public abstract class AbstractLoggingConfiguration {

    private TimeZone timeZone = TimeZone.getDefault();
    private Level loggingThreshold = Level.ALL;
    private String logFormat;

    public AbstractLoggingConfiguration(Map<String, ?> config) {
        if (config.containsKey("timeZone")) {
            setTimeZone(TimeZone.getTimeZone(config.get("timeZone").toString()));
        }
        if (config.containsKey("threshold")) {
            setLoggingThreshold(Level.toLevel(config.get("threshold").toString()));
        }
        if (config.containsKey("logFormat")) {
            setLogFormat(config.get("logFormat").toString());
        }

        // handle deprecated logging format
        if (config.containsKey("rootLevel")) {
            throw new IllegalArgumentException(
                    "As of lightweight-deploy versions > 0.9.0 rootLevel is now set one level up under 'logging'.");
        }
        if (config.containsKey("loggers")) {
            throw new IllegalArgumentException(
                    "As of lightweight-deploy versions > 0.9.0 loggers is now set one level up under 'logging'.");
        }

    }

    public Level getThreshold() {
        return loggingThreshold;
    }

    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    public Optional<String> getLogFormat() {
        return Optional.fromNullable(logFormat);
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public void setLoggingThreshold(Level loggingThreshold) {
        this.loggingThreshold = loggingThreshold;
    }

    public void setLogFormat(String logFormat) {
        this.logFormat = logFormat;
    }

}
