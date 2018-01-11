package com.example.epubreader.config;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.example.epubreader.util.BookConstract;

import java.net.ContentHandler;

/**
 * Created by Boyad on 2018/1/11.
 */

public class ConfigShadow implements ServiceConnection {
    private final Context mContext;
    private volatile ConfigInterface mInterface;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    public ConfigShadow(Context mContext) {
        this.mContext = mContext;
        mContext.bindService(new Intent(mContext, ConfigService.class), this, Service.BIND_AUTO_CREATE);
    }

    public String getValue(String name){
        if (mInterface == null) {
            return "";
        }
        try {
            return mInterface.getValue(name);
        } catch (RemoteException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mInterface = ConfigInterface.Stub.asInterface(service);
        mContext.registerReceiver(mReceiver, new IntentFilter(BookConstract.ACTION_CONFIG_OPTION_CHANGE));
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mContext.unregisterReceiver(mReceiver);
    }
}
