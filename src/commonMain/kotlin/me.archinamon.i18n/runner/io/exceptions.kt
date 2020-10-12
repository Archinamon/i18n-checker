package me.archinamon.i18n.runner.io

class FileNotFoundException(fileName: String, reason: String) : Exception("FileNotFoundException: $fileName ($reason)")

class IllegalFileAccess(fileName: String, reason: String) : Exception("Access denied: $fileName ($reason)")