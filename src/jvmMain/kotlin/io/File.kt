package me.archinamon.i18n.runner.io

import java.nio.charset.Charset

actual typealias File = java.io.File

actual fun File.readText() = readText(Charset.defaultCharset())

actual fun File.appendText(text: String) = appendText(text, Charset.defaultCharset())

actual fun File.writeText(text: String) = writeText(text, Charset.defaultCharset())