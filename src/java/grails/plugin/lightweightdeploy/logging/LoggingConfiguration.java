package grails.plugin.lightweightdeploy.logging;

import ch.qos.logback.classic.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class LoggingConfiguration {

    private FileLoggingConfiguration fileConfiguration;
    private ConsoleLoggingConfiguration consoleConfiguration;
    private Level rootLevel = Level.INFO;
    private Map<String, Level> loggers = new HashMap<String, Level>();

    public LoggingConfiguration(Map<String, ?> config) {
        if (config.containsKey("file")) {
            Map<String, ?> fileConfig = (Map<String, ?>) config.get("file");
            this.fileConfiguration = new FileLoggingConfiguration(fileConfig);
        }
        if (config.containsKey("console")) {
            Map<String, ?> consoleConfig = (Map<String, ?>) config.get("console");
            this.consoleConfiguration = new ConsoleLoggingConfiguration(consoleConfig);
        }
        if (config.containsKey("rootLevel")) {
            rootLevel = Level.toLevel(config.get("rootLevel").toString());
        }
        if (config.containsKey("loggers")) {
            for (Map.Entry<String, ?> entry : ((Map<String, ?>) config.get("loggers")).entrySet()) {
                loggers.put(entry.getKey(), Level.toLevel(entry.getValue().toString()));
            }
        }
    }


    public FileLoggingConfiguration getFileConfiguration() {
        return fileConfiguration;
    }

    public ConsoleLoggingConfiguration getConsoleConfiguration() {
        return consoleConfiguration;
    }

    public Level getRootLevel() {
        return rootLevel;
    }

    public Map<String, Level> getLoggers() {
        return loggers;
    }

    public boolean hasFileConfiguration() {
        return fileConfiguration != null;
    }

    public boolean hasConsoleConfiguration() {
        return consoleConfiguration != null;
    }

    public TimeZone getTimeZone() {

        if (hasConsoleConfiguration()) {
            return consoleConfiguration.getTimeZone();
        } else if (hasFileConfiguration()) {
            return fileConfiguration.getTimeZone();
        }

        return TimeZone.getDefault();

    }

}
