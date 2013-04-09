class GoodformGrailsPlugin {
    // the plugin version
    def version = "1.0.0-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on

    def loadAfter = ["rendering"]
    // resources that are excluded from plugin packaging
    def pluginExcludes = []

    def title = "Goodform Plugin" // Headline display name of the plugin
    def author = "Peter McNeil"
    def authorEmail = "peter@nerderg.com"
    def description = '''\
Create extremely usable complex forms with rules based flow that work well in browsers.
Good form features a form definition DSL that allows rapid human readable form creation.
'''

    // URL to the plugin's documentation
    def documentation = "http://nerderg.com/Good+Form"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [ name: "nerdEg Pty Ltd", url: "http://www.nerderg.com/" ]

    // Any additional developers beyond the author specified above.
    def developers = [ [ name: "Ross Rowe", email: "ross@nerderg.com" ]]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "JIRA", url: "https://github.com/nerdErg/GoodForm/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/nerdErg/GoodForm" ]

}
