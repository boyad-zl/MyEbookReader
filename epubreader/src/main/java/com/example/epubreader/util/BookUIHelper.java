package com.example.epubreader.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

import com.example.epubreader.ReaderApplication;

import java.util.IllegalFormatCodePointException;

/**
 * Created by Boyad on 2017/11/20.
 */

public class BookUIHelper {

    private static long sLastClickTime; // 记录上次点击的毫秒值

    /**
     * 将dp转换成px
     *
     * @param dpValue
     * @return
     */
    public static int dp2px(float dpValue) {
        final float scale = ReaderApplication.getInstance().getWindowSize().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 获取屏幕宽高
     *
     * @return
     */
    public static DisplayMetrics getWindowSize() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) ReaderApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    public static void setNotFastClickListener(View view, View.OnClickListener listener) {
        setNotFastClickListener(view, 300, listener);
    }

    private static void setNotFastClickListener(View view, final int intervalTime, final View.OnClickListener listener) {
        if (view == null || listener == null) return;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long nowTime = System.currentTimeMillis();
                if (nowTime > sLastClickTime + intervalTime || nowTime <= sLastClickTime) {
                    listener.onClick(v);
                }
                sLastClickTime = nowTime;
            }
        });
    }

    public static int getNavigationBarHeight() {
        return Resources.getSystem().getDimensionPixelSize(Resources.getSystem().getIdentifier("navigation_bar_height", "dimen", "android"));
    }

    public static boolean isNavigationBarShowing() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            WindowManager wm = (WindowManager) ReaderApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.y != size.y;
        } else {
            boolean menu = ViewConfiguration.get(ReaderApplication.getInstance().getApplicationContext()).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            if (menu || back) {
                return false;
            } else {
                return true;
            }
        }
    }

    public static void setForceStatusBarColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = activity.getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            SystemBarTintManager tintManager = new SystemBarTintManager(activity);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintColor(color);
        }
    }
}
