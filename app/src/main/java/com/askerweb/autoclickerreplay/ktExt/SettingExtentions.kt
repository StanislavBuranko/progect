@file: JvmName("SettingExt")

package com.askerweb.autoclickerreplay.ktExt

import androidx.preference.PreferenceManager
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R

@JvmField val KEY_SIZE_POINT = context.resources.getString(R.string.key_preference_size_point)
@JvmField val KEY_REPEAT = context.resources.getString(R.string.key_preference_repeat)
@JvmField val KEY_BOUNDS_ON = context.resources.getString(R.string.key_preference_bounds_on)
@JvmField val KEY_SIZE_CONTROL_PANEL = context.resources.getString(R.string.key_preference_size_control_panel)

@JvmField val defaultSizeControl = context.resources.getStringArray(R.array.arr_size_control_panel_values)[1].toInt()
@JvmField val defaultSizePoint = context.resources.getStringArray(R.array.arr_size_point_values)[1].toInt()
const val defaultRepeat = -1
const val defaultBoundsOn = false

fun getSetting(key:String, defaultValue:String):String?{
    return PreferenceManager
            .getDefaultSharedPreferences(context)
            .getString(key, defaultValue)
}

fun getSetting(key:String, defaultValue:Int):Int?{
    return PreferenceManager
            .getDefaultSharedPreferences(context)
            .getString(key, defaultValue.toString())?.toInt()
}

fun getSetting(key:String, defaultValue:Boolean):Boolean?{
    return PreferenceManager
            .getDefaultSharedPreferences(context)
            .getBoolean(key, defaultValue)
}