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