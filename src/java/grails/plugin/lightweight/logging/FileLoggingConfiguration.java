package grails.plugin.lightweight.logging;

import ch.qos.logback.classic.Level;
import com.google.common.base.Optional;
import java.util.TimeZone;

public interface FileLoggingConfiguration {

    TimeZone getTimeZone();

    boolean isArchive();

    String getCurrentLogFilename();

    Level getThreshold();

    String getArchivedLogFilenamePattern();

    int getArchivedFileCount();

    Optional<String> getLogFormat();

    boolean isFileLoggingEnabled();
}
