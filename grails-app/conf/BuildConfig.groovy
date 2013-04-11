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
        compile 'net.sf.ezmorph:ezmorph:1.0.6', {
            excludes 'commons-beanutils', 'commons-lang', 'commons-logging', 'junit', 'log4j'
        }
        compile 'org.codehaus.groovy.modules:groovyws:0.5.2', {
            excludes 'FastInfoset', 'XmlSchema', 'ant', 'ant-javamail', 'bcprov-jdk15', 'commons-logging',
                     'cxf-bundle', 'geronimo-javamail_1.4_spec', 'geronimo-jaxws_2.1_spec',
                     'geronimo-servlet_2.5_spec', 'geronimo-ws-metadata_2.0_spec', 'groovy-all', 'jaxb-impl',
                     'jaxb-xjc', 'jdom', 'jetty', 'jetty-util', 'junit', 'neethi', 'saaj-impl', 'serializer',
                     'wsdl4j', 'wss4j', 'xalan', 'xml-resolver', 'xmlsec'
        }
        compile 'org.codehaus.groovy.modules.http-builder:http-builder:0.5.1', {
            excludes 'commons-io', 'groovy', 'httpclient', 'json-lib', 'junit', 'log4j', 'nekohtml',
                     'signpost-commonshttp4', 'signpost-core', 'xercesImpl', 'xml-resolver'
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
