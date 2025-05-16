import lib.lib_args_parse.CmdArgsParser
import org.junit.jupiter.api.Test
import kotlin.test.fail

class CmdArgsClassCastExceptionTest {

    @Test
    fun noTransformIntParamThrowsClassCastException() {
        class CmdArgsClassCastExceptionTestArgs(parser: CmdArgsParser) {
            val e: Int by parser.requiredArg("-i", valueLabel = "", help = "")
        }

        CmdArgsParser(
            arrayOf("-i", "3"),
            "CmdArgsClassCastExceptionTestArgs.kt"
        ).parse(::CmdArgsClassCastExceptionTestArgs).onSuccess {
            fail("Should fail parsing")
        }.onFailure {
            assert(it.cause is ClassCastException)
        }
    }

    @Test
    fun noTransformClassParamThrowsClassCastException() {
        class CmdArgsClassCastExceptionTestArgs(parser: CmdArgsParser) {
            val e: Test by parser.requiredArg("-t", valueLabel = "", help = "")

            inner class Test(val x: Int)
        }

        CmdArgsParser(
            arrayOf("-t", "failure"),
            "CmdArgsClassCastExceptionTestArgs.kt"
        ).parse(::CmdArgsClassCastExceptionTestArgs).onSuccess {
            fail("Should fail parsing")
        }.onFailure {
            assert(it.cause is ClassCastException)
        }
    }
}