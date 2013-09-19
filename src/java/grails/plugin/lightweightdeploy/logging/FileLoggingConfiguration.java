package grails.plugin.lightweightdeploy.logging;

import java.util.Map;

public class FileLoggingConfiguration extends AbstractLoggingConfiguration {

    private String logFilePath;

    public FileLoggingConfiguration(Map<String, ?> config) {
        super(config);
        this.logFilePath = config.get("currentLogFilename").toString();
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

}
