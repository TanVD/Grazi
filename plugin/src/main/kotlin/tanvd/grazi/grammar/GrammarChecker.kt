package tanvd.grazi.grammar

import com.intellij.psi.PsiElement
import tanvd.grazi.utils.Text
import tanvd.grazi.utils.toPointer
import tanvd.kex.*

class GrammarChecker(private val ignoreChar: LinkedSet<(CharSequence, Char) -> Boolean> = LinkedSet(),
                     private val replaceChar: LinkedSet<(CharSequence, Char) -> Char?> = LinkedSet(),
                     private val ignoreToken: LinkedSet<(String) -> Boolean> = LinkedSet(),
                     private val afterResultReplace: LinkedSet<(String) -> String> = LinkedSet()) {

    constructor(checker: GrammarChecker, ignoreChar: LinkedSet<(CharSequence, Char) -> Boolean> = LinkedSet(),
                replaceChar: LinkedSet<(CharSequence, Char) -> Char?> = LinkedSet(),
                ignoreToken: LinkedSet<(String) -> Boolean> = LinkedSet(),
                afterResultReplace: LinkedSet<(String) -> String> = LinkedSet())
            : this(LinkedSet(checker.ignoreChar + ignoreChar), LinkedSet(checker.replaceChar + replaceChar),
                LinkedSet(checker.ignoreToken + ignoreToken), LinkedSet(checker.afterResultReplace + afterResultReplace))

    companion object {
        object Rules {
            val deduplicateBlanks: (CharSequence, Char) -> Boolean = { prefix, cur ->
                prefix.lastOrNull()?.isWhitespace().orTrue() && cur.isWhitespace()
            }
            //TODO probably we need to flat them only if previous char is not end of sentence punctuation mark
            val flatNewlines: (CharSequence, Char) -> Char? = { _, cur ->
                Text.isNewline(cur).ifTrue { ' ' }
            }

            val ignoreQuotesAtBorders: (String) -> String = { result ->
                val begin = result.indexOfFirst { !it.isWhitespace() && it != '\"' && it != '\'' }
                val end = result.indexOfLast { !it.isWhitespace() && it != '\"' && it != '\'' }
                "\r".repeat(begin) + result.substring(begin, end + 1) + "\r".repeat(result.length - end - 1)
            }
        }

        val default = GrammarChecker(ignoreChar = linkedSetOf(Rules.deduplicateBlanks), replaceChar = linkedSetOf(Rules.flatNewlines))
        val ignoringQuotes = GrammarChecker(default, afterResultReplace = linkedSetOf(Rules.ignoreQuotesAtBorders))
    }

    fun <T : PsiElement> check(vararg tokens: T, getText: (T) -> String = { it.text }) = check(tokens.toList(), getText)

    fun <T : PsiElement> check(tokens: Collection<T>, getText: (T) -> String = { it.text },
                               indexBasedIgnore: (T, Int) -> Boolean = { _, _ -> false }): Set<Typo> {
        if (tokens.isEmpty()) return emptySet()

        val indexesShift = HashMap<Int, Int>()
        val tokenMapping = HashMap<IntRange, T>()

        val resultText = buildString {
            var index = 0
            //iterate through non-ignored tokens
            for (token in tokens.filter { token -> !ignoreToken.any { it(getText(token)) } }) {
                val tokenStartIndex = index

                var totalExcluded = 0
                indexesShift[index] = totalExcluded
                for ((tokenIndex, char) in getText(token).withIndex()) {
                    //perform replacing of char (depending on already seen string)
                    @Suppress("NAME_SHADOWING") val char = replaceChar.untilNotNull { it(this, char) } ?: char

                    //check if char should be ignored
                    if (ignoreChar.any { it(this, char) }
                            || indexBasedIgnore(token, tokenIndex)) {
                        indexesShift[index] = ++totalExcluded
                        continue
                    }

                    append(char)

                    index++
                }
                if (tokenStartIndex < index) {
                    tokenMapping[IntRange(tokenStartIndex, index - 1)] = token
                }

                if (!lastOrNull()?.isWhitespace().orTrue()) {
                    append(' ')
                    index++
                }
            }
        }

        val typos = GrammarEngine.getTypos(afterResultReplace.untilNotNull { it(resultText) } ?: resultText)

        val sortedIndexesShift = indexesShift.toList().sortedBy { it.first }

        return typos.mapNotNull { typo ->
            tokenMapping.filter { typo.location.range.start in it.key }.entries.firstOrNull()?.let { (range, firstToken) ->
                val secondToken = tokenMapping.filter { typo.location.range.endInclusive in it.key }.values.firstOrNull()
                if (firstToken == secondToken) {
                    val startShift = sortedIndexesShift.lastOrNull { it.first <= typo.location.range.start }?.second ?: 0
                    val endShift = sortedIndexesShift.lastOrNull { it.first <= typo.location.range.endInclusive }?.second ?: 0
                    val newRange = IntRange(typo.location.range.start + startShift - range.start, typo.location.range.endInclusive + endShift - range.start)
                    typo.copy(location = typo.location.copy(range = newRange, pointer = firstToken.toPointer()))
                } else null
            }
        }.toSet()
    }
}
