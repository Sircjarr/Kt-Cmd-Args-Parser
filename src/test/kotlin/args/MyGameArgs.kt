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

package args

import com.github.sircjarr.cmdargsparser.CmdArgsParser
import java.io.File

class MyGameArgs(parser: CmdArgsParser) {
    val seed: String? by parser.optionalArg(
        "-s", "--seed",
        valueLabel = "SEED",
        help = "Seed for the game instance. Uses random seed if not set.",
    )

    val numLives: Int by parser.optionalArg(
        "-l", "--num-lives",
        valueLabel = "COUNT",
        help = "Set count of player lives",
        default = 3
    ) { argString ->
        argString.toInt().also {
            require(it > 0) { "Lives must be > 0" }
        }
    }

    val cheatsEnabled: Boolean by parser.flagArg(
        "-c", "--cheats-enabled",
        help = "Enable use of cheat codes"
    )

    val mode: Mode by parser.requiredMapArg(
        "-m", "--mode",
        valueLabel = "MODE",
        help = "Set game mode difficulty",
        map = mapOf(
            "easy" to Mode.EASY,
            "medium" to Mode.MEDIUM,
            "hard" to Mode.HARD
        )
    )

    val playerSpeed: Double by parser.positionalArg(
        valueLabel = "SPEED",
        help = "Player speed"
    ) { argString ->
        argString.toDouble().also {
            require(it >= 0.0)
        }
    }

    val saveFile: File by parser.positionalArg(
        valueLabel = "FILE",
        help = "Save file location"
    ) { File(it) }

    enum class Mode { EASY, MEDIUM, HARD }
}