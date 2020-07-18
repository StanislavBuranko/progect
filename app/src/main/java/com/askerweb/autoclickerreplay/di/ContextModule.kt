package com.askerweb.autoclickerreplay.di

import android.content.Context
import com.askerweb.autoclickerreplay.activity.MainActivity
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Scope
import javax.inject.Singleton

@Module
class ContextModule(private val appContext: Context, private val mainActivityContext: Context) {

        @Singleton
        @Provides
        fun provideContext() = appContext

        @Singleton
        @Provides
        @Named("ActivityContext")
        fun provideActivityContext() = mainActivityContext
}

//@Module
//class ActivityModule(private val mainActivity: Context){
//
//        @ActivityScope
//        @Provides
//        @Named("ActivityContext")
//        fun provideActivityContext() = mainActivity
//}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityScope

