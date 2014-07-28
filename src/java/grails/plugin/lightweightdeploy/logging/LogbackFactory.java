package grails.plugin.lightweightdeploy.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.spi.FilterAttachable;
import ch.qos.logback.core.spi.FilterReply;
import com.google.common.collect.ImmutableSet;

import java.util.HashSet;
import java.util.List;
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

    public static OutputStreamAppender<ILoggingEvent> buildFilteredAppender(final FilteredLoggingConfiguration filtered,
                                                                            LoggerContext context) {
        AbstractLoggingConfiguration appenderConfig = filtered.getAppender();
        OutputStreamAppender<ILoggingEvent> appender = buildAppender(appenderConfig, context);
        appender.addFilter(new LogEventFilter(filtered));
        return appender;
    }

    private static void addThresholdFilter(FilterAttachable<ILoggingEvent> appender, Level threshold) {
        final ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel(threshold.toString());
        filter.start();
        appender.addFilter(filter);
    }

    public static Set<OutputStreamAppender<ILoggingEvent>> buildAppenders(LoggingConfiguration configuration,
                                                                          LoggerContext context) {
        final Set<OutputStreamAppender<ILoggingEvent>> appenders = new HashSet<OutputStreamAppender<ILoggingEvent>>();
        List<AbstractLoggingConfiguration> appenderConfigs = configuration.getAppenderConfigurations();
        for (AbstractLoggingConfiguration appenderConfig : appenderConfigs) {
            appenders.add(buildAppender(appenderConfig, context));
        }
        return appenders;
    }

    private static OutputStreamAppender<ILoggingEvent> buildAppender(AbstractLoggingConfiguration appenderConfig,
                                                                     LoggerContext context) {
        if (appenderConfig instanceof ConsoleLoggingConfiguration) {
            return buildConsoleAppender((ConsoleLoggingConfiguration) appenderConfig, context);
        } else if (appenderConfig instanceof FileLoggingConfiguration) {
            return buildFileAppender((FileLoggingConfiguration) appenderConfig, context);
        } else if (appenderConfig instanceof FilteredLoggingConfiguration) {
            return buildFilteredAppender((FilteredLoggingConfiguration) appenderConfig, context);
        } else {
            throw new IllegalArgumentException("Unrecognised appender config type: " + appenderConfig.getClass());
        }
    }

    private static class LogEventFilter extends Filter<ILoggingEvent> {

        private final Set<String> includedLoggerNames;
        private final Set<String> excludedLoggerNames;

        public LogEventFilter(FilteredLoggingConfiguration filtered) {
            includedLoggerNames = ImmutableSet.copyOf(filtered.getInclusions());
            excludedLoggerNames = ImmutableSet.copyOf(filtered.getExclusions());
        }

        @Override
        public FilterReply decide(ILoggingEvent event) {
            String loggerName = event.getLoggerName();

            if (!includedLoggerNames.isEmpty() && !includedLoggerNames.contains(loggerName)) {
                return FilterReply.DENY;
            } else if (excludedLoggerNames.contains(loggerName)) {
                return FilterReply.DENY;
            } else {
                return FilterReply.NEUTRAL;
            }
        }

    }

}
