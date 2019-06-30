package tanvd.grazi.ide

import com.intellij.codeInspection.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.ide.quickfix.*
import tanvd.grazi.spellcheck.GraziSpellchecker
import tanvd.grazi.spellcheck.IdeaSpellchecker
import tanvd.grazi.utils.*
import tanvd.kex.buildList

class GraziInspection : LocalInspectionTool() {
    companion object {


        private fun getProblemMessage(fix: Typo): String {
            val message = if (fix.isSpellingTypo) {
                //language=HTML
                """
                    <html>
                        <body>
                            <div>
                                <p>${fix.info.rule.description}</p>
                            </div>
                        </body>
                    </html>
                """.trimIndent()
            } else {
                val examples = fix.info.incorrectExample?.let {
                    if (it.corrections.isEmpty()) {
                        //language=HTML
                        """
                            <tr style='padding-top: 5px;'>
                                <td style='vertical-align: top; color: gray;'>Incorrect:</td>
                                <td>${it.toIncorrectHtml()}</td>
                            </tr>
                        """.trimIndent()

                    } else {
                        //language=HTML
                        """
                            <tr style='padding-top: 5px;'>
                                <td style='vertical-align: top; color: gray;'>Incorrect:</td>
                                <td style='text-align: left'>${it.toIncorrectHtml()}</td>
                            </tr>
                            <tr>
                                <td style='vertical-align: top; color: gray;'>Correct:</td>
                                <td style='text-align: left'>${it.toCorrectHtml()}</td>
                            </tr>
                        """.trimIndent()
                    }
                } ?: ""

                val fixes = if (fix.fixes.isNotEmpty()) {
                    //language=HTML
                    """
                        <tr><td colspan='2' style='padding-bottom: 3px;'>${fix.word} &rarr; ${fix.fixes.take(3).joinToString(separator = ", ")}</td></tr>
                    """
                } else ""

                //language=HTML
                """
                    <html>
                        <body>
                            <div>
                                <table>
                                $fixes
                                <tr><td colspan='2'>${fix.info.rule.description}</td></tr>
                                </table>
                                <table>
                                $examples
                                </table>
                            </div>
                        </body>
                    </html>
                """.trimIndent()
            }
            if (fix.info.rule.description.length > 50 || fix.info.incorrectExample?.example?.length ?: 0 > 50) {
                return message.replaceFirst("<div>", "<div style='width: 300px;'>")
            }
            return message
        }

        private fun createProblemDescriptor(fix: Typo, manager: InspectionManager, isOnTheFly: Boolean): ProblemDescriptor? {
            return fix.location.element?.let { element ->
                val fixes = buildList<LocalQuickFix> {
                    if (fix.info.rule.isDictionaryBasedSpellingRule) {
                        add(GraziAddWord(fix))
                    }

                    if (fix.fixes.isNotEmpty() && isOnTheFly) {
                        if (fix.location.shouldUseRename) {
                            add(GraziRenameTypo(fix))
                        } else {
                            add(GraziReplaceTypo(fix))
                        }
                    }

                    add(GraziDisableRule(fix))
                }

                manager.createProblemDescriptor(element, fix.toSelectionRange(), getProblemMessage(fix),
                        fix.info.category.highlight, isOnTheFly, *fixes.toTypedArray())
            }
        }
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement?) {
                element ?: return
                IdeaSpellchecker.init(element.project)

                val typos = HashSet<Typo>()

                for (ext in LanguageSupport.all.filter { it.isSupported(element.language) && it.isRelevant(element) }) {
                    typos.addAll(ext.getFixes(element))
                }

                if (GraziConfig.state.enabledSpellcheck) {
                    typos.addAll(GraziSpellchecker.getFixes(element))
                }


                typos.mapNotNull { createProblemDescriptor(it, holder.manager, isOnTheFly) }.forEach {
                    holder.registerProblem(it)
                }

                super.visitElement(element)
            }
        }
    }

    override fun getDisplayName(): String {
        return "Grazi proofreading inspection"
    }
}
