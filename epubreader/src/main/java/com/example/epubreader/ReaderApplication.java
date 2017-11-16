package com.example.epubreader;

import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.example.epubreader.util.BookAttributeUtil;
import com.example.epubreader.util.MyReadLog;

/**
 * Created by zjb on 2017/10/31.
 */

public class ReaderApplication extends Application{
    private static ReaderApplication sInstance;

    /**
     * 初始化
     */
    @Override
    public void onCreate() {
        super.onCreate();
        MyReadLog.i("Application init:UId="+ Process.myUid()+ ", Pid = " + Process.myPid() + ", Tid = " + Process.myTid());
        if (sInstance == null) {
            sInstance = this;
            BookAttributeUtil.setEmSize(20);
        }
    }

    /**
     * 获取屏幕宽高
     *
     * @return
     */
    public DisplayMetrics getWindowSize() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    public static ReaderApplication getInstance() {
        return sInstance;
    }
}
