package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class GoSupportTest : GraziTestBase(true) {
    fun `test spellcheck in constructs`() {
        runHighlightTestForFile("ide/language/go/Constructs.go")
    }

    fun `test grammar check in docs`() {
        runHighlightTestForFile("ide/language/go/Docs.go")
    }

    fun `test grammar check in string literals`() {
        runHighlightTestForFile("ide/language/go/StringLiterals.go")
    }
}
