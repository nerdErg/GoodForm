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

Then define your validation logic as a method within the service.  The method should take two arguments, the first
being a FormElement instance, and the second being a String, which represents the entered value.  The method should
also return a boolean, which indicates whether the field passes validation or not.

For our example, let's create a method which validates that the attachment is a pdf file.

Add the following method to the FileValidationService class

```groovy
    def pdf_only(FormElement formElement, String filename) {
        return filename.endsWith(".pdf")
    }
```

#TODO error message can be defined as goodform.validate.pdf_only in messages.properties


#Validators that don't extend from FormValidationService can be added by...

```groovy
def ctx = servletContext.getAttribute(ApplicationAttributes.APPLICATION_CONTEXT)
ctx.formDataService.addValidator(ctx.yourService.customValidationClosure)
```

The closure should define FormElement and String input parameters, eg.

```groovy
def customValidation = {FormElement formElement, String fieldValue ->
        def error = false
        if (fieldValue && formElement.attr.containsKey('validate') && hasError(formElement, fieldValue)) {
            formElement.attr.error += g.message(code: "goodform.validate." + formElement.attr.validate + ".invalid")
            error = true
        }
        return error
    }
```


