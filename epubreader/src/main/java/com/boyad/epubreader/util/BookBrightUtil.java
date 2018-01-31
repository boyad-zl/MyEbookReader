package com.boyad.epubreader.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by Boyad on 2018/1/17.
 */

public class BookBrightUtil {
    public static final int BOOK_READ_MAX_BRIGHTNESS = 255;
    public static final int BOOK_READ_MIN_BRIGHTNESS = 0;

    /**
     * 判断是否开启了自动亮度调节
     *
     * @param aContentResolver
     * @return
     */
    public static boolean isAutoBrightness(ContentResolver aContentResolver) {
        boolean automicBrightness = false;
        try {
            automicBrightness = Settings.System.getInt(aContentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return automicBrightness;
    }

    /**
     * 获取屏幕的亮度
     *
     * @param activity
     * @return
     */
    public static int getScreenBrightness(Activity activity) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        if (lp.screenBrightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
            return -1;
        } else {
            return (int) (lp.screenBrightness * 255);
        }
//        int nowBrightnessValue = 0;
//        ContentResolver resolver = activity.getContentResolver();
//        try {
//            nowBrightnessValue = android.provider.Settings.System.getInt(
//                    resolver, Settings.System.SCREEN_BRIGHTNESS);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return nowBrightnessValue;
    }

    /**
     * 设置亮度
     *
     * @param activity
     * @param brightness
     */
    public static void setBrightness(Activity activity, int brightness) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
        activity.getWindow().setAttributes(lp);

    }

    /**
     * 停止自动亮度调节
     *
     * @param activity
     */
    public static void stopAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    /**
     * 开启亮度自动调节
     *
     * @param activity
     */
    public static void startAutoBrightness(Activity activity) {
//        Settings.System.putInt(activity.getContentResolver(),
//                Settings.System.SCREEN_BRIGHTNESS_MODE,
//                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        activity.getWindow().setAttributes(lp);
    }
}
