package com.askerweb.autoclickerreplay.di

import android.app.Application
import dagger.Component
import javax.inject.Singleton

@Component(modules = [JsonModule::class])
@Singleton
interface AppComponent {
    fun inject(app: Application)
}