/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package java.io

public actual open class ByteArrayInputStream actual constructor(buf: ByteArray, offset: Int, length: Int) : InputStream() {
    private val buf = buf
    private var pos = offset
    private val count = minOf(offset + length, buf.size)

    public actual constructor(buf: ByteArray) : this(buf, 0, buf.size)

    actual override fun read(): Int = if (pos < count) buf[pos++].toInt() and 0xff else -1

    actual override fun read(b: ByteArray, off: Int, len: Int): Int {
        checkBounds(b, off, len)
        if (pos >= count) return -1
        val n = minOf(len, count - pos)
        if (n <= 0) return 0
        buf.copyInto(b, off, pos, pos + n)
        pos += n
        return n
    }

    actual override fun skip(n: Long): Long {
        val k = minOf(n, (count - pos).toLong()).coerceAtLeast(0)
        pos += k.toInt()
        return k
    }

    actual override fun available(): Int = count - pos

    actual override fun close() {}
}
