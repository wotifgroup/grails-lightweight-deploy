package grails.plugin.lightweightdeploy.logging;

import com.google.common.base.Objects;

import java.util.Map;

public class FileLoggingConfiguration extends AbstractLoggingConfiguration {

    private String currentLogFilename;
    private boolean archive;
    private String archivedLogFilenamePattern;
    private int archivedFileCount;

    public FileLoggingConfiguration(Map<String, ?> config) {
        super(config);
        this.currentLogFilename = config.get("currentLogFilename").toString();
        this.archive = Objects.firstNonNull((Boolean) config.get("archive"), true);
        this.archivedLogFilenamePattern = (String) config.get("archivedLogFilenamePattern");
        this.archivedFileCount = Objects.firstNonNull((Integer) config.get("archivedFileCount"), 5);
    }

    public boolean isArchive() {
        return archive;
    }

    public String getCurrentLogFilename() {
        return this.currentLogFilename;
    }

    public String getArchivedLogFilenamePattern() {
        return archivedLogFilenamePattern;
    }

    public int getArchivedFileCount() {
        return archivedFileCount;
    }

}
