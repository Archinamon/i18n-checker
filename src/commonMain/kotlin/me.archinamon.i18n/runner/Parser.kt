package me.archinamon.i18n.runner

import me.archinamon.i18n.I18nValidator
import me.archinamon.i18n.Language
import me.archinamon.i18n.Problems
import me.archinamon.i18n.StringItem
import me.archinamon.i18n.runner.io.File
import me.archinamon.i18n.runner.io.appendText
import me.archinamon.i18n.runner.io.nameWithoutExtension
import me.archinamon.i18n.runner.io.readText
import me.archinamon.i18n.runner.io.validate
import me.archinamon.i18n.runner.io.writeText
import me.archinamon.i18n.second
import me.archinamon.i18n.whenEmpty
import me.archinamon.i18n.whenNotEmpty

class Parser {

    private var state = ParsingData()

    fun parse(args: Array<String>) {
        val pairedArgs = args.asList().chunked(2)
        if (HELP in pairedArgs) {
            println(USAGE.trimIndent())
            return
        }

        val input = pairedArgs.extract(INPUT)
        if (input == null) {
            println(USAGE.trimIndent())
            return
        }

        val inputFile = File(input)
        if (!inputFile.exists()) {
            println("Input file ($input) not exists!")
            return
        }

        val output: String = pairedArgs.extract(OUTPUT, "./${inputFile.nameWithoutExtension}_validated.csv")!!
        val outputFile = File(output)
        if (!outputFile.exists()) {
            if (outputFile.isDirectory()) {
                throw IllegalArgumentException("Invalid path for output file: '$output' is a directory, not a file!")
            }
        }

        proceed(inputFile.also(File::validate), outputFile)
    }

    private fun proceed(inputFile: File, outputFile: File) {
        val languageCodes = linkedSetOf<String>()

        inputFile.readText()
            .split('\n')
            .filterNot(String::isNullOrBlank)
            .apply { first().parseHeader(languageCodes) }
            .drop(1) // skip header
            .flatMap { row -> row.parseRow(languageCodes) }
            .also {
                if (state.parseErrors.isNotEmpty())
                    state.parseErrors
                        .joinToString(separator = "\n", prefix = "Cannot parse these strings:\n")
                        .let(::println)
            }
            .groupBy(TransitiveLangItem::langCode, TransitiveLangItem::item)
            .map(::Language)
            .also { languages ->
                state = state.copy(parsedLanguages = languages)

                I18nValidator.process(
                    input = languages.dropWhile { it.code == "type" },
                    baseline = languages.first()
                )
            }
            .writeOutputReport(outputFile)
    }

    private fun String.parseHeader(languageCodes: MutableSet<String>) {
        if (
            this.split(';')
                .asSequence()
                .drop(2)
                .filter(String::isNotBlank)
                .map { it.trim('\n', ' ', '\r', '\t') }
                .map(languageCodes::add)
                .any { inserted -> !inserted }
        ) {
            val error = "Duplicate language found in CSV file! => " + languageCodes.joinToString()
            throw IllegalArgumentException(error)
        }

        state = state.copy(header = this, semicolons = this.countSemicolons())
    }

    private fun String.parseRow(languageCodes: MutableSet<String>): List<TransitiveLangItem> {
        if (state.semicolons != this.countSemicolons()) {
            if (Regex("([a-f0-9\\-]+);") in this) {
                state.parseErrors += this.split(';').second()
            }

            return emptyList()
        }

        val attributes = this.split(';')
        return attributes.drop(2)
            .filter(String::isNotBlank)
            .mapIndexed { index, translation ->
                val rowId = attributes.first()
                val rowKey = attributes.second()

                val code = kotlin.runCatching { languageCodes.elementAt(index) }
                    .getOrElse {
                        throw IllegalArgumentException(
                            "Problem with key [$rowKey] — too many semicolons!\n" +
                                "Current index is ${index + 1}, but we have only ${languageCodes.size} columns!"
                        )
                    }
                    .takeIf(String::isNotBlank)
                    ?: throw IllegalArgumentException(
                        "Language code have to be non-empty string! Codes: " + languageCodes.joinToString()
                    )

                val item = StringItem(
                    id = rowId,
                    key = rowKey,
                    value = translation
                )

                return@mapIndexed TransitiveLangItem(code, item)
            }
    }

    private fun List<Language>.writeOutputReport(outputFile: File) {
        this.filter { language ->
                language.items
                    .map(StringItem::problems)
                    .any(Problems::any)
            }
            .whenEmpty {
                println("No problems detected!")
                return
            }
            .whenNotEmpty {
                with(outputFile) {
                    if (!exists()) {
                        getParentFile().mkdirs()
                        createNewFile()
                    }

                    validate()
                    writeText(state.header.trim())
                    appendText(";problems\n")
                }
            }

        val rows = HashMap<String, MutableMap<String, StringItem>>()

        this.forEach { language ->
            language.items.forEach { str ->
                val strId = "${str.id};${str.key};"
                if (strId !in rows) rows[strId] = mutableMapOf()
                rows[strId]!![language.code] = str
            }
        }

        rows.entries.forEach { (strId, stringsContainer) ->
            val hasProblems = stringsContainer.values.any { it.problems.any() }
            val problems = stringsContainer.filter { (_, item) -> item.problems.any() }
                .entries.joinToString { (lang, item) -> "${item.problems} ($lang)" }

            val strValues = stringsContainer.entries
                .joinToString(separator = ";", prefix = strId) { it.value.value.trim() }

            outputFile.appendText(if (hasProblems) {
                "$strValues;$problems\n"
            } else {
                "$strValues\n"
            })
        }
    }

    private data class TransitiveLangItem(val langCode: String, val item: StringItem)

    private companion object CliParams {
        const val HELP = "h"
        const val INPUT = "i"
        const val OUTPUT = "o"

        const val USAGE = """
        Usage:
            -i : input file
            -o : output directory (optional)
            -h : this screen
    """

        fun List<List<String>>.extract(name: String, default: String? = null): String? {
            return find { arg ->
                '-' in arg.first() && arg.first().endsWith(name)
            }?.last() ?: default
        }

        operator fun List<List<String>>.contains(name: String): Boolean {
            return any { arg ->
                '-' in arg.first() && arg.first().endsWith(name)
            }
        }

        fun String.countSemicolons() = this.count { it == ';' }
    }
}
