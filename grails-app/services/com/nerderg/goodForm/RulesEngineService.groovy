package com.nerderg.goodForm

import grails.plugins.rest.client.RestBuilder
import org.springframework.web.client.ResourceAccessException

/**
 * Handles making REST requests to a <a href="">One-Ring</a> instance to process rules.
 *
 * @author Peter McNeil
 */
class RulesEngineService {

    static transactional = false

    def grailsApplication

    private String rulesEngineUri = null
    private RestBuilder rest = new RestBuilder()

    String getRulesEngineRestUri() {
        if(!rulesEngineUri) {
            String uri = grailsApplication.config.goodform.rulesEngine.uri
            if (!uri) {
                throw new RulesEngineException("rulesEngine.uri must be defined in grails-app/conf/Config.groovy")
            }
            uri = (uri.endsWith('/') ? uri : "$uri/")
            rulesEngineUri = "${uri}rest/applyRules"
        }
        return rulesEngineUri
    }

    /**
     * Ask the rules engine to process a single map of facts and return a processed JSON object. This method handles
     * errors by throwing a RulesEngineException if a response other than 200 is received, the response includes
     * an error element or any exception is caught.
     *
     * @param ruleSet the name of the ruleSet to run
     * @param facts a map of facts to pass to the ruleSet
     * @return a net.sf.json.JSONObject of results - a map of results
     * @see <a href='http://json-lib.sourceforge.net/apidocs/net/sf/json/JSONObject.html'>JSONObject</a>
     * @throws RulesEngineException , NullPointerException
     */
    def ask(String ruleSet, Map facts) {
        if (facts == null) {
            throw new NullPointerException("Facts Map can't be null")
        }

        try {
            log.debug facts.toMapString(2)
            def resp = askRuleset(ruleSet, [facts])
            Map data = (resp.json as List)[0] as Map
            if (resp.status != 200) {
                throw new RulesEngineException("Error talking to Rules Engine: $resp.status, error: ${data?.error}")
            }
            if (data?.error) {
                throw new RulesEngineException("Error Rules Engine: Invoking $ruleSet got error ${data?.error}")
            }
            if (data.equals(null)) {
                data = null //turn it into a real null
            }
            return data
        }
        catch (RulesEngineException e) {
            throw e
        }
        catch (ResourceAccessException e) {
            log.error e.message
            throw new RulesEngineException("Unable to connect to the Rules Engine. at ${getRulesEngineRestUri()}", e)
        }
        catch (Exception e) {
            throw new RulesEngineException(e)
        }
    }

    /**
     * Ask the rules engine to process a list of maps of facts and return the raw JSON Object
     * This leaves the processing of errors to the caller.
     * @param ruleSet
     * @param facts
     * @return RestResponse
     * @see grails.plugins.rest.client.RestBuilder http://springsource.github.io/grails-data-mapping/rest-client/api/grails/plugins/rest/client/RestResponse.html
     */
    def askRuleset(String ruleSet, List facts) {
        if (!facts) {
            log.warn "no facts set to ask rules engine to $ruleSet"
            return []
        }
        def uri = getRulesEngineRestUri()

        log.debug "ask json ${uri} $ruleSet"

        return rest.post(uri) {
            contentType "application/json"
            json ([ruleSet: ruleSet, facts: facts])
        }
    }

}

class RulesEngineException extends Throwable {
    RulesEngineException() {
        super()
    }

    RulesEngineException(String message) {
        super(message)
    }

    RulesEngineException(String message, Throwable cause) {
        super(message, cause)
    }

    RulesEngineException(Throwable cause) {
        super(cause)
    }
}
