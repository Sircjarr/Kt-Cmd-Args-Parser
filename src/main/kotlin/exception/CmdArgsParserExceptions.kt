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