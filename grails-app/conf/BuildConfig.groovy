grails.project.work.dir = 'target'

grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'

    repositories {
        grailsCentral()
    }

    plugins {

        // including rendering plugin as a compile dependency seems to cause loading problems
        compile ":rendering:0.4.3",
                  ":cxf:1.0.7",
                  ":jquery:1.8.3",
                  ":jquery-ui:1.8.24",
                  ":nerderg-form-tags:2.1.3"

        build ':release:2.2.1', ':rest-client-builder:1.0.3', {
            export = false
        }

        runtime ":hibernate:$grailsVersion", {
            export = false
        }
    }
}
