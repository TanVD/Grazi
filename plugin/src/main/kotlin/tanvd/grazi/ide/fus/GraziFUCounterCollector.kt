package tanvd.grazi.ide.fus

import com.intellij.internal.statistic.eventLog.FeatureUsageData
import com.intellij.internal.statistic.service.fus.collectors.FUCounterUsageLogger
import tanvd.grazi.grammar.Typo
import tanvd.grazi.language.Lang

object GraziFUCounterCollector {
    private fun log(eventId: String, body: FeatureUsageData.() -> Unit) = FUCounterUsageLogger.getInstance()
            .logEvent("grazi", eventId, FeatureUsageData().apply(body))

    fun logLanguageDetectionResult(lang: Lang?) = log("detection") {
        addData("language", lang?.shortCode ?: "")
    }

    fun logTypo(typo: Typo, isSpellcheck: Boolean) = log("typo") {
        addData("id", typo.info.rule.id)
        addData("fixes", typo.fixes.size)
        addData("spellcheck", isSpellcheck)
    }

    fun logQuickFixResult(ruleId: String, cancelled: Boolean, isSpellcheck: Boolean) = log("quickfix") {
        addData("id", ruleId)
        addData("cancelled", cancelled)
        addData("spellcheck", isSpellcheck)
    }
}
