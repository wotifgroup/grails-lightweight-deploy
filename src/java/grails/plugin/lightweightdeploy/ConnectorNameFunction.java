package grails.plugin.lightweightdeploy;

import com.google.common.base.Function;
import org.eclipse.jetty.server.Connector;

enum ConnectorNameFunction implements Function<Connector, String> {
    INSTANCE; // enum singleton pattern

    @Override
    public String apply(Connector input) {
        return input.getName();
    }
}
