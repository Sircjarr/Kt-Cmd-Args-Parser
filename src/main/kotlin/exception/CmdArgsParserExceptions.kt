package lib.lib_args_parse.exception

class CmdArgsParserInitializationException(e: Exception) : IllegalArgumentException(e)
class CmdArgsMalformedException(e: Exception) : IllegalArgumentException(e)
class CmdArgsParseException(e: Exception) : IllegalArgumentException(e)
class CmdArgsBuiltinCommandException(cmd: String) : Exception("Builtin command processed: $cmd")