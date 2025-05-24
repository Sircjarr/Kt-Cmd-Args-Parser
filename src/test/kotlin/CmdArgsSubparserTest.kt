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

import args.FileEncryptorArgs
import com.github.sircjarr.cmdargsparser.CmdArgsParser
import kotlin.test.*

class CmdArgsSubparserTest {

    @Test
    fun testNoArgs() {
        val args = arrayOf("--help")
        CmdArgsParser(args, "CmdArgsSubparserTest.kt").parse(::FileEncryptorArgs).onSuccess {
            fail("Usage should be printed")
        }
    }

    @Test
    fun testEncryption() {
        val args = arrayOf(
            "encrypt",
            "-f", "^.*\\.txt$",
            "--enc-dirreg", "^.*\\.noga$",
            "in.txt",
            "out.txt"
        )
        CmdArgsParser(args, "CmdArgsSubparserTest.kt").parse(::FileEncryptorArgs).onSuccess {
            assertNotNull(it.encryptionArgs)
            assertNull(it.decryptionArgs)
            val encryptionArgs = it.encryptionArgs!!
            assertEquals("^.*\\.txt$", encryptionArgs.encFileExcludeRegex.toString())
            assertEquals("^.*\\.noga$", encryptionArgs.encDirExcludeRegex.toString())
            assertEquals("in.txt", encryptionArgs.srcFile.toString())
            assertEquals("out.txt", encryptionArgs.destFile.toString())
        }.onFailure {
            it.printStackTrace()
            fail("Should succeed")
        }
    }

    @Test
    fun testEncryptionHelp() {
        val args = arrayOf(
            "encrypt",
            "--help"
        )
        CmdArgsParser(args, "CmdArgsSubparserTest.kt").parse(::FileEncryptorArgs).onSuccess {
            fail("Usage should be printed")
        }
    }

    @Test
    fun testDecryption() {
        val args = arrayOf(
            "decrypt",
            "in.txt",
            "out.txt"
        )
        CmdArgsParser(args, "CmdArgsSubparserTest.kt").parse(::FileEncryptorArgs).onSuccess {
            assertNotNull(it.decryptionArgs)
            assertNull(it.encryptionArgs)
            val decryptionArgs = it.decryptionArgs!!
            assertEquals("in.txt", decryptionArgs.srcFile.toString())
            assertEquals("out.txt", decryptionArgs.destFile.toString())
        }.onFailure {
            it.printStackTrace()
            fail("Should succeed")
        }
    }

    @Test
    fun testDecryptionHelp() {
        val args = arrayOf(
            "decrypt",
            "--help"
        )
        CmdArgsParser(args, "CmdArgsSubparserTest.kt").parse(::FileEncryptorArgs).onSuccess {
            fail("Usage should be printed")
        }
    }

    @Test
    fun processOptionNoSubcommand() {
        val args = arrayOf(
            "--flag"
        )
        CmdArgsParser(args, "CmdArgsSubparserTest.kt").parse(::FileEncryptorArgs).onSuccess {
            assertTrue(it.flag)
        }
    }
}