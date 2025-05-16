## Description
An easy-to-use command-line argument parser for Kotlin apps. Interprets an `Array<String>`, `args`, formatted in a familiar syntax and serializes a valid, stable object of arguments to be coveniently read in any given program.

## Features
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

## Table of contents
- [Argument nomenclature](#argument-nomenclature)
- [Game args example walkthrough](#game-args-example-walkthrough)
	- [Optional args](#optional-args)
	- [Required args](#required-args)
	- [Optional args with defaults](#optional-args-with-defaults)
	- [Flag args](#flag-args)
	- [Mapped args](#mapped-args)
	- [Positional args](#positional-args)
	- [Configuring help output](#configuring-help-output)
	- [Complete example](#complete-example)
- [Exceptions](#exceptions)
- [Subcommands](#subcommands)
- [Tests](#tests)
- [Importing the library](#importing-the-library)
- [Todo](#todo)
- [Contributing](#contributing)
- [Aknowledgement](#aknowledgement)

---

# Argument nomenclature

**Options**: Generic term for the args defined before the positional args. Options encompass: requireds, optionals, and flag args.

**Optionals**: Key-value option that may or may not be declared in `args` and may or may not have a default fallback value

**Required**: Key-value option that must be provided in `args` to successfully parse

**Flag**: Optional without a key that maps to a `Boolean`. `False` by default and can be overriden to be `true` by default.

**Mapped**: Key-value option with a restricted set of values. Can be made either required or optional.

**Positional**: Argument(s) found after the option declarations in `args`. Their 'position' in `args` matters relative to thier declaration order.

# Game args example walkthrough
Example creating and parsing `args` for a game program

#### Create the custom args class
To begin, create a custom class with only `CmdArgsParser` in the constructor.

```kotlin
class MyGameArgs(parser: CmdArgsParser)
```
We are now ready to start defining the args as member properties on this class.

#### Optional args
Say we wanted a 'seed' argument for the program, where the user may or may not specify it. This can looks something like this: 
```kotlin
val seed: String? by parser.optionalArg(
    "-s", "--seed",
    valueLabel = "SEED",
    help = "Seed for the game instance. Uses random seed if not set.",
)
```
Breaking this down, `parser.optionalArg()` returns a modified `Lazy` delgate whose initialized value is nullable. So we explicitly define the return type as `String?` and not `String`. The value of the `help` parameter here indicates that if the user does not specify a `--seed` then null will be set and the program can later interpret that to mean generating a random seed for the game instance. The vararg param `keys` allows us to accept either `-s` or its verbose form `--seed` as keys in `args`. Lastly, the `valueLabel` parameter is used by the `--help` command to demonstrate usage of the command eg, `[-s SEED]` 

#### Optional args with defaults
If we wanted an optional arg to fallback to some default value instead of null, we can change the return type to a non-nullable type `Int` and include a default parameter: 
```kotlin
val numLives: Int by parser.optionalArg(
    "-l", "--num-lives",
    valueLabel = "COUNT",
    help = "Set count of player lives",
    default = 3
) 
```

#### Casting arg values
Note in the previous example that `numLives` is an `Int`. In its current state the app would return a `Result.Failure` with a `CmdArgsParseException` and print `error: Failed casting value for member 'numLives' with type kotlin.Int. Did you include the initializer() parameter?`. By default the parser does not handle casting the value of the arg. You must explicity cast them from `String` to the desired type and return it in the `initializer` parameter. You can also optionally add a validation check:   
```kotlin
val numLives: Int by parser.optionalArg(
    "-l", "--num-lives",
    valueLabel = "COUNT",
    help = "Set count of player lives",
    default = 3,
    initializer = { argString ->
        argString.toInt().also {
            require(it > 0) { "Lives must be > 0" }
        }
    }
) 
```

#### Required args
Specifying required args is very similar to specifying optional arguments. Make the seed arg required by changing the return type to a non-null `String` and calling `parser.requiredArg()`:
```kotlin
val seed: String by parser.requiredArg(
    "-s", "--seed",
    valueLabel = "SEED",
    help = "Seed for the game instance"
)
```

#### Flag args
#### Mapped args
#### Positional args
#### Configuring help output
#### Complete example
```kotlin
class MyGameArgs(parser: CmdArgsParser): CmdArgHelpConfigHolder {

     override val cmdArgHelpConfig: CmdArgHelpConfig
        get() = CmdArgHelpConfig(
                prologue = "Prologue - A challenging puzzle game all about life",
		epilogue = "Epilogue - Have fun!"
            }
        )

    val seed: String? by parser.optionalArg(
        "-s", "--seed",
        valueLabel = "SEED",
        help = "Seed for the game instance. Uses random seed if not set.",
    )

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

    val cheatsEnabled: Boolean by parser.flagArg(
        "-c", "--cheats-enabled",
        help = "Enable use of cheat codes"
    )

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

    val playerSpeed: Double by parser.positionalArg(
        valueLabel = "SPEED",
        help = "Player speed"
    ) { argString ->
        argString.toDouble().also {
            require(it >= 0.0)
        }
    }

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
.onSuccess { parsedArgs ->
    handleParsedArgs(parsedArgs)
}.onFailure {
    // Optionally handle parse failure
}
```

# Formatting `--help` output
Formatting the `--help` output is limited in the project's current state. However, you may set a prologue or an epilogue statement like so: 

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
