* Once the ruleset(s) and form definition has been defined, we can now run the grails application.

We first need to run the OneRing application - this will act as a web service that responds to requests from our
example application with information about the questions to be displayed.

Run the OneRing Grails application by navigating to the OneRing directory and run:

    grails run-app

Once the OneRing process is running, navigate to the goodform_example directory and run

    grails run-app

We can now view to the form within the controller we created earlier.

Navigate to http://localhost:8080/goodform-example/contactDetails/createForm?formName=ContactDetails

