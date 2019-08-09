package tanvd.grazi.ide.language.go

import com.goide.inspections.fmtstring.GoFmtStringUtil
import com.goide.inspections.fmtstring.parser.tokens.GoFmtStringToken
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
        return GrammarChecker.ignoringQuotes.check(element).filter {
            GoFmtStringUtil.parse(element).forEach { fmt ->
                if (checkIfTextRangeNearFmt(element.text, it.location.range, fmt)) return@filter false
            }

            true
        }.toSet()
    }

    private fun checkIfTextRangeNearFmt(text: String, range: IntRange, fmt: GoFmtStringToken): Boolean {
        var start = fmt.start
        while (start > 0 && text[start - 1].isWhitespace()) {
            start--
        }

        var end = fmt.end
        while (end < text.length - 1 && text[end].isWhitespace()) {
            end++
        }

        if (range.start <= end + 1 && range.endInclusive >= start - 1) return true

        return false
    }
}
