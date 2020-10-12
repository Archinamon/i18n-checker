package me.archinamon.i18n

data class Language(val code: String, val items: List<StringItem>) {
    constructor(entry: Map.Entry<String, List<StringItem>>) : this(entry.key, entry.value)
}