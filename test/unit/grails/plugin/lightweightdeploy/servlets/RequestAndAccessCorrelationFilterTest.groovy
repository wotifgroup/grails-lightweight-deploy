package grails.plugin.lightweightdeploy.servlets

import org.junit.Test
import org.slf4j.MDC

import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.fest.assertions.api.Assertions.*
import static org.mockito.Mockito.*

class RequestAndAccessCorrelationFilterTest {

    private HttpServletRequest request = mock(HttpServletRequest.class);

    private HttpServletResponse response = mock(HttpServletResponse.class);

    @Test
    public void echoesRequestId() throws Exception {
        final String expected = "foo";

        final Filter f = new RequestAndAccessCorrelationFilter();
        final FilterChain c = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                assertThat(MDC.get("requestId"))
                        .isEqualTo(expected);
            }
        };

        when(request.getHeader(RequestAndAccessCorrelationFilter.X_OPAQUE_ID)).thenReturn(expected);
        when(request.getAttribute(RequestAndAccessCorrelationFilter.START_TIME)).thenReturn(0L);

        f.doFilter(request, response, c);

        verify(response, times(1)).setHeader(RequestAndAccessCorrelationFilter.X_OPAQUE_ID, expected);

        assertThat(MDC.get("requestId"))
                .isEqualTo(expected);
        assertThat(MDC.get("timeTaken"))
                .isNotNull();
    }

    @Test
    public void generatesRequestIdIfNoneGiven() throws Exception {
        final StringBuffer got = new StringBuffer();
        final Filter f = new RequestAndAccessCorrelationFilter();
        final FilterChain c = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                final String requestId = MDC.get("requestId");
                got.append(requestId);

                assertThat(requestId)
                        .isNotEmpty();
            }
        };

        when(request.getAttribute(RequestAndAccessCorrelationFilter.START_TIME)).thenReturn(0L);

        f.doFilter(request, response, c);

        verify(response, times(1)).setHeader(RequestAndAccessCorrelationFilter.X_OPAQUE_ID, got.toString());

        assertThat(MDC.get("requestId"))
                .isNotNull();
        assertThat(MDC.get("timeTaken"))
                .isNotNull();

    }


}
