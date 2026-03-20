package com.sect.game.tools.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.Debt
import org.jetbrains.kotlin.psi.KtNamedFunction

class NoChineseInTestMethodName(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        javaClass.simpleName,
        Severity.Warning,
        "测试方法名不应包含中文字符，请使用英文命名",
        Debt.TEN_MINS
    )

    private val chinesePattern = Regex("[\u4e00-\u9fa5]")

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)

        if (!isTestFunction(function)) {
            return
        }

        val methodName = function.name
        if (methodName != null && chinesePattern.containsMatchIn(methodName)) {
            report(
                CodeSmell(
                    issue,
                    Entity.atName(function),
                    "测试方法 '${methodName}' 名称中包含中文字符，应使用英文命名"
                )
            )
        }
    }

    private fun isTestFunction(function: KtNamedFunction): Boolean {
        return function.annotationEntries.any { annotation ->
            val annotationText = annotation.text
            annotationText.contains("Test") ||
                (annotation.typeReference?.text == "Test")
        }
    }
}
