package grails.plugin.lightweight.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;

public class RequestLogLayout extends LayoutBase<ILoggingEvent> {
    @Override
    public String doLayout(ILoggingEvent event) {
        return event.getFormattedMessage() + CoreConstants.LINE_SEPARATOR;
    }
}
