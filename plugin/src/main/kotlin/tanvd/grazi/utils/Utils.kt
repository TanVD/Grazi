package tanvd.grazi.utils

import org.languagetool.rules.RuleMatch

fun RuleMatch.toIntRange(offset: Int = 0) = IntRange(fromPos + offset, toPos + offset - 1)

fun IntRange.withOffset(offset: Int) = IntRange(start + offset, endInclusive + offset)

fun <T> List<T>.dropFirstIf(body: (T) -> Boolean) = this.firstOrNull()?.let { if (body(it)) drop(1) else this } ?: this

fun String.filterOutNewLines() = this.replace("\n", "")

fun String.safeSubstring(startIndex: Int) = if (this.length <= startIndex) "" else substring(startIndex)
