Once the ruleset(s) and form definition has been defined, we can now run the `goodform_tutorial` Grails application.

We first need to run the OneRing Grails application - this will act as a web service that responds to requests from our
example application with information about the questions to be displayed.

Run the OneRing Grails application by navigating to the OneRing directory and run:

    grails run-app

Once the OneRing process is running, navigate to the `goodform_tutorial` directory and run

    grails run-app

We can now view to the form within the controller we created earlier.

Navigate to [http://localhost:8080/goodform_tutorial/contactDetailsForm/createForm].  You should see the
following content:

![Sample Form](##sample-form.png##)

If we try to submit the form without filling in any of the fields, we get an error message.

![Mandatory fields](##mandatory-fields.png##)

Once we fill in the mandatory fields and click submit, a confirmation screen is displayed.

![Confirmation](##form-confirmation.png##)

The completed form is stored in the FORM_INSTANCE table.  The contents of the form are contained
within the FORM_DATA column, and are contained in JSON format, eg.

```json
[{"Q1":{"order":"0","What_is_your_name":{"names":{"Given_Names":"test","Have_you_been_or_are_you_known_by_any_other_names":{"List_your_other_names":{"aliases":{"Type_of_name":"","Other_name":""}}},"Last_or_Family_Name":"test","Title":""}}},"next":["End"],"instanceId":"1","action":"next","require":[],"dummy":"value","controller":"contactDetails","formVersion":1}]
```

Let's revisit what we've done so far:

* Installed OneRing
* Installed the Goodform plugin within our Grails project
* Defined our form definition in the BootStrap.groovy file
* Defined the rulesets for our form
* Ran OneRing and our Grails tutorial project, and successfully submitted our sample form

Now let's create another sample form, this time demonstrating some more complexity in the ruleset definitions.

_Next_: [Create Complex Form](##06-CreateComplexForm.md##)
