/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

import dev.gitlive.firebase.database.externals.Database
import dev.gitlive.firebase.externals.jsGet
import dev.gitlive.firebase.externals.jsIsArray
import dev.gitlive.firebase.externals.jsObject
import dev.gitlive.firebase.externals.jsSet
import dev.gitlive.firebase.externals.jsTypeOf
import dev.gitlive.firebase.externals.json
import dev.gitlive.firebase.externals.toJs
import dev.gitlive.firebase.database.externals.DataSnapshot as JsDataSnapshot

/**
 * @property js The underlying Firebase JS SDK object.
 * @property database The JS SDK database the snapshot belongs to.
 */
public actual class DataSnapshot internal constructor(public val js: JsDataSnapshot, public val database: Database) {
    public actual fun exists(): Boolean = rethrow { js.exists() }
    public actual val key: String? get() = js.key
    public actual val ref: DatabaseReference get() = DatabaseReference(js.ref, database)
    public actual val value: Any? get() = rethrow { js.`val`().fromJs() }

    public actual fun getValue(useExportFormat: Boolean): Any? = if (useExportFormat) rethrow { js.exportVal().fromJs() } else value

    @Suppress("UNCHECKED_CAST")
    public actual fun <T> getValue(t: GenericTypeIndicator<T>): T? = value as T?

    public actual val priority: Any? get() = js.priority.fromJs()

    public actual fun child(path: String): DataSnapshot = rethrow { DataSnapshot(js.child(path), database) }

    public actual fun hasChild(path: String): Boolean = rethrow { js.hasChild(path) }

    public actual fun hasChildren(): Boolean = js.hasChildren()

    public actual val childrenCount: Long get() = js.size.toLong()

    public actual val children: Iterable<DataSnapshot>
        get() = rethrow {
            buildList {
                js.forEach { child ->
                    add(DataSnapshot(child, database))
                    false
                }
            }
        }

    override fun toString(): String = "DataSnapshot(key=$key, value=$value)"
}

/**
 * The data of a transaction as a JS value tree: the JS SDK's transaction receives the current value and returns the
 * new one, so [root] holds the value being edited and each child refers to a path in it.
 *
 * @property root The value being edited, as the JS SDK's transaction receives it and returns it.
 */
public actual class MutableData internal constructor(internal val root: Root, private val path: List<String>, public actual val key: String?) {
    internal constructor(current: JsAny?, key: String?) : this(Root(current), emptyList(), key)

    /** The JS value being edited. */
    internal class Root(var js: JsAny?)

    private val node: JsAny? get() = path.fold(root.js) { parent, segment -> parent?.takeIf { isObject(it) }?.let { jsGet(it, segment) } }

    public actual var value: Any?
        get() = node.fromJs()
        set(value) {
            setNode(value.toJs())
        }

    /** The data as the JS SDK reads and writes it. */
    internal var jsValue: JsAny?
        get() = node
        set(value) {
            setNode(value)
        }

    @Suppress("UNCHECKED_CAST")
    public actual fun <T> getValue(t: GenericTypeIndicator<T>): T? = value as T?

    /** The JS SDK writes a priority through a `{ ".value": ..., ".priority": ... }` value. */
    public actual var priority: Any?
        get() = node?.takeIf { isObject(it) }?.let { jsGet(it, ".priority").fromJs() }
        set(value) {
            val current = node
            val data = if (current != null && !jsIsArray(current) && isObject(current) && ".value" in objectKeys(current)) jsGet(current, ".value") else current
            setNode(if (value == null) data else json(".value" to data, ".priority" to value.toJs()))
        }

    private fun setNode(value: JsAny?) {
        if (path.isEmpty()) {
            root.js = value
            return
        }
        var parent = root.js
        if (parent == null || !isObject(parent)) {
            parent = jsObject()
            root.js = parent
        }
        for (segment in path.dropLast(1)) {
            var next = jsGet(parent!!, segment)
            if (next == null || !isObject(next)) {
                next = jsObject()
                jsSet(parent, segment, next)
            }
            parent = next
        }
        jsSet(parent!!, path.last(), value)
    }

    public actual fun child(path: String): MutableData {
        val segments = path.split('/').filter { it.isNotEmpty() }
        return MutableData(root, this.path + segments, segments.lastOrNull() ?: key)
    }

    public actual fun hasChild(path: String): Boolean = child(path).node != null

    public actual fun hasChildren(): Boolean = childrenCount > 0

    public actual val childrenCount: Long get() = childKeys().size.toLong()

    public actual val children: Iterable<MutableData> get() = childKeys().map { MutableData(root, path + it, it) }

    @Suppress("UNCHECKED_CAST", "UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    private fun childKeys(): List<String> {
        val current = node ?: return emptyList()
        if (!isObject(current)) return emptyList()
        return if (jsIsArray(current)) (0 until (current as JsArray<JsAny?>).length).map { it.toString() } else objectKeys(current).filter { it != ".priority" && it != ".value" }
    }

    override fun toString(): String = "MutableData(key=$key, value=$value)"
}

private fun isObject(value: JsAny): Boolean = jsTypeOf(value) == "object"
