package com.boyad.epubreader.view.widget;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.boyad.epubreader.view.book.BookViewEnums;

import java.util.List;

/**
 * Created by Boyad on 2017/11/21.
 */

public interface BookReadListener {
    void reset();
    void repaint();

    void startManualScrolling(int x, int y, BookViewEnums.Direction direction);
    void scrollManuallyTo(int x, int y);
    void startAnimatedScrolling(int pageIndex, int x, int y, BookViewEnums.Direction direction);
    void startAnimatedScrolling(int pageIndex, BookViewEnums.Direction direction);
    void startAnimatedScrolling(int x, int y);

    void drawSelectedRegion(List<Rect> rects);

    void drawOnBitmap(Bitmap myBitmap, int pageIndex);
//    void setScreenBrightness(int percent);
//    int getScreenBrightness();
}
