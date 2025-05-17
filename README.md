## Description
An easy-to-use command-line argument parser for Kotlin apps. Interprets an `Array<String>`, `args`, formatted in a familiar syntax and serializes a valid, stable object of arguments to be coveniently read in any given program.

## Features
* Declare required, optional, flag, and positional args
* Supports multiple key-value delimiters for options, eg: `-n 3`, `-n=3`, or `-n3`
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
    - [Casting arg values](#casting-arg-values)
    - [Required args](#required-args)
    - [Flag args](#flag-args)
    - [Mapped args](#mapped-args)
    - [Positional args](#positional-args)
    - [Configuring help output](#configuring-help-output)
    - [Complete custom args class](#complete-custom-args-class)
    - [Parsing and using the args](#parsing-and-using-the-args)
    - [Builtin help command](#builtin-help-command)
    - [Builtin version command](#builtin-version-command)
- [File encryption example with subcommands](#file-encryption-example-with-subcommands)
- [Exceptions](#exceptions)
- [Tests](#tests)
- [Importing the library](#importing-the-library)
- [Todo](#todo)
- [Contributing](#contributing)
- [Aknowledgement](#aknowledgement)

---

## Argument nomenclature

**Options**: Generic term for the args defined before the positional args. Options encompass: requireds, optionals, and flag args.

**Optionals**: Key-value option that may or may not be declared in `args` and may or may not have a default fallback value

**Required**: Key-value option that must be provided in `args` to successfully parse

**Flag**: Optional without a key that maps to a `Boolean`. `false` by default and can be overriden to be `true` by default.

**Mapped**: Key-value option with a restricted set of values. Can be made either required or optional.

**Positional**: Argument(s) found after the option declarations in `args`. Their 'position' in `args` matters relative to thier declaration order.

## Game args example walkthrough
Example creating and parsing `args` for a game program

### Create the custom args class
To begin, create a custom class with only `CmdArgsParser` in the constructor.

```kotlin
class MyGameArgs(parser: CmdArgsParser)
```
We are now ready to start defining the args as member properties on this class.

### Optional args
Say we wanted a 'seed' argument for the program, where the user may or may not specify it. This can looks something like this: 
```kotlin
val seed: String? by parser.optionalArg(
    "-s", "--seed",
    valueLabel = "SEED",
    help = "Seed for the game instance. Uses random seed if not set.",
)
```
Breaking this down, `parser.optionalArg()` returns a modified `Lazy` delgate whose initialized value is nullable. So we explicitly define the return type as `String?` and not `String`. The value of the `help` parameter here indicates that if the user does not specify a `--seed` then null will be set and the program can later interpret that to mean generating a random seed for the game instance. The vararg param `keys` allows us to accept either `-s` or its verbose form `--seed` as keys in `args`. Lastly, the `valueLabel` parameter is used by the `--help` command to demonstrate usage of the command eg, `[-s SEED]` 

If we wanted an optional arg to fallback to some default value instead of null, we can change the return type to a non-nullable type `Int` and include a default parameter: 
```kotlin
val numLives: Int by parser.optionalArg(
    "-l", "--num-lives",
    valueLabel = "COUNT",
    help = "Set count of player lives",
    default = 3
) 
```

### Casting arg values
Note in the previous example that `numLives` is an `Int`. In its current state the app would return a `Result.Failure` with a `CmdArgsParseException` and print `error: Failed casting value for member 'numLives' with type kotlin.Int. Did you include the initializer() parameter?`. By default the parser does not handle casting the value of the arg. You must explicity cast them from `String` to the desired type and return it in the `initializer` parameter.  

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

### Required args
Specifying required args is very similar to specifying optional arguments. Make the seed arg required by changing the return type to a non-null `String` and calling `parser.requiredArg()`:
```kotlin
val seed: String by parser.requiredArg(
    "-s", "--seed",
    valueLabel = "SEED",
    help = "Seed for the game instance"
)
```

### Flag args
Flags are optionals whose value is either `true` or `false`. These are parsed as `false` if not found in `args`, and `true` otherwise.
Setup a flag that when declared, enables the use of cheat codes:
```kotlin
val cheatsEnabled: Boolean by parser.flagArg(
    "-c", "--cheats-enabled",
    help = "Enable use of cheat codes"
)
```
The behavior can be reversed to set cheats enabled by default by specifying the `default` parameter:
```kotlin
val cheatsEnabled: Boolean by parser.flagArg(
    "-c", "--no-cheats",
    help = "Disable use of cheat codes",
    default = true
)
```

### Mapped args
Args with a restricted value set can use `parser.optionalMapArg()` or `parser.requiredMapArg()`. The following defines a required arg which maps values "easy", "medium", and "hard" to the enums `Mode.EASY`, `Mode.MEDIUM`, and `Mode.HARD` respectively:
```kotlin
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
```

### Positional args
Positional args are declared with `parser.positionalArg()`. Here we define player speed and the path of the path of the save file: 
```kotlin
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
```
The order in which they are declared in the args class matters. For example, the command line should should specify `SPEED` and then `FILE`.

### Configuring help output
Formatting the `--help` output is limited in the project's current state. However, you may set a prologue or an epilogue statement like so: 
```kotlin
class MyGameArgs(parser: CmdArgsParser): CmdArgHelpConfigHolder {
    override val cmdArgHelpConfig: CmdArgHelpConfig
        get() = CmdArgHelpConfig(
	    prologue = "Prologue - A challenging puzzle game all about life",
	    epilogue = "Epilogue - Have fun!"
        }
// ...
```
We will see the usage and output of the `--help` command shortly.

### Complete custom args class
```kotlin
class MyGameArgs(parser: CmdArgsParser): CmdArgHelpConfigHolder {

    override val cmdArgHelpConfig: CmdArgHelpConfig
        get() = CmdArgHelpConfig(
	    prologue = "Prologue - A challenging puzzle game all about life",
	    epilogue = "Epilogue - Have fun!"
        }

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
### Parsing and using the args
A basic usage looks like this:
```kotlin
val args = arrayOf(
  "-l", "9",
  "--cheats-enabled",
  "--mode=medium",
  "--",
  "100.50",
  "C:\\Users\\User\\MyGame\\saves"
)

CmdArgsParser(args, programName = "MyGame.jar").parse(::MyGameArgs)
.onSuccess { parsedArgs ->
    handleParsedArgs(parsedArgs)
}.onFailure {
    // Optionally handle parse failure
}
```
Observe that an `Array<String> args` has been defined where, according to the configuration of `MyGameArgs`, the player has 9 lives (`-l 9`), cheats are enabled (`--cheats-enabled`), medium difficulty is set (`--mode=medium`), the player has 100.50 speed (SPEED `100.50`), and the save file path is (FILE `C:\\Users\\User\\MyGame\\saves`). These `args` have been passed into the `CmdArgsParser` along with a `programName` which is referenced in the output of the `--help` command. Then, the `parse(::MyGameArgs)` call returns a `Kotlin.Result` where `Result.Success` is only returned when the args are validated and parsed successfully. 

### Builtin help command
Running the `--help` command is as simple as providing "--help" as the only arg:
```kotlin
val args = arrayOf("--help")
```

**Output**
```
Usage: MyGame.jar
-m=MODE 
[-s=SEED] [-l=COUNT] 
[-c] 
[--] SPEED FILE

Prologue - A challenging puzzle game all about life

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

Epilogue - Have fun!
```

### Builtin version command
Supply "--version" as the only arg. This will print out the value of the `String version` param provided in the `CmdArgsParser`'s constructor. 
```kotlin
val args = arrayOf("--version")
CmdArgsParser(args, programName = "MyGame.jar", version = "MyGame version 1.0").parse(::MyGameArgs)
```

**Output**

`MyGame version 1.0`

## File encryption example with subcommands 
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

**`--help` output**

```
Usage: FileEncryptor.jar
SUBCOMMAND [ARGS]
                                                               
Subcommands:                                                     
encrypt      : Encryption mode                              
decrypt      : Decryption mode   
```

**encrypt subcommand --help output**

# Exceptions

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
