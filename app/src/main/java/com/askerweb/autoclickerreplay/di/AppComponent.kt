package com.askerweb.autoclickerreplay.di

import android.app.Service
import android.content.Context
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.activity.MainActivity
import com.askerweb.autoclickerreplay.point.Point
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.google.gson.Gson
import dagger.Component
import dagger.Subcomponent
import javax.inject.Named
import javax.inject.Singleton


@Singleton
@Component(modules = [JsonModule::class, ContextModule::class])
interface AppComponent {
    fun inject(app: App)
    fun inject(context: Context)
    fun inject(point: Point)
    fun inject(autoClickService: AutoClickService)
//    fun plus(activityModule: ActivityModule):ActivityComponent
    fun getGson(): Gson
    fun getAppContext():Context
    @Named("ActivityContext")
    fun getActivityContext():Context
}

