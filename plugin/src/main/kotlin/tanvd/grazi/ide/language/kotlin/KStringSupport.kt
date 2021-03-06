package tanvd.grazi.ide.language.kotlin

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.*
import tanvd.grazi.GraziBundle
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.filterFor
import tanvd.grazi.utils.filterNotToSet

class KStringSupport : LanguageSupport(GraziBundle.langConfig("global.literal_string.disabled")) {
    companion object {
        private fun isExpressionEntry(entry: PsiElement?) = entry is KtStringTemplateEntryWithExpression || entry is KtSimpleNameStringTemplateEntry
    }

    override fun isRelevant(element: PsiElement) = element is KtStringTemplateExpression

    override fun check(element: PsiElement): Set<Typo> {
        val entries = element.filterFor<KtLiteralStringTemplateEntry>()

        return GrammarChecker.default.check(entries).filterNotToSet {
            (it.location.isAtEnd() && isExpressionEntry(it.location.element?.nextSibling))
                    || (it.location.isAtStart() && isExpressionEntry(it.location.element?.prevSibling))
        }
    }
}
