Now let's create a more complicated form that simulates a loan application which uses Spring Security.

Update the `grails-app\conf\BuildConfig.groovy` file in the `goodform_tutorial` project to include the following

```groovy
plugins {
É
    compile ':spring-security-core:1.2.7.3'
É
}
```

Run the following command

    grails s2-quickstart com.goodform.example SecUser SecRole

Add the following lines to the grails-app/conf/UrlMappings.groovy within the mappings definition:

    "/login/$action?"(controller: "login")
    "/logout/$action?"(controller: "logout")

Define the access control rules in the grails-app/conf/Config.groovy file
```groovy
grails.plugins.springsecurity.interceptUrlMap = [
    '/loanApplicationForm/*':         ['ROLE_USER'],
    '/**':               ['IS_AUTHENTICATED_ANONYMOUSLY']
]
```

Add the following code to the Bootstrap.groovy file, to create some dummy users and groups:

```groovy
    def init = { servletContext ->
        //snip
        if (!SecRole.findByAuthority('ROLE_APPROVER')) {
                    SecRole approverRole = new SecRole(authority: "ROLE_APPROVER")
                    save approverRole
                    SecUser jane = createUser("Jane Approver", "jane", "password")
                    addRole(jane, approverRole)
                }

                if (!SecRole.findByAuthority('ROLE_LOANEE')) {
                    SecRole loaneeRole = new SecRole(authority: "ROLE_LOANEE")
                    save loaneeRole
                    SecUser joe = createUser("Joe Loanee", "joe", "password")
                    addRole(joe, loaneeRole)
                }

    }
     def createUser(fullname, name, password) {
        def user = new SecUser(fullname: fullname, username: name, password: password,
                enabled: true, accountExpired: false, accountLocked: false, passwordExpired: false)
        save user
        return user
    }

    def addRole(user, role) {
        SecUserSecRole.create user, role, true
    }

    def save(thing, flush = false) {
        if (thing.save(flush: flush)) {
            if (thing.hasErrors()) {
                log.error "*** Errors Saving ***"
                thing.errors.allErrors.each { log.error it }
                return false
            }
            log.debug("$thing saved")
            return true
        } else {
            log.error "*** Errors Saving ***"
            thing.errors.allErrors.each { log.error it }
            return false
        }
    }
```

Create the following rulesets within the ~/.OneRing/rules/application directory:

#LoanApplication_firstQuestion.ruleset
```groovy
ruleset("LoanApplication") {

    require(['role'])

    abortOnFail = false

    rule("Display first  questions") {
        when {
            role == 'APPLICANT'
        }
        then {
            next = ['Loan1']
        }
    }
}
```

#LoanApplication_Loan1.ruleset

```groovy
ruleset("LoanApplicationLoan1") {

    require(['Loan1', 'role'])

    abortOnFail = false

    rule("Display first  questions") {
        when {
            role == 'APPLICANT'
        }
        then {
            next = ['Loan2']
        }
    }

}
```

#LoanApplication_Loan2.ruleset

```groovy
ruleset("LoanApplicationLoan2") {

    require(['Loan2', 'role'])

    abortOnFail = false

    rule("Display second  questions") {
        when {
            role == 'APPLICANT'
        }
        then {
            next = ['Loan3']
        }
    }

}
```

#LoanApplication_Loan3.ruleset

```groovy
ruleset("LoanApplicationLoan3") {

    require(['Loan3', 'role'])

    abortOnFail = false

    rule("Display fourth questions") {
        when {
            role == 'APPLICANT'
        }
        then {
            next = ['Loan4']
        }
    }

}
```

#LoanApplication_Loan4.ruleset

```groovy
ruleset("LoanApplicationLoan4") {

    require(['Loan4', 'role'])

    abortOnFail = false

    rule("Display fifth questions") {
        when {
            role == 'APPLICANT'
        }
        then {
            next = ['Loan5']
        }
    }

}
```

#LoanApplication_Loan5.ruleset

```groovy
ruleset("LoanApplicationLoan5") {

    require(['Loan5', 'role'])

    abortOnFail = false

    rule("Display sixth questions") {
        when {
            role == 'APPLICANT'
        }
        then {
            next = ['End']
        }
    }
}
```

Let's create a new controller for this form.  We do this by running

    grails create-controller LoanApplicationForm

Update the LoanApplicationFormController class to extend from the GoodForm FormController class.

```groovy
class LoanApplicationFormController extends FormController {

}
```

Now let's update the controller to override the getRuleFacts() method so that we can pass the user's role to our ruleset.

```groovy
   public Map getRuleFacts() {
       ['role': getRole()]
   }

   private String getRole() {
       if (SpringSecurityUtils.ifAllGranted("ROLE_APPROVER")) {
           return 'ROLE_APPROVER'
       }
       return 'ROLE_LOANEE'
   }
```

