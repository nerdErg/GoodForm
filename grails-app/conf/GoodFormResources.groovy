modules = {
    goodForm {
        dependsOn 'jquery, jquery-ui'
        resource url:[plugin: 'goodForm', dir:'css', file:'goodform.css'], disposition: 'head'
        resource url:[plugin: 'goodForm', dir:'js', file:'goodform.js'], disposition: 'head'
        resource url:[plugin: 'goodForm', dir:'js', file:"jquery.timeentry.min.js"], disposition: 'head'
    }
}