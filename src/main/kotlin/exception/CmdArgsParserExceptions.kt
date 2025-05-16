package lib.lib_args_parse.exception

class CmdArgsParserInitializationException(e: Exception) : IllegalArgumentException(e)
class CmdArgsMalformedException(e: Exception) : IllegalArgumentException(e)
class CmdArgsParseException(t: Throwable) : IllegalArgumentException(t)
class CmdArgsBuiltinCommandException(cmd: String) : Exception("Builtin command processed: $cmd")