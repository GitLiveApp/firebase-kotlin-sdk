package dev.gitlive.firebase

import com.google.firebase.ErrorCode

class NetworkException(code: ErrorCode, detailMessage: String, e: Throwable): FirebaseException(code, detailMessage, e)

open class TooManyRequestsException(code: ErrorCode, detailMessage: String, e: Throwable): FirebaseException(code, detailMessage, e)

open class ApiNotAvailableException(code: ErrorCode, detailMessage: String, e: Throwable): FirebaseException(code, detailMessage, e)

class FirestoreException(code: ErrorCode, detailMessage: String, e: Throwable): FirebaseException(code, detailMessage, e)