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

private const val FILL_NULL = "null"
private const val FIELD_PADDING_SIZE = 1

internal object GridLogger {

    fun log(tuples: List<List<Any?>>) {
        val (colWidths, rowHeights) = getWidthsAndHeights(tuples)
        printGrid(tuples, colWidths, rowHeights)
    }

    private fun getWidthsAndHeights(tuples: List<List<Any?>>): Pair<List<Int>, List<Int>> {
        val numColumns = getNumColumns(tuples)

        val colWidths = mutableListOf<Int>()
        val rowHeights = IntArray(tuples.size) { 1 }

        repeat(numColumns) { c ->
            var maxC = 0
            for (i in 0..tuples.lastIndex) {
                val tuple = tuples[i]

                val fieldValue = tuple.getOrElse(c) { "" }

                if (c > tuple.lastIndex) continue

                when (fieldValue) {
                    null -> {
                        maxC = maxOf(maxC, FILL_NULL.length)
                    }

                    is Collection<Any?> -> {
                        val listMax = if (fieldValue.isEmpty()) {
                            0
                        } else {
                            fieldValue.maxBy {
                                it.toString().length
                            }
                        }

                        maxC = maxOf(maxC, listMax!!.toString().length)
                        rowHeights[i] = maxOf(rowHeights[i], fieldValue.size)
                    }

                    else -> {
                        maxC = maxOf(maxC, fieldValue.toString().length)
                    }
                }
            }

            colWidths.add(maxC)
        }

        return colWidths to rowHeights.toList()
    }

    private fun getNumColumns(tuples: List<List<Any?>>): Int {
        return tuples.maxBy { it.size }.size
    }

    private fun printGrid(
        tuples: List<List<Any?>>,
        colWidths: List<Int>,
        rowHeights: List<Int>
    ) {
        val numColumns = colWidths.size
        repeat(tuples.size) { r ->

            val rowHeight = rowHeights[r]

            repeat(rowHeight) { currentRow ->
                repeat(numColumns) { c ->
                    val colWidth = colWidths[c]
                    val field = tuples[r].getOrElse(c) { "" }

                    printField(field, colWidth, currentRow)
                }
                print("\n")
            }
        }
    }

    private fun printField(
        field: Any?,
        colWidth: Int,
        currentRow: Int
    ) {
        val paddingString = buildString { repeat(FIELD_PADDING_SIZE) { append(" ") } }

        val field2: Any = when (field) {
            is List<*> -> {
                field.getOrElse(currentRow) { "" } ?: FILL_NULL
            }

            null -> {
                if (currentRow == 0) FILL_NULL else ""
            }

            else -> {
                if (currentRow == 0) field else ""
            }
        }

        print(
            "${
                field2.toString().padEnd(colWidth)
            }$paddingString"
        )
    }
}