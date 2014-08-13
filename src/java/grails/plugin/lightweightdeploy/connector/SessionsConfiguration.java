package grails.plugin.lightweightdeploy.connector;

import com.google.common.base.Objects;

import java.util.Map;

public class SessionsConfiguration {

    private final boolean enabled;

    private final String workerName;

    public SessionsConfiguration() {
        this(true, "");
    }

    public SessionsConfiguration(final Map<String, ?> config) {
        this(Objects.firstNonNull((Boolean) config.get("enabled"), true),
                Objects.firstNonNull((String) config.get("workerName"), ""));
    }

    private SessionsConfiguration(final boolean enabled, final String workerName) {
        this.enabled = enabled;
        this.workerName = workerName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getWorkerName() {
        return workerName;
    }

}
