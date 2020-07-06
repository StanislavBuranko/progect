@file: JvmName("SettingExt")

package com.askerweb.autoclickerreplay

import androidx.preference.PreferenceManager

@JvmField val KEY_SIZE_POINT = App.getContext().resources.getString(R.string.key_preference_size_point)
@JvmField val KEY_REPEAT = App.getContext().resources.getString(R.string.key_preference_repeat)
@JvmField val KEY_BOUNDS_ON = App.getContext().resources.getString(R.string.key_preference_bounds_on)
@JvmField val KEY_SIZE_CONTROL_PANEL = App.getContext().resources.getString(R.string.key_preference_size_control_panel)

@JvmField val defaultSizeControl = App.getContext().resources.getStringArray(R.array.arr_size_control_panel_values)[1].toInt()
@JvmField val defaultSizePoint = App.getContext().resources.getStringArray(R.array.arr_size_point_values)[1].toInt()
const val defaultRepeat = -1
const val defaultBoundsOn = false

fun getSetting(key:String, defaultValue:String):String?{
    return PreferenceManager
            .getDefaultSharedPreferences(App.getContext())
            .getString(key, defaultValue)
}

fun getSetting(key:String, defaultValue:Int):Int?{
    return PreferenceManager
            .getDefaultSharedPreferences(App.getContext())
            .getString(key, defaultValue.toString())?.toInt()
}

fun getSetting(key:String, defaultValue:Boolean):Boolean?{
    return PreferenceManager
            .getDefaultSharedPreferences(App.getContext())
            .getBoolean(key, defaultValue)
}