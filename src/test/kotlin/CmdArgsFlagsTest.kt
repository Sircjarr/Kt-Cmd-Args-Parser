import lib.lib_args_parse.CmdArgsParser
import lib.lib_args_parse.exception.CmdArgsMalformedException
import org.junit.jupiter.api.Assertions.assertFalse
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

class CmdArgsFlagsTest {

    @Test
    fun singleFlag() {
        class MyArgs(parser: CmdArgsParser) {
            val x: Boolean by parser.flagArg("-f", "--flag", help = "")
            val y: String? by parser.optionalArg("-v", help = "", valueLabel = "")
        }

        CmdArgsParser(arrayOf("-v=boga"), "CmdArgsFlagsTest.kt").parse(::MyArgs)
            .onSuccess {
                assertFalse(it.x)
            }.onFailure { fail("Should parse successfully") }

        CmdArgsParser(arrayOf("-f"), "CmdArgsFlagsTest.kt").parse(::MyArgs)
            .onSuccess {
                assertTrue(it.x)
            }.onFailure { fail("Should parse successfully") }
    }

    @Test
    fun singleFlagWithDefault() {
        class MyArgs(parser: CmdArgsParser) {
            val x: Boolean by parser.flagArg("-f", "--no-flag", help = "", default = true)
            val y: String? by parser.optionalArg("-v", help = "", valueLabel = "")
        }

        CmdArgsParser(arrayOf("-v=boga"), "CmdArgsFlagsTest.kt").parse(::MyArgs)
            .onSuccess {
                assertTrue(it.x)
            }.onFailure { fail("Should parse successfully") }

        CmdArgsParser(arrayOf("--no-flag"), "CmdArgsFlagsTest.kt").parse(::MyArgs)
            .onSuccess {
                assertFalse(it.x)
            }.onFailure { fail("Should parse successfully") }
    }

    @Test
    fun stackedFlags() {
        class MyArgs(parser: CmdArgsParser) {
            val w: Boolean by parser.flagArg("-w", help = "")
            val x: Boolean by parser.flagArg("-x", help = "", default = true)
            val y: Boolean by parser.flagArg("-y", help = "")
        }

        CmdArgsParser(arrayOf("-wxy"), "CmdArgsFlagsTest.kt").parse(::MyArgs)
            .onSuccess {
                assertTrue(it.w)
                assertFalse(it.x)
                assertTrue(it.y)
            }.onFailure { fail("Should parse successfully") }
    }

    @Test
    fun stackedFlagsDuplicate() {
        class MyArgs(parser: CmdArgsParser) {
            val w: Boolean by parser.flagArg("-w", help = "")
            val x: Boolean by parser.flagArg("-x", help = "", default = true)
            val y: Boolean by parser.flagArg("-y", help = "")
        }

        CmdArgsParser(arrayOf("-wxxy"), "CmdArgsFlagsTest.kt").parse(::MyArgs)
            .onSuccess {
                assertTrue(it.w)
                assertFalse(it.x)
                assertTrue(it.y)
            }.onFailure { fail("Should parse successfully") }
    }

    @Test
    fun unknownStackedFlagFails() {
        class MyArgs(parser: CmdArgsParser) {
            val w: Boolean by parser.flagArg("-w", help = "")
            val x: Boolean by parser.flagArg("-x", help = "", default = true)
            val y: Boolean by parser.flagArg("-y", help = "")
        }

        CmdArgsParser(arrayOf("-wxzy"), "CmdArgsFlagsTest.kt").parse(::MyArgs)
            .onFailure {
                assert(it is CmdArgsMalformedException)
            }.onSuccess { fail("Should fail parsing") }
    }
}