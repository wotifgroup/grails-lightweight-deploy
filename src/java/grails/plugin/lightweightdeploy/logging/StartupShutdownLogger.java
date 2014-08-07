package grails.plugin.lightweightdeploy.logging;

import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.component.LifeCycle.Listener;
import org.slf4j.Logger;

public class StartupShutdownLogger implements Listener {

    private final Logger logger;
    private final String appName;

    public StartupShutdownLogger(Logger logger, String appName) {
        this.logger = logger;
        this.appName = appName;
    }

    @Override
    public void lifeCycleStarting(LifeCycle lifeCycle) {

    }

    @Override
    public void lifeCycleStarted(LifeCycle lifeCycle) {
        logger.info("{} has started", appName);
    }

    @Override
    public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable) {

    }

    @Override
    public void lifeCycleStopping(LifeCycle lifeCycle) {

    }

    @Override
    public void lifeCycleStopped(LifeCycle lifeCycle) {
        logger.info("{} has shutdown", appName);
    }
}
