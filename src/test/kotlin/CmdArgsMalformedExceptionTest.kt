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
import com.github.sircjarr.cmdargsparser.exception.CmdArgsMalformedException
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