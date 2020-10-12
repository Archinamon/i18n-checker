package me.archinamon.i18n.runner

import me.archinamon.i18n.EMPTY_STRING
import me.archinamon.i18n.Language

data class ParsingData(
    val header: String = EMPTY_STRING,
    val semicolons: Int = -1,
    val parsedLanguages: List<Language> = emptyList(),
    val parseErrors: MutableList<String> = mutableListOf()
)