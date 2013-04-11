Install OneRing Rules Engine
====

The Goodforms plugin for Grails is dependant on a [OneRing](http://nerderg.com/One+Ring) rules engine instance running.  OneRing provides a web service
that will provide information to Goodforms regarding the question sets to be displayed based on where the processing
of a form instance is up to.

OneRing is a standalone Open Source Grails application, so it can either be run from the command line (for local development and testing
purposes) or deployed to a web container.

To install, see [The One Ring install doc] (http://nerderg.com/One+Ring+install) and either download the
[OneRing source](https://github.com/pmcneil/One-Ring) and run it locally by executing

    grails run-app

or

download the [OneRing Rules Engine WAR](http://nerderg.com/media/show/1295?file=rulesEngine-0.7.war) distribution and install it
into an existing Java EE Web Container (like Tomcat)

or

download the [OneRing + Tomcat package] (http://nerderg.com/media/show/1356?file=one-ring-0.9.2-apache-tomcat-7.0.22.zip)

* _Next_: [Create or Update a Grails Project](03-CreateOrUpdateGrailsProject.md)
