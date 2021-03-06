package tanvd.grazi.ide.ui

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testGuiFramework.fixtures.JDialogFixture
import com.intellij.testGuiFramework.impl.*
import com.intellij.testGuiFramework.util.step
import org.junit.Test
import tanvd.grazi.GraziGuiTestBase

class EditorGuiTest : GraziGuiTestBase() {
    private fun JDialogFixture.toggleTestRules() {
        fun toggle(vararg path: String) {
            with(checkboxTree(*path)) {
                robot().moveMouse(0, 0)
                clickCheckbox()
            }
        }

        toggle("Russian", "Заглавные буквы", "Все буквы в слове ЗАГЛАВНЫЕ")
        toggle("Russian", "False friends", "false friend hint for: актуальный")
        toggle("Russian", "Грамматика", "Согласование с обобщающим словом")

        toggle("English (US)", "Capitalization", "Checks that a sentence starts with an uppercase letter")
        toggle("English (US)", "Punctuation", "Warn when the serial comma is used (incomplete)")
        toggle("English (US)", "Plain English", "Possible wordiness: be a X one")

        toggle("English (US)", "Commonly Confused Words", "'all the further' is a common, but incorrect phrase")
        toggle("English (US)", "Grammar", "'advise', 'help' and 'remind' used with gerund instead of infinitive")
        toggle("English (US)", "Miscellaneous", "Use of 'a' vs. 'an'")
        toggle("English (US)", "Nonstandard Phrases", "very known (very well-known, well-known)")
        toggle("English (US)", "Semantics", "Interval scale: doubling values ('twice as hot')")

        toggle("Russian", "Грамматика", "Склонение  «числительное + существительное»")
        toggle("Russian", "Грамматика", "Склонение (число) «все ли + существительное»")
        toggle("Russian", "Логические ошибки", "Неверная дата, например «31 февраля 2014»")
        toggle("Russian", "Логические ошибки", "Опечатка «не проронила и слово»")
        toggle("Russian", "Общие правила", "Правописание через дефис")

        waitADecentMoment()
    }

    //FIXME Fix and enable test
    @Test
    fun `test rules highlightings in ide editor`() {
        project {
            settings {
                actionButton("Add").click()
                popupMenu("Russian").clickSearchedItem()
                //Do not click OK until russian is loaded
                waitADecentMoment()
            }

            openTestFile()
            editor {
                step("Check default enabled/disabled rules") {
                    typeText(" В коробке лежало пять карандаша.\n")
                    typeText("А все ли ошибка найдены?\n")
                    typeText("Это случилось 31 ноября 2014 г.\n")
                    typeText("За весь вечер она не проронила и слово.\n")
                    typeText("Собрание состоится в конференц зале.\n")
                    typeText("Здесь БОЛЬШИЕ БУКВЫ.\n")
                    typeText("Актуальный плагин.\n")
                    typeText("На конференции работали советы по секциям: химия, история.\n")

                    typeText("\n")

                    typeText("That's all the further I'll go.\n")
                    typeText("He advised staying calm.\n")
                    typeText("The train arrived a hour ago.\n")
                    typeText("He is a very known actor.\n")
                    typeText("It's half as warm as it was yesterday.\n")
                    typeText("Sentence. it was built in 1950.\n")
                    typeText("The pen, pencil, and book are on the desk.\n")
                    typeText("This test is an easy one.\n")


                    waitForCodeAnalysisHighlightCount(HighlightSeverity.INFORMATION, 10)
                    requireHighlights(HighlightSeverity.INFORMATION,
                            "пять карандаша &rarr; пять карандашейСклонение  «числительное + существительное»Incorrect:В коробке лежало пять карандаша.Correct:В коробке лежало пять карандашей.",
                            "все ли ошибка &rarr; все ли ошибкиСклонение (число) «все ли + существительное»Incorrect:А все ли ошибка найдены?Correct:А все ли ошибки найдены?",
                            "Неверная дата, например «31 февраля 2014»Incorrect:Это случилось 31 ноября 2014 г.",
                            "не проронила и слово &rarr; не проронила ни словаОпечатка «не проронила и слово»Incorrect:За весь вечер она не проронила и слово.Correct:За весь вечер она не проронила ни слова.",
                            "конференц зале &rarr; конференц-залеПравописание через дефисIncorrect:Собрание состоится в конференц зале.Correct:Собрание состоится в конференц-зале.",
                            "all the further &rarr; as far as'all the further' is a common, but incorrect phraseIncorrect:That's all the further I'll go.Correct:That's as far as I'll go.",
                            "staying &rarr; to stay'advise', 'help' and 'remind' used with gerund instead of infinitiveIncorrect:He advised staying calm.Correct:He advised to stay calm.",
                            "a &rarr; anUse of 'a' vs. 'an'Incorrect:The train arrived a hour ago.Correct:The train arrived an hour ago.",
                            "very known &rarr; very well-known/well-knownvery known (very well-known, well-known)Incorrect:He is a very known actor.Correct:He is a very well-known actor.",
                            "Interval scale: doubling values ('twice as hot')Incorrect:It's half as warm as it was yesterday.")
                }
            }

            step("Check disabling/enabling rules") {
                settings {
                    toggleTestRules()
                }

                editor {
                    waitForCodeAnalysisHighlightCount(HighlightSeverity.INFORMATION, 6)
                    requireHighlights(HighlightSeverity.INFORMATION,
                            "БОЛЬШИЕ &rarr; большиеВсе буквы в слове ЗАГЛАВНЫЕIncorrect:Не выделяйте текст ЗАГЛАВНЫМИ буквами.Correct:Не выделяйте текст заглавными буквами.",
                            "Актуальный &rarr; Настоящий/Фактический/Реальныйfalse friend hint for: актуальный",
                            "химия, история &rarr; химии, историиСогласование с обобщающим словомIncorrect:На конференции работали  советы по секциям: химия, история.Correct:На конференции работали  советы по секциям: химии, истории.",
                            "it &rarr; ItChecks that a sentence starts with an uppercase letterIncorrect:This house is old. it was built in 1950.Correct:This house is old. It was built in 1950.",
                            "pencil, and &rarr; pencil andWarn when the serial comma is used (incomplete)Incorrect:The pen, pencil, and book are on the desk.Correct:The pen, pencil and book are on the desk.",
                            "is an easy one &rarr; is easyPossible wordiness: be a X oneIncorrect:This test is an easy one.Correct:This test is easy.")
                }
            }

            step("Returning state back") {
                settings {
                    toggleTestRules()
                    button("Apply").click()

                    jList("English (US)").selectItems("Russian")
                    actionButton("Remove").click()
                }
            }
        }
    }
}
