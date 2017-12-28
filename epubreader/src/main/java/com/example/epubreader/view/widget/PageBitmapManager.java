package com.example.epubreader.view.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Boyad on 2017/12/14.
 */

public interface PageBitmapManager {
    Bitmap getBitmap(int pageIndex);
    void drawBitmap(Canvas canvas,int x, int y, int pageIndex, Paint paint);
}
