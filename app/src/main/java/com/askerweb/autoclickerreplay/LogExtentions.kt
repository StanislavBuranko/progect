@file:JvmName("LogExt")

package com.askerweb.autoclickerreplay

import android.content.ContentValues
import android.util.Log

const val TAG = "AutoClick"

@JvmOverloads fun Any.logd(tag: String = TAG) {
    if (!BuildConfig.DEBUG) return
    if (this is String) {
        Log.d(tag, this)
    } else {
        Log.d(tag, this.toString())
    }
}

fun Any.loge(tag: String = TAG) {
    if (!BuildConfig.DEBUG) return
    if (this is String) {
        Log.e(tag, this)
    } else {
        Log.e(tag, this.toString())
    }
}