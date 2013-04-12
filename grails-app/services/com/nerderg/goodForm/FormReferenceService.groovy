package com.nerderg.goodForm

/**
 *
 * Stores custom cross reference services for form elements for use by the {@link com.nerderg.goodForm.FormDataService#validateField}.
 *
 * Projects using Goodforms can add a custom reference service by executing the following as part of the Bootstrap:
 *
 * <pre>
 * def customReferenceService
 * def formReferenceService
 *
 * def init = { servletContext ->
 *    formReferenceService.addReferenceService('yourRefName', {fieldValue -> customReferenceService.performSomeCrossReference(fieldValue)})
 * }
 * </pre>
 *
 *
 * @author Peter McNeil
 * @author Ross Rowe
 */
class FormReferenceService {

    static transactional = true

    private customReferenceServiceMap = [:]

    void addReferenceService(String validationName, Closure closure) {
        customReferenceServiceMap.put(validationName, closure)
    }

    def lookupReference(String referenceName, String fieldValue) {

        Closure referenceService = customReferenceServiceMap[referenceName]
        if (!referenceService) {
            throw new FormReferenceServiceMissingException("No reference lookup service called $referenceName found. Add it using formReferenceService.addReferenceService()")
        }
        return referenceService(fieldValue)
    }
}
/**
 * Custom exception class that is thrown when a reference is defined in the form definition, but not included in the {@link FormReferenceService#customReferenceServiceMap}
 *
 * @author Ross Rowe
 */
class FormReferenceServiceMissingException extends Exception {
    FormReferenceServiceMissingException() {
        super()
    }

    FormReferenceServiceMissingException(String message) {
        super(message)
    }

    FormReferenceServiceMissingException(String message, Throwable cause) {
        super(message, cause)
    }

    FormReferenceServiceMissingException(Throwable cause) {
        super(cause)
    }
}

