Good Form

Good Form is a grails plugin that lets you create easy to use, good looking, easy to maintain, complex web forms.

Checkout the [documentation] (http://nerderg.com/Good+Form)

And the [Tutorial] (tutorial/01-Introduction.md)

Version 2.0.0

* remove nerderg form tags as the basis for the form elelments
* write templates for all element and form pages for ease of skinning and adding new elements for forms.
* add a script to import the templates into your project for modification
* make the form element definitions in the DSL more modular to allow for simpler addition of new custom elements
* support bootstrap out of the box, and implement a new default bootstrap theme for the forms
* add a form index bar to assist going back in the form
* add the ability to extend the form element types

Version 1.0.4

* Add a select field so that you can have a "select an option" select: ['one','two','three'] ... which creates a drop down select box
* Improve the file attachment upload handling. Attachments are now put in uploaded.file.location/formName/formInstanceID/

Version 1.0.3

* Add a generic field errors message to the top of the form page to indicate that there are errors below.
* Add a div with class qsetDisplay to the form question set displayed on answered questions and set the default display to none.
* Put a div around the form version message so it can be hidden.
* Fix #16 check for arrays and lists to display the listOf element
* Fix #17 remove th index for the each form element as the fields are individually named
* remove excess line of code
