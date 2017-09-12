package br.com.olivum.recorder.application;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Created by allann on 8/6/17.
 */

public class App extends Application {
    private static final String TAG = "App";

    // Create the instance

    private static App instance = null;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
    }

    public static App getInstance() {
        return instance;
    }

    public Context getContext() {
        return instance.getApplicationContext();
    }

    public App() {
        Log.d(TAG, "App()");
    }
}
