Now let's define some custom validation logic for our JobApplication form.

An easy way to add a custom validator is to extend the `FormValidationService` class,

To do, create a new Service for the goodform_tutorial project, by running

    grails create-service JobApplicationValidatorService

Once this has been done, open the JobApplicationValidatorService class and update it to be a subclass of the `FormValidationService` class.

```groovy

import com.nerderg.goodForm.FormValidationService

class JobApplicationValidatorService extends FormValidationService {

}

```

The FormValidationService class contains a `customValidation` closure which


```groovy
class BootStrap {

    def init = { servletContext ->

    //snip

        def ctx = servletContext.getAttribute(ApplicationAttributes.APPLICATION_CONTEXT)
        ctx.yourValidationService.addValidator(ctx.formValidationService.customValidation)

    }

```