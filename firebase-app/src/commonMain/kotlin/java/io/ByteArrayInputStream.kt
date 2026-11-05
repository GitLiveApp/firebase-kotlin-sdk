/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package java.io

/**
 * Mirrors `java.io.ByteArrayInputStream`: an [InputStream] over a byte array, for handing bytes already in memory to a
 * member taking a stream. On Android and the JVM the class is a header stub for the JDK's (see [InputStream]).
 */
public expect open class ByteArrayInputStream : InputStream {
    /** A stream over all of [buf]. */
    public constructor(buf: ByteArray)

    /** A stream over [length] bytes of [buf] from [offset]. */
    public constructor(buf: ByteArray, offset: Int, length: Int)

    override fun read(): Int

    override fun read(b: ByteArray, off: Int, len: Int): Int

    override fun skip(n: Long): Long

    override fun available(): Int

    override fun close()
}
