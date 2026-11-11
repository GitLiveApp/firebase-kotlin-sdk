/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package java.util.concurrent

import dev.gitlive.firebase.EXECUTOR_JVM_ONLY

/**
 * Stands in for `java.util.concurrent.Executor` so that the Firebase Android SDK members taking one (the `Executor` overloads
 * of the `Task` listeners and continuations and of `addSnapshotListener`, `SnapshotListenOptions.Builder.setExecutor`) can be
 * declared in common code and deprecated with an error naming their multiplatform counterparts. A value of this type cannot
 * exist in common code: the interface has no members and nothing implements it. On Android and the JVM the interface is a
 * header stub that is stripped from the output (see buildSrc utils/HeaderStubs.kt), so code there uses the JDK's.
 */
@Deprecated(EXECUTOR_JVM_ONLY, level = DeprecationLevel.ERROR)
public interface Executor
