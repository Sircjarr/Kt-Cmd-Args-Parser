// Kt-Cmd-Args-Parser: A library for parsing command-line arguments.
// Copyright (C) 2025 Cliff Jarrell
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.

import com.github.sircjarr.cmdargsparser.CmdArgsParser
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