package ee.androbus.core.gtfs

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path

data class CsvTable(
    val headers: List<String>,
    val rows: List<CsvRecord>,
)

data class CsvRecord(
    private val values: Map<String, String>,
) {
    fun required(column: String): String {
        val value = values[column]
            ?: throw GtfsParseException("Missing required column '$column'.")
        if (value.isBlank()) {
            throw GtfsParseException("Blank value for required column '$column'.")
        }
        return value
    }

    fun optional(column: String): String? = values[column]?.takeIf { it.isNotBlank() }
}

class CsvTableReader {
    fun read(path: Path): CsvTable = read(Files.readString(path, UTF_8))

    fun read(content: String): CsvTable {
        val rows = parseRows(content)
        if (rows.isEmpty()) {
            throw GtfsParseException("CSV content is empty.")
        }

        val headers = rows.first().mapIndexed { index, value ->
            if (index == 0) {
                value.removePrefix("\uFEFF").trim()
            } else {
                value.trim()
            }
        }

        if (headers.isEmpty() || headers.any { it.isBlank() }) {
            throw GtfsParseException("CSV header row is empty or contains blank header names.")
        }

        val duplicateHeaders = headers
            .groupingBy { it }
            .eachCount()
            .filterValues { it > 1 }
            .keys
        if (duplicateHeaders.isNotEmpty()) {
            throw GtfsParseException("CSV header contains duplicate column names: ${duplicateHeaders.joinToString(", ")}")
        }

        val dataRows = rows
            .drop(1)
            .filterNot { row -> row.all { it.isBlank() } }

        val records = dataRows.mapIndexed { index, row ->
            if (row.size != headers.size) {
                throw GtfsParseException(
                    "CSV row ${index + 2} has ${row.size} field(s); expected ${headers.size}.",
                )
            }
            CsvRecord(headers.zip(row).toMap())
        }

        return CsvTable(headers = headers, rows = records)
    }

    private fun parseRows(content: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val currentRow = mutableListOf<String>()
        val currentField = StringBuilder()

        var index = 0
        var inQuotes = false

        fun flushField() {
            currentRow += currentField.toString()
            currentField.clear()
        }

        fun flushRow() {
            rows += currentRow.toList()
            currentRow.clear()
        }

        while (index < content.length) {
            val char = content[index]
            if (inQuotes) {
                when (char) {
                    '"' -> {
                        val nextIndex = index + 1
                        if (nextIndex < content.length && content[nextIndex] == '"') {
                            currentField.append('"')
                            index = nextIndex
                        } else {
                            inQuotes = false
                        }
                    }
                    else -> currentField.append(char)
                }
            } else {
                when (char) {
                    '"' -> {
                        if (currentField.isNotEmpty()) {
                            currentField.append(char)
                        } else {
                            inQuotes = true
                        }
                    }
                    ',' -> flushField()
                    '\n' -> {
                        flushField()
                        flushRow()
                    }
                    '\r' -> {
                        flushField()
                        flushRow()
                        if (index + 1 < content.length && content[index + 1] == '\n') {
                            index++
                        }
                    }
                    else -> currentField.append(char)
                }
            }
            index++
        }

        if (inQuotes) {
            throw GtfsParseException("CSV contains an unterminated quoted field.")
        }

        flushField()
        if (currentRow.any { it.isNotBlank() } || rows.isEmpty()) {
            flushRow()
        }

        return rows
    }
}
