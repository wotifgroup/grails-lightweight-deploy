package grails.plugin.lightweightdeploy.connector;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.eclipse.jetty.http.HttpHeaders;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.Deflater;

public class GzipConfiguration {

    private boolean enabled = true;
    private int minimumEntitySize = 256;
    private int bufferSize = 8 * 1024;
    private Set<String> excludedUserAgents;
    private Set<String> compressedMimeTypes;
    private Set<String> includedMethods;
    private Set<Pattern> excludedUserAgentPatterns;
    private boolean gzipCompatibleDeflation = true;
    private String vary = HttpHeaders.ACCEPT_ENCODING;
    private int deflateCompressionLevel = Deflater.DEFAULT_COMPRESSION;

    public GzipConfiguration() { }

    public GzipConfiguration(final Map<String, ?> gzipConfig) {
        this.enabled = Objects.firstNonNull((Boolean) gzipConfig.get("enabled"), enabled);
        this.minimumEntitySize = Objects.firstNonNull((Integer) gzipConfig.get("minimumEntitySize"), minimumEntitySize);
        this.bufferSize = Objects.firstNonNull((Integer) gzipConfig.get("bufferSize"), bufferSize);
        this.excludedUserAgents = Optional.fromNullable(extractConfiguredStringSet(gzipConfig, "excludedUserAgents")).or(Optional.fromNullable(excludedUserAgents)).orNull();
        this.compressedMimeTypes = Optional.fromNullable(extractConfiguredStringSet(gzipConfig, "compressedMimeTypes")).or(Optional.fromNullable(compressedMimeTypes)).orNull();
        this.includedMethods = Optional.fromNullable(extractConfiguredStringSet(gzipConfig, "includedMethods")).or(Optional.fromNullable(includedMethods)).orNull();
        this.excludedUserAgentPatterns = Optional.fromNullable(extractConfiguredPatternSet(gzipConfig, "excludedUserAgentPatterns")).or(Optional.fromNullable(excludedUserAgentPatterns)).orNull();
        this.gzipCompatibleDeflation = Objects.firstNonNull((Boolean) gzipConfig.get("gzipCompatibleDeflation"), gzipCompatibleDeflation);
        this.vary = Objects.firstNonNull((String) gzipConfig.get("vary"), vary);
        this.deflateCompressionLevel = Objects.firstNonNull((Integer) gzipConfig.get("deflateCompressionLevel"), deflateCompressionLevel);
    }

    @SuppressWarnings("unchecked")
    private List<String> extractConfiguredStringList(final Map<String, ?> config, final String key) {
        return (List<String>) config.get(key);
    }

    private Set<String> extractConfiguredStringSet(final Map<String, ?> config, final String key) {
        List<String> values = extractConfiguredStringList(config, key);
        return values == null ? null : ImmutableSet.copyOf(values);
    }

    private Set<Pattern> extractConfiguredPatternSet(final Map<String, ?> config, final String key) {
        List<String> values = extractConfiguredStringList(config, key);
        return values == null ? null : ImmutableSet.copyOf(Lists.transform(values, new Function<String, Pattern>() {
            @Override
            public Pattern apply(String input) {
                return Pattern.compile(input);
            }
        }));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMinimumEntitySize() {
        return minimumEntitySize;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getDeflateCompressionLevel() {
        return deflateCompressionLevel;
    }

    public String getVary() {
        return vary;
    }

    public Set<String> getExcludedUserAgents() {
        return excludedUserAgents;
    }

    public Set<String> getCompressedMimeTypes() {
        return compressedMimeTypes;
    }

    public Set<String> getIncludedMethods() {
        return includedMethods;
    }

    public Set<Pattern> getExcludedUserAgentPatterns() {
        return excludedUserAgentPatterns;
    }

    public boolean isGzipCompatibleDeflation() {
        return gzipCompatibleDeflation;
    }

}
