/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package java.io

import dev.gitlive.firebase.stub

/** Header stub of the JDK's `java.io.InputStream` (see buildSrc utils/HeaderStubs.kt): stripped from the output, so the JDK's class is used. */
public actual abstract class InputStream actual constructor() : AutoCloseable {
    public actual abstract fun read(): Int
    public actual open fun read(b: ByteArray): Int = stub()
    public actual open fun read(b: ByteArray, off: Int, len: Int): Int = stub()
    public actual open fun skip(n: Long): Long = stub()
    public actual open fun available(): Int = stub()
    actual override fun close(): Unit = stub()
}
