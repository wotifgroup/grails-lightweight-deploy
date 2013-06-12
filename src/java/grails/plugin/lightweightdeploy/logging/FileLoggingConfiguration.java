package grails.plugin.lightweightdeploy.logging;

import ch.qos.logback.classic.Level;
import com.google.common.base.Optional;
import java.util.TimeZone;

public class FileLoggingConfiguration {

    private String logFilePath;
    private TimeZone logFileTimeZone = TimeZone.getDefault();
    private Level loggingThreshold = Level.INFO;

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

    public void setLogFileTimeZone(TimeZone logFileTimeZone) {
        this.logFileTimeZone = logFileTimeZone;
    }

    public void setLoggingThreshold(Level loggingThreshold) {
        this.loggingThreshold = loggingThreshold;
    }
}
