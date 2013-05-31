import org.apache.commons.io.filefilter.WildcardFileFilter
import org.freecompany.redline.Builder
import org.freecompany.redline.payload.Directive

includeTargets << grailsScript('_GrailsBootstrap')

target(rpmMain: "Build the application RPM") {
    depends(configureProxy, classpath, loadApp)
    Builder builder = initBuilder()

    addScripts(builder)

    addContent(builder)

    writeRpm(builder)

    println "Complete"
}

addContent = { Builder builder ->
    config.rpm.structure.each { nextDirectoryName, nextDirectoryStructure ->
        addDirectory(builder, "/$nextDirectoryName", nextDirectoryStructure)

    }
}

addDirectory = {Builder builder, String directoryPath, def directory ->
    int permissions = 775
    String user = "root"
    String group = "root"
    Directive directoryDirective = Directive.NONE
    directory.each {key, value ->
        if (key == "permissions") {
            permissions = value
        } else if (key == "user") {
            user = value
        } else if (key == "group") {
            group = value
        } else if (key == "files") {
            //ignore, will handle after
        } else if (key == "directive") {
            directoryDirective = value
        } else {
            //must be another directory
            addDirectory(builder, "$directoryPath/$key", value)
        }
    }

    directory.files.each { fileName, fileInfo ->
        if (!fileInfo.permissions) {
            throw new IllegalArgumentException("No permissions set for file $fileName")
        }
        Directive fileDirective = fileInfo.directive ?: Directive.NONE
        String fileUser = fileInfo.user ?: "root"
        String fileGroup = fileInfo.group ?: "group"
        File file = new File(".", fileName)
        println "Looking for $fileName"
        file.parentFile.listFiles((FilenameFilter) new WildcardFileFilter(file.name)).each { nextFile ->
            println "Adding file $nextFile.absolutePath"
            builder.addFile("$directoryPath/$nextFile.name",  nextFile, fileInfo.permissions, fileDirective, fileUser, fileGroup)
        }
    }

    println "Adding directory $directoryPath"
    builder.addDirectory(directoryPath,
                         permissions,
                         directoryDirective,
                         user,
                         group,
                         false)
}

addScripts = { Builder builder ->
    if (config.rpm.preRemove) {
        builder.setPreUninstallScript(new File(config.rpm.preRemove).text)
    }
    if (config.rpm.postInstall) {
        builder.setPostInstallScript(new File(config.rpm.postInstall).text)
    }
}

def getRpmName() {
    def rpmName = argsMap.name
    if (!rpmName) {
        String appName = metadata['app.name']
        String appVersion = metadata['app.version']
        String appRelease = new Date().format("yyyy.MM.dd")
        String appBuildNumber = argsMap.release
        if (appBuildNumber) {
            appRelease += "_" + appBuildNumber
        }
        artifactName = "$appName-$appVersion-$appRelease"
    }
    rpmName
}

initBuilder = { ->
    def rpmConfig = config.rpm

    Builder builder = new Builder(rpmConfig.metaData)

    builder.setPackage(rpmConfig.packageInfo.name, rpmConfig.packageInfo.version, argsMap.release ?: "")

    builder.setPlatform(rpmConfig.platform.arch, rpmConfig.platform.osName)

    rpmConfig.dependencies.each { nextDependency ->
        builder.addDependencyMore(nextDependency.key, nextDependency.value)
    }

    builder
}

writeRpm = { Builder builder ->
    String rpmFileName = "${rpmName}.noarch.rpm"
    String path = "target/${rpmFileName}"
    println "Building RPM: $path"
    File rpmTarget = new File("$path")
    def fos = new FileOutputStream(rpmTarget, false)
    builder.build(fos.getChannel())
    fos.close()
}

setDefaultTarget(rpmMain)
