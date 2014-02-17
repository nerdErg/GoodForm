import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

includeTargets << grailsScript("_GrailsSettings")

USEAGE = """
    install-goodform-templates

    Copies the GoodForm templates into your project Views/goodFormTemplates for modification.
"""

target(main: "Installs default Goodform templates into your project so you can modify them.") {
    targetDir = "${basedir}/grails-app/views/goodFormTemplates"
    overwrite = false

    // only if template dir already exists in, ask to overwrite templates
    if (new File(targetDir).exists()) {
        if (!isInteractive || confirmInput("Overwrite existing templates?","overwrite.templates")) {
            overwrite = true
        }
    }
    else {
        ant.mkdir(dir: targetDir)
    }

    String goodformDir = GrailsPluginUtils.pluginInfos.find { it.name == 'goodform' }.pluginDir

    ant.copydir(dest: "$targetDir", src: "${goodformDir}/grails-app/views/goodFormTemplates", forceoverwrite: overwrite)

}

setDefaultTarget(main)
