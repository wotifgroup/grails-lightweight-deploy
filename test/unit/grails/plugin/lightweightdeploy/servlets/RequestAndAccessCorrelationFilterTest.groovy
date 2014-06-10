package grails.plugin.lightweightdeploy.servlets

import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletResponse
import org.junit.Test
import org.slf4j.MDC

import javax.servlet.*
import javax.servlet.http.HttpServletResponse

import static org.fest.assertions.api.Assertions.assertThat

class RequestAndAccessCorrelationFilterTest {

    @Test
    public void echoesRequestId() throws Exception {
        final GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest();
        final GrailsMockHttpServletResponse mockResponse = new GrailsMockHttpServletResponse();
        final String expected = "foo";

        mockRequest.setRemoteAddr("127.0.0.1");
        mockRequest.addHeader(RequestAndAccessCorrelationFilter.X_OPAQUE_ID, expected);

        final Filter f = new RequestAndAccessCorrelationFilter();
        final FilterChain c = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                assertThat(((HttpServletResponse) response).getHeader(RequestAndAccessCorrelationFilter.X_OPAQUE_ID))
                        .isEqualTo(expected);
                assertThat(MDC.get(RequestAndAccessCorrelationFilter.REQUEST_ID))
                        .isEqualTo(expected);
            }
        };

        f.doFilter(mockRequest, mockResponse, c);

        assertThat(((HttpServletResponse) mockResponse).getHeader(RequestAndAccessCorrelationFilter.X_OPAQUE_ID))
                .isEqualTo(expected);
        assertThat(MDC.get(RequestAndAccessCorrelationFilter.REQUEST_ID))
                .isEqualTo(expected);
    }

    @Test
    public void generatesRequestIdIfNoneGiven() throws Exception {
        final GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest();
        final GrailsMockHttpServletResponse mockResponse = new GrailsMockHttpServletResponse();

        mockRequest.setRemoteAddr("127.0.0.1");

        final StringBuffer got = new StringBuffer();
        final Filter f = new RequestAndAccessCorrelationFilter();
        final FilterChain c = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                final String requestId = MDC.get(RequestAndAccessCorrelationFilter.REQUEST_ID);
                got.append(requestId);

                assertThat(((HttpServletResponse) response).getHeader(RequestAndAccessCorrelationFilter.X_OPAQUE_ID))
                        .isNotNull();
                assertThat(requestId)
                        .isNotNull();
            }
        };

        f.doFilter(mockRequest, mockResponse, c);

        assertThat(((HttpServletResponse) mockResponse).getHeader(RequestAndAccessCorrelationFilter.X_OPAQUE_ID))
                .isNotNull();
        assertThat(MDC.get(RequestAndAccessCorrelationFilter.REQUEST_ID))
                .isNotNull();
    }

    @Test
    public void generatesRequestIfNotLocalRequestAndDoesntSendItBack() throws Exception {
        final GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest();
        final GrailsMockHttpServletResponse mockResponse = new GrailsMockHttpServletResponse();

        final String notExpected = "foo";

        mockRequest.setRemoteAddr("8.8.8.8");
        mockRequest.addHeader(RequestAndAccessCorrelationFilter.X_OPAQUE_ID, notExpected);

        final StringBuffer got = new StringBuffer();
        final Filter f = new RequestAndAccessCorrelationFilter();
        final FilterChain c = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                final String requestId = MDC.get(RequestAndAccessCorrelationFilter.REQUEST_ID);
                got.append(requestId);

                assertThat(((HttpServletResponse) response).getHeader(RequestAndAccessCorrelationFilter.X_OPAQUE_ID))
                        .isNull();
                assertThat(requestId)
                        .isNotEqualTo(notExpected);
            }
        };

        f.doFilter(mockRequest, mockResponse, c);

        assertThat(((HttpServletResponse) mockResponse).getHeader(RequestAndAccessCorrelationFilter.X_OPAQUE_ID))
                .isNull();
        assertThat(MDC.get(RequestAndAccessCorrelationFilter.REQUEST_ID))
                .isNotEqualTo(notExpected);
    }

    @Test
    public void generatesRequestIfCrapRequestAndDoesntSendItBack() throws Exception {
        final GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest();
        final GrailsMockHttpServletResponse mockResponse = new GrailsMockHttpServletResponse();

        final String notExpected = "foo";

        mockRequest.setRemoteAddr("unknown");
        mockRequest.addHeader(RequestAndAccessCorrelationFilter.X_OPAQUE_ID, notExpected);

        final StringBuffer got = new StringBuffer();
        final Filter f = new RequestAndAccessCorrelationFilter();
        final FilterChain c = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                final String requestId = MDC.get(RequestAndAccessCorrelationFilter.REQUEST_ID);
                got.append(requestId);

                assertThat(((HttpServletResponse) response).getHeader(RequestAndAccessCorrelationFilter.X_OPAQUE_ID))
                        .isNull();
                assertThat(requestId)
                        .isNotEqualTo(notExpected);
            }
        };

        f.doFilter(mockRequest, mockResponse, c);

        assertThat(((HttpServletResponse) mockResponse).getHeader(RequestAndAccessCorrelationFilter.X_OPAQUE_ID))
                .isNull();
        assertThat(MDC.get(RequestAndAccessCorrelationFilter.REQUEST_ID))
                .isNotEqualTo(notExpected);
    }

}
