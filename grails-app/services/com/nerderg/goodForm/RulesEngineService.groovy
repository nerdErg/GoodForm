package com.nerderg.goodForm

import groovyx.net.http.RESTClient
import groovyx.net.http.URIBuilder
import net.sf.json.JSONArray

import org.apache.http.client.ClientProtocolException

/**
 * Handles making REST requests to a <a href="">One-Ring</a> instance to process rules.
 *
 * @author Peter McNeil
 */
class RulesEngineService {

    static transactional = false

    def grailsApplication

    def rulesEngine

    def getRulesEngineRestUri() {
        if (!grailsApplication.config.rulesEngine.uri.toString()) {
            throw new RulesEngineException("rulesEngine.uri must be defined in grails-app/conf/Config.groovy")
        }
        return new URIBuilder(grailsApplication.config.rulesEngine.uri.toString() + "/rest/applyRules")
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
            if (resp.status != 200) {
                throw new RulesEngineException("Error talking to Rules Engine: $resp.status")
            }
            if (resp.data[0].error) {
                throw new RulesEngineException("Error Rules Engine: Invoking $ruleSet got error ${resp.data[0].error}")
            }
            def data = resp.data[0]
            if (data.equals(null)) {
                data = null //turn it into a real null
            }
            return data

        } catch (ClientProtocolException e) {
            String errorMessage = e.message
            if (e.response?.responseData && e.response.responseData instanceof JSONArray && e.response.responseData.size() > 0 && e.response.responseData[0].error) {
                errorMessage = e.response.responseData[0].error
            }
            throw new RulesEngineException(errorMessage)
        }
        catch (RulesEngineException e) {
            throw e
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
     * @return HttpResponseDecorator containing a net.sf.json.JSONObject of results in resp.data - a List of maps in a data property
     * @see groovyx.net.http.HttpResponseDecorator http://json-lib.sourceforge.net/apidocs/net/sf/json/JSONObject.html
     * @see groovyx.net.http.RESTClient http://groovy.codehaus.org/modules/http-builder/doc/rest.html
     * @throws java.net.URISyntaxException , org.apache.http.client.ClientProtocolException, java.io.IOException
     */
    def askRuleset(String ruleSet, List facts) {
        if (!facts) {
            log.warn "no facts set to ask rules engine to $ruleSet"
            return []
        }
        def uri = getRulesEngineRestUri()
        if (!rulesEngine) {
            rulesEngine = new RESTClient(uri)
        }
        log.debug "ask json ${uri} $ruleSet"
        return rulesEngine.post(body: [ruleSet: ruleSet, facts: facts],
                requestContentType: groovyx.net.http.ContentType.JSON)
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
