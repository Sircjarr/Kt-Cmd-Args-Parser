import lib.lib_args_parse.CmdArgsParser
import lib.lib_args_parse.exception.CmdArgsParseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CmdArgsRequiredTest {

    @Test
    fun requiredArgsDeclared() {
        class TestArgs(parser: CmdArgsParser) {
            val w: String by parser.requiredMapArg("--mode", map = mapOf("e" to "easy"), valueLabel = "X", hint = "")
            val x: String by parser.requiredArg("-v", valueLabel = "X", hint = "")

            val y: Int? by parser.optionalArg("-x", valueLabel = "Y", hint = "") { it.toInt() }

            val z: Double by parser.requiredArg("-z", valueLabel = "Z", hint = "") { it.toDouble() }
        }

        CmdArgsParser(arrayOf("--mode=e", "-vtest", "-x345", "-z", "7.89"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            assertEquals("easy", it.w)
            assertEquals("test", it.x)
            assertEquals(345, it.y)
            assertEquals(7.89, it.z)
        }.onFailure {
            fail("Should parse successfully")
        }
    }

    @Test
    fun requiredArgsDuplicateOverwritesLast() {
        class TestArgs(parser: CmdArgsParser) {
            val w: String by parser.requiredMapArg("--mode", map = mapOf("e" to "easy"), valueLabel = "X", hint = "")
            val x: String by parser.requiredArg("-v", valueLabel = "X", hint = "")

            val y: Int? by parser.optionalArg("-x", valueLabel = "Y", hint = "") { it.toInt() }

            val z: Double by parser.requiredArg("-z", valueLabel = "Z", hint = "") { it.toDouble() }
        }

        CmdArgsParser(arrayOf("--mode=e", "-vtest", "-x345", "-x", "888", "-z", "7.89"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            assertEquals("easy", it.w)
            assertEquals("test", it.x)
            assertEquals(888, it.y)
            assertEquals(7.89, it.z)
        }.onFailure {
            fail("Should parse successfully")
        }
    }

    @Test
    fun throwsExceptionWhenRequiredArgMissing() {
        class TestArgs(parser: CmdArgsParser) {
            val w: String by parser.requiredMapArg("--mode", map = mapOf("e" to "easy"), valueLabel = "X", hint = "")
            val x: String by parser.requiredArg("-v", valueLabel = "X", hint = "")

            val y: Int? by parser.optionalArg("-x", valueLabel = "Y", hint = "") { it.toInt() }

            val z: Double by parser.requiredArg("-z", valueLabel = "Z", hint = "") { it.toDouble() }
        }

        CmdArgsParser(arrayOf("--mode=e", "-x345", "-z", "7.89"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            fail("Parse should fail")
        }.onFailure {
            assert(it is CmdArgsParseException)
        }
    }
}