package me.archinamon.i18n

object I18nValidator {

    private val curlyTagPattern = Regex("\\{.*?}")
    private val htmlTagPattern = Regex("</?.*?>")
    private val newLinePattern = Regex("\\n")
    private val unicodePattern = Regex("(\\\\u)([A-F0-9]{4})")

    private val checks = mapOf(
        curlyTagPattern to Problems::badTags,
        htmlTagPattern to Problems::badHtml,
        newLinePattern to Problems::brokenControls,
        unicodePattern to Problems::badUnicode
    )

    fun process(input: List<Language>, baseline: Language) {
        input
            .asSequence()
            .filter { it.code != baseline.code } // do not check base lang against itself
            .forEach { lang ->
                lang against baseline
            }
    }

    private infix fun Language.against(baseline: Language) {
        if (this.items.size != baseline.items.size) {
            println("Baseline language and $code has different count of string keys!")
            println("Different keys are: ${this.items.diff(baseline.items).joinToString { it.key }}")
            return
        }

        baseline.items.forEachIndexed { index, item ->
            item runChecksAgainst this.items[index]
        }
    }

    private infix fun StringItem.runChecksAgainst(itemToCheck: StringItem) {
        val problems = itemToCheck.problems

        checks.forEach checker@{ (check, functor) ->
            val lvalueMatcher = StringMatcher(check, this.value)
            val rvalueMatcher = StringMatcher(check, itemToCheck.value)

            val lvalueHasArg = lvalueMatcher.hasArgs()
            val rvalueHasArg = rvalueMatcher.hasArgs()

            if (!lvalueHasArg xor !rvalueHasArg) {
                problems.argsNotMatch = true
                return@checker
            }

            if (!lvalueHasArg && !rvalueHasArg) {
                return@checker
            }

            val lvalueArgsCount = lvalueMatcher.getArgsCount()
            val rvalueArgsCount = rvalueMatcher.getArgsCount()

            if (lvalueArgsCount xor rvalueArgsCount != 0) {
                functor.set(problems, true)
                return@checker
            }

            (0 until lvalueArgsCount).forEach { idx ->
                val argsDefault = lvalueMatcher.getSelectedArg(idx)
                if (!rvalueMatcher.hasArgOccurrence(argsDefault)) {
                    functor.set(problems, true)
                    return@checker
                }
            }
        }
    }
}