package tanvd.grazi.ide.language

import tanvd.grazi.GraziTestBase


class PropertiesSupportTest : GraziTestBase(true) {
    fun `test grammar check in file`() {
        runHighlightTestForFile("ide/language/properties/Example.properties")
    }
}
