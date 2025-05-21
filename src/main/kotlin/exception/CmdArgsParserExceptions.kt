package com.github.sircjarr.cmdargsparser.exception

/**
 * Runtime [Exception] indicating there was an issue with adding the argument to the
 * [com.github.sircjarr.cmdargsparser.CmdArgsParser].
 */
class CmdArgsParserInitializationException(e: Exception) : IllegalArgumentException(e)

/**
 * Indicates `args` is malformed in some way. For example: unrecognized key, missing arg
 * value, or too many positionals declared.
 */
class CmdArgsMalformedException(e: Exception) : IllegalArgumentException(e)

/**
 * When there is an issue with parsing on of the args from `args`. For example: required
 * arg not found, casting failure, or some other error thrown from the `initializer` param.
 */
class CmdArgsParseException(t: Throwable) : IllegalArgumentException(t)

/**
 * Indicates a builtin command has been intercepted and ran.
 */
class CmdArgsBuiltinCommandException(cmd: String) : Exception("Builtin command processed: $cmd")