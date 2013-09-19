package grails.plugin.lightweightdeploy.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.spi.FilterAttachable;

import java.util.HashSet;
import java.util.Set;

/**
 * Borrowed heavily from com.yammer.dropwizard.logging.LogbackFactory.
 */
public class LogbackFactory {

    private LogbackFactory() { /* singleton */ }

    // TODO: duplication with below method
    public static ConsoleAppender<ILoggingEvent> buildConsoleAppender(ConsoleLoggingConfiguration config,
                                                                      LoggerContext context) {
        final LogFormatter formatter = new LogFormatter(context, config.getTimeZone());
        for (String format : config.getLogFormat().asSet()) {
            formatter.setPattern(format);
        }
        formatter.start();

        final ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<ILoggingEvent>();
        appender.setContext(context);
        appender.setLayout(formatter);
        addThresholdFilter(appender, config.getThreshold());
        appender.start();

        return appender;

    }

    public static FileAppender<ILoggingEvent> buildFileAppender(FileLoggingConfiguration file,
                                                                LoggerContext context) {
        final LogFormatter formatter = new LogFormatter(context, file.getTimeZone());
        for (String format : file.getLogFormat().asSet()) {
            formatter.setPattern(format);
        }
        formatter.start();

        final FileAppender<ILoggingEvent> appender =
                file.isArchive() ? new RollingFileAppender<ILoggingEvent>() :
                        new FileAppender<ILoggingEvent>();

        appender.setAppend(true);
        appender.setContext(context);
        appender.setLayout(formatter);
        appender.setFile(file.getCurrentLogFilename());
        appender.setPrudent(false);

        addThresholdFilter(appender, file.getThreshold());

        if (file.isArchive()) {

            final DefaultTimeBasedFileNamingAndTriggeringPolicy<ILoggingEvent> triggeringPolicy =
                    new DefaultTimeBasedFileNamingAndTriggeringPolicy<ILoggingEvent>();
            triggeringPolicy.setContext(context);

            final TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<ILoggingEvent>();
            rollingPolicy.setContext(context);
            rollingPolicy.setFileNamePattern(file.getArchivedLogFilenamePattern());
            rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(
                    triggeringPolicy);
            triggeringPolicy.setTimeBasedRollingPolicy(rollingPolicy);
            rollingPolicy.setMaxHistory(file.getArchivedFileCount());

            ((RollingFileAppender<ILoggingEvent>) appender).setRollingPolicy(rollingPolicy);
            ((RollingFileAppender<ILoggingEvent>) appender).setTriggeringPolicy(triggeringPolicy);

            rollingPolicy.setParent(appender);
            rollingPolicy.start();
        }

        appender.stop();
        appender.start();

        return appender;
    }

    private static void addThresholdFilter(FilterAttachable<ILoggingEvent> appender, Level threshold) {
        final ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel(threshold.toString());
        filter.start();
        appender.addFilter(filter);
    }

    public static Set<OutputStreamAppender<ILoggingEvent>> buildAppenders(LoggingConfiguration configuration, LoggerContext context) {
        final Set<OutputStreamAppender<ILoggingEvent>> appenders = new HashSet<OutputStreamAppender<ILoggingEvent>>();
        if (configuration.hasFileConfiguration()) {
            appenders.add(buildFileAppender(configuration.getFileConfiguration(), context));
        }
        if (configuration.hasConsoleConfiguration()) {
            appenders.add(buildConsoleAppender(configuration.getConsoleConfiguration(), context));
        }
        return appenders;
    }
}
