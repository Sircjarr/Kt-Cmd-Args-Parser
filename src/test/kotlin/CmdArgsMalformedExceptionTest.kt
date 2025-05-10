import lib.lib_args_parse.CmdArgsParser
import lib.lib_args_parse.exception.CmdArgsMalformedException
import kotlin.test.Test
import kotlin.test.fail

class CmdArgsMalformedExceptionTest {

    @Test
    fun unexpectedArgThrowsException() {
        class TestArgs(parser: CmdArgsParser) {
            val w: String by parser.requiredMapArg("--mode", map = mapOf("e" to "easy"), valueLabel = "X", help = "")
            val x: String by parser.requiredArg("-v", valueLabel = "X", help = "")
            val y: Int? by parser.optionalArg("-x", valueLabel = "Y", help = "") { it.toInt() }
            val z: Double by parser.requiredArg("-z", valueLabel = "Z", help = "") { it.toDouble() }
        }

        CmdArgsParser(arrayOf("--mode=e", "-vtest", "illegal_input", "-x345", "-z", "7.89"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            fail("Should fail parsing")
        }.onFailure {
            assert(it is CmdArgsMalformedException)
        }
    }

    @Test
    fun noValueDefinedThrowsException() {
        class TestArgs(parser: CmdArgsParser) {
            val w: String by parser.requiredMapArg("--mode", map = mapOf("e" to "easy"), valueLabel = "X", help = "")
            val x: String by parser.requiredArg("-v", valueLabel = "X", help = "")
            val y: Int? by parser.optionalArg("-x", valueLabel = "Y", help = "") { it.toInt() }
            val z: Double by parser.requiredArg("-z", valueLabel = "Z", help = "") { it.toDouble() }
        }

        CmdArgsParser(arrayOf("--mode=e", "-vtest", "-x", "-z", "7.89"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            fail("Should fail parsing")
        }.onFailure {
            assert(it is CmdArgsMalformedException)
        }

        CmdArgsParser(arrayOf("--mode=e", "-vtest", "-x345", "-z"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            fail("Should fail parsing")
        }.onFailure {
            assert(it is CmdArgsMalformedException)
        }

        CmdArgsParser(arrayOf("--mode=e", "-v", "-x345", "-z", "7.89"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            fail("Should fail parsing")
        }.onFailure {
            assert(it is CmdArgsMalformedException)
        }

        CmdArgsParser(arrayOf("-x345", "-v", "--mode=e", "-z", "7.89"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            fail("Should fail parsing")
        }.onFailure {
            assert(it is CmdArgsMalformedException)
        }
    }

    @Test
    fun noValueDefinedBeforePosDelimThrowsException() {
        class TestArgs(parser: CmdArgsParser) {
            val w: String by parser.requiredMapArg("--mode", map = mapOf("e" to "easy"), valueLabel = "X", help = "")
            val x: String by parser.requiredArg("-v", valueLabel = "X", help = "")
            val y: Int? by parser.optionalArg("-x", valueLabel = "Y", help = "") { it.toInt() }
            val z: String by parser.positionalArg(valueLabel = "Z", help = "")
        }

        CmdArgsParser(arrayOf("--mode=e", "-vtest", "-x", "--", "-z", "7.89"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            fail("Should fail parsing")
        }.onFailure {
            assert(it is CmdArgsMalformedException)
        }
    }
}