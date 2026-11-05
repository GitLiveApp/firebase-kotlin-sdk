/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package java.io

import dev.gitlive.firebase.stub

/** Header stub of the JDK's `java.io.ByteArrayInputStream` (see buildSrc utils/HeaderStubs.kt): stripped from the output, so the JDK's class is used. */
public actual open class ByteArrayInputStream : InputStream {
    public actual constructor(buf: ByteArray)
    public actual constructor(buf: ByteArray, offset: Int, length: Int)
    actual override fun read(): Int = stub()
    actual override fun read(b: ByteArray, off: Int, len: Int): Int = stub()
    actual override fun skip(n: Long): Long = stub()
    actual override fun available(): Int = stub()
    actual override fun close(): Unit = stub()
}
