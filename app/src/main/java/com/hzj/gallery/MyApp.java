package com.hzj.gallery;

import android.app.Application;

import timber.log.Timber;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());
    }
}
