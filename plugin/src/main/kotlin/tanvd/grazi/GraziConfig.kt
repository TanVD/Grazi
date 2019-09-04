package tanvd.grazi

import com.intellij.openapi.components.*
import com.intellij.openapi.project.ProjectManager
import com.intellij.util.xmlb.annotations.Property
import tanvd.grazi.ide.msg.GraziStateLifecycle
import tanvd.grazi.language.Lang
import tanvd.kex.ifTrue

@State(name = "GraziConfig", storages = [Storage("grazi_global.xml")])
class GraziConfig : PersistentStateComponent<GraziConfig.State> {
    data class State(@Property val enabledLanguages: Set<Lang> = hashSetOf(Lang.AMERICAN_ENGLISH),
                @Property val nativeLanguage: Lang = enabledLanguages.first(),
                @Property val enabledSpellcheck: Boolean = false,
                @Property val userWords: Set<String> = HashSet(),
                @Property val userDisabledRules: Set<String> = HashSet(),
                @Property val userEnabledRules: Set<String> = HashSet(),
                @Property val lastSeenVersion: String? = null,
                val availableLanguages: Set<Lang> = enabledLanguages.filter { it.jLanguage != null }.toSet()) {

        val missedLanguages: Set<Lang>
            get() = enabledLanguages.filter { it.jLanguage == null }.toSet() +
                    ((nativeLanguage.jLanguage == null).ifTrue { setOf(nativeLanguage) } ?: emptySet())

        fun clone() = State(
                enabledLanguages = HashSet(enabledLanguages), nativeLanguage = nativeLanguage, enabledSpellcheck = enabledSpellcheck,
                userWords = HashSet(userWords), userDisabledRules = HashSet(userDisabledRules), userEnabledRules = HashSet(userEnabledRules),
                lastSeenVersion = lastSeenVersion, availableLanguages = availableLanguages)

        fun hasMissedLanguages(withNative: Boolean = true) = (withNative && nativeLanguage.jLanguage == null) || enabledLanguages.any { it.jLanguage == null }

        fun update(enabledLanguages: Set<Lang> = this.enabledLanguages,
                   nativeLanguage: Lang = this.nativeLanguage,
                   enabledSpellcheck: Boolean = this.enabledSpellcheck,
                   userWords: Set<String> = this.userWords,
                   userDisabledRules: Set<String> = this.userDisabledRules,
                   userEnabledRules: Set<String> = this.userEnabledRules,
                   lastSeenVersion: String? = this.lastSeenVersion) = State(enabledLanguages, nativeLanguage, enabledSpellcheck,
                userWords, userDisabledRules, userEnabledRules, lastSeenVersion, enabledLanguages.filter { it.jLanguage != null }.toSet())
    }

    companion object {
        private val instance: GraziConfig by lazy { ServiceManager.getService(GraziConfig::class.java) }

        /**
         * Get copy of Grazi config state
         *
         * Should never be called in GraziStateLifecycle actions
         */
        fun get() = instance.state

        /** Update Grazi config state */
        @Synchronized
        fun update(change: (State) -> State) = instance.loadState(change(get()))
    }

    private var myState = State()

    override fun getState() = myState.clone()

    override fun loadState(state: State) {
        val prevState = myState
        myState = state

        if (prevState != myState) {
            ProjectManager.getInstance().openProjects.forEach { GraziStateLifecycle.publisher.update(prevState, myState, it) }
        }
    }
}
