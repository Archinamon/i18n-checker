package me.archinamon.i18n

class StringMatcher(private val regex: Regex, private val input: String) {

    private val found by lazy {
        regex.findAll(input)
    }

    fun hasArgs() = regex.containsMatchIn(input)

    fun getArgsCount() = found.count()

    fun hasArgOccurrence(arg: String): Boolean {
        return (0 until getArgsCount())
            .map(::getSelectedArg)
            .any { nextArg -> nextArg == arg }
    }

    fun getSelectedArg(index: Int): String {
        val listFoundValues = found.toList()

        if (index >= listFoundValues.size) {
            throw IllegalStateException("Arg number $index not exists in string: $input")
        }

        return listFoundValues[index].value
    }
}