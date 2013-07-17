package grails.plugin.lightweightdeploy.logging;

import ch.qos.logback.classic.Level;
import com.google.common.base.Optional;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class FileLoggingConfiguration {

    private String logFilePath;
    private TimeZone logFileTimeZone = TimeZone.getDefault();
    private Level loggingThreshold = Level.ALL;
    private Level rootLevel = Level.INFO;
    private Map<String, Level> loggers = new HashMap<String, Level>();
    private String logFormat;

    public FileLoggingConfiguration(Map<String, ?> config) {
        this.logFilePath = config.get("currentLogFilename").toString();
        if (config.containsKey("timeZone")) {
            setLogFileTimeZone(TimeZone.getTimeZone(config.get("timeZone").toString()));
        }
        if (config.containsKey("threshold")) {
            setLoggingThreshold(Level.toLevel(config.get("threshold").toString()));
        }
        if (config.containsKey("rootLevel")) {
            setRootLevel(Level.toLevel(config.get("rootLevel").toString()));
        }
        if (config.containsKey("loggers")) {
            for (Map.Entry<String, ?> entry : ((Map<String, ?>) config.get("loggers")).entrySet()) {
                addLogger(entry.getKey(), Level.toLevel(entry.getValue().toString()));
            }
        }
        if (config.containsKey("logFormat")) {
            setLogFormat(config.get("logFormat").toString());
        }
    }

    public void addLogger(String packagePath, Level level) {
        this.loggers.put(packagePath, level);
    }

    public Level getThreshold() {
        return loggingThreshold;
    }

    public TimeZone getTimeZone() {
        return this.logFileTimeZone;
    }
    
    public boolean isArchive() {
        //not currently supported because of bug in logback's file rolling.
        return false;
    }
    
    public String getCurrentLogFilename() {
        return this.logFilePath;
    }
    
    public String getArchivedLogFilenamePattern() {
        //not currently supported because of bug in logback's file rolling.
        return null;
    }
    
    public int getArchivedFileCount() {
        //not currently supported because of bug in logback's file rolling.
        return 0;
    }

    public Optional<String> getLogFormat() {
        return Optional.fromNullable(logFormat);
    }

    public Level getRootLevel() {
        return this.rootLevel;
    }

    public Map<String, Level> getLoggers() {
        return loggers;
    }

    public void setLogFileTimeZone(TimeZone logFileTimeZone) {
        this.logFileTimeZone = logFileTimeZone;
    }

    public void setLoggingThreshold(Level loggingThreshold) {
        this.loggingThreshold = loggingThreshold;
    }

    public void setRootLevel(Level rootLevel) {
        this.rootLevel = rootLevel;
    }

    public void setLogFormat(String logFormat) {
        this.logFormat = logFormat;
    }
}
