grails-lightweight
==================

This plugin is intended to produce a lightweight, production-ready, deployable grails application. It embeds jetty, and uses a number of
the conventions from [Dropwizard](http://dropwizard.codahale.com) that make sense. This includes reading configuration from an externalised yml file,
auto-instrumenting of controllers with codahale metrics and exposing a secondary port for the AdminServlet.

##Quick Start
Add the following to your BuildConfig:
```
compile ":lightweight:0.4.0"
```
This will then allow you to:
```
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
        threshold: INFO
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

##Credit
This plugin uses some code from both the [standalone](http://grails.org/plugin/standalone) plugin by Burt Beckwith and [Dropwizard](http://dropwizard.codahale.com) by Codahale.
