package com.askerweb.autoclickerreplay.di

import android.content.Context
import com.askerweb.autoclickerreplay.activity.MainActivity
import com.askerweb.autoclickerreplay.point.Point
import com.askerweb.autoclickerreplay.service.AutoClickService
import dagger.Binds
import dagger.Module
import dagger.Provides
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Scope
import javax.inject.Singleton

@Module
class ApplicationModule(private val appContext: Context) {
        @Singleton
        @Provides
        fun provideContext() = appContext
}

@Module
class ActivityModule(private val mainActivity: Context){

        @ActivityScope
        @Named("ActivityContext")
        @Provides
        fun provideActivityContext() = mainActivity
}

@Module
class ListCommandModule public constructor(val listCommand: MutableList<Point>){
        @ServiceScope
        @Provides
        fun provideListPoints() = listCommand
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityScope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ServiceScope

