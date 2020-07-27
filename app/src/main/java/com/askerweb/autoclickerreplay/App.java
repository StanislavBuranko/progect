package com.askerweb.autoclickerreplay;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.askerweb.autoclickerreplay.di.ActivityComponent;
import com.askerweb.autoclickerreplay.di.ActivityModule;
import com.askerweb.autoclickerreplay.di.AppComponent;
import com.askerweb.autoclickerreplay.di.ApplicationModule;
import com.askerweb.autoclickerreplay.di.DaggerAppComponent;
import com.askerweb.autoclickerreplay.di.ListCommandModule;
import com.askerweb.autoclickerreplay.di.ServiceComponent;
import com.askerweb.autoclickerreplay.ktExt.LogExt;
import com.askerweb.autoclickerreplay.point.Point;
import com.askerweb.autoclickerreplay.service.AutoClickService;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

public class App extends Application {


    private static App instance = null;

    public static AppComponent appComponent = null;
    public static ActivityComponent activityComponent = null;
    public static ServiceComponent serviceComponent = null;

    public static App getInstance() {
        return instance;
    }

    public static PurchasesUpdatedListener purchasesListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {

        }
    };

    public static BillingClientStateListener billingStateListener = new BillingClientStateListener() {
        @Override
        public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

            }
        }

        @Override
        public void onBillingServiceDisconnected() {

        }
    };

    @Inject
    BillingClient clientBilling;

    List<String> mSkuIds = Arrays.asList("turn_off_ad");

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        MobileAds.initialize(instance);
        List<String> android_id = Collections.singletonList("B661284821DE7327318792508C54E72D");
        MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder()
                        .setTestDeviceIds(android_id)
                        .build()
        );
        appComponent = DaggerAppComponent.builder()
                .applicationModule(new ApplicationModule(instance))
                .build();
        appComponent.inject(instance);
        clientBilling.startConnection(billingStateListener);
    }

    public static void initActivityComponent(Context mainActivityContext){
        if(activityComponent == null) {
            synchronized (instance){
                if(activityComponent == null){
                    activityComponent = appComponent.plus(new ActivityModule(mainActivityContext));
                }
            }
        }
    }

    public static void initServiceComponent(AutoClickService service){
        if(serviceComponent == null) {
            synchronized (instance){
                if(serviceComponent == null){
                    serviceComponent = appComponent.builderServiceComponent()
                            .service(service)
                            .listCommandModule(new ListCommandModule(new LinkedList<Point>()))
                            .build();
                }
            }
        }
    }



    private void querySkuDetails() {
        SkuDetailsParams.Builder skuDetailsParamsBuilder = SkuDetailsParams.newBuilder();
        skuDetailsParamsBuilder.setSkusList(mSkuIds).setType(BillingClient.SkuType.INAPP);
        clientBilling.querySkuDetailsAsync(skuDetailsParamsBuilder.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                if (billingResult.getResponseCode() == 0) {
                    for (SkuDetails skuDetails : list) {
//                        mSkuDetailsMap.put(skuDetails.getSku(), skuDetails);
                    }
                }
            }
        });
    }

    public static void disableServiceComponent(){
        if(serviceComponent != null) {
            synchronized (instance){
                if(serviceComponent != null){
                    serviceComponent = null;
                }
            }
        }
    }




}
