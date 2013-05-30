grails-lightweight
==================

This plugin is intended to produce a lightweight, deployable grails application. It embeds jetty, and uses a number of
the conventions from Dropwizard that make sense. This includes reading configuration from an externalised yml file,
auto-instrumenting of controllers with yammer metrics and exposing a secondary port for the AdminServlet.
This plugin uses some code from both the standalone plugin by Burt Beckwith and Dropwizard by Codahale.
