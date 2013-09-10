grails-lightweight-deploy
==================

This plugin is intended to produce a lightweight, production-ready, deployable grails application. It embeds jetty, and uses a number of
the conventions from [Dropwizard](http://dropwizard.codahale.com) that make sense. This includes reading configuration from an externalised yml file,
auto-instrumenting of controllers with codahale metrics and exposing a secondary port for the AdminServlet.

##Getting Started
Add the plugin to your BuildConfig:
```
compile ":lightweight-deploy:latest"
```
ideally you should also replace the tomcat plugin with:
```
build ':jetty:2.0.3'
```
for the sake of consistency.

grails-lightweight-deploy uses logback for logging. This requires removing log4j usage from Grails by default. In order to do this, add the following to your global excludes in BuildConfig.groovy
```
excludes "log4j", "grails-plugin-log4j"
```
now create a logback.xml file in grails-app/conf:
```
<configuration>
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>
  <root level="info">
    <appender-ref ref="STDOUT" />
  </root>
</configuration>
```
This file will only be used when running grails commands locally. When the server is actually running on Jetty, it will use the configuration from your yml file.
Finally, due to a [long-standing bug](http://jira.grails.org/browse/GRAILS-3929) in Grails, you need to:
```
grails run-app
```
once to get the logback.xml copied to the correct location inside /.grails. Note that this first build will spam with DEBUG messages, but commands subsequent will be fine.

Now you should be able to produce your executable jar file:
```
grails refresh-dependencies
grails lightweight
```
which will produce a jar file inside your target directory.

Next you need to create a yml file somewhere inside your source tree which will store your externalised configuration, something like the following:
```
http:
    port: 8080
    adminPort: 8048

logging:
    file:
        currentLogFilename: ./server.log
        rootLevel: INFO
```
execute:
```
java -jar tar/appName-date.jar path/to/config.yml
```
then:
```
tail -f ./server.log
```
and you should see your server starting up successfully.

##Configuration
Here's a complete sample configuration file, including comments describing the properties:
```
http:
    #The port number that your grails application will be hosted on. By default this is http, however if the ssl block below is specified it will be https.
    port: 8080
    #A secondary port which will serve your administrative content. This should be firewalled off from external access. Check http://localhost:8048/ for what it provides.
    adminPort: 8048
    #If this block is specified, then the port will be over https
    ssl:
        #The path to the keystore which will be used to encrypt traffic over SSL on the port.
        keyStore: /etc/pki/tls/jks/keystore.jks
        #The alias inside the keystore which will be used.
        certAlias: subdomain.domain.com
        #The path to a plain-text file which stores the password for the keystore. This is ops-friendly externalisation, who can then use another automation framework to manage the passwords.
        keyStorePasswordPath: /path/to/plain/text/file.txt
        #Overrides keyStorePasswordPath if specified, this is the password for the keystore
        keyStorePassword: password
    #If specified, then an access log will be written.
    requestLog:
        file:
            #The path to the file to write access log to.
            currentLogFilename: ./server_access_log.txt

logging:
    file:
        #The path to the file to write the server log to.
        currentLogFilename: ./server.log
        #The default log level
        rootLevel: INFO
        #The threshold over which log statements must be before being logged.
        threshold: ALL
        #custom log levels
        loggers:
            foo: ERROR
            bar.baz: DEBUG

#If this block is present, then a jmx server will be started on the given ports. Both are required.
jmx:
    serverPort: 10000
    registryPort: 10001

#If specified, this directory will be used for temporary work files whilst the server is running. Defaults to java.io.tmpdir.
workDir: ./work
```

##Customised Bootstrapping
By default, the server will expose only the content in your grails application. It is possible to perform extra configuration of the Jetty server though. To do this, you need to write a custom Launcher. Here's a basic example:
```
package com.name;

public class ApplicationLauncher extends grails.plugin.lightweightdeploy.Launcher {

    public ApplicationLauncher(String configYmlPath) throws IOException {
        super(configYmlPath);
    }

    public static void main(String[] args) throws IOException {
        verifyArgs(args);
		new ApplicationLauncher(args[0]).start();
    }
}
```
to get grails-lightweight-deploy to use this launcher, you then specify the following in your Config.groovy:
```
grails.plugin.lightweightdeploy.mainClass="com.name.ApplicationLauncher"
```
from there, you can perform any extra bootstrapping required.

If your bootstrapping relies on some extra dependencies, you can use the following:
```
grails.plugin.lightweightdeploy.extraDependencies = ["group:artifact:version", ...]
```
These jars will then be exploded into the bootstrapping portion of your runnable jar.

You can also include specific classes or directories of classes from your application into the bootstrapping portion of the runnable jar:
```
grails.plugin.lightweightdeploy.extraClasses = ["**/Awesome.class", "**/util/**"]
```

###Extra configuration on the external or admin connectors
It is possible, with a custom Launcher, to perform extra configuration on the external or admin connectors. To do so you need to override either of the following methods:
```
protected void configureExternalServlets(WebAppContext context);
protected void configureInternalServlets(ServletContextHandler handler);
```

For example, to add an extra servlet to the external connector:
```
@Override
protected void configureExternalServlets(WebAppContext context) {
    super.configureExternalServlets(context);

    context.addServlet(new ServletHolder(new MyCustomServlet()), "/custom/url");
}
```
Similarly the plugin provides a default [ThreadDeadlockHealthCheck](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/health/jvm/ThreadDeadlockHealthCheck.html)
at "http://server:adminPort/healthcheck"
Custom HealthChecks can be added by overriding:
```
protected void configureHealthChecks()
```
Be sure to still call super when overriding these methods for the default behaviour.

Alternatively, if you need to add HealthChecks from a Spring-managed bean, or Resources.groovy, you can use the HealthCheckUtil class to get easy access to the HealthCheckRegistry.

## Command-Line Arguments
By default, the artifact produced will have a name of the form: appName-appVersion-date (e.g. testapp-1.0-2013.01.01). This is
fairly friendly, informational default. It is, however possible to configure the name of the artifact produced from the command line.
You can either specify the entire artifact name:
```
grails prod lightweight --artifactName=testName
```
or you can specify just a "release" modifier, which will be appended to the standard default:
```
grails prod lightweight --release=123
```
Which would produce a name of the form appName-appVersion-date_123. This can be useful to set to the build number from your CI
server, to make tracking of the build into production easier.

##Metrics
lightweight-deploy automatically instruments all controller methods with [Codahale Metrics](http://metrics.codahale.com/). Each Timer is created dynamically
as requests come in, named as "controllerName.actionName". The metrics are viewable at "http://server:adminPort/metrics".

You can access the MetricRegistry and create your own metrics using:
```
grails.plugin.lightweightdeploy.application.metrics.MetricsUtil.getMetricRegistry()
```

##License
lightweight-deploy is licensed under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

##Credit
This plugin uses some code from both the [standalone](http://grails.org/plugin/standalone) plugin by Burt Beckwith and [Dropwizard](http://dropwizard.codahale.com) by Codahale.
