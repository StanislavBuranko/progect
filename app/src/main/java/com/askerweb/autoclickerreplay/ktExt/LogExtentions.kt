@file:JvmName("LogExt")

package com.askerweb.autoclickerreplay.ktExt

import android.content.ContentValues
import android.util.Log
import com.askerweb.autoclickerreplay.BuildConfig

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