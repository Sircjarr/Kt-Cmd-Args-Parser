package lib.lib_args_parse

import lib.lib_args_parse.help.CmdArgsParserHelpPrinter
import kotlin.system.exitProcess

private const val OPTIONS_POSITIONALS_ARGS_DELIM = "--"

private const val EXIT_CODE_SUCCESS = 0
private const val EXIT_CODE_ERR_GENERAL = 1
private const val EXIT_CODE_ERR_CMD_MISUSE = 2

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
        validateSubparser(subcommand)

        return if (args.firstOrNull() == subcommand) {
            val subparser = CmdArgsParser(args.sliceArray(1..args.lastIndex), "$programName $subcommand", version)
            subparsers[subcommand] = Subcommand(subcommand, subparser) {
                subparser.parse(creator).getOrThrow()
            }
            activeSubcommand = subparsers[subcommand]!!
            activeSubcommand as Subcommand<T?>
        } else {
            val subparser = CmdArgsParser(args, "$programName $subcommand", version)
            subparsers[subcommand] = Subcommand(subcommand, subparser) { null }
            creator(subparser)
            subparsers[subcommand]!! as Subcommand<T?>
        }
    }

    private fun validateSubparser(subcommand: String) {
        require(subcommand.matches("[a-zA-Z0-9]+".toRegex())) { "Invalid subcommand format: $subcommand" }
        require(!subparsers.containsKey(subcommand)) { "Subcommand already declared: $subcommand" }
    }

    private fun validatePosValueLabel(label: String) {
        require(positionals.none { it.valueLabel == label }) { "Positional arg already declared: $label" }
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
            require(keys.all { it.isKeyArg() }) { "${getKeysLogTag(keys)} args keys must match key arg pattern" }
            require(keys.all { !optsKeyValueMap.containsKey(it) }) { "${getKeysLogTag(keys)} key already declared" }
            require(keys.none { it.isBuiltInCommand() }) { "${getKeysLogTag(keys)} key contains a builtin command" }
        } catch (e: Exception) {
            throw CmdArgsInitializeException(e)
        }
        optsKeyValueMap.putAll(keys.associate { it to null })
    }

    private fun <T> validateMapKeys(mapping: Map<String, T>) {
        try {
            require(mapping.isNotEmpty()) { "mapping must not be empty" }
            mapping.keys.forEach {
                require(!it.isKeyArg()) { "serializable arg $it must match not match the key arg pattern" }
            }
        } catch (e: CmdArgsSerializationException) {
            throw e
        } catch (e: Exception) {
            throw CmdArgsInitializeException(e)
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

        if (args.isEmpty() || args.firstOrNull()?.matches("help|--help".toRegex()) == true) {
            printHelp()
            // exitProcess(EXIT_CODE_SUCCESS)
            return Result.failure(CmdArgsRanBuiltInCommandException("--help"))
        }

        if (args.isEmpty() || args.firstOrNull()?.matches("version|--version".toRegex()) == true) {
            printVersion()
            // exitProcess(EXIT_CODE_SUCCESS)
            return Result.failure(CmdArgsRanBuiltInCommandException("--version"))
        }

        val quitArg = args.firstOrNull()?.matches("q|quit|exit|--quit|--exit".toRegex()) == true
        if (quitArg) {
            exitProcess(EXIT_CODE_SUCCESS)
            return Result.failure(CmdArgsRanBuiltInCommandException("quit"))
        }

        if (activeSubcommand != null) {
            return try {
                validateActiveSubCommand()
                Result.success(parsedArgs)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        return try {
            validateArgsListFormat()
            validateCmdArgValues()

            Result.success(parsedArgs)
        } catch (e: Exception) {
            val parseEx = CmdArgsParseException(e)
            printHelpAndExceptionAndExitProcess(e)
            Result.failure(parseEx)
        }
    }

    private fun printHelpAndExceptionAndExitProcess(e: Exception) {
        printHelp()
        e.printStackTrace()
        // exitProcess(EXIT_CODE_ERR_CMD_MISUSE)
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
        // Validate flags, optionals, and required args have valid arguments

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
                                optsKeyValueMap[arg] = args[i + 1]
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
                require(!args[i].isKeyArg() && args[i].isNotBlank()) { "Positional ${p.valueLabel} with value ${args[i]} does not match the expected format" }
                i++
            }
        } else if (positionalArgs.size > positionals.size) {
            throw IllegalArgumentException("Unexpected arg: ${positionalArgs.first()}")
        } else {
            val missingPos = positionals.subList(args.lastIndex, positionals.size)
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
        return isVerboseKey() || isSingleKeyArg() || isStackedFlagArg()
    }

    private fun String.isVerboseKey(): Boolean {
        return matches("^--[a-zA-Z]+[a-zA-Z-]*$".toRegex())
    }

    private fun String.isSingleKeyArg(): Boolean {
        return matches("^-[a-zA-Z]$".toRegex())
    }

    private fun String.isStackedFlagArg(): Boolean {
        return matches("^-[a-zA-Z][a-zA-Z]+$".toRegex())
    }

    private fun String.isBuiltInCommand(): Boolean {
        return matches("--help|--version|q|quit|exit".toRegex())
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