package me.archinamon.i18n

data class StringItem(
    val id: String,
    val key: String,
    val value: String,
    var problems: Problems = Problems()
) : Matcher<String> {

    override val matchKey: String
        get() = key
}