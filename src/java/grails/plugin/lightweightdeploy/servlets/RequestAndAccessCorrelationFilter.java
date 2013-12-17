package grails.plugin.lightweightdeploy.servlets;

import org.slf4j.MDC;

import javax.servlet.*;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class RequestAndAccessCorrelationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { /* unused */ }

    @Override
    public void destroy() { /* unused */ }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String requestId =  String.format("%016x", ThreadLocalRandom.current().nextLong());
        MDC.put("requestId", requestId);
        request.setAttribute("requestId", requestId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("requestId");
        }
    }

}
