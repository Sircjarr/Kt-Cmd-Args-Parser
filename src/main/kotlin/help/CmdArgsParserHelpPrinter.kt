package lib.lib_args_parse.help

import lib.lib_args_parse.CmdArgNonNull
import lib.lib_args_parse.CmdArgNullable
import lib.lib_args_parse.Subcommand

private const val CHAR_TABLE_SEPARATOR = ":"

internal object CmdArgsParserHelpPrinter {

    fun print(
        programName: String,
        epilogue: String? = null,
        prologue: String? = null,
        opts: List<CmdArgNullable.KeyValue<Any?>>,
        optDefaults: List<CmdArgNonNull.KeyValue<Any>>,
        required: List<CmdArgNonNull.KeyValue<Any>>,
        flags: List<CmdArgNonNull.Flag>,
        positionals: List<CmdArgNonNull.Positional<Any>>,
        subcommands: MutableMap<String, Subcommand<Any?>>
    ) {
        printUsage(programName, opts, optDefaults, required, flags, positionals, subcommands)

        prologue?.let {
            println(it)
            println()
        }

        val tuples = mutableListOf<List<Any>>()
        buildRequiredTuples(required, tuples)
        buildPositionalTuples(positionals, tuples)
        buildOptionalTuples(opts, optDefaults, tuples)
        buildFlagTuples(flags, tuples)
        buildSubcommandTuples(subcommands, tuples)

        GridLogger.log(tuples)

        epilogue?.let {
            println(it)
        }
    }

    private fun buildSubcommandTuples(subcommands: MutableMap<String, Subcommand<Any?>>, tuples: MutableList<List<Any>>) {
        if (subcommands.isNotEmpty()) {
            tuples.add(listOf("Subcommands:"))
            subcommands.forEach { k, v ->
                tuples.add(listOf(k))
            }
        }
    }

    private fun printUsage(
        programName: String,
        opts: List<CmdArgNullable.KeyValue<Any?>>,
        optDefaults: List<CmdArgNonNull.KeyValue<Any>>,
        required: List<CmdArgNonNull.KeyValue<Any>>,
        flags: List<CmdArgNonNull.Flag>,
        positionals: List<CmdArgNonNull.Positional<Any>>,
        subcommands: MutableMap<String, Subcommand<Any?>>
    ) {
        val flagString = flags.joinToString("") { "[${it.keys.first()}] " }
        val optString = opts.joinToString("") {
            "[${it.keys.first()}=${it.valueLabel}] "
        }
        val optDefaultsString = optDefaults.joinToString("") {
            "[${it.keys.first()}=${it.valueLabel}] "
        }
        val reqString = required.joinToString("") { "${it.keys.first()}=${it.valueLabel} " }
        val posString = positionals.joinToString("") { "${it.valueLabel} " }

        val usageString = buildString {
            append("Usage: $programName\n")
            if (reqString.isNotEmpty()) {
                append("$reqString\n")
            }
            if (optString.isNotEmpty() || optDefaultsString.isNotEmpty()) {
                if (optString.isNotEmpty()) {
                    append(optString)
                }
                if (optDefaultsString.isNotEmpty()) {
                    append(optDefaultsString)
                }
                append("\n")
            }
            if (flagString.isNotEmpty()) {
                append("$flagString\n")
            }
            if (posString.isNotEmpty()) {
                append(posString)
            }
            if (subcommands.isNotEmpty()) {
                append("SUBCOMMAND [ARGS]")
            }
        }
        println("$usageString\n")
    }

    private fun buildRequiredTuples(required: List<CmdArgNonNull.KeyValue<Any>>, tuples: MutableList<List<Any>>) {
        if (required.isNotEmpty()) {
            tuples.add(listOf("Required args:"))
            for (r in required) {
                val tuple = buildList {
                    add(r.keys.joinToString(", ") { "$it ${r.valueLabel}" })
                    add(CHAR_TABLE_SEPARATOR)
                    add(r.hint)
                    if (r.default != null) {
                        add(defaultValueString(r.default))
                    }
                }

                tuples.add(tuple)

                val mapping = when (r) {
                    is CmdArgNonNull.KeyValue.Mapped -> r.map
                    else -> null
                }

                if (mapping?.entries?.isNotEmpty() == true) {
                    val choices = mapping.entries.joinToString(",") { it.key }
                    tuples.add(listOf("\t${r.valueLabel}={$choices}"))
                }
            }
            tuples.add(emptyList<Unit>())
        }
    }

    private fun buildPositionalTuples(
        positionals: List<CmdArgNonNull.Positional<Any>>,
        tuples: MutableList<List<Any>>
    ) {
        if (positionals.isNotEmpty()) {
            tuples.add(listOf("Positional args:"))
            for (p in positionals) {
                val label = p.valueLabel
                val hint = p.hint
                tuples.add(listOf(label, CHAR_TABLE_SEPARATOR, hint))
            }
            tuples.add(emptyList<Unit>())
        }
    }

    private fun buildOptionalTuples(
        opts: List<CmdArgNullable.KeyValue<Any?>>,
        optDefaults: List<CmdArgNonNull.KeyValue<Any>>,
        tuples: MutableList<List<Any>>
    ) {
        if (opts.isNotEmpty() || optDefaults.isNotEmpty()) {
            tuples.add(listOf("Optional args:"))
            for (opt in opts) {
                val tuple = buildList {
                    add(opt.keys.joinToString(", ") { "$it ${opt.valueLabel}" })
                    add(CHAR_TABLE_SEPARATOR)
                    add(opt.hint)
                }

                tuples.add(tuple)

                val mapping = when (opt) {
                    is CmdArgNullable.KeyValue.Mapped -> opt.map
                    else -> null
                }

                if (mapping?.entries?.isNotEmpty() == true) {
                    val choices = mapping.entries.joinToString(",") { it.key }
                    tuples.add(listOf("\t${opt.valueLabel}={$choices}"))
                }
            }

            for (opt in optDefaults) {
                val tuple = buildList {
                    add(opt.keys.joinToString(", ") { "$it ${opt.valueLabel}" })
                    add(CHAR_TABLE_SEPARATOR)
                    add(opt.hint)

                    val default = when (opt) {
                        is CmdArgNonNull.KeyValue.Single -> opt.default
                        is CmdArgNonNull.KeyValue.Mapped -> {
                            opt.default?.let {
                                findKeyByValue(opt.map, it)
                            }
                        }
                    }

                    if (default != null) {
                        add(defaultValueString(default))
                    }
                }

                tuples.add(tuple)

                val mapping = when (opt) {
                    is CmdArgNonNull.KeyValue.Mapped -> opt.map
                    else -> null
                }

                if (mapping?.entries?.isNotEmpty() == true) {
                    val choices = mapping.entries.joinToString(",") { it.key }
                    tuples.add(listOf("\t${opt.valueLabel}={$choices}"))
                }
            }
            tuples.add(emptyList<Unit>())
        }
    }

    private fun findKeyByValue(map: Map<String, Any>, value: Any): String {
        for ((k, v) in map) {
            if (v == value) {
                return k
            }
        }

        throw IllegalStateException("Failed to resolve key from value: $value")
    }

    private fun buildFlagTuples(
        flags: List<CmdArgNonNull.Flag>,
        tuples: MutableList<List<Any>>
    ) {
        if (flags.isNotEmpty()) {
            tuples.add(listOf("Flag args:"))
            for (flag in flags) {
                val keys = flag.keys.joinToString(", ") { it }
                val hint = flag.hint
                tuples.add(listOf(keys, CHAR_TABLE_SEPARATOR, hint, defaultValueString(flag.default)))
            }
            tuples.add(emptyList<List<Unit>>())
        }
    }

    private fun defaultValueString(value: Any): String {
        return "Default - $value"
    }
}