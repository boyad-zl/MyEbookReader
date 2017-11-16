package com.example.zjb.myebookreader;

import android.app.Application;
import android.os.Process;
import android.util.Log;

/**
 * Created by zjb on 2017/10/31.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("MyReader", "Oncreate init: Pid = " + Process.myPid());
    }
}
