package com.askerweb.autoclickerreplay.di

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.Context
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.activity.AdActivity
import com.askerweb.autoclickerreplay.activity.MainActivity
import com.askerweb.autoclickerreplay.point.Point
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.google.android.gms.ads.InterstitialAd
import com.google.gson.Gson
import dagger.BindsInstance
import dagger.Component
import dagger.Subcomponent
import java.util.*
import javax.inject.Named
import javax.inject.Singleton


@Singleton
@Component(modules = [JsonModule::class, ApplicationModule::class, AdModule::class, BillingModule::class])
interface AppComponent {
    fun inject(app: App)
    fun inject(context: Context)
    fun inject(adActivity: AdActivity)
    fun plus(activityModule:ActivityModule):ActivityComponent
    fun builderServiceComponent():ServiceComponent.Builder
    fun getGson(): Gson
    fun getAppContext():Context
    fun getAdInterstitial():InterstitialAd
}

@ActivityScope
@Subcomponent(modules = [ActivityModule::class])
interface ActivityComponent{
    @Named("ActivityContext")
    fun getActivityContext():Context
    fun inject(point: Point)
    fun inject(activity: MainActivity)
}

@ServiceScope
@Subcomponent(modules = [ListCommandModule::class])
interface ServiceComponent{
    @Subcomponent.Builder
    interface Builder{
        fun build():ServiceComponent
        @BindsInstance fun service(autoClickService: AutoClickService):Builder
        fun listCommandModule(listCommandModule: ListCommandModule):Builder
    }
    fun getService():AutoClickService
    fun getListPoint():MutableList<Point>
    fun inject(autoClickService: AutoClickService)
}