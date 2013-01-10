package com.nerderg.goodForm

import groovyx.net.http.URIBuilder
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import groovyx.net.http.RESTClient
import org.apache.http.client.ClientProtocolException
import net.sf.json.JSONArray
import grails.converters.JSON

/**
 * Handles making REST requests to a One-Ring instance to process rules.
 */
class RulesEngineService {

    static transactional = false

    def getRulesEngineRestUri() {
        return new URIBuilder(ConfigurationHolder.config.rulesEngine.uri.toString() + "/rest/applyRules")
    }

    /**
     * ask the rules engine to process a single map of facts and return a processed JSON object. This method handles
     * errors by throwing a RulesEngineException if a response other than 200 is received, the response includes
     * an error element or any exception is caught.
     *
     * @param ruleSet the name of the ruleSet to run
     * @param facts a map of facts to pass to the ruleSet
     * @return a net.sf.JSONObject of results - a map of results
     * @see http://json-lib.sourceforge.net/apidocs/net/sf/json/JSONObject.html
     * @throws RulesEngineException, NullPointerException
     */
    def ask(String ruleSet, Map facts) {
        if (facts == null) {
            throw new NullPointerException("Facts Map can't be null")
        }

        try {
            def uri = getRulesEngineRestUri()
            def rulesEngine = new RESTClient(uri.toString())

            log.debug "ask json ${uri.toString()} $ruleSet"
            log.debug facts.toMapString(2)

            def resp = rulesEngine.post(body: [ruleSet: ruleSet, facts: [facts]],
                    requestContentType: groovyx.net.http.ContentType.JSON)
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
        } catch (Exception e) {
            throw new RulesEngineException(e)
        }
    }

    /**
     * ask the rules engine to process a list of maps of facts and return the raw JSON Object
     * This leaves the processing of errors to the caller.
     * @param ruleSet
     * @param facts
     * @return HttpResponseDecorator containing a net.sf.JSONObject of results in resp.data - a List of maps in a data property
     * @see groovyx.net.http.HttpResponseDecorator http://json-lib.sourceforge.net/apidocs/net/sf/json/JSONObject.html
     * @see groovyx.net.http.RESTClient http://groovy.codehaus.org/modules/http-builder/doc/rest.html
     * @throws java.net.URISyntaxException , org.apache.http.client.ClientProtocolException, java.io.IOException
     */
    def ask(String ruleSet, List facts) {
        if (!facts) {
            log.warn "no facts set to ask rules engine to $ruleSet"
            return []
        }
        def uri = getRulesEngineRestUri()
        def rulesEngine = new RESTClient(uri.toString())

//        log.debug "ask json ${uri.toString()} $ruleSet ${facts.toString(2)}"
        def results
        return rulesEngine.post(body: [ruleSet: ruleSet, facts: facts],
                requestContentType: groovyx.net.http.ContentType.JSON)
    }

    Map cleanUpJSONNullMap(Map m) {
        m.each {
            if (it.value.equals(null)) {
                it.value = null
            } else if (it.value instanceof Map) {
                it.value = cleanUpJSONNullMap(it.value)
            } else if (it.value instanceof Collection) {
                it.value = cleanUpJSONNullCollection(it.value)
            }
        }
    }

    Collection cleanUpJSONNullCollection(Collection c) {
        //create a new collection sans JSONObject.Null objects
        List collect = []
        c.each { v ->
            if (!v.equals(null)) {
                if (v instanceof Collection) {
                    collect.add(cleanUpJSONNullCollection(v))
                } else if (v instanceof Map) {
                    collect.add(cleanUpJSONNullMap(v))
                } else {
                    collect.add(v)
                }
            } else {
                collect.add(null)
            }
        }
        return collect
    }
}

class RulesEngineException extends Throwable {
    def RulesEngineException() {
        super()
    }

    def RulesEngineException(String message) {
        super(message)
    }

    def RulesEngineException(String message, Throwable cause) {
        super(message, cause)
    }

    def RulesEngineException(Throwable cause) {
        super(cause)
    }

}
