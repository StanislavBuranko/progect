package com.askerweb.autoclickerreplay.di

import com.askerweb.autoclickerreplay.App
import dagger.Component
import javax.inject.Singleton

@Component(modules = [JsonModule::class])
@Singleton
interface AppComponent {
    fun inject(app: App)
}