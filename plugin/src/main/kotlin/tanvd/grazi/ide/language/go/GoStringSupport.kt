package tanvd.grazi.ide.language.go

import com.goide.psi.GoStringLiteral
import com.intellij.psi.PsiElement
import tanvd.grazi.GraziBundle
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport

class GoStringSupport : LanguageSupport(GraziBundle.langConfig("global.literal_string.disabled")) {
    override fun isRelevant(element: PsiElement) = element is GoStringLiteral

    override fun check(element: PsiElement): Set<Typo> {
        require(element is GoStringLiteral) { "Got not GoStringLiteral in a GoStringSupport" }

        return GrammarChecker.ignoringQuotes.check(element)
    }
}
