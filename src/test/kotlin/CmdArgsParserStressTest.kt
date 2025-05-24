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
import args.MyGameArgs
import com.github.sircjarr.cmdargsparser.CmdArgsParser
import kotlin.test.*

class CmdArgsParserTest {

    @Test
    fun parsesSuccessfully() {
        val args = arrayOf(
            // Optionals
            "-o", "optional string value",
            "-d", "4.5",
            "--mode", "e",
            // Optional defaults
            "-D", "2.3",
            "-L", "144",
            "-S", "h",
            // Required
            "--required-s", "My required string",
            "--required-d", "32",
            "--req-i", "24",
            "-Z", "1983",
            "--serializable", "m",
            // Flags
            "-fF",
            "--flagg",
            "--noflag-six",
            // Positonals
            "--",
            "boga.txt",
            "noga.txt"
        )

        val parser = CmdArgsParser(args, "CmdArgsParserStressTest.kt")
        val res = parser.parse(::CmdArgsParserStressTestArgs)

        res.onSuccess { args ->
            // Optionals
            assertEquals("optional string value", args.o)
            assertEquals(4.5, args.d)
            assertNull(args.i)
            assertNull(args.l)
            assertEquals(CmdArgsParserStressTestArgs.MODE.EASY, args.s)

            // Optional defaults
            assertEquals("0 Default value", args.o2)
            assertEquals(2.3, args.d2)
            assertEquals(99, args.i2)
            assertEquals(144, args.l2)
            assertEquals(CmdArgsParserStressTestArgs.MODE.HARD, args.s2)

            // Required
            assertEquals("My required string", args.o3)
            assertEquals(32.0, args.d3)
            assertEquals(24, args.i3)
            assertEquals(1983, args.l3)
            assertEquals(CmdArgsParserStressTestArgs.MODE.MEDIUM, args.s3)

            // Flags
            assert(args.f)
            assert(args.f2)
            assertFalse(args.f3)
            assert(args.f4)
            assert(args.f5)
            assertFalse(args.f6)

            // Positionals
            assertEquals("boga.txt", args.src)
            assertEquals("noga.txt", args.dest)
        }.onFailure {
            org.junit.jupiter.api.fail(it)
        }
    }

    @Test
    fun parsesSuccessfullyWithDelimiters() {
        val args = arrayOf(
            // Optionals
            "-o=optional string value",
            "-d4.5",
            "--mode", "e",
            // Optional defaults
            "-D", "2.3",
            "-L144",
            "-S=h",
            // Required
            "--required-s", "My required string",
            "--required-d=32",
            "--req-i", "24",
            "-Z1983",
            "--serializable=m",
            // Flags
            "-fF",
            "--flagg",
            "--noflag-six",
            // Positonals
            "--",
            "boga.txt",
            "noga.txt"
        )

        val parser = CmdArgsParser(args, "_CmdArgsParserTest")
        val res = parser.parse(::CmdArgsParserStressTestArgs)

        res.onSuccess { args ->
            // Optionals
            assertEquals("optional string value", args.o)
            assertEquals(4.5, args.d)
            assertNull(args.i)
            assertNull(args.l)
            assertEquals(CmdArgsParserStressTestArgs.MODE.EASY, args.s)

            // Optional defaults
            assertEquals("0 Default value", args.o2)
            assertEquals(2.3, args.d2)
            assertEquals(99, args.i2)
            assertEquals(144, args.l2)
            assertEquals(CmdArgsParserStressTestArgs.MODE.HARD, args.s2)

            // Required
            assertEquals("My required string", args.o3)
            assertEquals(32.0, args.d3)
            assertEquals(24, args.i3)
            assertEquals(1983, args.l3)
            assertEquals(CmdArgsParserStressTestArgs.MODE.MEDIUM, args.s3)

            // Flags
            assert(args.f)
            assert(args.f2)
            assertFalse(args.f3)
            assert(args.f4)
            assert(args.f5)
            assertFalse(args.f6)

            // Positionals
            assertEquals("boga.txt", args.src)
            assertEquals("noga.txt", args.dest)
        }.onFailure {
            org.junit.jupiter.api.fail(it)
        }
    }

    @Test
    fun gameExample() {
        val args = arrayOf(
            "-l", "9",
            "--cheats-enabled",
            "--mode=medium",
            "--",
            "100.50",
            "C:\\Users\\User\\MyGame\\saves"
        )

        CmdArgsParser(args, "MyGame.jar").parse(::MyGameArgs).onSuccess { myGameArgs ->
            assertNull(myGameArgs.seed)
            assertEquals(myGameArgs.numLives, 9)
            assertTrue(myGameArgs.cheatsEnabled)
            assertEquals(myGameArgs.mode, MyGameArgs.Mode.MEDIUM)
            assertEquals(myGameArgs.playerSpeed, 100.50)
            assertEquals(myGameArgs.saveFile.absolutePath, "C:\\Users\\User\\MyGame\\saves")
        }.onFailure {
            // Optionally handle failure
        }
    }
}