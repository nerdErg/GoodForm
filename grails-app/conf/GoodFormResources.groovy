modules = {
    goodForm {
        dependsOn 'nerdergFormTags'
        resource url:[plugin: 'goodForm', dir:'css', file:'goodform.css']
        resource url:[plugin: 'goodForm', dir:'js', file:'goodform.js']
    }
}