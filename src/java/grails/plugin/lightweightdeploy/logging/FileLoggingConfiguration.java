package grails.plugin.lightweightdeploy.logging;

import ch.qos.logback.classic.Level;
import com.google.common.base.Optional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class FileLoggingConfiguration {

    private String logFilePath;
    private TimeZone logFileTimeZone = TimeZone.getDefault();
    private Level loggingThreshold = Level.ALL;
    private Level rootLevel = Level.INFO;
    private Map<String, Level> loggers = new HashMap<String, Level>();



    public FileLoggingConfiguration(String logFilePath) {
        this.logFilePath = logFilePath;
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
        //TODO: support custom log format
        return Optional.absent();
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

    public void setLoggers(Map<String, Level> loggers) {
        this.loggers = loggers;
    }
}
