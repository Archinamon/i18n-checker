package me.archinamon.i18n

const val EMPTY_STRING = ""

interface Matcher<T> {
    val matchKey: T
}

fun <T : Matcher<*>> List<T>.diff(other: List<T>): List<T> {
    return ArrayList<T>().apply {
        this@diff.filter { other.none { oit -> oit.matchKey == it.matchKey } }.let(::addAll)
        other.filter { this@diff.none { dit -> dit.matchKey == it.matchKey } }.let(::addAll)
    }
}

inline fun <T> List<T>.whenEmpty(block: List<T>.() -> Unit): List<T> {
    if (isEmpty()) block()
    return this
}

inline fun <T> List<T>.whenNotEmpty(block: List<T>.() -> Unit): List<T> {
    if (isNotEmpty()) block()
    return this
}

fun <T> List<T>.second(): T {
    if (isEmpty())
        throw NoSuchElementException("List is empty.")
    return this[1]
}

fun <T> List<T>.third(): T {
    if (isEmpty())
        throw NoSuchElementException("List is empty.")
    return this[2]
}

fun <T> List<T>.fourth(): T {
    if (isEmpty())
        throw NoSuchElementException("List is empty.")
    return this[3]
}

fun <T> List<T>.fifth(): T {
    if (isEmpty())
        throw NoSuchElementException("List is empty.")
    return this[4]
}

fun <T> List<T>.sixth(): T {
    if (isEmpty())
        throw NoSuchElementException("List is empty.")
    return this[5]
}

fun <T> List<T>.seventh(): T {
    if (isEmpty())
        throw NoSuchElementException("List is empty.")
    return this[6]
}

fun <T> List<T>.eighth(): T {
    if (isEmpty())
        throw NoSuchElementException("List is empty.")
    return this[7]
}

fun <T> List<T>.ninth(): T {
    if (isEmpty())
        throw NoSuchElementException("List is empty.")
    return this[8]
}