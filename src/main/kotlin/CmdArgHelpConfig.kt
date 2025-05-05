package lib.lib_args_parse


data class CmdArgHelpConfig(
    var epilogue: String? = null,
    var prologue: String? = null
)

interface CmdArgHelpConfigHolder {
    val cmdArgHelpConfig: CmdArgHelpConfig
}