// Kt-Cmd-Args-Parser: A library for parsing command-line arguments.
// Copyright (C) 2025 Cliff Jarrell
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.

package com.github.sircjarr.cmdargsparser.help

import com.github.sircjarr.cmdargsparser.model.CmdArgNonNull
import com.github.sircjarr.cmdargsparser.model.CmdArgNullable
import com.github.sircjarr.cmdargsparser.model.Subcommand

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
        if (subcommands.isEmpty()) return

        tuples.add(listOf("Subcommands:"))
        subcommands.forEach { k, v ->
            tuples.add(listOf(k,  ":", v.help))
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
                append("[--] $posString")
            }
            if (subcommands.isNotEmpty()) {
                append("SUBCOMMAND [ARGS]")
            }
        }
        println("$usageString\n")
    }

    private fun buildRequiredTuples(required: List<CmdArgNonNull.KeyValue<Any>>, tuples: MutableList<List<Any>>) {
        if (required.isEmpty()) return

        tuples.add(listOf("Required args:"))
        for (r in required) {
            val tuple = buildList {
                add(r.keys.joinToString(", ") { "$it ${r.valueLabel}" })
                add(CHAR_TABLE_SEPARATOR)
                add(defaultValueString(r.help, r.default))
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

    private fun buildPositionalTuples(
        positionals: List<CmdArgNonNull.Positional<Any>>,
        tuples: MutableList<List<Any>>
    ) {
        if (positionals.isEmpty()) return

        tuples.add(listOf("Positional args:"))
        for (p in positionals) {
            val label = p.valueLabel
            val hint = p.help
            tuples.add(listOf(label, CHAR_TABLE_SEPARATOR, hint))
        }
        tuples.add(emptyList<Unit>())
    }

    private fun buildOptionalTuples(
        opts: List<CmdArgNullable.KeyValue<Any?>>,
        optDefaults: List<CmdArgNonNull.KeyValue<Any>>,
        tuples: MutableList<List<Any>>
    ) {
        if (opts.isEmpty() && optDefaults.isEmpty()) return

        tuples.add(listOf("Optional args:"))
        for (opt in opts) {
            val tuple = buildList {
                add(opt.keys.joinToString(", ") { "$it ${opt.valueLabel}" })
                add(CHAR_TABLE_SEPARATOR)
                add(opt.help)
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

                val default = when (opt) {
                    is CmdArgNonNull.KeyValue.Single -> opt.default
                    is CmdArgNonNull.KeyValue.Mapped -> {
                        opt.default?.let {
                            findKeyByValue(opt.map, it)
                        }
                    }
                }

                add(defaultValueString(opt.help, default))
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
        if (flags.isEmpty()) return

        tuples.add(listOf("Flag args:"))
        for (flag in flags) {
            val keys = flag.keys.joinToString(", ") { it }
            val hint = flag.help
            tuples.add(listOf(keys, CHAR_TABLE_SEPARATOR, defaultValueString(hint, flag.default)))
        }
        tuples.add(emptyList<List<Unit>>())
    }

    private fun defaultValueString(help: String, defaultValue: Any?): String {
        return if (defaultValue != null) {
            "$help (Default $defaultValue)"
        } else help
    }
}