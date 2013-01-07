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

For our example, let's create a method which validates blah..

```groovy
    def postcode(FormElement formElement, String postcode) {
        return !addressWranglingService.isValidPostcode(postcode)
    }
```

