package lib.lib_args_parse

import lib.lib_args_parse.exception.CmdArgsBuiltinCommandException
import lib.lib_args_parse.exception.CmdArgsParseException
import lib.lib_args_parse.exception.CmdArgsParserInitializationException
import lib.lib_args_parse.exception.CmdArgsMalformedException
import lib.lib_args_parse.help.CmdArgHelpConfig
import lib.lib_args_parse.help.CmdArgHelpConfigHolder
import lib.lib_args_parse.help.CmdArgsParserHelpPrinter
import lib.lib_args_parse.model.CmdArgNonNull
import lib.lib_args_parse.model.CmdArgNullable
import lib.lib_args_parse.model.Subcommand

private const val OPTIONS_POSITIONALS_ARGS_DELIM = "--"

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

    fun <T> optionalArg(
        vararg keys: String,
        valueLabel: String,
        hint: String,
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
            hint = hint,
            valueLabel = valueLabel,
            keys = keys.toList().toTypedArray()
        ).also {
            opts.add(it as CmdArgNullable.KeyValue<Any?>)
        }
    }

    fun <T> optionalArg(
        vararg keys: String,
        default: T,
        valueLabel: String,
        hint: String,
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
            hint = hint,
            valueLabel = valueLabel,
            keys = keys.toList().toTypedArray(),
            default = default
        ).also {
            optDefaults.add(it as CmdArgNonNull.KeyValue<Any>)
        }
    }

    fun <T> requiredArg(
        vararg keys: String,
        valueLabel: String,
        hint: String,
        initializer: (String) -> T = { it as T }
    ): CmdArgNonNull.KeyValue.Single<T> {

        validateArgKeys(keys.toList())

        return CmdArgNonNull.KeyValue.Single(
            initializer = {
                val v = findValueInArgs(keys.toList())
                requireNotNull(v) { "${getKeysLogTag(keys.toList())} Required value not found" }
                initializer(v)
            },
            hint = hint,
            valueLabel = valueLabel,
            keys = keys.toList().toTypedArray(),
            default = null
        ).also {
            reqs.add(it as CmdArgNonNull.KeyValue<Any>)
        }
    }

    fun <T> optionalMappedArg(
        vararg keys: String,
        valueLabel: String,
        hint: String,
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
            hint = hint,
            valueLabel = valueLabel,
            keys = keys.toList().toTypedArray(),
            map = map
        ).also {
            opts.add(it as CmdArgNullable.KeyValue<Any?>)
        }
    }

    fun <T> optionalMappedArg(
        vararg keys: String,
        default: T,
        valueLabel: String,
        hint: String,
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
            hint = hint,
            valueLabel = valueLabel,
            keys = keys.toList().toTypedArray(),
            map = map,
            default = default
        ).also {
            optDefaults.add(it as CmdArgNonNull.KeyValue<Any>)
        }
    }

    fun <T> requiredMapArg(
        vararg keys: String,
        valueLabel: String,
        hint: String,
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
            hint = hint,
            valueLabel = valueLabel,
            keys = keys.toList().toTypedArray(),
            map = map,
            default = null
        ).also {
            reqs.add(it as CmdArgNonNull.KeyValue<Any>)
        }
    }

    fun flagArg(
        vararg keys: String,
        hint: String,
        default: Boolean = false
    ): CmdArgNonNull.Flag {

        validateArgKeys(keys.toList())

        return CmdArgNonNull.Flag(
            keys = keys,
            hint = hint,
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

    fun <T> positionalArg(
        valueLabel: String,
        hint: String,
        initializer: (String) -> T = { it as T }
    ): CmdArgNonNull.Positional<T> {

        validatePosValueLabel(valueLabel)

        return CmdArgNonNull.Positional(
            hint,
            valueLabel,
            initializer = {
                val i = positionalIndexMap[valueLabel]!!
                initializer(args[i])
            }
        ).also {
            positionals.add(it as CmdArgNonNull.Positional<Any>)
        }
    }

    fun <T> subparser(subcommand: String, creator: (CmdArgsParser) -> T): Subcommand<T?> {
        validateSubcommand(subcommand)

        return if (args.firstOrNull() == subcommand) {
            val subparser = CmdArgsParser(args.sliceArray(1..args.lastIndex), "$programName $subcommand", version)
            subparsers[subcommand] = Subcommand {
                subparser.parse(creator).getOrThrow()
            }
            activeSubcommand = subparsers[subcommand]!!
            activeSubcommand as Subcommand<T?>
        } else {
            val subparser = CmdArgsParser(args, "$programName $subcommand", version)
            subparsers[subcommand] = Subcommand { null }
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
            validateCmdArgValues()
            Result.success(parsedArgs)
        } catch (e: Exception) {
            val parseEx = CmdArgsParseException(e)
            printParseError(e)
            Result.failure(parseEx)
        }
    }

    private fun printParseError(e: Exception) {
        println("error: ${e.message}")
    }

    private fun printVersion() {
        println(version)
    }

    // Ensure stability for when you go to parse the sub args later in runtime
    private fun validateActiveSubCommand() {
        activeSubcommand!!.initializer!!.invoke()
    }

    /**
     * Inspects each String in the args Array and checks that they are accounted for in the parsed CmdArgs
     * In addition, checks all required and positional args are accounted for
     */
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
                        val k = findKeyInOptionals(arg)

                        if (k != null) {
                            require(args.size > i + 1) { "No value specified for arg $k" }
                            optsKeyValueMap[arg] = args[i + 1]
                            i += 2
                        } else {
                            throw IllegalArgumentException("Arg not registered in optionals: $arg")
                        }
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
                        val k = findKeyInOptionals(a)

                        when {
                            arg == k -> {
                                require(args.size > i + 1) { "No value specified for arg $k" }
                                val value = args[i + 1]
                                require(!value.startsWith("-")) { "No value specified for arg $k" }
                                optsKeyValueMap[arg] = value
                                i += 2
                            }

                            k != null && arg.length > k.length -> {
                                optsKeyValueMap[a] = arg.substring(2)
                                i++
                            }

                            else -> throw IllegalArgumentException("No key found for arg: $arg")
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

    private fun findKeyInOptionals(arg: String): String? {
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

        return null
    }

    private fun validateCmdArgValues() {
        opts.forEach { it.validate() }
        optDefaults.forEach { it.validate() }
        reqs.forEach { it.validate() }
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

    fun printHelp() {
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