import lib.lib_args_parse.help.CmdArgHelpConfig
import lib.lib_args_parse.help.CmdArgHelpConfigHolder
import lib.lib_args_parse.CmdArgsParser
import kotlin.test.*

class CmdArgsParserTestArgs(parser: CmdArgsParser) : CmdArgHelpConfigHolder {

    // Optionals
    val o: String? by parser.optionalArg("-o", "--opt-string", valueLabel = "OPT_O", hint = "Optional O hint")
    val d: Double? by parser.optionalArg("-d", valueLabel = "OPT_D", hint = "Optional D hint 0") { it.toDouble() }
    val i: Int? by parser.optionalArg("-i", valueLabel = "OPT_I", hint = "Optional I hint 0") { it.toInt() }
    val l: Long? by parser.optionalArg("-l", valueLabel = "OPT_L", hint = "Optional L hint 0") { it.toLong() }
    val s: MODE? by parser.optionalMappedArg(
        "-s", "--mode", map = mapOf(
            "e" to MODE.EASY,
            "m" to MODE.MEDIUM,
            "h" to MODE.HARD
        ), valueLabel = "DIFFICULTY", hint = "set difficulty of the game"
    )

    // Optional defaults
    val o2: String by parser.optionalArg(
        "-O",
        valueLabel = "OPT_O_2",
        hint = "Optional O hint 2",
        default = "0 Default value"
    )
    val d2: Double by parser.optionalArg(
        "-D",
        "--opt-double",
        valueLabel = "OPT_D_2",
        hint = "Optional D hint 2",
        default = 101.0
    ) { it.toDouble() }
    val i2: Int by parser.optionalArg(
        "-I",
        valueLabel = "OPT_I_2",
        hint = "Optional I hint 2",
        default = 99
    ) { it.toInt() }
    val l2: Long by parser.optionalArg(
        "-L",
        valueLabel = "OPT_L_2",
        hint = "Optional L hint 2",
        default = 123
    ) { it.toLong() }
    val s2: MODE by parser.optionalMappedArg(
        "-S", map = mapOf(
            "e" to MODE.EASY,
            "m" to MODE.MEDIUM,
            "h" to MODE.HARD
        ), valueLabel = "DIFFICULTY", hint = "set difficulty of the game", default = MODE.MEDIUM
    )

    // Required
    val o3: String by parser.requiredArg("--required-s", valueLabel = "REQ_O_3", hint = "Required O hint 3")
    val d3: Double by parser.requiredArg(
        "--required-d",
        valueLabel = "REQ_D_3",
        hint = "Required D hint 3"
    ) { it.toDouble() }
    val i3: Int by parser.requiredArg(
        "--required-i",
        "--req-i",
        valueLabel = "REQ_D_3",
        hint = "Required I hint 3"
    ) { it.toInt() }
    val l3: Long by parser.requiredArg(
        "--required-l",
        "-Z",
        valueLabel = "REQ_L_3",
        hint = "Required L hint 3"
    ) { it.toLong() }
    val s3: MODE by parser.requiredMapArg(
        "--serializable", map = mapOf(
            "e" to MODE.EASY,
            "m" to MODE.MEDIUM,
            "h" to MODE.HARD
        ), valueLabel = "DIFFICULTY", hint = "set difficulty of the game"
    )

    // Flags
    val f: Boolean by parser.flagArg("-f", hint = "Flag")
    val f2: Boolean by parser.flagArg("-F", hint = "Flag 2")
    val f3: Boolean by parser.flagArg("--flag", hint = "Flag 3")
    val f4: Boolean by parser.flagArg("--flagg", hint = "Flag 4")
    val f5: Boolean by parser.flagArg("--noflag-five", hint = "Flag 5", default = true)
    val f6: Boolean by parser.flagArg("--noflag-six", hint = "Flag 6", default = true)

    // Positionals
    val src: String by parser.positionalArg("SRC", hint = "source positional hint")
    val dest: String by parser.positionalArg("DEST", hint = "dest positional hint")

    override val cmdArgHelpConfig: CmdArgHelpConfig
        get() = CmdArgHelpConfig(
            epilogue = "my epilogue",
            prologue = "my prologue"
        )

    enum class MODE {
        EASY, MEDIUM, HARD
    }
}

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
        val res = parser.parse(::CmdArgsParserTestArgs)

        res.onSuccess { args ->
            // Optionals
            assertEquals("optional string value", args.o)
            assertEquals(4.5, args.d)
            assertNull(args.i)
            assertNull(args.l)
            assertEquals(CmdArgsParserTestArgs.MODE.EASY, args.s)

            // Optional defaults
            assertEquals("0 Default value", args.o2)
            assertEquals(2.3, args.d2)
            assertEquals(99, args.i2)
            assertEquals(144, args.l2)
            assertEquals(CmdArgsParserTestArgs.MODE.HARD, args.s2)

            // Required
            assertEquals("My required string", args.o3)
            assertEquals(32.0, args.d3)
            assertEquals(24, args.i3)
            assertEquals(1983, args.l3)
            assertEquals(CmdArgsParserTestArgs.MODE.MEDIUM, args.s3)

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
        val res = parser.parse(::CmdArgsParserTestArgs)

        res.onSuccess { args ->
            // Optionals
            assertEquals("optional string value", args.o)
            assertEquals(4.5, args.d)
            assertNull(args.i)
            assertNull(args.l)
            assertEquals(CmdArgsParserTestArgs.MODE.EASY, args.s)

            // Optional defaults
            assertEquals("0 Default value", args.o2)
            assertEquals(2.3, args.d2)
            assertEquals(99, args.i2)
            assertEquals(144, args.l2)
            assertEquals(CmdArgsParserTestArgs.MODE.HARD, args.s2)

            // Required
            assertEquals("My required string", args.o3)
            assertEquals(32.0, args.d3)
            assertEquals(24, args.i3)
            assertEquals(1983, args.l3)
            assertEquals(CmdArgsParserTestArgs.MODE.MEDIUM, args.s3)

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
}