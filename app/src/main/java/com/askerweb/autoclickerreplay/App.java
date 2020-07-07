package com.askerweb.autoclickerreplay;

import android.app.Application;
import android.content.Context;
import com.askerweb.autoclickerreplay.di.AppComponent;
import com.askerweb.autoclickerreplay.di.DaggerAppComponent;
import com.google.gson.Gson;

import javax.inject.Inject;

public class App extends Application {

    private static App instance = null;

    @Inject
    protected Gson gson;

    @Override
    public void onCreate() {
        super.onCreate();
        AppComponent component = DaggerAppComponent.create();
        component.inject(this);
        instance = this;
    }

    public static Context getContext(){
        return instance;
    }

    public static Gson getGson(){
        return instance.gson;
    }
}
