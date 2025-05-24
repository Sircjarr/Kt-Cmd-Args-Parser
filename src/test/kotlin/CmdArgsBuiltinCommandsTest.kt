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

import args.CmdArgsParserStressTestArgs
import com.github.sircjarr.cmdargsparser.CmdArgsParser
import com.github.sircjarr.cmdargsparser.exception.CmdArgsBuiltinCommandException
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