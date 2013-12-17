package grails.plugin.lightweightdeploy.servlets

import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletResponse
import org.junit.Test

import javax.servlet.*

class ThreadNameFilterTest {

    @Test
    void shouldRenameThread() throws IOException {
        final Filter f = new ThreadNameFilter();

        final String currentName = Thread.currentThread().getName();
        final Boolean wasCalled = false;

        final GrailsMockHttpServletRequest request = new GrailsMockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/hamsterdance.gif")
        request.setQueryString("yes=1");

        final GrailsMockHttpServletResponse response = new GrailsMockHttpServletResponse();
        final FilterChain chain = new FilterChain() {
            @Override
            void doFilter(final ServletRequest sreq, final ServletResponse sres) throws IOException, ServletException {
                wasCalled = true;
                assertEquals(currentName + " - GET /hamsterdance.gif?yes=1", Thread.currentThread().getName());
                throw new ServletException("Fatal");
            }
        };

        try {
            f.doFilter(request, response, chain);
        } catch (ServletException e) {
            assertEquals("Passes exception", "Fatal", e.getMessage());
        }

        assertTrue("Calls filter chain", wasCalled);
        assertEquals("Restores old thread name", currentName, Thread.currentThread().getName());
    }

}
