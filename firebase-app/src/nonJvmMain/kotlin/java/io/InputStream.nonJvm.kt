/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package java.io

/** The JDK's `InputStream` contracts: the array and skip forms read byte by byte unless a subclass does better. */
public actual abstract class InputStream actual constructor() : AutoCloseable {
    public actual abstract fun read(): Int

    public actual open fun read(b: ByteArray): Int = read(b, 0, b.size)

    public actual open fun read(b: ByteArray, off: Int, len: Int): Int {
        checkBounds(b, off, len)
        if (len == 0) return 0
        val first = read()
        if (first == -1) return -1
        b[off] = first.toByte()
        var count = 1
        while (count < len) {
            val next = read()
            if (next == -1) break
            b[off + count] = next.toByte()
            count++
        }
        return count
    }

    public actual open fun skip(n: Long): Long {
        var remaining = n
        if (remaining <= 0) return 0
        val buffer = ByteArray(minOf(remaining, SKIP_BUFFER_SIZE.toLong()).toInt())
        while (remaining > 0) {
            val count = read(buffer, 0, minOf(remaining, buffer.size.toLong()).toInt())
            if (count < 0) break
            remaining -= count
        }
        return n - remaining
    }

    public actual open fun available(): Int = 0

    actual override fun close() {}

    private companion object {
        const val SKIP_BUFFER_SIZE = 2048
    }
}

/** Throws the JDK's [IndexOutOfBoundsException] when [off] and [len] do not fit [b]. */
internal fun checkBounds(b: ByteArray, off: Int, len: Int) {
    if (off < 0 || len < 0 || len > b.size - off) throw IndexOutOfBoundsException("Range $off..${off + len} is out of bounds for an array of size ${b.size}")
}
