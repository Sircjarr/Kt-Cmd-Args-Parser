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
import com.github.sircjarr.cmdargsparser.exception.CmdArgsParseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CmdArgsRequiredTest {

    @Test
    fun requiredArgsDeclared() {
        class TestArgs(parser: CmdArgsParser) {
            val w: String by parser.requiredMapArg("--mode", map = mapOf("e" to "easy"), valueLabel = "X", help = "")
            val x: String by parser.requiredArg("-v", valueLabel = "X", help = "")

            val y: Int? by parser.optionalArg("-x", valueLabel = "Y", help = "") { it.toInt() }

            val z: Double by parser.requiredArg("-z", valueLabel = "Z", help = "") { it.toDouble() }
        }

        CmdArgsParser(arrayOf("--mode=e", "-vtest", "-x345", "-z", "7.89"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            assertEquals("easy", it.w)
            assertEquals("test", it.x)
            assertEquals(345, it.y)
            assertEquals(7.89, it.z)
        }.onFailure {
            fail("Should parse successfully")
        }
    }

    @Test
    fun requiredArgsDuplicateOverwritesLast() {
        class TestArgs(parser: CmdArgsParser) {
            val w: String by parser.requiredMapArg("--mode", map = mapOf("e" to "easy"), valueLabel = "X", help = "")
            val x: String by parser.requiredArg("-v", valueLabel = "X", help = "")

            val y: Int? by parser.optionalArg("-x", valueLabel = "Y", help = "") { it.toInt() }

            val z: Double by parser.requiredArg("-z", valueLabel = "Z", help = "") { it.toDouble() }
        }

        CmdArgsParser(arrayOf("--mode=e", "-vtest", "-x345", "-x", "888", "-z", "7.89"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            assertEquals("easy", it.w)
            assertEquals("test", it.x)
            assertEquals(888, it.y)
            assertEquals(7.89, it.z)
        }.onFailure {
            fail("Should parse successfully")
        }
    }

    @Test
    fun throwsExceptionWhenRequiredArgMissing() {
        class TestArgs(parser: CmdArgsParser) {
            val w: String by parser.requiredMapArg("--mode", map = mapOf("e" to "easy"), valueLabel = "X", help = "")
            val x: String by parser.requiredArg("-v", valueLabel = "X", help = "")

            val y: Int? by parser.optionalArg("-x", valueLabel = "Y", help = "") { it.toInt() }

            val z: Double by parser.requiredArg("-z", valueLabel = "Z", help = "") { it.toDouble() }
        }

        CmdArgsParser(arrayOf("--mode=e", "-x345", "-z", "7.89"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            fail("Parse should fail")
        }.onFailure {
            assert(it is CmdArgsParseException)
        }
    }
}