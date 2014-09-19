package grails.plugin.lightweightdeploy.connector

import com.google.common.collect.ImmutableSet
import org.eclipse.jetty.http.HttpHeaders
import org.eclipse.jetty.http.HttpMethods
import org.junit.Test

import java.util.zip.Deflater

import static org.junit.Assert.*

class GzipConfigurationTest {

    GzipConfiguration defaultConfiguration = new GzipConfiguration()
    GzipConfiguration nonDefaultConfiguration = new GzipConfiguration([
            enabled: false,
            minimumEntitySize: 1024,
            bufferSize: 16 * 1024,
            excludedUserAgents: [
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)",
                    "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0 )",
                    "Mozilla/4.0 (compatible; MSIE 5.5; Windows 98; Win 9x 4.90)"
            ],
            compressedMimeTypes: ["application/json", "application/xml"],
            includedMethods: [HttpMethods.GET, HttpMethods.POST, HttpMethods.POST],
            excludedUserAgentPatterns: [".*MSIE.*", ".*Opera.*"],
            gzipCompatibleDeflation: false,
            vary: "${HttpHeaders.ACCEPT_ENCODING}, ${HttpHeaders.ACCEPT_CHARSET}".toString(),
            deflateCompressionLevel: Deflater.BEST_SPEED
    ])

    @Test
    void enabledShouldDefault() {
        assertTrue(defaultConfiguration.enabled)
    }

    @Test
    void minimumEntitySizeShouldDefault() {
        assertEquals(256, defaultConfiguration.minimumEntitySize)
    }

    @Test
    void bufferSizeShouldDefault() {
        assertEquals(8 * 1024, defaultConfiguration.bufferSize)
    }

    @Test
    void excludedUserAgentsShouldDefault() {
        assertNull(defaultConfiguration.excludedUserAgents)
    }

    @Test
    void compressedMimeTypesShouldDefault() {
        assertNull(defaultConfiguration.compressedMimeTypes)
    }

    @Test
    void includedMethodShouldDefault() {
        assertNull(defaultConfiguration.includedMethods)
    }

    @Test
    void excludedUserAgentPatternsShouldDefault() {
        assertNull(defaultConfiguration.excludedUserAgentPatterns)
    }

    @Test
    void gzipCompatibleDeflationShouldDefault() {
        assertTrue(defaultConfiguration.gzipCompatibleDeflation)
    }

    @Test
    void varyShouldDefault() {
        assertEquals(HttpHeaders.ACCEPT_ENCODING, defaultConfiguration.vary)
    }

    @Test
    void deflateCompressionLevelShouldDefault() {
        assertEquals(Deflater.DEFAULT_COMPRESSION, defaultConfiguration.deflateCompressionLevel)
    }

    @Test
    void shouldSetEnabledFromConfig() {
        assertFalse(nonDefaultConfiguration.enabled)
    }

    @Test
    void shouldSetMinimumEntitySizeFromConfig() {
        assertEquals(1024, nonDefaultConfiguration.minimumEntitySize)
    }

    @Test
    void shouldSetBufferSizeFromConfig() {
        assertEquals(16 * 1024, nonDefaultConfiguration.bufferSize)
    }

    @Test
    void shouldSetExcludedUserAgentsFromConfig() {
        assertEquals(
                ImmutableSet.of("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)", "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0 )", "Mozilla/4.0 (compatible; MSIE 5.5; Windows 98; Win 9x 4.90)"),
                nonDefaultConfiguration.excludedUserAgents
        )
    }

    @Test
    void shouldSetCompressedMimeTypesFromConfig() {
        assertEquals(
                ImmutableSet.of("application/json", "application/xml"),
                nonDefaultConfiguration.compressedMimeTypes
        )
    }

    @Test
    void shouldSetIncludedMethodsFromConfig() {
        assertEquals(
                ImmutableSet.of(HttpMethods.GET, HttpMethods.POST, HttpMethods.POST),
                nonDefaultConfiguration.includedMethods
        )
    }

    @Test
    void shouldSetExcludedUserAgentPatternsFromConfig() {
        assertEquals(
                [".*MSIE.*", ".*Opera.*"],
                nonDefaultConfiguration.excludedUserAgentPatterns*.pattern().sort()
        )
    }

    @Test
    void shouldSetGzipCompatibleDeflactionFromConfig() {
        assertFalse(nonDefaultConfiguration.gzipCompatibleDeflation)
    }

    @Test
    void shouldSetVaryFromConfig() {
        assertEquals("${HttpHeaders.ACCEPT_ENCODING}, ${HttpHeaders.ACCEPT_CHARSET}".toString(), nonDefaultConfiguration.vary)
    }

    @Test
    void shouldSetDeflateCompressionLevelFromConfig() {
        assertEquals(Deflater.BEST_SPEED, nonDefaultConfiguration.deflateCompressionLevel)
    }

}
