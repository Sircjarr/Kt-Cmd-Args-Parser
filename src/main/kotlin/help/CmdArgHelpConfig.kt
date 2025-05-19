package com.github.sircjarr.cmdargsparser.help

data class CmdArgHelpConfig(
    var epilogue: String? = null,
    var prologue: String? = null
)

interface CmdArgHelpConfigHolder {
    val cmdArgHelpConfig: CmdArgHelpConfig
}