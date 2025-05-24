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