package com.askerweb.autoclickerreplay.fragment

import android.content.SharedPreferences
import android.os.Bundle

import androidx.preference.PreferenceManager
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.logd
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.takisoft.preferencex.PreferenceFragmentCompat

class SettingFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_general, rootKey)
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        "jjj".logd()
        AutoClickService.requestAction(context, AutoClickService.ACTION_UPDATE_SETTING)
    }


}