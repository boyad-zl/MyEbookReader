package com.example.epubreader.view.widget;

/**
 * Created by Boyad on 2017/11/21.
 */

public interface BookReadListener {
//    void reset();
    void repaint();

//    void startManualScrolling(int x, int y);
//    void scrollManuallyTo(int x, int y);
//    void startAnimatedScrolling(ZLView.PageIndex pageIndex, int x, int y, ZLView.Direction direction, int speed);
//    void startAnimatedScrolling(ZLView.PageIndex pageIndex, ZLView.Direction direction, int speed);
    void startAnimatedScrolling(int x, int y);

//    void setScreenBrightness(int percent);
//    int getScreenBrightness();
}
