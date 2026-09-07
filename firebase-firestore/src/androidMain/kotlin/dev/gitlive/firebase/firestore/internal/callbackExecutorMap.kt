package dev.gitlive.firebase.firestore.internal

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor

// Since on iOS Callback threads are set as settings, we store the settings explicitly here as well
internal val callbackExecutorMap = ConcurrentHashMap<dev.gitlive.firebase.android.firestore.FirebaseFirestore, Executor>()
