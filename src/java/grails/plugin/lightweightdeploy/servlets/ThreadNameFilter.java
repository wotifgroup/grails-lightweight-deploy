package grails.plugin.lightweightdeploy.servlets;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * A servlet filter which adds the request method and URI to the thread name processing the request
 * for the duration of the request.
 */
public class ThreadNameFilter implements Filter {

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // Nothing to do
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) request;
        final Thread current = Thread.currentThread();
        final String oldName = current.getName();
        try {
            current.setName(formatName(req, oldName));
            chain.doFilter(request, response);
        } finally {
            current.setName(oldName);
        }
    }

    private static String formatName(HttpServletRequest req, String oldName) {
        return oldName + " - " + req.getMethod() + ' ' + getFullUrl(req);
    }

    private static String getFullUrl(HttpServletRequest request) {
        final StringBuilder url = new StringBuilder(100).append(request.getRequestURI());
        if (request.getQueryString() != null) {
            url.append('?').append(request.getQueryString());
        }
        return url.toString();
    }

    @Override
    public void destroy() {
        // Nothing to do
    }

}
