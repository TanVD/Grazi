package tanvd.grazi

import com.intellij.testGuiFramework.framework.*
import com.intellij.testGuiFramework.launcher.ide.CommunityIde
import org.junit.runner.RunWith
import org.junit.runners.Suite
import tanvd.grazi.ide.ui.*

@RunWithIde(CommunityIde::class)
@RunWith(GuiTestSuiteRunner::class)
@Suite.SuiteClasses(NativeLanguageGuiTest::class, RulesTreeGuiTest::class, SpellcheckGuiTest::class, LanguageListGuiTest::class, EditorGuiTest::class)
class GraziGuiTestSuite : GuiTestSuite()
