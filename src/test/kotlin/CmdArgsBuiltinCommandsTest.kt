import lib.lib_args_parse.CmdArgsParser
import lib.lib_args_parse.exception.CmdArgsBuiltinCommandException
import kotlin.test.Test
import kotlin.test.fail

private const val PROGRAM_NAME = "CmdArgsBuiltinCommandsTest.kt"

class CmdArgsBuiltinCommandsTest {

    @Test
    fun printHelp() {
        val args = arrayOf("--help")
        CmdArgsParser(args, PROGRAM_NAME).parse(::CmdArgsParserTestArgs).onSuccess {
            fail("parse should not succeed")
        }.onFailure {
            assert(it is CmdArgsBuiltinCommandException)
        }
    }

    @Test
    fun printVersion() {
        val args = arrayOf("--version")
        CmdArgsParser(args, PROGRAM_NAME).parse(::CmdArgsParserTestArgs).onSuccess {
            fail("parse should not succeed")
        }.onFailure {
            assert(it is CmdArgsBuiltinCommandException)
        }
    }

    @Test
    fun quit() {
        val args = arrayOf("q")
        CmdArgsParser(args, PROGRAM_NAME).parse(::CmdArgsParserTestArgs).onSuccess {
            fail("parse should not succeed")
        }.onFailure {
            assert(it is CmdArgsBuiltinCommandException)
        }
    }
}