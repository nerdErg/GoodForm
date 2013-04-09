Customize the form behaviour
===

Let's customize the JobApplicationController to generate a PDF of the entered form.

First, include the pdfRenderingService within the controller and override the submit() method:

```groovy
class JobApplicationController extends FormController {

    def pdfRenderingService

    def createForm() {
        createForm('JobApplication')
    }

    @Override
    def submit(Long id) {

        FormInstance formInstance = formDataService.getFormInstance(id)
        Map formData = formInstance.storedFormData()
        File location = new File(grailsApplication.config.goodform.uploaded.file.location.toString() + '/form/' + id)
        location.mkdirs()
        File upload = new File(location, "applicationForm.pdf")
        upload.withOutputStream {outputStream ->
            pdfRenderingService.render([template: '/jobApplication/view', model: [formInstance: formInstance, formData: formData]], outputStream)
        }
        return super.submit(id)
    }
}
```

[Click here to see the change](https://github.com/rossrowe/GoodForm-Tutorial/compare/tutorial-step6-c...tutorial-step7-a)

We also need to define a template that will be used to render the PDF.  Let's create a _view.gsp file in the `grails-app/views/jobApplication`
directory with the following contents:


```html
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page import="org.codehaus.groovy.grails.commons.ConfigurationHolder" contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>Job Application</title>
</head>

<body>
<div class="goodFormContainer">
  <form:displayText formInstance="${formInstance}" store="${formData}" readOnly="${formInstance.readOnly}"/>
</div>
</body>
</html>
```

[Click here to see the change](https://github.com/rossrowe/GoodForm-Tutorial/compare/tutorial-step7-a...tutorial-step7-b)

We also need to update our `grails-app/conf/Config.groovy' to define the location where we want the PDF to be stored, eg:

```groovy
environments {
    development {
        rulesEngine.uri = 'http://localhost:8081/rulesEngine'
        goodform.uploaded.file.location = '.'
    }
}
```

[Click here to see the change](https://github.com/rossrowe/GoodForm-Tutorial/compare/tutorial-step7-b...tutorial-step7-c)

Now when we click the 'Submit' link, a PDF will be generated in the goodform_tutorial/form

_Next_: [Custom Validators](07-CustomValidators.md)