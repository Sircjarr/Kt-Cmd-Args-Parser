import lib.lib_args_parse.CmdArgsParser
import lib.lib_args_parse.exception.CmdArgsParserInitializationException
import kotlin.test.Test
import kotlin.test.fail

class CmdArgsParserInitializationExceptionTest {

    @Test
    fun optionalKey() {
        class TestArgs(parser: CmdArgsParser) {
            val x: Int? by parser.optionalArg("v", valueLabel = "", hint = "") { it.toInt() }
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
            val x: Int? by parser.optionalArg("---v", default = 2, valueLabel = "", hint = "") { it.toInt() }
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
            val x: Int by parser.requiredArg("-cat", valueLabel = "", hint = "") { it.toInt() }
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
            val x: Int by parser.requiredArg(valueLabel = "", hint = "") { it.toInt() }
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
            val x: Int by parser.requiredMapArg(" ", map = mapOf("1" to 1), valueLabel = "", hint = "")
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
            val x: Int by parser.requiredMapArg("-v", map = mapOf(), valueLabel = "", hint = "")
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
            val x: Int by parser.requiredArg("-v", valueLabel = "", hint = "") { it.toInt() }
            val y: Int by parser.requiredArg("--verbose", "-v", valueLabel = "", hint = "") { it.toInt() }
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
            val x: Int by parser.requiredArg("--help", valueLabel = "", hint = "") { it.toInt() }
        }

        try {
            CmdArgsParser(arrayOf(""), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs)
            fail("Exception not thrown")
        } catch (e: CmdArgsParserInitializationException) {
            print(e.toString())
        }
    }
}