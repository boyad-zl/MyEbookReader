package com.example.epubreader.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.example.epubreader.ReaderApplication;

/**
 * Created by Boyad on 2017/11/20.
 */

public class BookUIHelper {
    
    /**
     * 将dp转换成px
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
}
