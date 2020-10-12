package me.archinamon.i18n.runner.io

import ensureUnixCallResult
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import platform.osx._nc_is_dir_path
import platform.posix.FILENAME_MAX
import platform.posix.F_OK
import platform.posix.O_APPEND
import platform.posix.O_CREAT
import platform.posix.O_RDWR
import platform.posix.R_OK
import platform.posix.SEEK_END
import platform.posix.SEEK_SET
import platform.posix.S_IRWXG
import platform.posix.S_IRWXO
import platform.posix.S_IRWXU
import platform.posix.W_OK
import platform.posix.access
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.fwrite
import platform.posix.getcwd
import platform.posix.mkdir
import platform.posix.stat
import platform.posix.strlen

actual class File actual constructor(
    private val pathname: String
) {

    private val fileSaperator
        get() = if (Platform.osFamily == OsFamily.WINDOWS) "\\" else "/"

    private val modeRead = "r"
    private val modeAppend = "a"
    private val modeRewrite = "w"

    actual fun getParentFile(): File {
        return File(getAbsolutePath().substringBeforeLast(fileSaperator))
    }

    actual fun getName(): String {
        return if (fileSaperator in pathname) {
            pathname.split(fileSaperator).last()
        } else {
            pathname
        }
    }

    actual fun getAbsolutePath(): String {
        return if (!pathname.contains(fileSaperator)) {
            memScoped {
                getcwd(allocArray(FILENAME_MAX), FILENAME_MAX)
                    ?.toKString() + fileSaperator + pathname
            }
        } else pathname
    }

    actual fun lastModified(): Long = memScoped {
        val result = alloc<stat>()
        if (stat(pathname, result.ptr) != 0) {
            return 0L
        }

        result.st_mtimespec.tv_sec
    }

    actual fun mkdirs(): Boolean {
        if (!getParentFile().exists()) {
            getParentFile().mkdirs()
        }

        if (exists()) {
            return true
        }

        mkdir(pathname, (S_IRWXU or S_IRWXG or S_IRWXO).convert())
            .ensureUnixCallResult("mkdir") { ret -> ret == 0 }

        return true
    }

    actual fun isDirectory(): Boolean = memScoped {
        if (!exists()) {
            return false
        }

        return _nc_is_dir_path(pathname)
    }

    @ExperimentalUnsignedTypes
    actual fun createNewFile(): Boolean {
        if (exists()) {
            return true
        }

        fopen(pathname, modeRewrite). let { fd ->
            fclose(fd).ensureUnixCallResult("fclose") { ret -> ret == 0 }
        }

        return exists()
    }

    actual fun exists(): Boolean {
        return access(pathname, F_OK) != -1
    }

    actual fun canRead(): Boolean {
        return access(getAbsolutePath(), R_OK) != -1
    }

    actual fun canWrite(): Boolean {
        return access(getAbsolutePath(), W_OK) != -1
    }

    @ExperimentalUnsignedTypes
    internal fun readBytes(): ByteArray {
        val fd = fopen(getAbsolutePath(), modeRead)
        try {
            memScoped {
                fseek(fd, 0, SEEK_END)
                val size = ftell(fd).convert<Int>()
                fseek(fd, 0, SEEK_SET)

                return ByteArray(size + 1).also { buffer ->
                    fread(buffer.refTo(0), 1UL, size.convert(), fd)
                        .ensureUnixCallResult("fread") { ret -> ret > 0U }
                }
            }
        } finally {
            fclose(fd).ensureUnixCallResult("fclose") { ret -> ret == 0 }
        }
    }

    @ExperimentalUnsignedTypes
    internal fun writeBytes(bytes: ByteArray, mode: Int, size: ULong = ULong.MAX_VALUE, elemSize: ULong = 1U) {
        val fd = fopen(getAbsolutePath(), if (mode and O_APPEND == O_APPEND) modeAppend else modeRewrite)
        try {
            memScoped {
                bytes.usePinned { pinnedBytes ->
                    val bytesSize: ULong = if (size != ULong.MAX_VALUE) size else pinnedBytes.get().size.convert()
                    fwrite(pinnedBytes.addressOf(0), elemSize, bytesSize, fd)
                        .ensureUnixCallResult("fwrite") { ret -> ret == bytesSize }
                }
            }
        } finally {
            fclose(fd).ensureUnixCallResult("fclose") { ret -> ret == 0 }
        }
    }

    override fun toString(): String {
        return "File {\n" +
            "path=${getAbsolutePath()}\n" +
            "name=${getName()}\n" +
            "exists=${exists()}\n" +
            "canRead=${canRead()}\n" +
            "canWrite=${canWrite()}\n" +
            "isDirectory=${isDirectory()}\n" +
            "lastModified=${lastModified()}\n" +
            "}"
    }
}

@ExperimentalUnsignedTypes
actual fun File.readText(): String {
    return readBytes().toKString()
}

@ExperimentalUnsignedTypes
actual fun File.appendText(text: String) {
    writeBytes(text.encodeToByteArray(), O_RDWR or O_APPEND, strlen(text))
}

@ExperimentalUnsignedTypes
actual fun File.writeText(text: String) {
    writeBytes(text.encodeToByteArray(), O_RDWR or O_CREAT, strlen(text))
}