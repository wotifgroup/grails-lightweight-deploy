class LightweightGrailsPlugin {
    def version = "0.2"
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

}
