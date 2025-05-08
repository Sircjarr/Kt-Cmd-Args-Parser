import lib.lib_args_parse.CmdArgsParser
import lib.lib_args_parse.exception.MalformedArgsException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CmdArgsPositionalsTest {

    @Test
    fun singlePositionalParsed() {
        class TestArgs(parser: CmdArgsParser) {
            val x: String by parser.positionalArg(valueLabel = "", hint = "")
        }

        CmdArgsParser(arrayOf("hello_world"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            assertEquals("hello_world", it.x)
        }.onFailure {
            fail("Should parse successfully")
        }
    }

    @Test
    fun multiplePositionalsParsed() {
        class TestArgs(parser: CmdArgsParser) {
            val w: String by parser.requiredMapArg("--mode", map = mapOf("e" to "easy"), valueLabel = "X", hint = "")

            val x: String by parser.positionalArg(valueLabel = "X", hint = "")
            val y: Int by parser.positionalArg(valueLabel = "Y", hint = "") { it.toInt() }
            val z: Double by parser.positionalArg(valueLabel = "Z", hint = "") { it.toDouble() }
        }

        CmdArgsParser(arrayOf("--mode=e", "test", "345", "29.87"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            assertEquals("easy", it.w)

            assertEquals("test", it.x)
            assertEquals(345, it.y)
            assertEquals(29.87, it.z)
        }.onFailure {
            fail("Should parse successfully")
        }
    }

    @Test
    fun missingPositionalThrowsException() {
        class TestArgs(parser: CmdArgsParser) {
            val w: String by parser.requiredArg("-v", valueLabel = "VAL", hint = "")

            val x: String by parser.positionalArg(valueLabel = "X", hint = "")
            val y: Int by parser.positionalArg(valueLabel = "Y", hint = "") { it.toInt() }
            val z: Double by parser.positionalArg(valueLabel = "Z", hint = "") { it.toDouble() }
        }

        CmdArgsParser(arrayOf("-v=value", "test", "345"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            fail("Should fail parsing")
        }.onFailure {
            assert(it is MalformedArgsException)
        }

        CmdArgsParser(arrayOf("-v=value", "test"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            fail("Should fail parsing")
        }.onFailure {
            assert(it is MalformedArgsException)
        }
    }

    @Test
    fun delimiterWorks() {
        class TestArgs(parser: CmdArgsParser) {
            val w: String by parser.requiredArg("-v", valueLabel = "VAL", hint = "")

            val x: String by parser.positionalArg(valueLabel = "X", hint = "")
            val y: Int by parser.positionalArg(valueLabel = "Y", hint = "") { it.toInt() }
            val z: Double by parser.positionalArg(valueLabel = "Z", hint = "") { it.toDouble() }
        }

        CmdArgsParser(arrayOf("-v=value", "--", "-t", "9999", "22.9"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            assertEquals("value", it.w)
            assertEquals("-t", it.x)
            assertEquals(9999, it.y)
            assertEquals(22.9, it.z)
        }.onFailure {
            fail("Parse should succeed")
        }
    }

    @Test
    fun missingDelimiterFails() {
        class TestArgs(parser: CmdArgsParser) {
            val w: String by parser.requiredArg("-v", valueLabel = "VAL", hint = "")

            val x: String by parser.positionalArg(valueLabel = "X", hint = "")
            val y: Int by parser.positionalArg(valueLabel = "Y", hint = "") { it.toInt() }
            val z: Double by parser.positionalArg(valueLabel = "Z", hint = "") { it.toDouble() }
        }

        CmdArgsParser(arrayOf("-v=value", "-t", "9999", "22.9"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            fail("Parse should fail")
        }.onFailure {
            assert(it is MalformedArgsException)
        }
    }

    @Test
    fun delimiterAndMissingPositionalFails() {
        class TestArgs(parser: CmdArgsParser) {
            val w: String by parser.requiredArg("-v", valueLabel = "VAL", hint = "")

            val x: String by parser.positionalArg(valueLabel = "X", hint = "")
            val y: Int by parser.positionalArg(valueLabel = "Y", hint = "") { it.toInt() }
            val z: Double by parser.positionalArg(valueLabel = "Z", hint = "") { it.toDouble() }
        }

        CmdArgsParser(arrayOf("-v=value", "--", "-t", "9999"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            fail("Parse should fail")
        }.onFailure {
            assert(it is MalformedArgsException)
        }
    }
}