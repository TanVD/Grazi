package tanvd.grazi.language

import org.languagetool.*
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.GraziLifecycle
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object LangTool : GraziLifecycle {
    private val langs: MutableMap<Lang, JLanguageTool> = ConcurrentHashMap()

    private const val cacheMaxSize = 25_000L
    private const val cacheExpireAfterMinutes = 5

    operator fun get(lang: Lang): JLanguageTool {
        return langs.getOrPut(lang) {
            val cache = ResultCache(cacheMaxSize, cacheExpireAfterMinutes, TimeUnit.MINUTES)
            JLanguageTool(lang.jLanguage, GraziConfig.state.nativeLanguage.jLanguage,
                    cache, UserConfig(GraziConfig.state.userWords.toList())).apply {
                lang.configure(this)

                allRules.forEach { rule ->
                    if (rule.id in GraziConfig.state.userDisabledRules) disableRule(rule.id)
                    if (rule.id in GraziConfig.state.userEnabledRules) enableRule(rule.id)
                }

                // In case of English spellcheck will be done by Grazi spellchecker
                if (lang.isEnglish()) {
                    disableRules(allActiveRules.filter { it.isDictionaryBasedSpellingRule }.map { it.id })
                }
            }
        }
    }

    override fun init() {
        for (lang in GraziConfig.state.enabledLanguages) {
            get(lang)
        }
    }

    override fun reset() {
        langs.clear()
    }
}
