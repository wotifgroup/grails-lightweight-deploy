package grails.plugin.lightweightdeploy.logging;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static java.util.Collections.unmodifiableList;

public class LoggingConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(LoggingConfiguration.class);

    private List<AbstractLoggingConfiguration> appenderConfigurations = new ArrayList<AbstractLoggingConfiguration>();
    private Level rootLevel = Level.INFO;
    private Map<String, Level> loggers = new HashMap<String, Level>();
    private List<String> trackingCookies = new ArrayList<String>();

    public LoggingConfiguration(Map<String, ?> config) {
        if (config.containsKey("level")) {
            rootLevel = Level.toLevel(config.get("level").toString());
        }
        if (config.containsKey("appenders")) {
            List<Map<String, ?>> appendersConfig = (List<Map<String, ?>>) config.get("appenders");
            for (Map<String, ?> appenderConfig : appendersConfig) {
                appenderConfigurations.add(createAppenderConfiguration(appenderConfig));
            }
        }
        if (config.containsKey("loggers")) {
            for (Map.Entry<String, ?> entry : ((Map<String, ?>) config.get("loggers")).entrySet()) {
                loggers.put(entry.getKey(), Level.toLevel(entry.getValue().toString()));
            }
        }
        if (config.containsKey("cookies")) {
            trackingCookies = (List<String>) config.get("cookies");
        }

        // handle deprecated logging format
        if (config.containsKey("file")) {
            logger.warn("'file' is deprecated - please move to 'appenders' list with type 'file'");
            Map<String, ?> fileConfig = (Map<String, ?>) config.get("file");
            appenderConfigurations.add(new FileLoggingConfiguration(fileConfig));
        }
        if (config.containsKey("console")) {
            logger.warn("'console' is deprecated - please move to 'appenders' list with type 'console'");
            Map<String, ?> consoleConfig = (Map<String, ?>) config.get("console");
            appenderConfigurations.add(new ConsoleLoggingConfiguration(consoleConfig));
        }
        if (config.containsKey("rootLevel")) {
            logger.warn("'rootLevel' is deprecated in favor of 'level'");
            rootLevel = Level.toLevel(config.get("rootLevel").toString());
        }
    }

    public List<AbstractLoggingConfiguration> getAppenderConfigurations() {
        return unmodifiableList(appenderConfigurations);
    }

    static AbstractLoggingConfiguration createAppenderConfiguration(Map<String, ?> appenderConfig) {
        String type = appenderConfig.get("type").toString();
        if (type.equals("file")) {
            return new FileLoggingConfiguration(appenderConfig);
        } else if (type.equals("console")) {
            return new ConsoleLoggingConfiguration(appenderConfig);
        } else if (type.equals("filtered")) {
            return new FilteredLoggingConfiguration(appenderConfig);
        } else {
            throw new IllegalArgumentException("Unknown appender type '" + type + "'");
        }
    }

    public List<String> getTrackingCookies() { return trackingCookies; }

    public Level getRootLevel() {
        return rootLevel;
    }

    public Map<String, Level> getLoggers() {
        return loggers;
    }

    public TimeZone getTimeZone() {
        if (!appenderConfigurations.isEmpty()) {
            return appenderConfigurations.get(0).getTimeZone();
        }
        return TimeZone.getDefault();
    }

}
