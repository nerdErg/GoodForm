grails.project.work.dir = 'target'

grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'

    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        compile 'net.htmlparser.jericho:jericho-html:3.2', {
            excludes 'log4j', 'commons-logging-api','slf4j-api'
        }

        compile 'net.sf.ezmorph:ezmorph:1.0.6', {
            excludes 'commons-beanutils', 'commons-lang', 'commons-logging', 'junit', 'log4j'
        }

        compile 'org.codehaus.groovy.modules.http-builder:http-builder:0.5.2', {
            excludes 'commons-io', 'groovy', 'httpclient', 'junit', 'log4j', 'nekohtml',
                     'signpost-commonshttp4', 'signpost-core', 'xercesImpl'
        }
        compile 'org.apache.httpcomponents:httpclient:4.0.3', {
            excludes 'commons-codec', 'commons-logging', 'httpcore', 'junit'
        }
        compile 'org.apache.httpcomponents:httpcore:4.0.1', {
            excludes 'junit'
        }
    }

    plugins {

        // including rendering plugin as a compile dependency seems to cause loading problems
        compile ":rendering:0.4.3",
                  ":jquery:1.8.3",
                  ":jquery-ui:1.8.24"

        test ":code-coverage:1.2.6"

        build ':release:2.2.1', ':rest-client-builder:1.0.3', {
            export = false
        }

        runtime ":hibernate:$grailsVersion", {
            export = false
        }
    }
}
