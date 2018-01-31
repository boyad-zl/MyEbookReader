package com.boyad.epubreader.util;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.SparseArray;

/**
 * Created by Boyad on 2017/12/8.
 */

public class StringWidthMeasureHelper {
    private static SparseArray<Float> charWidthSparseArray = new SparseArray();
    private static final float VALUE_IF_KEY_NOT_FOUND = -2;
    private static final int BASE_FONT_SIZE = 100;// 默认字体大小
    private static Typeface measureTypeface  = Typeface.SERIF; //默认字体
    private static Paint measurePaint = new Paint();
    static {
        measurePaint.setTextSize(BASE_FONT_SIZE);
        measurePaint.setTypeface(measureTypeface);
    }

    public static void setMeasureTypeface(Typeface typeface) {
        if (typeface != null && !measurePaint.equals(typeface)) {
            measureTypeface = typeface;
            measurePaint.setTypeface(measureTypeface);
            charWidthSparseArray.clear();
            BookContentDrawHelper.setDrawTypeface(measureTypeface);
        }
    }

    public static float getStringWidth(String word) {
        float width = 0;
        if (TextUtils.isEmpty(word)) return width;
        char[] chars = word.toCharArray();
        if (chars.length == 1) return getCharWidth(chars[0]);
        for (int i = 0; i < chars.length; i++) {
            width = width + getCharWidth(chars[i]);
//            MyReadLog.i("c = " + chars[i] + ", width = " + width);
        }
        return width;
    }

    public static float getCharWidth(char c) {
        if (BookStingUtil.isOnlyChinese(c)){
            return BASE_FONT_SIZE;
        }
        float charWidth = charWidthSparseArray.get((int)c, VALUE_IF_KEY_NOT_FOUND);
        if (charWidth == VALUE_IF_KEY_NOT_FOUND) {
            synchronized (measurePaint){
                charWidth = measurePaint.measureText(String.valueOf(c));
                charWidthSparseArray.put((int)c, charWidth);
                return charWidth;
            }
        } else {
            return charWidth;
        }
    }

    public static float[] getStringCharWidths(String content) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        float[] charWidths = new float[content.length()];
        measurePaint.getTextWidths(content, charWidths);
        return charWidths;
    }
}
