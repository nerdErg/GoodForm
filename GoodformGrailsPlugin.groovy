class GoodformGrailsPlugin {
    def version = "1.0.0"
    def grailsVersion = "2.0 > *"

    def loadAfter = ["rendering"]

    def title = "Goodform Plugin"
    def author = "Peter McNeil"
    def authorEmail = "peter@nerderg.com"
    def description = '''\
Create extremely usable complex forms with rules based flow that work well in browsers.
Good form features a form definition DSL that allows rapid human readable form creation.
'''

    def documentation = "http://nerderg.com/Good+Form"

    def license = "APACHE"
    def organization = [ name: "nerdEg Pty Ltd", url: "http://www.nerderg.com/" ]
    def developers = [ [ name: "Ross Rowe", email: "ross@nerderg.com" ]]
    def issueManagement = [ system: "JIRA", url: "https://github.com/nerdErg/GoodForm/issues" ]
    def scm = [ url: "https://github.com/nerdErg/GoodForm" ]
}
