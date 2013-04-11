package com.nerderg.goodForm

import grails.test.mixin.TestFor

import org.junit.Before

/**
 * @author Ross Rowe
 */
@TestFor(RulesEngineService)
class RulesEngineServiceTests {

    class MockRESTClient {
        def responseCode = 200
        def result = [:]

        def post(Map<String, ?> args) {
            [status: responseCode, data: [result]]
        }
    }

    def restClient = new MockRESTClient()

    @Before void setUp() {
        service.rulesEngine = restClient
    }

    void testNoRulesEngineURI() {
        shouldFail(RulesEngineException) {
            service.ask("Test", [:])
        }
    }

    void testErrorMessage() {
        config.rulesEngine.uri = 'http://localhost'
        def result = restClient.result
        result.put('error', 'testing')
        shouldFail(RulesEngineException) {
            service.ask("Test", [:])
        }
    }

    void testErrorResponseCode() {
        config.rulesEngine.uri = 'http://localhost'
        restClient.responseCode = 404
        shouldFail(RulesEngineException) {
            service.ask("Test", [:])
        }
    }

    void testNoData() {
        config.rulesEngine.uri = 'http://localhost'
        def results = service.ask("Test", [:])
        assertTrue(results.isEmpty())

    }

    void testNoFacts() {
        config.rulesEngine.uri = 'http://localhost'
        Map map = null
        shouldFail(NullPointerException) {
            service.ask("Test", map)
        }
    }
}
