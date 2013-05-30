grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolution = {
    inherits("global")
    log "warn"

    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        compile "com.google.guava:guava:14.0.1"
        compile "org.yaml:snakeyaml:1.12"

        //explicitly depend on servlet-api to workaround issue with Ivy and orbit dependencies
        compile ("javax.servlet:javax.servlet-api:3.0.1") {
            export = false
        }
		compile ('org.eclipse.jetty.aggregate:jetty-all:8.1.11.v20130520') {
            export = false
            exclude 'javax.servlet'
		}
    }

    plugins {
        build(":release:2.2.1",
              ":rest-client-builder:1.0.3") {
            export = false
        }

        runtime ":yml-config:1.0"
    }
}
