package tanvd.grazi.ide.language.properties

import com.intellij.lang.properties.psi.impl.PropertyValueImpl
import com.intellij.psi.PsiElement
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.filterNotToSet

class PropertiesSupport : LanguageSupport() {
    companion object {
        private val ignoredCategories = listOf(Typo.Category.CASING)
    }

    override fun isRelevant(element: PsiElement) = element is PropertyValueImpl

    override fun check(element: PsiElement) = GrammarChecker.default.check(element).filterNotToSet { it.info.category in ignoredCategories }
}
