package com.askerweb.autoclickerreplay.di

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.askerweb.autoclickerreplay.App
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class BillingModule {

    @Singleton
    @Provides
    fun provideBuilderBillingClient(context: Context):BillingClient.Builder{
        return BillingClient.newBuilder(context)
                .enablePendingPurchases()
                .setListener(App.purchasesListener)
    }

    @Singleton
    @Provides
    fun provideBillingClient(builder:BillingClient.Builder) = builder.build()

}