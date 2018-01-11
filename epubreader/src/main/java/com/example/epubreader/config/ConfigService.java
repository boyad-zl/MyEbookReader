package com.example.epubreader.config;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.epubreader.util.MyReadLog;

/**
 * Created by Boyad on 2018/1/11.
 */

public class ConfigService extends Service {
    private ConfigInterface.Stub myConfig;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myConfig;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MyReadLog.i("ConfigService => oncreate!");
        myConfig = new SQLiteConfig(ConfigService.this);
    }

    @Override
    public void onDestroy() {
        if (myConfig != null) {
            myConfig = null;
        }
        super.onDestroy();
    }
}
