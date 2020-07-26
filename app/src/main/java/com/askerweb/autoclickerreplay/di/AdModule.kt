package com.askerweb.autoclickerreplay.di

import android.content.Context
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.logd
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAdsInitProvider
import com.google.android.gms.ads.internal.overlay.AdOverlayInfoParcel
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class AdModule{
    @Singleton
    @Provides
    fun provideMainInterstitial(context: Context): InterstitialAd {
        val interstitialAd = InterstitialAd(context)
        interstitialAd.adUnitId = context.getString(R.string.Interstitial_Unit_Ad_ID)
        return interstitialAd
    }
}