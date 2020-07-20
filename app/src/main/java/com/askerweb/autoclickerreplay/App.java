package com.askerweb.autoclickerreplay;

import android.app.Application;
import android.content.Context;

import com.askerweb.autoclickerreplay.di.ActivityComponent;
import com.askerweb.autoclickerreplay.di.ActivityModule;
import com.askerweb.autoclickerreplay.di.AppComponent;
import com.askerweb.autoclickerreplay.di.ApplicationModule;
import com.askerweb.autoclickerreplay.di.DaggerAppComponent;
import com.askerweb.autoclickerreplay.di.ListCommandModule;
import com.google.android.gms.ads.MobileAds;

public class App extends Application {


    private static App instance = null;

    public static AppComponent appComponent = null;
    public static ActivityComponent activityComponent = null;



    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        MobileAds.initialize(this);
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
}
