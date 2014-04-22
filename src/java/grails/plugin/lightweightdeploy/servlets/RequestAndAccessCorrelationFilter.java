package grails.plugin.lightweightdeploy.servlets;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

public class RequestAndAccessCorrelationFilter implements Filter {

    @VisibleForTesting
    static final String START_TIME = RequestAndAccessCorrelationFilter.class.getName() + ".startTime";

    @VisibleForTesting
    static final String X_OPAQUE_ID = "X-Opaque-ID";

    private static final Pattern NEW_LINES = Pattern.compile("[\r\n]");

    private final IdGenerator idGenerator;

    public RequestAndAccessCorrelationFilter() {
        this.idGenerator = new IdGenerator();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { /* unused */ }

    @Override
    public void destroy() { /* unused */ }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException,
            ServletException {
        request.setAttribute(START_TIME, System.currentTimeMillis());

        MDC.clear();

        if (request instanceof HttpServletRequest) {
            final HttpServletRequest httpRequest = (HttpServletRequest) request;
            final HttpServletResponse httpResponse = (HttpServletResponse) response;

            String requestId = httpRequest.getHeader(X_OPAQUE_ID);
            if (Strings.isNullOrEmpty(requestId)) {
                requestId = idGenerator.generate();
            } else {
                // Protect against HTTP response splitting.
                requestId = NEW_LINES.matcher(requestId).replaceAll("");
            }

            MDC.put("requestId", requestId);
            request.setAttribute("requestId", requestId);

            httpResponse.setHeader(X_OPAQUE_ID, requestId);
        }

        chain.doFilter(request, response);

        long startTime = (Long) request.getAttribute(START_TIME);
        final long duration = System.currentTimeMillis() - startTime;
        MDC.put("timeTaken", String.valueOf(duration));
    }

}
