package com.askerweb.autoclickerreplay.fragment

import android.os.Bundle

import androidx.preference.PreferenceManager
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.takisoft.preferencex.PreferenceFragmentCompat

class SettingFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_general, rootKey)
        PreferenceManager
                .getDefaultSharedPreferences(App.component.getAppContext())
                .registerOnSharedPreferenceChangeListener { _, _ ->
                    AutoClickService.requestAction(context, AutoClickService.ACTION_UPDATE_SETTING)
                }
    }
}