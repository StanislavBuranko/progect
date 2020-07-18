package com.askerweb.autoclickerreplay;

import android.app.Application;
import android.content.Context;

import com.askerweb.autoclickerreplay.activity.MainActivity;
import com.askerweb.autoclickerreplay.di.AppComponent;
import com.askerweb.autoclickerreplay.di.ContextModule;
import com.askerweb.autoclickerreplay.di.DaggerAppComponent;
import com.askerweb.autoclickerreplay.di.JsonModule;
import com.google.gson.Gson;

import javax.inject.Inject;

public class App extends Application {

    public static App instance = null;

    public static AppComponent component = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static void initComponent(Context mainActivityContext){
        if(component == null) {
            synchronized (instance){
                if(component == null){
                    component = DaggerAppComponent.builder()
                            .contextModule(new ContextModule(instance, mainActivityContext))
                            .build();
                }
            }
        }
    }
}
