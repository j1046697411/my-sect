package com.sect.game.tools.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class NoChineseInTestMethodNameRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "no-chinese-in-test-method"

    override fun instance(config: Config): RuleSet {
        return RuleSet(
            ruleSetId,
            listOf(NoChineseInTestMethodName(config)),
        )
    }
}
