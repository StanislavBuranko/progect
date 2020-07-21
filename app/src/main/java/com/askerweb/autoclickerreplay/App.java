package com.askerweb.autoclickerreplay;

import android.app.Application;
import android.content.Context;

import com.askerweb.autoclickerreplay.di.ActivityComponent;
import com.askerweb.autoclickerreplay.di.ActivityModule;
import com.askerweb.autoclickerreplay.di.AppComponent;
import com.askerweb.autoclickerreplay.di.ApplicationModule;
import com.askerweb.autoclickerreplay.di.DaggerAppComponent;
import com.askerweb.autoclickerreplay.di.ListCommandModule;
import com.askerweb.autoclickerreplay.di.ServiceComponent;
import com.askerweb.autoclickerreplay.point.Point;
import com.askerweb.autoclickerreplay.service.AutoClickService;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.LinkedList;
import java.util.List;

import kotlin.jvm.internal.markers.KMutableList;

public class App extends Application {


    private static App instance = null;

    public static AppComponent appComponent = null;
    public static ActivityComponent activityComponent = null;
    public static ServiceComponent serviceComponent = null;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        appComponent = DaggerAppComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
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


}
