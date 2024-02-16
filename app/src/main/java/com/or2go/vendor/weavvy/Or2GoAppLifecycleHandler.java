package com.or2go.vendor.weavvy;

import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.util.Log;

import androidx.annotation.NonNull;

public class Or2GoAppLifecycleHandler implements ComponentCallbacks2 {

    private static final String TAG = "Or2Go";//AppLifecycleHandler.class.getSimpleName();
    private static boolean isInBackground = false;
    AppEnv gAppEnv;

    public void setApplicationInfo(AppEnv appenv) {gAppEnv = appenv;}

    @Override
    public void onTrimMemory(int level) {
        if(level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN){
            Log.i(TAG, "app went to background");
            isInBackground = true;
        }
        if (level >= TRIM_MEMORY_MODERATE) { // 60
            // Nearing middle of list of cached background apps
            // shutdown app
            Log.i(TAG, "shutting down application.");
            gAppEnv.appExit();
        }else if (level >= TRIM_MEMORY_BACKGROUND) { // 40
            // Entering list of cached background apps; evict oldest half of our
            // thumbnail cache
            Log.i(TAG, "App going to LRU");
            //gAppEnv.ShutdownAppEnv();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {}

    @Override
    public void onLowMemory() {}
}
