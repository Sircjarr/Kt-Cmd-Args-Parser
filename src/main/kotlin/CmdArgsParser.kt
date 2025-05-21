package com.github.sircjarr.cmdargsparser

import com.github.sircjarr.cmdargsparser.exception.CmdArgsBuiltinCommandException
import com.github.sircjarr.cmdargsparser.exception.CmdArgsParseException
import com.github.sircjarr.cmdargsparser.exception.CmdArgsParserInitializationException
import com.github.sircjarr.cmdargsparser.exception.CmdArgsMalformedException
import com.github.sircjarr.cmdargsparser.help.CmdArgHelpConfig
import com.github.sircjarr.cmdargsparser.help.CmdArgHelpConfigHolder
import com.github.sircjarr.cmdargsparser.help.CmdArgsParserHelpPrinter
import com.github.sircjarr.cmdargsparser.model.CmdArgNonNull
import com.github.sircjarr.cmdargsparser.model.CmdArgNullable
import com.github.sircjarr.cmdargsparser.model.Subcommand
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.full.declaredMembers

private const val OPTIONS_POSITIONALS_ARGS_DELIM = "--"

/**
 * A command-line argument parser.
 *
 * @property args The command-line arguments.
 * @property programName The name of the program printed in the `--help` command output.
 * @property version The version of the program printed in the `--version` command output.
 */
@Suppress("UNCHECKED_CAST")
class CmdArgsParser(
    private val args: Array<String>,
    private val programName: String,
    private val version: String = "$programName version <TODO>"
) {
    private val opts = mutableListOf<CmdArgNullable.KeyValue<Any?>>()
    private val optDefaults = mutableListOf<CmdArgNonNull.KeyValue<Any>>()
    private val reqs = mutableListOf<CmdArgNonNull.KeyValue<Any>>()

    private val flags = mutableListOf<CmdArgNonNull.Flag>()
    private val positionals = mutableListOf<CmdArgNonNull.Positional<Any>>()

    // Map of value label to args index
    private val positionalIndexMap = mutableMapOf<String, Int>()

    // Map of arg key to value in arg array
    private val optsKeyValueMap = mutableMapOf<String, String?>()

    private var cmdArgHelpConfig: CmdArgHelpConfig? = null

    private val subparsers: MutableMap<String, Subcommand<Any?>> by lazy { mutableMapOf() }
    private var activeSubcommand: Subcommand<Any?>? = null

    private var parseResult: Result<Any>? = null

    private val argKeyRegex = "^-([a-zA-Z]|-[a-zA-Z][a-zA-Z-]*)$".toRegex()
    private val helpRegex = "help|--help".toRegex()
    private val versionRegex = "version|--version".toRegex()
    private val quitRegex = "q|quit|exit|--quit|--exit".toRegex()
    private val subcommandRegex = "[a-zA-Z0-9]+".toRegex()

    /**
     * Add an optional argument to this class.
     * - Falls back to `null` if not found in [args].
     *
     * @param keys Valid keys for this arg starting with either "-" or "--".
     * @param valueLabel Arg value label to be used by the `--help` command. For example, [-i INCLUDE].
     * @param help Description of this arg shown in the output of the `--help` command.
     * @param initializer Function to cast and validate the arg value to desired type.
     * @return [CmdArgNullable.KeyValue.Single]
     * @throws [CmdArgsParserInitializationException] if any [keys] have invalid format, are the same as a builtin
     * command, or already declared.
     */
    fun <T> optionalArg(
        vararg keys: String,
        valueLabel: String,
        help: String,
        initializer: (String) -> T = { it as T }
    ): CmdArgNullable.KeyValue.Single<T?> {

        validateArgKeys(keys.toList())

        return CmdArgNullable.KeyValue.Single<T?>(
            initializer = {
                val v = findValueInArgs(keys.toList())

                if (v != null) {
                    initializer(v)
                } else null
            },
            help = help,
            valueLabel = valueLabel,
            keys = keys.toList().toTypedArray()
        ).also {
            opts.add(it as CmdArgNullable.KeyValue<Any?>)
        }
    }

    /**
     * Add an optional argument to this class.
     * - Falls back to [default] if not found in [args].
     *
     * @param keys Valid keys for this arg starting with either "-" or "--".
     * @param default Default value to return if not found in [args].
     * @param valueLabel Arg value label to be used by the `--help` command. For example, [-i INCLUDE].
     * @param help Description of this arg shown in the output of the `--help` command.
     * @param initializer Function to cast and validate the arg value to desired type.
     * @return [CmdArgNonNull.KeyValue.Single]
     * @throws [CmdArgsParserInitializationException] if any [keys] have invalid format, are the same as a builtin
     * command, or already declared.
     */
    fun <T> optionalArg(
        vararg keys: String,
        default: T,
        valueLabel: String,
        help: String,
        initializer: (String) -> T = { it as T }
    ): CmdArgNonNull.KeyValue.Single<T> {

        validateArgKeys(keys.toList())

        return CmdArgNonNull.KeyValue.Single(
            initializer = {
                val v = findValueInArgs(keys.toList())

                if (v != null) {
                    initializer(v)
                } else default
            },
            help = help,
            valueLabel = valueLabel,
            keys = keys.toList().toTypedArray(),
            default = default
        ).also {
            optDefaults.add(it as CmdArgNonNull.KeyValue<Any>)
        }
    }

    /**
     * Add a required argument to this class.
     * - [parse] returns [Result.Failure] with [CmdArgsParseException] if not declared in [args].
     *
     * @param keys Valid keys for this arg starting with either "-" or "--".
     * @param valueLabel Arg value label to be used by the `--help` command. For example, [-i INCLUDE].
     * @param help Description of this arg shown in the output of the `--help` command.
     * @param initializer Function to cast and validate the arg value to desired type.
     * @return [CmdArgNonNull.KeyValue.Single]
     * @throws [CmdArgsParserInitializationException] if any [keys] have invalid format, are the same as a builtin
     * command, or already declared.
     */
    fun <T> requiredArg(
        vararg keys: String,
        valueLabel: String,
        help: String,
        initializer: (String) -> T = { it as T }
    ): CmdArgNonNull.KeyValue.Single<T> {

        validateArgKeys(keys.toList())

        return CmdArgNonNull.KeyValue.Single(
            initializer = {
                val v = findValueInArgs(keys.toList())
                requireNotNull(v) { "${getKeysLogTag(keys.toList())} Required value not found" }
                initializer(v)
            },
            help = help,
            valueLabel = valueLabel,
            keys = keys.toList().toTypedArray(),
            default = null
        ).also {
            reqs.add(it as CmdArgNonNull.KeyValue<Any>)
        }
    }

    /**
     * Add an optional argument with restricted values to this class.
     * - Falls back to `null` if not found in [args].
     *
     * @param keys Valid keys for this arg starting with either "-" or "--".
     * @param valueLabel Arg value label to be used by the `--help` command. For example, [-m MODE].
     * @param help Description of this arg shown in the output of the `--help` command.
     * @param map Map of possible values to desired object.
     * @return [CmdArgNullable.KeyValue.Mapped]
     * @throws [CmdArgsParserInitializationException] if any [keys] have invalid format, are the same as a builtin
     * command, or already declared.
     */
    fun <T> optionalMappedArg(
        vararg keys: String,
        valueLabel: String,
        help: String,
        map: Map<String, T>
    ): CmdArgNullable.KeyValue.Mapped<T> {

        validateArgKeys(keys.toList())
        validateMapKeys(map)

        return CmdArgNullable.KeyValue.Mapped(
            initializer = {
                val v = findValueInArgs(keys.toList())
                if (v != null) {
                    val res = map[v]
                    requireNotNull(res) { "${getKeysLogTag(keys.toList())} Mapping not found for value $res" }
                    map[v] as T
                } else null
            },
            help = help,
            valueLabel = valueLabel,
            keys = keys.toList().toTypedArray(),
            map = map
        ).also {
            opts.add(it as CmdArgNullable.KeyValue<Any?>)
        }
    }

    /**
     * Add an optional argument with restricted values to this class.
     * - Falls back to [default] if not found in [args].
     *
     * @param keys Valid keys for this arg starting with either "-" or "--".
     * @param default Default value to return if not found in [args].
     * @param valueLabel Arg value label to be used by the `--help` command. For example, [-m MODE].
     * @param help Description of this arg shown in the output of the `--help` command.
     * @param map Map of possible values to desired object.
     * @return [CmdArgNonNull.KeyValue.Mapped]
     * @throws [CmdArgsParserInitializationException] if any [keys] have invalid format, are the same as a builtin
     * command, or already declared.
     */
    fun <T> optionalMappedArg(
        vararg keys: String,
        default: T,
        valueLabel: String,
        help: String,
        map: Map<String, T>
    ): CmdArgNonNull.KeyValue.Mapped<T> {

        validateArgKeys(keys.toList())
        validateMapKeys(map)

        return CmdArgNonNull.KeyValue.Mapped(
            initializer = {
                val v = findValueInArgs(keys.toList())
                if (v != null) {
                    val res = map[v]
                    requireNotNull(res) { "${getKeysLogTag(keys.toList())} Mapping not found for value $res" }
                    map[v] as T
                } else default
            },
            help = help,
            valueLabel = valueLabel,
            keys = keys.toList().toTypedArray(),
            map = map,
            default = default
        ).also {
            optDefaults.add(it as CmdArgNonNull.KeyValue<Any>)
        }
    }

    /**
     * Add a required argument with restricted values to this class.
     * - [parse] returns [Result.Failure] with [CmdArgsParseException] if not found in [args].
     *
     * @param keys Valid keys for this arg starting with either "-" or "--".
     * @param default Default value to return if not found in [args].
     * @param valueLabel Arg value label to be used by the `--help` command. For example, [-m MODE].
     * @param help Description of this arg shown in the output of the `--help` command.
     * @param map Map of possible values to desired object.
     * @return [CmdArgNullable.KeyValue.Mapped]
     * @throws [CmdArgsParserInitializationException] if any [keys] have invalid format, are the same as a builtin
     * command, or already declared.
     */
    fun <T> requiredMapArg(
        vararg keys: String,
        valueLabel: String,
        help: String,
        map: Map<String, T>
    ): CmdArgNonNull.KeyValue.Mapped<T> {

        validateArgKeys(keys.toList())
        validateMapKeys(map)

        return CmdArgNonNull.KeyValue.Mapped(
            initializer = {
                val v = findValueInArgs(keys.toList())
                requireNotNull(v) { "${getKeysLogTag(keys.toList())} Required value not found" }

                val res = map[v]
                requireNotNull(res) { "${getKeysLogTag(keys.toList())} Mapping not found for value $res" }
                map[v] as T
            },
            help = help,
            valueLabel = valueLabel,
            keys = keys.toList().toTypedArray(),
            map = map,
            default = null
        ).also {
            reqs.add(it as CmdArgNonNull.KeyValue<Any>)
        }
    }

    /**
     * Add an optional flag argument that maps to a `Boolean`.
     * - Falls back to [default] if not declared in [args].
     *
     * @param keys Valid keys for this arg starting with either "-" or "--".
     * @param help Description of this arg shown in the output of the `--help` command.
     * @param default Default value to return if not found in [args].
     * @return [CmdArgNonNull.Flag]
     * @throws [CmdArgsParserInitializationException] if any [keys] have invalid format, are the same as a builtin
     * command, or already declared.
     */
    fun flagArg(
        vararg keys: String,
        help: String,
        default: Boolean = false
    ): CmdArgNonNull.Flag {

        validateArgKeys(keys.toList())

        return CmdArgNonNull.Flag(
            keys = keys,
            help = help,
            default = default
        ) {
            val isFound = argsContainsFlag(keys.toList())
            if (isFound) !default else default
        }.also {
            flags.add(it)
        }
    }

    private fun argsContainsFlag(keys: List<String>): Boolean {
        return keys.any {
            optsKeyValueMap[it] == "true"
        }
    }

    /**
     * Add a required positional argument to this class.
     * - [parse] returns [Result.Failure] with [CmdArgsParseException] if not specified in [args].
     * - Options and positional args may be separated with "--".
     *
     * @param valueLabel Arg value label to be used by the `--help` command. For example, [-m MODE].
     * @param help Description of this arg shown in the output of the `--help` command.
     * @param initializer Function to cast and validate the arg value to desired type.
     * @return [CmdArgNullable.KeyValue.Mapped]
     */
    fun <T> positionalArg(
        valueLabel: String,
        help: String,
        initializer: (String) -> T = { it as T }
    ): CmdArgNonNull.Positional<T> {

        validatePosValueLabel(valueLabel)

        return CmdArgNonNull.Positional(
            help,
            valueLabel,
            initializer = {
                val i = positionalIndexMap[valueLabel]!!
                initializer(args[i])
            }
        ).also {
            positionals.add(it as CmdArgNonNull.Positional<Any>)
        }
    }

    /**
     * Add a new subcommand to this class. Internally parses the args in [creator] the same way with a new
     * [CmdArgsParser] instance.
     *
     * @param subcommand Name of the subcommand.
     * @param help Description of this subcommand shown in the output of the `--help` command.
     * @param creator Initializer of the custom args class.
     * @return [Subcommand] Where the resolved value will be `null` if [subcommand] is not found in [args], or a
     * non-null custom args class if parsing was successful.
     * @throws [CmdArgsParserInitializationException] if [subcommand] is of invalid format or already declared.
     */
    fun <T> subparser(
        subcommand: String,
        help: String,
        creator: (CmdArgsParser) -> T
    ): Subcommand<T?> {
        validateSubcommand(subcommand)

        return if (args.firstOrNull() == subcommand) {
            val subparser = CmdArgsParser(args.sliceArray(1..args.lastIndex), "$programName $subcommand", version)
            subparsers[subcommand] = Subcommand(help) {
                subparser.parse(creator).getOrThrow()
            }
            activeSubcommand = subparsers[subcommand]!!
            activeSubcommand as Subcommand<T?>
        } else {
            val subparser = CmdArgsParser(args, "$programName $subcommand", version)
            subparsers[subcommand] = Subcommand(help) { null }
            creator(subparser)
            subparsers[subcommand]!! as Subcommand<T?>
        }
    }

    private fun findValueInArgs(
        keys: List<String>
    ): String? {

        for (k in keys) {
            if (optsKeyValueMap[k] != null) return optsKeyValueMap[k]
        }

        return null
    }

    private fun validateArgKeys(keys: List<String>) {
        try {
            require(keys.isNotEmpty()) { "No args keys provided" }
            require(keys.all { it.isKeyArg() }) { "${getKeysLogTag(keys)} args keys must match key arg pattern" }
            require(keys.all { !optsKeyValueMap.containsKey(it) }) { "${getKeysLogTag(keys)} key already declared" }
            require(keys.none { it.isBuiltInCommand() }) { "${getKeysLogTag(keys)} key contains a builtin command" }
        } catch (e: Exception) {
            throw CmdArgsParserInitializationException(e)
        }
        optsKeyValueMap.putAll(keys.associateWith { null })
    }

    private fun <T> validateMapKeys(mapping: Map<String, T>) {
        try {
            require(mapping.isNotEmpty()) { "key-value mapping must not be empty" }
            mapping.keys.forEach {
                require(!it.startsWith("-")) { "map key arg $it must not match the key arg pattern" }
            }
        } catch (e: Exception) {
            throw CmdArgsParserInitializationException(e)
        }
    }

    private fun validatePosValueLabel(label: String) {
        try {
            require(positionals.none { it.valueLabel == label }) { "Positional arg already declared: $label" }
        } catch (e: Exception) {
            throw CmdArgsParserInitializationException(e)
        }
    }

    private fun validateSubcommand(subcommand: String) {
        try {
            require(subcommand.matches(subcommandRegex)) { "Invalid subcommand format: $subcommand" }
            require(!subparsers.containsKey(subcommand)) { "Subcommand already declared: $subcommand" }
        } catch (e: Exception) {
            throw CmdArgsParserInitializationException(e)
        }
    }

    /**
     * Parse [args] into desired type [T].
     *
     * @param creator Initializer of the custom args class.
     * @return [Result] where [Result.isSuccess] implies stable, valid args have been parsed. [Result.Failure]
     * contains one of the custom [Exception] classes.
     * @see CmdArgsBuiltinCommandException
     * @see CmdArgsMalformedException
     * @see CmdArgsParseException
     */
    fun <T> parse(creator: (CmdArgsParser) -> T): Result<T> {
        if (parseResult != null) {
            return parseResult as Result<T>
        }

        parseResult = doParse(creator) as Result<Any>

        return parseResult as Result<T>
    }

    private fun <T> doParse(creator: (CmdArgsParser) -> T): Result<T> {
        val parsedArgs = creator(this)
        cmdArgHelpConfig = if (parsedArgs is CmdArgHelpConfigHolder) parsedArgs.cmdArgHelpConfig else null

        if (args.isEmpty() || args.firstOrNull()?.matches(helpRegex) == true) {
            printHelp()
            return Result.failure(CmdArgsBuiltinCommandException("--help"))
        }

        if (args.isEmpty() || args.firstOrNull()?.matches(versionRegex) == true) {
            printVersion()
            return Result.failure(CmdArgsBuiltinCommandException("--version"))
        }

        val quitArg = args.firstOrNull()?.matches(quitRegex) == true
        if (quitArg) {
            return Result.failure(CmdArgsBuiltinCommandException("quit"))
        }

        if (activeSubcommand != null) {
            return try {
                validateActiveSubCommand()
                Result.success(parsedArgs)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        try {
            validateArgsListFormat()
        } catch (e: Exception) {
            val parseEx = CmdArgsMalformedException(e)
            printParseError(e)
            return Result.failure(parseEx)
        }

        return try {
            validateCmdArgValues(parsedArgs)
            Result.success(parsedArgs)
        } catch (e: Exception) {
            val parseEx = CmdArgsParseException(e)
            printParseError(e)
            Result.failure(parseEx)
        }
    }

    private fun printParseError(t: Throwable) {
        System.err.println("error: ${t.message}")
    }

    private fun printVersion() {
        println(version)
    }

    // Ensure subcommand stability
    private fun validateActiveSubCommand() {
        activeSubcommand!!.initializer!!.invoke()
    }


    // Inspects each String in the args Array and verifies format compliance with the declared CmdArgs
    private fun validateArgsListFormat() {
        var i = 0
        while (i < args.size) {
            val arg = args[i]

            when {
                arg == OPTIONS_POSITIONALS_ARGS_DELIM -> {
                    i++
                    break
                }
                // --my-key=my_value | -n=43
                arg.contains("=") -> {
                    optsKeyValueMap[arg.substringBefore("=")] = arg.substringAfter("=")
                    i++
                }
                // --my-flag | --my-key value
                arg.startsWith("--") -> {

                    if (isFlagKey(arg)) {
                        optsKeyValueMap[arg] = "true"
                        i++
                    } else {
                        val k = requireKeyInOptionals(arg)

                        require(args.size > i + 1) { "No value specified for arg $k" }
                        optsKeyValueMap[arg] = args[i + 1]
                        i += 2
                    }
                }
                // -F | -FLAGS | -n 24 | -n24
                arg.startsWith("-") -> {
                    val a = arg.substring(0, 2)

                    if (isFlagKey(a)) {
                        i++
                        optsKeyValueMap[a] = "true"

                        for (f in arg.substring(2)) {
                            require(isFlagKey("-$f")) { "Unknown flag specified: $f" }
                            optsKeyValueMap["-$f"] = "true"
                        }
                    } else {
                        val k = requireKeyInOptionals(a)

                        when {
                            arg == k -> {
                                require(args.size > i + 1) { "No value specified for arg $k" }
                                val value = args[i + 1]
                                require(!value.startsWith("-")) { "No value specified for arg $k" }
                                optsKeyValueMap[arg] = value
                                i += 2
                            }

                            arg.length > k.length -> {
                                optsKeyValueMap[a] = arg.substring(2)
                                i++
                            }

                            else -> throw IllegalStateException("Invalid arg $arg and key $k")
                        }
                    }
                }

                else -> break
            }
        }

        // Validate all positional args provided
        val positionalArgs = args.copyOfRange(i, args.size)
        if (positionalArgs.size == positionals.size) {
            for (p in positionals) {
                positionalIndexMap[p.valueLabel] = i
                require(args[i].isNotBlank()) { "Positional ${p.valueLabel} with value ${args[i]} does not match the expected format" }
                i++
            }
        } else if (positionalArgs.size > positionals.size) {
            throw IllegalArgumentException("Unexpected arg: ${positionalArgs.first()}")
        } else {
            val missingPos = positionals.subList(positionalArgs.size, positionals.size)
            throw IllegalArgumentException("Positional arg(s) not provided: ${missingPos.joinToString { it.valueLabel }}")
        }
    }

    private fun isFlagKey(argKey: String): Boolean {
        return flags.any { flag -> flag.keys.any { it == argKey } }
    }

    private fun requireKeyInOptionals(arg: String): String {
        for (opt in reqs) {
            for (k in opt.keys) {
                if (k == arg) {
                    return k
                }
            }
        }

        for (opt in opts) {
            for (k in opt.keys) {
                if (k == arg) {
                    return k
                }
            }
        }

        for (opt in optDefaults) {
            for (k in opt.keys) {
                if (k == arg) {
                    return k
                }
            }
        }

        throw IllegalArgumentException("No key found for arg $arg")
    }

    private fun <T> validateCmdArgValues(parsedArgs: T) {
        parsedArgs!!::class.declaredMembers.forEach {
            try {
                it.call(parsedArgs)
            } catch (e: InvocationTargetException) {
                val cause = e.cause!!
                if (cause is ClassCastException) {
                    throw ClassCastException("Failed casting value for member '${it.name}' with type ${it.returnType}. Did you include the initializer() parameter?")
                }
                throw cause
            }
        }
    }

    private fun getKeysLogTag(keys: List<String>): String {
        return "[${keys.joinToString()}]"
    }

    private fun String.isKeyArg(): Boolean {
        return matches(argKeyRegex)
    }

    private fun String.isBuiltInCommand(): Boolean {
        return matches(helpRegex) || matches(versionRegex) || matches(quitRegex)
    }

    private fun printHelp() {
        CmdArgsParserHelpPrinter.print(
            programName,
            cmdArgHelpConfig?.epilogue,
            cmdArgHelpConfig?.prologue,
            opts,
            optDefaults,
            reqs,
            flags,
            positionals,
            subparsers
        )
    }
}