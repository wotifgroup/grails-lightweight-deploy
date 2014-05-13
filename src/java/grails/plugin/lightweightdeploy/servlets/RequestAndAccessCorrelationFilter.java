package grails.plugin.lightweightdeploy.servlets;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterators;
import com.google.common.net.InetAddresses;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.regex.Pattern;

public class RequestAndAccessCorrelationFilter implements Filter {

    @VisibleForTesting
    static final String X_OPAQUE_ID = "X-Opaque-ID";
    @VisibleForTesting
    static final String REQUEST_ID = "requestId";

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
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            final boolean siteLocal = isSiteLocalRequest(request);

            final String requestId = buildRequestId(siteLocal, (HttpServletRequest) request);

            MDC.put(REQUEST_ID, requestId);
            request.setAttribute(REQUEST_ID, requestId);
            // Response headers must be set prior to handling request as Transfer-Encoding is "chunked"
            sendRequestId(siteLocal, (HttpServletResponse) response, requestId);
        }

        chain.doFilter(request, response);
    }

    private boolean isSiteLocalRequest(final ServletRequest request) {
        InetAddress inetAddress = InetAddresses.forString(request.getRemoteAddr());
        return inetAddress.isLoopbackAddress() || inetAddress.isSiteLocalAddress();
    }

    // TODO: Move this to common request handler library
    private String buildRequestId(final boolean siteLocal, final HttpServletRequest httpRequest) {
        final Iterator<String> headers = Iterators.forEnumeration(httpRequest.getHeaderNames());
        if (siteLocal && Iterators.contains(headers, X_OPAQUE_ID)) {
            final String requestId = httpRequest.getHeader(X_OPAQUE_ID);
            return NEW_LINES.matcher(requestId).replaceAll("");
        }
        return idGenerator.generate();
    }

    // TODO: Move this to common request handler library
    private void sendRequestId(final boolean siteLocal, final HttpServletResponse httpResponse, final String requestId) {
        if (siteLocal) {
            httpResponse.setHeader(X_OPAQUE_ID, requestId);
        }
    }

}
