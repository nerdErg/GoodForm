package com.nerderg.goodForm

import org.junit.Test

/**
 * @author Ross Rowe
 */
class FormAdminControllerIntegrationTests extends AbstractIntegrationTest {

    private FormAdminController controller = new FormAdminController()

    @Test
    void index() {
        controller.index()
    }

}
