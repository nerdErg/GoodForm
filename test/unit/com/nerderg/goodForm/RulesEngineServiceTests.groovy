package com.nerderg.goodForm

import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.TestFor

/**
 * @author Ross Rowe
 * @author Peter McNeil
 */
@TestFor(RulesEngineService)
class RulesEngineServiceTests {

    void mockRestClient(int status, Map responseData) {
        //Just mock the RestResponse with a map because mockFor(RestResponse) doesn't let me override .json
        def restCLientMock = mockFor(RestBuilder, true)
        restCLientMock.demand.post { String uri, Closure c ->
            println "POST $uri"
            [status: status, json: [responseData]]
        }
        service.rest = restCLientMock.createMock()
    }

    void testNoRulesEngineURI() {
        config.goodform.rulesEngine.uri = null
        shouldFail(RulesEngineException) {
            service.ask("Test", [:])
        }
    }

    void testErrorMessage() {
        mockRestClient(200, [error: 'testing'])
        config.goodform.rulesEngine.uri = 'http://localhost:7070/rulesEngine'
        shouldFail(RulesEngineException) {
            service.ask("Test", [:])
        }
    }

    void testErrorResponseCode() {
        mockRestClient(404, [error: 'testing'])
        config.goodform.rulesEngine.uri = 'http://localhost:7070/rulesEngine'
        shouldFail(RulesEngineException) {
            service.ask("Test", [:])
        }
    }

    void testNoData() {
        mockRestClient(200, [:])
        config.goodform.rulesEngine.uri = 'http://localhost:7070/rulesEngine'
        def results = service.ask("Test", [:])
        assertTrue(results.isEmpty())

    }

    void testNoFacts() {
        config.goodform.rulesEngine.uri = 'http://localhost:7070/rulesEngine'
        Map map = null
        shouldFail(NullPointerException) {
            service.ask("Test", map)
        }
    }
}
