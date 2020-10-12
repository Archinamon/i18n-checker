package me.archinamon.i18n

data class Problems(
    var argsNotMatch: Boolean = false,
    var badTags: Boolean = false,
    var badHtml: Boolean = false,
    var brokenControls: Boolean = false,
    var badUnicode: Boolean = false
) {
    fun any() = argsNotMatch or badTags or badHtml or brokenControls or badUnicode

    fun none() = !any()

    override fun toString(): String {
        return when {
            argsNotMatch -> "tags appearance not matched with base lang"
            badTags -> "curly tags broken"
            badHtml -> "html tags broken"
            brokenControls -> "bad '\\n' sequencing"
            badUnicode -> "unicode sequencing broken"

            else -> "no problems"
        }
    }
}