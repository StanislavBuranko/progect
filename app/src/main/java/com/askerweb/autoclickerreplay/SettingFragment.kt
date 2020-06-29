package com.askerweb.autoclickerreplay

import android.os.Bundle

import androidx.preference.PreferenceManager
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.takisoft.preferencex.PreferenceFragmentCompat

class SettingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_general, rootKey)
        PreferenceManager
                .getDefaultSharedPreferences(App.getContext())
                .registerOnSharedPreferenceChangeListener { _, _ ->
                    AutoClickService.requestAction(AutoClickService.ACTION_UPDATE_SETTING)
                }
    }
}