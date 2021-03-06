package tanvd.grazi.ide.language.rust

import com.intellij.psi.PsiElement
import org.rust.lang.core.psi.RsLitExpr
import org.rust.lang.core.psi.ext.stubKind
import org.rust.lang.core.stubs.RsStubLiteralKind
import tanvd.grazi.GraziBundle
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport

class RsStringSupport : LanguageSupport(GraziBundle.langConfig("global.literal_string.disabled")) {
    override fun isRelevant(element: PsiElement) = element is RsLitExpr && element.stubKind is RsStubLiteralKind.String

    override fun check(element: PsiElement) = GrammarChecker.default.check(element)
}
