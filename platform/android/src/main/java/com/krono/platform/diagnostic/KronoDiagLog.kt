package com.krono.platform.diagnostic

object KronoDiagLog {
    fun d(tag: String, msg: String) {
        try {
            android.util.Log.d(tag, msg)
        } catch (_: Throwable) {
            println("$tag: $msg")
        }
    }

    fun w(tag: String, msg: String) {
        try {
            android.util.Log.w(tag, msg)
        } catch (_: Throwable) {
            println("WARN: $tag: $msg")
        }
    }

    fun e(tag: String, msg: String, tr: Throwable? = null) {
        try {
            if (tr != null) {
                android.util.Log.e(tag, msg, tr)
            } else {
                android.util.Log.e(tag, msg)
            }
        } catch (_: Throwable) {
            println("ERROR: $tag: $msg ${tr?.message ?: ""}")
        }
    }
}
