Complex Forms
===

Let's now create a new form that will span several pages.  Add the following to your BootStrap.groovy file:

```groovy
    String jobApplicationDefinition = """
        form {
           question("Job1") {
                "What is your name?" group: "names", {
                     "Title" text: 10, hint: "e.g. Mr, Mrs, Ms, Miss, Dr", suggest: "title", map: "Title"
                     "Given Names" text: 50, required: true, map: "givenNames"
                     "Last or Family Name" text: 50, required: true, map: "lastName"
                     "Have you been or are you known by any other names?" hint: "e.g. maiden name, previous married name, alias, name at birth", map: "hasAlias", {
                         "List your other names" listOf: "aliases", {
                             "Other name" text: 50, map: 'alias'
                             "Type of name" text: 40, hint: "e.g maiden name", suggest: "nameType", map: 'aliasType'
                         }
                     }
                 }
             }
             question("Job2") {
                 "Contact details" group: "contact", {
                     "Home address" text: 200, map: 'homeAddress'
                     "Postcode" number: 4, hint: "e.g. 2913", map: 'postcode'
                     "Home Phone" phone: 15, map: 'homePhone'
                     "Mobile Phone" phone: 15, map: 'mobilePhone'
                     "Work Phone" phone: 15, map: 'workPhone'
                 }
             }
             question("Job3") {
                "Education" group: "education", {
                    "University" listOf: "universities", {
                        "Name" text: 20, map: 'name'
                        "Years attended" text: 20, map: 'years'
                        "Degree" text: 20, map: 'degree'
                        "Course" text: 20, map: 'course'
                    }
                    "High School" listOf: "highSchools", {
                       "Name" text: 20, map: 'name'
                       "Years attended" text: 20, map: 'years'
                    }
                }
             }

             //role applying for
             question("Job4") {
                  "Enter the details of the role you are applying for" group: "role", {
                        "Job Number" text: 50, required: true, map: 'jobNumber'
                        "Position" text: 50, required: true, map: 'position'
                        "Company" text: 50, required: true, map: 'company'
                  }
             }
             //include copy of resume
             question("Job5") {
                  "Resume" group: "resume", {
                      "Attach a copy of your resume" attachment: "resume_file", map: 'resume'
                   }
             }
             //referee details
             question("Job6") {
                  "Enter details for two referees" group: "references", {
                        "Referee" listOf: "referee", {
                            "Given Names" text: 50, required: true, map: 'givenNames'
                            "Last or Family Name" text: 50, required: true, map: 'lastName'
                            "Contact Phone" phone: 15, required: true, map: 'phone'
                        }

                  }
             }
    }"""
    if(!FormDefinition.findByName('JobApplication')) {
            formDataService.createNewFormVersion('JobApplication', jobApplicationDefinition)
    }

```

[Click here to see the change](https://github.com/rossrowe/GoodForm-Tutorial/compare/tutorial-step4-b...tutorial-step6-a)

This creates a JobApplication form definition, which contains six questions.

This form adds some extra form elements, notably:

* phone - validates that the entered value conforms to phone number validation rules
* attachment - allows a file to be selected and uploaded as part of the form submission

To configure the questions to span multiple pages, create the following ruleset files in the ~/.OneRing/rules/application directory:

#JobApplication_firstQuestion.ruleset

```groovy
ruleset("JobApplication") {

    abortOnFail = false

    rule("First page of questions") {
        when {
            true
        }
        then {
            next = ['Job1', 'Job2', 'Job3']
        }
    }

}
```

#JobApplicationJob3.ruleset

```groovy
ruleset("JobApplicationJob3") {

    require(['Job1', 'Job2', 'Job3'])  //we need the answers to these questions

    abortOnFail = true

	rule("Second page of job application questions") {
        when {
            true
        }
        then {
            next = ['Job4', 'Job5', 'Job6']
        }
    }

}
```

#JobApplicationJob6.ruleset

```groovy
ruleset("JobApplicationJob6") {

    require(['Job4', 'Job5', 'Job6'])  //we need the answers to these questions

    abortOnFail = true

	rule('finished') {
        when {
            true
        }
        then {
            next = ['End']
        }
    }
}
```

#JobApplicationCheckRequiredDocuments.ruleset

```groovy
ruleset("JobApplicationCheckRequiredDocuments") {

    require()  //we need the answers to these questions

    rule('prep') {
        fact.require = []  //create the set 'require'
    }

    rule('Is colour indicated') {
        when {
            Job5 && !Job5.resume
        }
        otherwise {
            require.add([Q: 'Job5', message: 'You must supply a resume'])
        }
    }
}
```

[Click here to see the change](https://github.com/rossrowe/GoodForm-Tutorial/compare/tutorial-step6-a...tutorial-step6-b)

Let's go back to our goodform_tutorial Grails application, and create a new controller for this form.  We do this by running

    grails create-controller JobApplicationForm

from the ~/goodform_tutorial directory.

Update the controller to extend from the com.nerderg.goodForm.FormController class

```groovy
import com.nerderg.goodForm.FormController

class JobApplicationFormController extends FormController {
    def createForm() {
        createForm('JobApplication')
    }
}
```

[Click here to see the change](https://github.com/rossrowe/GoodForm-Tutorial/compare/tutorial-step6-b...tutorial-step6-c)

Once this has been done, run

    grails compile

and go to http://localhost:8080/goodform_tutorial/jobApplication/createForm

This will display the first three questions of the form.

![Job Application Page 1](img/job_application_page_1.png)

Upon entering the mandatory fields and clicking submit, the second page of form questions will be displayed.

![Job Application Page 2](img/job_application_page_2.png)

The answers entered on the first page will be listed below the questions.

![Job Application Answered Questions](img/job_application_answered_questions.png)

_Next_: [Customize Form Behaviour](06-CustomizeFormBehaviour.md)

