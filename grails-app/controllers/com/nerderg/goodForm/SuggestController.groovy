package com.nerderg.goodForm

import grails.converters.JSON

class SuggestController {

    def suggestService

    def suggest(String subject, String term) {
        List<String> suggestions = suggestService.getSuggestions(subject, term)
        render suggestions as JSON
    }

}
