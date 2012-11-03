package com.nerderg.goodForm

/**
 * Invoke the One-Ring engine locally (as opposed to {@link RulesEngineService} which invokes
 * the engine via a REST call).
 *
 * This service is intended to be used for local development and testing and not for production environments.
 */
class LocalRulesEngineService {

    def com.nerderg.rules.RulesEngineService rulesEngineService

    def ask(String ruleSet, Map facts) {
        return rulesEngineService.fireRules(ruleSet, facts)
    }
}
