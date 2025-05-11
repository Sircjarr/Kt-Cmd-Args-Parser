# Description
An easy-to-use command-line argument parser for Kotlin apps. Interprets an `Array<String>`, `args`, formatted in a familiar syntax and serializes a valid, stable object of arguments to be coveniently read in any given program.

#### Features
* Declare required, optional, flag, and positional args
* Supports multiple key-value delimiters for your preferred use, eg: `-n 3`, `-n=3`, or `-n3`
* Set default values for optional and flags args
* Convenient mapping arg for restricted values
* Transform parameter to convert `String` value to arbitrary type
* Stacked flag arguments eg, `-i` and `-v` => `-iv`
* Concise one-line feedback for parsing errors 
* Builtin `--help` command to print neatly-formatted and comprehensive usage statement
* Builtin `--version` and `--quit` commands
* `--` delimiter to separate options and positional args 
* Subcommands support for implementing commands like `git commit [options]` or `git add [options]` 

---

# Quickstart usage example
Example creating and parsing `args` to launch a game

#### Construct the app-specific args class
```kotlin
// Make CmdArgsParser the only param in the constructor
class MyGameArgs(parser: CmdArgsParser) {

    // Optional arg with behavior to fallback to null if not found in the args array
    val seed: String? by parser.optionalArg(
        "-s", "--seed",
 	// Referenced in the output of the --help command eg, [-s SEED]
        valueLabel = "SEED",
	// Description for this arg referenced in the --help command
        help = "Seed for the game instance. Uses random seed if not set.",
    )

    // Optional arg with behavior to fallback to 'default' if not found in the args array
    val numLives: Int by parser.optionalArg(
        "-l", "--num-lives",
        valueLabel = "COUNT",
        help = "Set count of player lives",
        default = 3
    ) { argString ->
        argString.toInt().also {
            require(it > 0) { "Lives must be > 0" }
        }
    }

    // Optional flag arg which is false by default unless found in the args array
    val cheatsEnabled: Boolean by parser.flagArg(
        "-c", "--cheats-enabled",
        help = "Enable use of cheat codes"
    )

    // Required in the args array
    // Map arg restricts possible String values and serializes to the Maps value
    val mode: Mode by parser.requiredMapArg(
        "-m", "--mode",
        valueLabel = "MODE",
        help = "Set game mode difficulty",
	map = mapOf(
            "easy" to Mode.EASY,
            "medium" to Mode.MEDIUM,
            "hard" to Mode.HARD
        )
    )

    // The first positional arg
    val playerSpeed: Double by parser.positionalArg(
        valueLabel = "SPEED",
        help = "Player speed"
    ) { argString ->
        argString.toDouble().also {
            require(it >= 0.0)
        }
    }

    // The second positional arg
    val saveFile: File by parser.positionalArg(
        valueLabel = "FILE",
        help = "Save file location"
    ) { File(it) }

    enum class Mode { EASY, MEDIUM, HARD }
}
```

#### Instantiate the `CmdArgsParser` with `args` and parse `MyGameArgs`
```kotlin
val args = arrayOf(
  "-l", "9",
  "--cheats-enabled",
  "--mode=medium",
  "--",
  "100.50",
  "C:\\Users\\User\\MyGame\\saves"
)

CmdArgsParser(args, "MyGame.jar").parse(::MyGameArgs)
// the parse() op returns a Kotlin Result where onSuccess is only called when parsing the args
// array into the custom args class was successful
.onSuccess { parsedArgs ->
    handleParsedArgs(parsedArgs)
}.onFailure {
    // Optionally handle parse failure
}
```

#### Help command output
```
Usage: MyGame.jar
-m=MODE 
[-s=SEED] [-l=COUNT] 
[-c] 
[--] SPEED FILE 

Required args:                                                                         
-m MODE, --mode MODE        : Set game mode difficulty                                 
	MODE={easy,medium,hard}                                                               
                                                                                       
Positional args:                                                                       
SPEED                       : Player speed                                             
FILE                        : Save file location                                       
                                                                                       
Optional args:                                                                         
-s SEED, --seed SEED        : Seed for the game instance. Uses random seed if not set. 
-l COUNT, --num-lives COUNT : Set count of player lives (Default 3)                    
                                                                                       
Flag args:                                                                             
-c, --cheats-enabled        : Enable use of cheat codes (Default false)
```

# Argument nomenclature
**Options**: Generic term for the args defined before the positional args. Options encompass: requireds, optionals, and flag args.

**Optionals**: Key-value option that may or may not be declared in `args` and may or may not have a default fallback value

**Required**: Key-value option that must be provided in `args` to successfully parse

**Flag**: Optional without a key that maps to a `Boolean`. `False` by default and can be overriden to be `true` by default.

**Mapping**: Key-value option with a restricted set of values. Can be made either required or optional.

**Positional**: Argument(s) found after the option declarations in `args`. Their 'position' in `args` matters relative to thier declaration order.

# Formatting `--help` output
Formatting the `--help` output is limited in the project's current state. However, you may set a prologue or an epilogue statement like so: 

# Exceptions

# Subcommands
Example of a file encryption program with 'encrypt' and 'decrypt' subcommands

```kotlin
class FileEncryptorArgs(parser: CmdArgsParser) {

    val encryptionArgs: EncryptionArgs? by parser.subparser("encrypt", "Encryption mode", ::EncryptionArgs)
    val decryptionArgs: DecryptionArgs? by parser.subparser("decrypt", "Decryption mode", ::DecryptionArgs)

    // Note that this can be a handy way to cut down on code duplication and share the same args amongst subcommands
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

    class EncryptionArgs(subparser: CmdArgsParser): SharedArgs(subparser) {
        val encFileExcludeRegex: Regex? by subparser.optionalArg(
            "-f", "--enc-filereg",
            valueLabel = "REGEX",
            help = "Exclude file regex for encryption"
        ) { it.toRegex() }

        val encDirExcludeRegex: Regex? by subparser.optionalArg(
            "-d", "--enc-dirreg",
            valueLabel = "REGEX",
            help = "Exclude directory regex for encryption"
        ) { it.toRegex() }
    }

    class DecryptionArgs(subparser: CmdArgsParser): SharedArgs(subparser)
}
```

#### `--help` output
```
Usage: FileEncryptor.jar
SUBCOMMAND [ARGS]
                                                               
Subcommands:                                                     
encrypt      : Encryption mode                              
decrypt      : Decryption mode   
```

# Tests

# Importing the library

# Todo
- [ ] Optional positonals
- [ ] Arbitrary # of positional args
- [ ] Arbitrary # of optional values
- [ ] Bash-style regex string parsing
- [ ] Customizable `--help` command formatting
- [ ] Range or other arg validation info in `--help`
- [ ] Embedded subcommands eg, 

# Contributing
MRs and creating issues are more than welcome 

# Aknowledgement
Thanks to [kotlin-argparser](https://github.com/xenomachina/kotlin-argparser) for the inspiration of this project
