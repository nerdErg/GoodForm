modules = {
    goodForm {
        dependsOn 'jquery, jquery-ui'
        resource url:[plugin: 'goodForm', dir:'css', file:'goodform.css']
        resource url:[plugin: 'goodForm', dir:'js', file:'goodform.js']
        resource url:[plugin: 'goodForm', dir:'js', file:"jquery.timeentry.min.js"]
    }
}