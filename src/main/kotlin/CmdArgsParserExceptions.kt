package lib.lib_args_parse

class CmdArgsParseException(e: Exception) : Exception(e.message, e)
class CmdArgsInitializeException(e: Exception) : Exception(e.message, e)
class CmdArgsSerializationException(cmd: String) : Exception("Failed to serialize cmd: $cmd")
class CmdArgsRanBuiltInCommandException(cmd: String) : Exception("Builtin command executed: $cmd")