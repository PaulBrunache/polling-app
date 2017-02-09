package faskteam.faskandroid.contollers.main_controllers;

import android.app.Application;
import android.content.Context;
import com.facebook.FacebookSdk;

public class MyApplication extends Application {
    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        mInstance = this;
        super.onCreate();
        // Initialize the SDK before executing any other operations
        FacebookSdk.sdkInitialize(mInstance);
    }

    public static MyApplication getInstance() {
        return mInstance;
    }


    public static Context getAppContext() {
        return mInstance.getApplicationContext();
    }
}
