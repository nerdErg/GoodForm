package com.nerderg.goodForm

import javax.naming.Context
import javax.naming.InitialContext

/**
 * Validates the postcode, language and country field against a pre-defined CSV file.
 *
 * needs the pc-full csv file from here http://auspost.com.au/products-and-services/download-postcode-data.html
 * modified to remove all columns after the state and to make it tab separated.
 *
 * TODO move this into a separate Grails plugin
 */
class AddressWranglingService {

    def grailsApplication
    static transactional = true

    private Map suburbByPostcode
    private Map stateByPostcode
    private Set countries
    private Set languages

    AddressWranglingService() {
        try {
            Context initContext = new InitialContext()
            Context envContext = initContext.lookup("java:comp/env")
            String dataDirectory = envContext.lookup("dataDirectory")
            if (dataDirectory) {
                println "Overriding data directory from context env (e.g. tomcat context.xml): $dataDirectory"
                grailsApplication.config.goodform.data.directory = dataDirectory
            }
        } catch (e) {
            //expected if context not set
        }
    }

    private Map<String, Set<Integer>> getStatePostcodeMap() {
        if (stateByPostcode) {
            return stateByPostcode
        }
        getPostcodeMap()
        return stateByPostcode
    }

    private Map<Integer, Set<String>> getPostcodeMap() {
        if (suburbByPostcode) {
            return suburbByPostcode
        }
        Map<Integer, Set<String>> subpc = [:]
        Map<String, Set<Integer>> statepc = [:]
        String dataDir = grailsApplication.config.goodform.data.directory
        File postcodeFile = new File(dataDir, 'postcodes.csv')
        postcodeFile.eachLine { line ->
            String[] fields = line.split('\t')
            if (fields[0].isInteger()) {
                Integer pcode = fields[0].toInteger()
                Set<String> suburbs = subpc[pcode] ?: new HashSet<String>()
                suburbs.add(fields[1])
                subpc.put(pcode, suburbs)
                if (fields[2] != 'State') {
                    Set<Integer> codes = statepc[fields[2]] ?: new HashSet<Integer>()
                    codes.add(pcode)
                    statepc.put(fields[2], codes)
                }
            }
        }
        suburbByPostcode = Collections.unmodifiableMap(subpc)
        stateByPostcode = Collections.unmodifiableMap(statepc)
        return suburbByPostcode
    }

    private Set<String> getCountries() {
        if (countries) {
            return countries
        }
        Set<String> country = []
        String dataDir = grailsApplication.config.goodform.data.directory
        File countryFile = new File(dataDir, 'countries.csv')
        countryFile.eachLine { line ->
            String[] fields = line.split('\t')
            country.add(fields[0])
        }
        countries = Collections.unmodifiableSet(country)
        return countries
    }

    private Set<String> getLanguages() {
        if (languages) {
            return languages
        }
        Set<String> lang = []
        String dataDir = grailsApplication.config.goodform.data.directory
        File countryFile = new File(dataDir, 'languages.txt')
        countryFile.eachLine { line ->
            lang.add(line.toUpperCase())
        }
        languages = Collections.unmodifiableSet(lang)
        return languages
    }

    List<String> findLanguage(String query) {
        def languages = getLanguages()
        String q = query.toUpperCase()
        List matches = languages.findAll { it.contains(q) }.sort {a, b ->
            a.indexOf(q) <=> b.indexOf(q)
        }
        return matches
    }

    List<String> findCountries(String query) {
        def countries = getCountries()
        String q = query.toUpperCase()
        return countries.findAll { it.contains(q) }.sort {a, b ->
            a.indexOf(q) <=> b.indexOf(q)
        }
    }

    Set<String> getSuburbsByPostcode(Integer postcode) {
        getPostcodeMap()[postcode]
    }

    String getState(String postcode) {
        if (postcode && postcode.isInteger()) {
            Integer pc = postcode.toInteger()
            Map.Entry kv = getStatePostcodeMap().find { key, val ->
                val.contains(pc)
            }
            return kv?.key
        }
        return null
    }

    Boolean isValidPostcode(String postcode) {
        if (postcode && postcode.isInteger()) {
            Integer pc = postcode.toInteger()
            Set suburbs = getSuburbsByPostcode(pc)
            return !(suburbs == null || suburbs.empty)
        }
    }

    Boolean isPostcodeInState(String state, Integer postcode) {
        Set postcodes = getStatePostcodeMap()[state]
        return postcodes.contains(postcode)
    }

    String findSuburb(String address, String postcode) {
        if (address && postcode && postcode.isInteger()) {
            HashSet<String> suburbs = getPostcodeMap()[postcode.toInteger()]
            String upperAddress = address.toUpperCase()
            HashSet<String> candidates = suburbs.findAll() { suburb ->
                upperAddress.contains(suburb)
            }

            if (candidates.size() == 1) {
                return candidates.find { return it }  //get it
            }

            if (candidates.size() > 1) {
                //multiple matches find the last match in the address string and return that
                int position = 0
                String lastSuburb = null
                candidates.each { suburb ->
                    int p = upperAddress.indexOf(suburb)
                    if (p > position) {
                        position = p
                        lastSuburb = suburb
                    }
                }
                return lastSuburb
            }

            if (candidates.size() == 0) {
                return null
            }
        }
        return null
    }
}
