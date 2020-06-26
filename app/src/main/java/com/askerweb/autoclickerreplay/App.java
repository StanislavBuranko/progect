package com.askerweb.autoclickerreplay;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

public class App extends Application {

    private static App instance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Context getContext(){
        return instance;
    }
}
