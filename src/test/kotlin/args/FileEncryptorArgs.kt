package args

import com.github.sircjarr.cmdargsparser.CmdArgsParser
import com.github.sircjarr.cmdargsparser.help.CmdArgHelpConfig
import com.github.sircjarr.cmdargsparser.help.CmdArgHelpConfigHolder
import java.io.File

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