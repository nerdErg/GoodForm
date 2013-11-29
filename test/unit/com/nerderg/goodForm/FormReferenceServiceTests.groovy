package com.nerderg.goodForm

import com.nerderg.goodForm.form.Form
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import org.junit.Before

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(FormReferenceService)
@TestMixin(ControllerUnitTestMixin)
class FormReferenceServiceTests {

    Form form = new Form()

    @Before
    void setUp() {

        service.addReferenceService("file", { String fieldValue ->
            if (fieldValue == '1234') {
                //valid file
                return 23
            } else {
                //invalid file
                return null
            }
        })
    }

    void testValidCrossReference() {
        def reference = service.lookupReference('file', '1234')
        assertNotNull("Reference was not found", reference)
    }

    void testInvalidCrossReference() {
        def reference = service.lookupReference('file', '5678')
        assertNull("Reference was not not null", reference)
    }

}
