package com.nerderg.goodForm.form

/**
 * User: pmcneil
 * Date: 6/08/12
 *
 */
class Updater {

    def goodFormService
    Map formData

    void rename(String from, String to) {
        if (formData && formData instanceof Map) {
            def data = goodFormService.findField(formData, from)
            if (data) {
                goodFormService.setField(formData, to, data)    //todo need to be able to create sub map elements
                goodFormService.removeField(formData, from)
            }
        }
    }

}
