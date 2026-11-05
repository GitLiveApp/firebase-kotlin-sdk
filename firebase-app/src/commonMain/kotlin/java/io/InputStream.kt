/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package java.io

/**
 * Mirrors the core of `java.io.InputStream`, so that the Firebase Android SDK members taking one
 * (`FirebaseFirestore.loadBundle(InputStream)`) keep their signatures and work in common code: a common subclass reads
 * its bytes from any source, and [ByteArrayInputStream] reads them from memory. On Android and the JVM the class is a
 * header stub that is stripped from the output (see buildSrc utils/HeaderStubs.kt), so code there uses the JDK's own
 * class and every one of its streams; elsewhere this class is the implementation, with the JDK's contracts.
 */
public expect abstract class InputStream() : AutoCloseable {
    /** The next byte as 0 to 255, or -1 at the end of the stream. */
    public abstract fun read(): Int

    /** Reads up to `b.size` bytes into [b], returning how many were read, or -1 at the end of the stream. */
    public open fun read(b: ByteArray): Int

    /** Reads up to [len] bytes into [b] from [off], returning how many were read, or -1 at the end of the stream. */
    public open fun read(b: ByteArray, off: Int, len: Int): Int

    /** Skips up to [n] bytes, returning how many were skipped. */
    public open fun skip(n: Long): Long

    /** An estimate of the bytes that can be read without blocking; 0 unless a subclass knows better. */
    public open fun available(): Int

    /** Releases the stream's resources; a plain stream has none. */
    override fun close()
}
