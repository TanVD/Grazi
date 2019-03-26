package tanvd.grazi.ide

import com.intellij.codeInspection.*
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import tanvd.grazi.model.GrammarEngine

class GraziInspection : LocalInspectionTool() {
    companion object {
        val EP_NAME = ExtensionPointName.create<GraziLanguageSupport>("tanvd.grazi.languageSupport")
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        for (ext in Extensions.getExtensions(EP_NAME)) {
            val blocks = ext.extract(file)
            if (blocks != null) {
                return checkBlocks(blocks, manager, isOnTheFly, ext)
            }
        }
        return emptyArray()
    }

    private fun checkBlocks(
            blocks: List<TextBlock>,
            manager: InspectionManager,
            isOnTheFly: Boolean,
            ext: GraziLanguageSupport
    ): Array<ProblemDescriptor> {
        val result = mutableListOf<ProblemDescriptor>()
        for (block in blocks) {
            val fixes = GrammarEngine.getFixes(block.text)
            fixes.forEach {
                val range = TextRange.create(it.range.start, it.range.endInclusive)
                val quickFixes = it.fix?.map { GraziQuickFix(ext, block, range, it) }?.toTypedArray() ?: emptyArray()
                result += manager.createProblemDescriptor(block.element, range,
                        it.description, ProblemHighlightType.ERROR, isOnTheFly, *quickFixes)
            }
        }
        return result.toTypedArray()
    }
}
