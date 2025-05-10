import lib.lib_args_parse.help.CmdArgHelpConfig
import lib.lib_args_parse.help.CmdArgHelpConfigHolder
import lib.lib_args_parse.CmdArgsParser
import java.io.File
import kotlin.test.*

class FileEncryptorArgs(parser: CmdArgsParser): CmdArgHelpConfigHolder {

    val flag: Boolean by parser.flagArg("--flag", help = "Option not part of any subcommand")

    val encryptionArgs: EncryptionArgs? by parser.subparser("encrypt", "encryption mode hint", ::EncryptionArgs)
    val decryptionArgs: DecryptionArgs? by parser.subparser("decrypt", "decryption mode hint", ::DecryptionArgs)

    override val cmdArgHelpConfig: CmdArgHelpConfig
        get() = CmdArgHelpConfig(
            prologue = buildString {
                append("This program will wipe and overwrite all files in the output folders")
            }
        )

    open class SharedArgs(parser: CmdArgsParser) {
        val srcFile: File by parser.positionalArg(
            valueLabel = "SRC",
            help = "Source file"
        ) { File(it) }

        val destFile: File by parser.positionalArg(
            valueLabel = "DEST",
            help = "Destination file"
        ) { File(it) }
    }

    class EncryptionArgs(subparser: CmdArgsParser): SharedArgs(subparser), CmdArgHelpConfigHolder {
        val encFileExcludeRegex: Regex? by subparser.optionalArg(
            "-f", "--enc-filereg",
            valueLabel = "FILE_EXCLUDE_REGEX",
            help = "Exclude file regex for encryption"
        ) { it.toRegex() }

        val encDirExcludeRegex: Regex? by subparser.optionalArg(
            "-d", "--enc-dirreg",
            valueLabel = "DIR_EXCLUDE_REGEX",
            help = "Exclude directory regex for encryption"
        ) { it.toRegex() }

        override val cmdArgHelpConfig: CmdArgHelpConfig
            get() = CmdArgHelpConfig(
                "example Java regex arg matching all .txt and .csv files: ^.*\\\\.(txt|csv)$",
                "encryption args prologue"
            )
    }

    class DecryptionArgs(subparser: CmdArgsParser): SharedArgs(subparser), CmdArgHelpConfigHolder {
        override val cmdArgHelpConfig: CmdArgHelpConfig
            get() = CmdArgHelpConfig("decryption args epilog", "decryption args prologue")
    }
}

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