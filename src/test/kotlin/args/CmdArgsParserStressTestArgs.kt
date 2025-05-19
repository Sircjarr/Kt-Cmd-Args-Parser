package args

import com.github.sircjarr.cmdargsparser.CmdArgsParser
import com.github.sircjarr.cmdargsparser.help.CmdArgHelpConfig
import com.github.sircjarr.cmdargsparser.help.CmdArgHelpConfigHolder

class CmdArgsParserStressTestArgs(parser: CmdArgsParser) : CmdArgHelpConfigHolder {

    // Optionals
    val o: String? by parser.optionalArg("-o", "--opt-string", valueLabel = "OPT_O", help = "Optional O hint")
    val d: Double? by parser.optionalArg("-d", valueLabel = "OPT_D", help = "Optional D hint 0") { it.toDouble() }
    val i: Int? by parser.optionalArg("-i", valueLabel = "OPT_I", help = "Optional I hint 0") { it.toInt() }
    val l: Long? by parser.optionalArg("-l", valueLabel = "OPT_L", help = "Optional L hint 0") { it.toLong() }
    val s: MODE? by parser.optionalMappedArg(
        "-s", "--mode", map = mapOf(
            "e" to MODE.EASY,
            "m" to MODE.MEDIUM,
            "h" to MODE.HARD
        ), valueLabel = "DIFFICULTY", help = "set difficulty of the game"
    )

    // Optional defaults
    val o2: String by parser.optionalArg(
        "-O",
        valueLabel = "OPT_O_2",
        help = "Optional O hint 2",
        default = "0 Default value"
    )
    val d2: Double by parser.optionalArg(
        "-D",
        "--opt-double",
        valueLabel = "OPT_D_2",
        help = "Optional D hint 2",
        default = 101.0
    ) { it.toDouble() }
    val i2: Int by parser.optionalArg(
        "-I",
        valueLabel = "OPT_I_2",
        help = "Optional I hint 2",
        default = 99
    ) { it.toInt() }
    val l2: Long by parser.optionalArg(
        "-L",
        valueLabel = "OPT_L_2",
        help = "Optional L hint 2",
        default = 123
    ) { it.toLong() }
    val s2: MODE by parser.optionalMappedArg(
        "-S", map = mapOf(
            "e" to MODE.EASY,
            "m" to MODE.MEDIUM,
            "h" to MODE.HARD
        ), valueLabel = "DIFFICULTY", help = "set difficulty of the game", default = MODE.MEDIUM
    )

    // Required
    val o3: String by parser.requiredArg("--required-s", valueLabel = "REQ_O_3", help = "Required O hint 3")
    val d3: Double by parser.requiredArg(
        "--required-d",
        valueLabel = "REQ_D_3",
        help = "Required D hint 3"
    ) { it.toDouble() }
    val i3: Int by parser.requiredArg(
        "--required-i",
        "--req-i",
        valueLabel = "REQ_D_3",
        help = "Required I hint 3"
    ) { it.toInt() }
    val l3: Long by parser.requiredArg(
        "--required-l",
        "-Z",
        valueLabel = "REQ_L_3",
        help = "Required L hint 3"
    ) { it.toLong() }
    val s3: MODE by parser.requiredMapArg(
        "--serializable", map = mapOf(
            "e" to MODE.EASY,
            "m" to MODE.MEDIUM,
            "h" to MODE.HARD
        ), valueLabel = "DIFFICULTY", help = "set difficulty of the game"
    )

    // Flags
    val f: Boolean by parser.flagArg("-f", help = "Flag")
    val f2: Boolean by parser.flagArg("-F", help = "Flag 2")
    val f3: Boolean by parser.flagArg("--flag", help = "Flag 3")
    val f4: Boolean by parser.flagArg("--flagg", help = "Flag 4")
    val f5: Boolean by parser.flagArg("--noflag-five", help = "Flag 5", default = true)
    val f6: Boolean by parser.flagArg("--noflag-six", help = "Flag 6", default = true)

    // Positionals
    val src: String by parser.positionalArg("SRC", help = "source positional hint")
    val dest: String by parser.positionalArg("DEST", help = "dest positional hint")

    override val cmdArgHelpConfig: CmdArgHelpConfig
        get() = CmdArgHelpConfig(
            epilogue = "my epilogue",
            prologue = "my prologue"
        )

    enum class MODE { EASY, MEDIUM, HARD }
}