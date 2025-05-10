import lib.lib_args_parse.CmdArgsParser
import lib.lib_args_parse.exception.CmdArgsParserInitializationException
import kotlin.test.Test
import kotlin.test.fail

class CmdArgsParserInitializationExceptionTest {

    @Test
    fun optionalKey() {
        class TestArgs(parser: CmdArgsParser) {
            val x: Int? by parser.optionalArg("v", valueLabel = "", help = "") { it.toInt() }
        }

        try {
            CmdArgsParser(arrayOf(""), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs)
            fail("Exception not thrown")
        } catch (e: CmdArgsParserInitializationException) {
            print(e.toString())
        }
    }

    @Test
    fun optionalKeyDash() {
        class TestArgs(parser: CmdArgsParser) {
            val x: Int? by parser.optionalArg("-", valueLabel = "", help = "") { it.toInt() }
        }

        try {
            CmdArgsParser(arrayOf(""), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs)
            fail("Exception not thrown")
        } catch (e: CmdArgsParserInitializationException) {
            print(e.toString())
        }
    }

    @Test
    fun optionalKeyDelimiter() {
        class TestArgs(parser: CmdArgsParser) {
            val x: Int? by parser.optionalArg("--", valueLabel = "", help = "") { it.toInt() }
        }

        try {
            CmdArgsParser(arrayOf(""), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs)
            fail("Exception not thrown")
        } catch (e: CmdArgsParserInitializationException) {
            print(e.toString())
        }
    }

    @Test
    fun optionalDefaultKey() {
        class TestArgs(parser: CmdArgsParser) {
            val x: Int? by parser.optionalArg("---v", default = 2, valueLabel = "", help = "") { it.toInt() }
        }

        try {
            CmdArgsParser(arrayOf(""), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs)
            fail("Exception not thrown")
        } catch (e: CmdArgsParserInitializationException) {
            print(e.toString())
        }
    }

    @Test
    fun requiredKey() {
        class TestArgs(parser: CmdArgsParser) {
            val x: Int by parser.requiredArg("-cat", valueLabel = "", help = "") { it.toInt() }
        }

        try {
            CmdArgsParser(arrayOf(""), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs)
            fail("Exception not thrown")
        } catch (e: CmdArgsParserInitializationException) {
            print(e.toString())
        }
    }

    @Test
    fun noKeysProvided() {
        class TestArgs(parser: CmdArgsParser) {
            val x: Int by parser.requiredArg(valueLabel = "", help = "") { it.toInt() }
        }

        try {
            CmdArgsParser(arrayOf(""), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs)
            fail("Exception not thrown")
        } catch (e: CmdArgsParserInitializationException) {
            print(e.toString())
        }
    }

    @Test
    fun requiredMap() {
        class TestArgs(parser: CmdArgsParser) {
            val x: Int by parser.requiredMapArg(" ", map = mapOf("1" to 1), valueLabel = "", help = "")
        }

        try {
            CmdArgsParser(arrayOf(""), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs)
            fail("Exception not thrown")
        } catch (e: CmdArgsParserInitializationException) {
            print(e.toString())
        }
    }

    @Test
    fun requiredMapEmptyMap() {
        class TestArgs(parser: CmdArgsParser) {
            val x: Int by parser.requiredMapArg("-v", map = mapOf(), valueLabel = "", help = "")
        }

        try {
            CmdArgsParser(arrayOf(""), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs)
            fail("Exception not thrown")
        } catch (e: CmdArgsParserInitializationException) {
            print(e.toString())
        }
    }

    @Test
    fun keyAlreadyDeclared() {
        class TestArgs(parser: CmdArgsParser) {
            val x: Int by parser.requiredArg("-v", valueLabel = "", help = "") { it.toInt() }
            val y: Int by parser.requiredArg("--verbose", "-v", valueLabel = "", help = "") { it.toInt() }
        }

        try {
            CmdArgsParser(arrayOf(""), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs)
            fail("Exception not thrown")
        } catch (e: CmdArgsParserInitializationException) {
            print(e.toString())
        }
    }

    @Test
    fun keyIsBuiltinCmd() {
        class TestArgs(parser: CmdArgsParser) {
            val x: Int by parser.requiredArg("--help", valueLabel = "", help = "") { it.toInt() }
        }

        try {
            CmdArgsParser(arrayOf(""), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs)
            fail("Exception not thrown")
        } catch (e: CmdArgsParserInitializationException) {
            print(e.toString())
        }
    }

    @Test
    fun requiredValidSubcommand() {
        class SubcommandTestArgs(parser: CmdArgsParser)

        class TestArgs(parser: CmdArgsParser) {
            val x: SubcommandTestArgs? by parser.subparser("encrypt-8", "", ::SubcommandTestArgs)
        }

        try {
            CmdArgsParser(arrayOf(""), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs)
            fail("Exception not thrown")
        } catch (e: CmdArgsParserInitializationException) {
            print(e.toString())
        }
    }
}