package com.github.sircjarr.cmdargsparser.help

/**
 * Holds the [epilogue] and [prologue] referenced by the output of the `--help` builtin command.
 *
 * @param epilogue Epilogue of the `--help` output.
 * @param prologue Prologue of the `--help` output.
 */
data class CmdArgHelpConfig(
    var epilogue: String? = null,
    var prologue: String? = null
)

/**
 * Extended by custom arg classes to provide additional info in the `--help` command output.
 */
interface CmdArgHelpConfigHolder {
    val cmdArgHelpConfig: CmdArgHelpConfig
}