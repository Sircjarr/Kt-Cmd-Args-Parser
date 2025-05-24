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
import kotlin.test.assertEquals
import kotlin.test.fail

class CmdArgsPositionalsTest {

    @Test
    fun singlePositionalParsed() {
        class TestArgs(parser: CmdArgsParser) {
            val x: String by parser.positionalArg(valueLabel = "", help = "")
        }

        CmdArgsParser(arrayOf("hello_world"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            assertEquals("hello_world", it.x)
        }.onFailure {
            fail("Should parse successfully")
        }
    }

    @Test
    fun multiplePositionalsParsed() {
        class TestArgs(parser: CmdArgsParser) {
            val w: String by parser.requiredMapArg("--mode", map = mapOf("e" to "easy"), valueLabel = "X", help = "")

            val x: String by parser.positionalArg(valueLabel = "X", help = "")
            val y: Int by parser.positionalArg(valueLabel = "Y", help = "") { it.toInt() }
            val z: Double by parser.positionalArg(valueLabel = "Z", help = "") { it.toDouble() }
        }

        CmdArgsParser(arrayOf("--mode=e", "test", "345", "29.87"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            assertEquals("easy", it.w)

            assertEquals("test", it.x)
            assertEquals(345, it.y)
            assertEquals(29.87, it.z)
        }.onFailure {
            fail("Should parse successfully")
        }
    }

    @Test
    fun missingPositionalThrowsException() {
        class TestArgs(parser: CmdArgsParser) {
            val w: String by parser.requiredArg("-v", valueLabel = "VAL", help = "")

            val x: String by parser.positionalArg(valueLabel = "X", help = "")
            val y: Int by parser.positionalArg(valueLabel = "Y", help = "") { it.toInt() }
            val z: Double by parser.positionalArg(valueLabel = "Z", help = "") { it.toDouble() }
        }

        CmdArgsParser(arrayOf("-v=value", "test", "345"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            fail("Should fail parsing")
        }.onFailure {
            assert(it is CmdArgsMalformedException)
        }

        CmdArgsParser(arrayOf("-v=value", "test"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            fail("Should fail parsing")
        }.onFailure {
            assert(it is CmdArgsMalformedException)
        }
    }

    @Test
    fun delimiterWorks() {
        class TestArgs(parser: CmdArgsParser) {
            val w: String by parser.requiredArg("-v", valueLabel = "VAL", help = "")

            val x: String by parser.positionalArg(valueLabel = "X", help = "")
            val y: Int by parser.positionalArg(valueLabel = "Y", help = "") { it.toInt() }
            val z: Double by parser.positionalArg(valueLabel = "Z", help = "") { it.toDouble() }
        }

        CmdArgsParser(arrayOf("-v=value", "--", "-t", "9999", "22.9"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            assertEquals("value", it.w)
            assertEquals("-t", it.x)
            assertEquals(9999, it.y)
            assertEquals(22.9, it.z)
        }.onFailure {
            fail("Parse should succeed")
        }
    }

    @Test
    fun missingDelimiterFails() {
        class TestArgs(parser: CmdArgsParser) {
            val w: String by parser.requiredArg("-v", valueLabel = "VAL", help = "")

            val x: String by parser.positionalArg(valueLabel = "X", help = "")
            val y: Int by parser.positionalArg(valueLabel = "Y", help = "") { it.toInt() }
            val z: Double by parser.positionalArg(valueLabel = "Z", help = "") { it.toDouble() }
        }

        CmdArgsParser(arrayOf("-v=value", "-t", "9999", "22.9"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            fail("Parse should fail")
        }.onFailure {
            assert(it is CmdArgsMalformedException)
        }
    }

    @Test
    fun delimiterAndMissingPositionalFails() {
        class TestArgs(parser: CmdArgsParser) {
            val w: String by parser.requiredArg("-v", valueLabel = "VAL", help = "")

            val x: String by parser.positionalArg(valueLabel = "X", help = "")
            val y: Int by parser.positionalArg(valueLabel = "Y", help = "") { it.toInt() }
            val z: Double by parser.positionalArg(valueLabel = "Z", help = "") { it.toDouble() }
        }

        CmdArgsParser(arrayOf("-v=value", "--", "-t", "9999"), "CmdArgsParserInitializationExceptionTest.kt").parse(::TestArgs).onSuccess {
            fail("Parse should fail")
        }.onFailure {
            assert(it is CmdArgsMalformedException)
        }
    }
}