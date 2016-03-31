package com.example.smartlock;
import com.umeng.analytics.MobclickAgent;

import android.app.Application;

public class MyApplication extends Application {

    private static MyApplication instance;  
    @Override  
    public void onCreate() {  
        super.onCreate();
        CrashHandler.getInstance().init(getApplicationContext());  
        MobclickAgent.setDebugMode( true );
    }  
    public static MyApplication getInstance() {  
        if (instance == null) {  
            instance = new MyApplication();  
        }  
        return instance;  
    }  
}
