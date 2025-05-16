import args.FileEncryptorArgs
import lib.lib_args_parse.CmdArgsParser
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