class LightweightGrailsPlugin {
    def version = "0.1"
    def grailsVersion = "2.0 > *"

    def title = "Lightweight Plugin"
    def description = '''\
This plugin is intended to produce a lightweight, deployable grails application. It embeds jetty, and uses a number of
the conventions from Dropwizard that make sense.
This plugin uses some code from both the standalone plugin by Burt Beckwith and Dropwizard by Codahale.
'''

    def documentation = "https://github.com/aharwood/grails-lightweight/"

    def license = "APACHE"

    def developers = [ [ name: "Adam Harwood", email: "adamtroyh@gmail.com" ]]

    def issueManagement = [ system: "github", url: "https://github.com/aharwood/grails-lightweight" ]

    def scm = [ url: "https://github.com/aharwood/grails-lightweight/" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
