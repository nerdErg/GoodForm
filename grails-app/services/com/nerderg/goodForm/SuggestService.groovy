package com.nerderg.goodForm

import javax.naming.Context
import javax.naming.InitialContext

/**
 * A service to provide suggestions for things. Works with SuggestController
 */
class SuggestService {

    def grailsApplication
    def addressWranglingService

    static transactional = false

    private List<String> banks

    protected Map<String, Closure> suggestionHandlers = [:]

    static final titles = ['Mr', 'Ms', 'Miss', 'Mrs', 'Master', 'Dr', 'Professor', 'Sir']
    static final nameTypes = ['maiden name', 'previous married name', 'alias', 'name at birth', 'pseudonym', 'twitter name']

    SuggestService() {
        try {
            Context initContext = new InitialContext()
            Context envContext = initContext.lookup("java:comp/env")
            String dataDirectory = envContext.lookup("goodformDataDirectory")
            if (dataDirectory) {
                println "Overriding goodForm data directory from context env (e.g. tomcat context.xml): $dataDirectory"
                grailsApplication.config.goodForm.data.directory = dataDirectory
            }
        } catch (e) {
            //expected if context not set
        }
        addSuggestionHandler('title', title)
        addSuggestionHandler('nameType', nameType)
        addSuggestionHandler('country', country)
        addSuggestionHandler('language', language)
        addSuggestionHandler('bank', bank)
    }

    private List<String> getBanks() {
        if(!banks) {
            List<String> banksM = []
            String dataDir = grailsApplication.config.goodForm.data.directory ?: '.'
            File bankFile = new File(dataDir, 'banks.txt')
            bankFile.eachLine { line ->
                banksM.add(line)
            }
            banks = Collections.unmodifiableList(banksM)
        }
        return banks
    }

    void addSuggestionHandler(String subject, Closure handler) {
        suggestionHandlers.put(subject, handler)
    }

    List<String> getSuggestions(String subject, String term) {
        suggestionHandlers[subject](term)
    }

    def title = { String term ->
        simpleSearchList(titles, term)
    }

    def nameType = { String term ->
        simpleSearchList(nameTypes, term)
    }

    def country = { String term ->
        addressWranglingService.findCountries(term)
    }

    def language = { String term ->
        addressWranglingService.findLanguage(term)
    }

    def bank = { String term ->
        String q = term.toUpperCase()
        return getBanks().findAll { it.toUpperCase().contains(q) }
    }

    private static List<String> simpleSearchList(List<String> list, String term) {
        String q = term.toUpperCase()
        list.findAll { String item -> item.toUpperCase().startsWith(q) }
    }

}
