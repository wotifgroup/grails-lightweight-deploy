import grails.util.BuildSettings
import grails.util.GrailsUtil
import grails.util.PluginBuildSettings

import org.apache.ivy.core.report.ResolveReport
import org.codehaus.groovy.grails.resolve.IvyDependencyManager
import org.springframework.util.FileCopyUtils

/**
 * @author <a href='mailto:adamtroyh@gmail.com'>Adam Harwood</a>
 * This script borrows heavily from BuildStandalone.groovy in the standalone plugin.
 */

includeTargets << grailsScript('_GrailsWar')

target(lightweight: 'Build a lightweight app with embedded jetty') {
	depends configureProxy, compile, loadPlugins

	try {
		File workDir = new File(grailsSettings.projectTargetDir, 'lightweight-temp-' + System.currentTimeMillis()).absoluteFile
		if (!workDir.deleteDir()) {
			event 'StatusError', ["Unable to delete $workDir"]
			return
		}
		if (!workDir.mkdirs()) {
			event 'StatusError', ["Unable to create $workDir"]
			return
		}

		String jarName = argsMap.artifactName
		File jar = jarName ? new File("target/${jarName}.jar").absoluteFile : new File(workDir.parentFile, defaultJarName()).absoluteFile

		event 'StatusUpdate', ["Building lightweight jar $jar.path"]

		if (argsMap.warfile) {
			File warFile = new File(argsMap.warfile).absoluteFile
            extractJar(warFile, workDir)
			if (warFile.exists()) {
				println "Using war file $argsMap.warfile"
                if (!buildJar(workDir, jar)) {
                    return
                }
			}
			else {
				errorAndDie "War file $argsMap.warfile not found"
			}
		}
		else {
			File warFile = buildWar(workDir)
            extractJar(warFile, workDir)
            warFile.delete()
            if (!buildJar(workDir, jar)) {
                return
            }
		}

		if (!workDir.deleteDir()) {
			event 'StatusError', ["Unable to delete $workDir"]
		}

		event 'StatusUpdate', ["Built $jar.path"]
	}
	catch (e) {
		GrailsUtil.deepSanitize e
		throw e
	}
}

buildWar = { File workDir ->
	File warfile = new File(workDir, 'embedded.war').absoluteFile
	warfile.deleteOnExit()

	argsMap.params.clear()
	argsMap.params << warfile.path
	war()

	warfile
}

buildJar = { File workDir, File jar ->
    File webDefaults = new File(workDir, 'webdefault.xml')
    File pluginDir = new PluginBuildSettings(buildSettings).getPluginDirForName('lightweight').file
    FileCopyUtils.copy new File(pluginDir, 'grails-app/conf/webdefault.xml'), webDefaults

	for (jarPath in resolveJars()) {
        println "Extracting jar $jarPath"
        //event 'StatusUpdate', ["Extracting jar $jarPath"]
        extractJar(new File(jarPath), workDir)
	}

	jar.canonicalFile.parentFile.mkdirs()
	ant.jar(destfile: jar) {
		fileset dir: workDir
        fileset(dir: pluginClassesDir) {
            include name: "grails/plugin/lightweight/**"
        }
		manifest {
			attribute name: 'Main-Class', value: resolveMainClass()
		}
	}

	true
}

defaultJarName = { ->
    String appName = metadata['app.name']
    String appVersion = metadata['app.version'] ?: '0.1-SNAPSHOT'
    String appBuildDate = new Date().format("yyyy.MM.dd")
    String appBuildNumber = argsMap.release;
    String appRelease = appBuildDate
    if (appBuildNumber) {
        appRelease += "_" + appBuildNumber
    }
    String artifactName = "$appName-$appVersion-$appRelease"
    "${artifactName}.jar"
}

extractJar = { File jarFile, File workDir ->
    ant.unjar src: jarFile, dest: workDir
}

resolveJars = { ->
	def deps = ["javax.servlet:javax.servlet-api:3.0.1",
                "org.eclipse.jetty.aggregate:jetty-all:8.1.11.v20130520",
                "com.google.guava:guava:14.0.1",
                "org.yaml:snakeyaml:1.12",
                "com.codahale.metrics:metrics-core:3.0.0-RC1",
                "com.codahale.metrics:metrics-servlet:3.0.0-RC1",
                "com.codahale.metrics:metrics-servlets:3.0.0-RC1",
                "com.codahale.metrics:metrics-jetty8:3.0.0-RC1",
                "org.slf4j:slf4j-api:1.7.4",
                "org.slf4j:jul-to-slf4j:1.7.4",
                "ch.qos.logback:logback-classic:1.0.13"]

    def config = config.grails.plugin.lightweight
	if (config.extraDependencies instanceof Collection) {
		deps.addAll config.extraDependencies
	}

	def manager = new IvyDependencyManager('lightweight', '0.1', new BuildSettings())
	manager.parseDependencies {
		repositories {
			mavenLocal()
			mavenCentral()
		}
		dependencies {
			compile(*deps) {
				transitive = true
                excludes "javax.servlet", "jetty-server"
			}
		}
	}

	ResolveReport report = manager.resolveDependencies()
	if (report.hasError()) {
		// TODO
		return null
	}

	def paths = []
	for (File file in report.allArtifactsReports.localFile) {
		if (file) paths << file.path
	}

	paths
}

String resolveMainClass() {
    if (config.grails.plugin.lightweight.mainClass) {
        config.grails.plugin.lightweight.mainClass
    } else {
        'grails.plugin.lightweight.Launcher'
    }
}

setDefaultTarget lightweight
