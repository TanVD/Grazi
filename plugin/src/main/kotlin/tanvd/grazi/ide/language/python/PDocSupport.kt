package tanvd.grazi.ide.language.python

import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.*
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport

class PDocSupport : LanguageSupport() {
    override fun isRelevant(element: PsiElement) = element is PyStringLiteralExpression && element.isDocString

    override fun check(element: PsiElement): Set<Typo> {
        require(element is PyStringLiteralExpression) { "Got the non doc PyStringLiteralExpression in a PDocSupport" }

        return GrammarChecker.ignoringQuotes.check(element.stringElements, indexBasedIgnore = { token, index ->
            when (token) {
                is PyFormattedStringElement -> token.literalPartRanges.all { index !in it }
                is PyPlainStringElement -> false
                else -> false
            }
        })
    }
}
