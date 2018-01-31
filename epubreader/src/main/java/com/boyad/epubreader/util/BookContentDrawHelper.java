package com.boyad.epubreader.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;

import com.boyad.epubreader.R;
import com.boyad.epubreader.ReaderApplication;


/**
 * 绘制工具类
 * Created by Boyad on 2018/1/19.
 */

public class BookContentDrawHelper {
    private static boolean isDayModel;
    private static FontBgTheme fontBgTheme;
    private static int navigationBarHeight;

    public enum FontBgTheme {
        WhiteBgBlackFont(Color.WHITE, Color.BLACK),
        YellowPicBgBlackFont(ReaderApplication.getInstance().getResources().getDrawable(R.drawable.bg_yellow_page), Color.BLACK);

        @ColorInt
        public final int BgColor, FontColor;
        public final Bitmap BgBitmap;

        FontBgTheme(int bgColor, int fontColor) {
            BgBitmap = null;
            BgColor = bgColor;
            FontColor = fontColor;
        }


        FontBgTheme(Drawable drawable, int fontColor) {
            BgBitmap = ((BitmapDrawable)drawable).getBitmap();
            BgColor = Color.WHITE;
            FontColor = fontColor;
        }

        public boolean isDrawableBg() {
            return BgBitmap != null;
        }

    }

    public static boolean isDayModel() {
        return isDayModel;
    }

    public static void setDayModel(boolean isDayModel) {
        BookContentDrawHelper.isDayModel = isDayModel;
    }

    public static void setIsDayModel(boolean isDayModel) {
        BookContentDrawHelper.isDayModel = isDayModel;
    }

    public static FontBgTheme getFontBgTheme() {
        return fontBgTheme;
    }

    public static void setFontBgTheme(int theme) {
        switch (theme) {
            case 0:
                fontBgTheme = FontBgTheme.WhiteBgBlackFont;
                break;
            case 1:
                fontBgTheme = FontBgTheme.YellowPicBgBlackFont;
                break;
            default:
                fontBgTheme = FontBgTheme.WhiteBgBlackFont;
                break;
        }
    }

    private static Typeface drawTypeface = Typeface.SERIF; //默认字体

    public static void setDrawTypeface(Typeface typeface) {
        drawTypeface = typeface;
    }

    public static Typeface getDrawTypeface() {
        return drawTypeface;
    }

    public static int getNavigationBarHeight() {
        return navigationBarHeight;
    }

    public static void setNavigationBarHeight(int navigationBarHeight) {
        BookContentDrawHelper.navigationBarHeight = navigationBarHeight;
    }
}
