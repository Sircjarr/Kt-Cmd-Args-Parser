import args.CmdArgsParserStressTestArgs
import lib.lib_args_parse.CmdArgsParser
import lib.lib_args_parse.exception.CmdArgsBuiltinCommandException
import kotlin.test.*

private const val PROGRAM_NAME = "CmdArgsBuiltinCommandsTest.kt"

class CmdArgsBuiltinCommandsTest {

    @Test
    fun printHelp() {
        val args = arrayOf("help", "--help")
        args.forEach {
            CmdArgsParser(arrayOf(it), PROGRAM_NAME).parse(::CmdArgsParserStressTestArgs)
                .onSuccess {
                    fail("parse should not succeed")
                }.onFailure {
                    assert(it is CmdArgsBuiltinCommandException)
                }
        }
    }

    @Test
    fun printVersion() {
        val args = arrayOf("version", "--version")
        args.forEach {
            CmdArgsParser(arrayOf(it), PROGRAM_NAME).parse(::CmdArgsParserStressTestArgs)
                .onSuccess {
                    fail("parse should not succeed")
                }.onFailure {
                    assert(it is CmdArgsBuiltinCommandException)
                }
        }
    }

    @Test
    fun quit() {
        val args = arrayOf("q", "quit", "exit", "--quit", "--exit")
        args.forEach {
            CmdArgsParser(arrayOf(it), PROGRAM_NAME).parse(::CmdArgsParserStressTestArgs)
                .onSuccess {
                    fail("parse should not succeed")
                }.onFailure {
                    assert(it is CmdArgsBuiltinCommandException)
                }
        }
    }
}