package dev.gitlive.firebase.storage

import com.google.firebase.storage.network.NetworkRequest
import com.google.firebase.storage.network.connection.HttpURLConnectionFactory
import java.io.InputStream
import java.io.OutputStream
import java.net.Authenticator
import java.net.HttpURLConnection
import java.net.URL
import java.security.Permission
import java.util.TreeMap

/**
 * Lets the Android Storage SDK talk to the Storage emulator from the JVM.
 *
 * The emulator sends its upload headers in lower case (`x-goog-upload-url`), the SDK looks them up by their canonical
 * name (`X-Goog-Upload-URL`) in the map [HttpURLConnection.getHeaderFields] returns, and on the JDK that map is
 * case-sensitive where Android's (OkHttp's) is not. Uploads then fail with "Unable to obtain an upload URL". The tests
 * install a connection factory whose connections present the response headers in a case-insensitive map instead.
 */
internal object CaseInsensitiveResponseHeaders {
    private val connectionFactory = NetworkRequest::class.java.getDeclaredField("connectionFactory").apply { isAccessible = true }

    fun install() {
        val current = connectionFactory.get(null) as HttpURLConnectionFactory
        if (current !is Factory) connectionFactory.set(null, Factory(current))
    }

    private class Factory(private val delegate: HttpURLConnectionFactory) : HttpURLConnectionFactory {
        override fun createInstance(url: URL): HttpURLConnection = Connection(delegate.createInstance(url) ?: error("No connection for $url"))
    }

    private class Connection(private val delegate: HttpURLConnection) : HttpURLConnection(delegate.url) {
        override fun getHeaderFields(): Map<String, List<String>> = TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER).apply {
            // The status line is keyed by null, which an ordered map cannot hold; the SDK never asks for it.
            delegate.headerFields.forEach { (key, value) -> if (key != null) put(key, value) }
        }

        override fun connect() = delegate.connect()
        override fun disconnect() = delegate.disconnect()
        override fun usingProxy(): Boolean = delegate.usingProxy()
        override fun getRequestMethod(): String = delegate.requestMethod
        override fun setRequestMethod(method: String) = delegate.setRequestMethod(method)
        override fun getResponseCode(): Int = delegate.responseCode
        override fun getResponseMessage(): String? = delegate.responseMessage
        override fun getErrorStream(): InputStream? = delegate.errorStream
        override fun getInputStream(): InputStream = delegate.inputStream
        override fun getOutputStream(): OutputStream = delegate.outputStream
        override fun getHeaderField(name: String?): String? = delegate.getHeaderField(name)
        override fun getHeaderField(n: Int): String? = delegate.getHeaderField(n)
        override fun getHeaderFieldKey(n: Int): String? = delegate.getHeaderFieldKey(n)
        override fun getHeaderFieldInt(name: String?, default: Int): Int = delegate.getHeaderFieldInt(name, default)
        override fun getHeaderFieldLong(name: String?, default: Long): Long = delegate.getHeaderFieldLong(name, default)
        override fun getHeaderFieldDate(name: String?, default: Long): Long = delegate.getHeaderFieldDate(name, default)
        override fun getContentLength(): Int = delegate.contentLength
        override fun getContentLengthLong(): Long = delegate.contentLengthLong
        override fun getContentType(): String? = delegate.contentType
        override fun getContentEncoding(): String? = delegate.contentEncoding
        override fun getContent(): Any? = delegate.content
        override fun getContent(classes: Array<out Class<*>>?): Any? = delegate.getContent(classes)
        override fun getDate(): Long = delegate.date
        override fun getExpiration(): Long = delegate.expiration
        override fun getLastModified(): Long = delegate.lastModified
        override fun getPermission(): Permission? = delegate.permission
        override fun getURL(): URL = delegate.url
        override fun getInstanceFollowRedirects(): Boolean = delegate.instanceFollowRedirects
        override fun setInstanceFollowRedirects(followRedirects: Boolean) = delegate.setInstanceFollowRedirects(followRedirects)
        override fun setChunkedStreamingMode(chunklen: Int) = delegate.setChunkedStreamingMode(chunklen)
        override fun setFixedLengthStreamingMode(contentLength: Int) = delegate.setFixedLengthStreamingMode(contentLength)
        override fun setFixedLengthStreamingMode(contentLength: Long) = delegate.setFixedLengthStreamingMode(contentLength)
        override fun setAuthenticator(auth: Authenticator?) = delegate.setAuthenticator(auth)
        override fun getConnectTimeout(): Int = delegate.connectTimeout
        override fun setConnectTimeout(timeout: Int) = delegate.setConnectTimeout(timeout)
        override fun getReadTimeout(): Int = delegate.readTimeout
        override fun setReadTimeout(timeout: Int) = delegate.setReadTimeout(timeout)
        override fun getDoInput(): Boolean = delegate.doInput
        override fun setDoInput(doInput: Boolean) = delegate.setDoInput(doInput)
        override fun getDoOutput(): Boolean = delegate.doOutput
        override fun setDoOutput(doOutput: Boolean) = delegate.setDoOutput(doOutput)
        override fun getAllowUserInteraction(): Boolean = delegate.allowUserInteraction
        override fun setAllowUserInteraction(allow: Boolean) = delegate.setAllowUserInteraction(allow)
        override fun getUseCaches(): Boolean = delegate.useCaches
        override fun setUseCaches(useCaches: Boolean) = delegate.setUseCaches(useCaches)
        override fun getDefaultUseCaches(): Boolean = delegate.defaultUseCaches
        override fun setDefaultUseCaches(default: Boolean) = delegate.setDefaultUseCaches(default)
        override fun getIfModifiedSince(): Long = delegate.ifModifiedSince
        override fun setIfModifiedSince(ifModifiedSince: Long) = delegate.setIfModifiedSince(ifModifiedSince)
        override fun getRequestProperties(): Map<String, List<String>> = delegate.requestProperties
        override fun getRequestProperty(key: String?): String? = delegate.getRequestProperty(key)
        override fun setRequestProperty(key: String?, value: String?) = delegate.setRequestProperty(key, value)
        override fun addRequestProperty(key: String?, value: String?) = delegate.addRequestProperty(key, value)
        override fun toString(): String = delegate.toString()
    }
}
