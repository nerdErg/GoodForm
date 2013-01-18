Now let's create a more complicated form that simulates a loan application which uses Spring Security.

Update the `grails-app\conf\BuildConfig.groovy` file in the `goodform_tutorial` project to include the following

```groovy
plugins {
É
    compile ':spring-security-core:1.2.7.3'
É
}
```

Let's create a new controller for this form.  We do this by running

    grails create-controller LoanApplicationForm

Run the following command

    grails s2-quickstart com.goodform.example SecUser SecRole

Update the LoanApplicationFormController class to extend from the GoodForm FormController class.

#Populate users, roles and mappings

Let's update the controller to override the getRuleFacts() method