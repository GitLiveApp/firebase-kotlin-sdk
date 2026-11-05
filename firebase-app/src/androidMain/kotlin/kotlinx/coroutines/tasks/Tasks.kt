/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.coroutines.tasks

import com.google.android.gms.tasks.Task
import dev.gitlive.firebase.stub

/*
 * Header stub for kotlinx-coroutines-play-services' `TasksKt.await` (see buildSrc utils/HeaderStubs.kt): compiled
 * against, verified to match the real facade, and deleted from the output so consumers bind to the real function.
 */

public actual suspend fun <T> Task<T>.await(): T = stub()
