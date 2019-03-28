package tanvd.grazi.model

import org.junit.Assert.assertEquals
import org.junit.Test
import tanvd.grazi.grammar.GrammarEngineService
import java.io.File
import kotlin.system.measureTimeMillis

class GrammarEngineServiceTest {
    @Test
    fun testCorrectText() {
        val fixes = GrammarEngineService().getFixes("Hello world")
        assertEquals(0, fixes.size)
    }

    @Test
    fun testFirstLetterText() {
        val fixes = GrammarEngineService().getFixes("hello world")
        assertEquals(1, fixes.size)
        assertEquals("Hello", fixes[0].fix!![0])
    }

    @Test
    fun testDifferentTypos() {
        val fixes = GrammarEngineService().getFixes("Hello. world,, tot he")
        assertEquals(3, fixes.size)
        assertEquals(Typo.Category.CASING, fixes[0].category)
        assertEquals(Typo.Category.PUNCTUATION, fixes[1].category)
        assertEquals(Typo.Category.TYPOS, fixes[2].category)
    }

    @Test
    fun testRanges() {
        val fixes = GrammarEngineService().getFixes("hello. world,, tot he.\nThis are my friend")
        assertEquals(5, fixes.size)
        assertEquals(IntRange(0, 5), fixes[0].range)
        assertEquals(IntRange(7, 12), fixes[1].range)
        assertEquals(IntRange(12, 14), fixes[2].range)
        assertEquals(IntRange(15, 21), fixes[3].range)
        assertEquals(IntRange(23, 31), fixes[4].range)
    }

    @Test
    fun testNotCorrectText() {
        val fixes = GrammarEngineService().getFixes("A sentence with a error in the Hitchhiker's Guide tot he Galaxy")
        assertEquals(2, fixes.size)
    }

    @Test
    fun testCachedMiddle() {
        val text = File("src/test/resources/english_big.txt").readText()
        val grammar = GrammarEngineService()
        val fixes1 = grammar.getFixes(text)
        var fixes2: List<Typo> = emptyList()
        val totalTime = measureTimeMillis {
            fixes2 = grammar.getFixes(text)
        }
        assertEquals(fixes1, fixes2)
        assert(0.01 > totalTime / 1000)
    }

    @Test
    fun testCachedBig() {
        val text = File("src/test/resources/english_big2.txt").readText()
        val grammar = GrammarEngineService()
        val fixes1 = grammar.getFixes(text)
        var fixes2: List<Typo> = emptyList()
        val totalTime = measureTimeMillis {
            fixes2 = grammar.getFixes(text)
        }
        assertEquals(fixes1, fixes2)
        assert(0.01 > totalTime / 1000)
    }
}
