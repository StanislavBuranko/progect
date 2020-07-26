package com.askerweb.autoclickerreplay.di

import android.content.Context
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.point.Point
import com.google.android.gms.ads.InterstitialAd
import dagger.Module
import dagger.Provides
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
class ListCommandModule constructor(val listCommand: MutableList<Point>){
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

