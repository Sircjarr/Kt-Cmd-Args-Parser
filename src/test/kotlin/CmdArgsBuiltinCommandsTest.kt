import lib.lib_args_parse.CmdArgsParser
import kotlin.test.Test

private const val PROGRAM_NAME = "CmdArgsBuiltinCommandsTest.kt"

class CmdArgsBuiltinCommandsTest {

    @Test
    fun printHelp() {
        val args = arrayOf("--help")
        CmdArgsParser(args, PROGRAM_NAME).parse(::MyArgs)
    }

    @Test
    fun printVersion() {
        val args = arrayOf("--version")
        CmdArgsParser(args, PROGRAM_NAME).parse(::MyArgs)
    }

    @Test
    fun quit() {
        val args = arrayOf("q")
        CmdArgsParser(args, PROGRAM_NAME).parse(::MyArgs)
    }
}