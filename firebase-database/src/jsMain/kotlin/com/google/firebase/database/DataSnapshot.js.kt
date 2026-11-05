/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

import dev.gitlive.firebase.database.externals.Database
import dev.gitlive.firebase.database.REFLECTIVE_WRITE
import kotlin.js.json
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

    public actual val priority: Any? get() = js.priority.toPriority()

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
 * new one, so [root] holds the value being edited and each child refers to a path in it. A node with a priority is held
 * in the SDK's `{ ".value": data, ".priority": priority }` form, which every operation looks through. The tree is read
 * and written through own properties only, so that a path such as `__proto__` cannot reach the prototype chain.
 *
 * @property root The value being edited, as the JS SDK's transaction receives it and returns it.
 */
public actual class MutableData internal constructor(internal val root: Root, private val path: List<String>, public actual val key: String?) {
    internal constructor(current: Any?, key: String?) : this(Root(current), emptyList(), key)

    /** The JS value being edited. */
    internal class Root(var js: Any?)

    /** The node at [path], priority form included. */
    private val node: Any? get() = path.fold(root.js) { parent, segment -> ownChild(unwrap(parent), segment) }

    public actual var value: Any?
        get() = node.toValue()

        @Deprecated(REFLECTIVE_WRITE, ReplaceWith("setData(value)", "com.google.firebase.serialization.setData"), DeprecationLevel.ERROR)
        set(value) {
            setNode(value.toJs(), keepPriority = true)
        }

    /** The data as the JS SDK reads and writes it, without the priority. */
    internal var jsValue: Any?
        get() = unwrap(node)
        set(value) {
            setNode(value, keepPriority = true)
        }

    /** The node as the JS SDK takes it back from a transaction: the data with its priority, in the `.value`/`.priority` form. */
    internal val jsNode: Any? get() = node

    public actual var priority: Any?
        get() = priorityOf(node).toPriority()
        set(value) {
            val data = unwrap(node)
            write(if (value == null) data else withPriority(data, value.toJs()!!))
        }

    /** Replaces the data at [path] with [value], keeping the node's priority when [keepPriority] (as the Android SDK's `setValue`). */
    private fun setNode(value: Any?, keepPriority: Boolean) {
        val priority = if (keepPriority && value != null) priorityOf(node) else null
        write(if (priority == null) value else withPriority(value, priority))
    }

    /** Stores [newNode] at [path], creating the objects on the way (each keeping its priority); null removes the node. */
    private fun write(newNode: Any?) {
        if (path.isEmpty()) {
            root.js = newNode
            return
        }
        if (newNode == null) {
            remove()
            return
        }
        var container = rootContainer()
        for (segment in path.dropLast(1)) container = childContainer(container, segment)
        defineOwn(container, path.last(), newNode)
    }

    /** The object holding the root's children, created if the root is not an object. */
    private fun rootContainer(): Any {
        val current = root.js
        unwrap(current)?.takeIf { isObject(it) }?.let { return it }
        return json().also { fresh -> root.js = priorityOf(current)?.let { withPriority(fresh, it) } ?: fresh }
    }

    /** The object holding the children of [parent]'s child [segment], created if that child is not an object. */
    private fun childContainer(parent: Any, segment: String): Any {
        val current = ownChild(parent, segment)
        unwrap(current)?.takeIf { isObject(it) }?.let { return it }
        return json().also { fresh -> defineOwn(parent, segment, priorityOf(current)?.let { withPriority(fresh, it) } ?: fresh) }
    }

    /**
     * Removes the node at [path], as Firebase does: nothing happens when there is no such node, and an ancestor left without
     * children goes with it, as an empty node is no node.
     */
    private fun remove() {
        val containers = ArrayList<Any>(path.size)
        var data: Any? = unwrap(root.js)
        for ((index, segment) in path.withIndex()) {
            if (data == null || !isObject(data) || !hasOwn(data, segment)) return
            containers += data
            if (index < path.lastIndex) data = unwrap(ownChild(data, segment))
        }
        deleteOwn(containers.last(), path.last())
        for (index in path.indices.reversed()) {
            if (keysWithData(containers[index]).isNotEmpty()) return
            if (index == 0) root.js = null else deleteOwn(containers[index - 1], path[index - 1])
        }
    }

    public actual fun child(path: String): MutableData {
        val segments = path.split('/').filter { it.isNotEmpty() }
        return MutableData(root, this.path + segments, segments.lastOrNull() ?: key)
    }

    public actual fun hasChild(path: String): Boolean = unwrap(child(path).node) != null

    public actual fun hasChildren(): Boolean = childrenCount > 0

    public actual val childrenCount: Long get() = childKeys().size.toLong()

    public actual val children: Iterable<MutableData> get() = childKeys().map { MutableData(root, path + it, it) }

    /** The keys of the children with data: a null entry of an array or a deleted child is no child, as on Android. */
    private fun childKeys(): List<String> = unwrap(node)?.let(::keysWithData) ?: emptyList()

    /** The keys of [data]'s children with data (see [childKeys]); none for a value that is not an object. */
    private fun keysWithData(data: Any): List<String> {
        if (!isObject(data)) return emptyList()
        val keys = if (isArray(data)) data.unsafeCast<Array<Any?>>().indices.map { it.toString() } else objectKeys(data).toList()
        return keys.filter { unwrap(ownChild(data, it)) != null }
    }

    override fun toString(): String = "MutableData(key=$key, value=$value)"
}

/** A node's data, looking through the `.value`/`.priority` form. */
private fun unwrap(node: Any?): Any? = if (isPriorityForm(node)) ownChild(node!!, ".value") else node

/** A node's priority, held in the `.value`/`.priority` form. */
private fun priorityOf(node: Any?): Any? = if (isPriorityForm(node)) ownChild(node!!, ".priority") else null

private fun isPriorityForm(node: Any?): Boolean = node != null && isObject(node) && !isArray(node) && hasOwn(node, ".value")

private fun withPriority(data: Any?, priority: Any): Any = json(".value" to data, ".priority" to priority)

/** A node as its natural Kotlin value, without priorities. */
private fun Any?.toValue(): Any? {
    val data = unwrap(this)
    return when {
        data == null || !isObject(data) -> data.fromJs()
        isArray(data) -> data.unsafeCast<Array<Any?>>().map { it.toValue() }
        else -> objectKeys(data).associateWith { ownChild(data, it).toValue() }
    }
}

/** A priority as the Android SDK presents it: a String, a Double or null. */
internal fun Any?.toPriority(): Any? = when (val priority = fromJs()) {
    is Number -> priority.toDouble()
    else -> priority
}

private fun isObject(value: Any): Boolean = jsTypeOf(value) == "object"

/** The own property [key] of [target], or null; inherited properties (such as `__proto__`) are not children. */
private fun ownChild(target: Any?, key: String): Any? = if (target != null && isObject(target) && hasOwn(target, key)) target.asDynamic()[key].unsafeCast<Any?>() else null

private fun hasOwn(target: Any, key: String): Boolean = js("Object.prototype.hasOwnProperty.call(target, key)").unsafeCast<Boolean>()

/** Defines an own data property, whatever the key: `target[key] = value` would run an inherited setter for `__proto__`. */
private fun defineOwn(target: Any, key: String, value: Any?) {
    js("Object.defineProperty(target, key, { value: value, enumerable: true, writable: true, configurable: true })")
}

/** Removes the own property [key] of [target]. */
private fun deleteOwn(target: Any, key: String) {
    js("delete target[key]")
}
