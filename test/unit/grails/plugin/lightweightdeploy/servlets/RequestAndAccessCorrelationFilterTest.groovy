package grails.plugin.lightweightdeploy.servlets

import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletResponse
import org.junit.Test
import org.slf4j.MDC

import javax.servlet.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RequestAndAccessCorrelationFilterTest {

    @Test
    void testAddRandomRequestIdToMDC() throws IOException {
        final Filter filter = new RequestAndAccessCorrelationFilter()

        final GrailsMockHttpServletRequest request = new GrailsMockHttpServletRequest()
        request.setMethod("GET")
        request.setRequestURI("/hotel/view/1")
        request.setQueryString("adults=2")

        final GrailsMockHttpServletResponse response = new GrailsMockHttpServletResponse()

        ExecutorService pool = Executors.newSingleThreadExecutor()

        // force thread initialisation in the main thread context, so MDC does not inherits a copy of its parent
        pool.submit(new Runnable() { void run() {} }).get();

        final FilterChain chain = new FilterChain() {
            @Override
            void doFilter(final ServletRequest req, final ServletResponse resp) throws IOException, ServletException {

                // MDC should be set on this thread during filter chain processing
                assert MDC.get("requestId") != null

                // But should not be set on other threads
                pool.submit(new Runnable() {
                    public void run() {
                        assert MDC.get("requestId") == null
                    }
                }).get()
            }
        };

        assert MDC.get("requestId") == null // MDC should not be empty prior execution
        filter.doFilter(request, response, chain)
        assert MDC.get("requestId") == null // MDC should clean itself up

        pool.shutdown();
    }


}
