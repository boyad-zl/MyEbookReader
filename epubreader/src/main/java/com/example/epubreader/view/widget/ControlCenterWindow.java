package com.example.epubreader.view.widget;

import android.graphics.Rect;

import java.util.List;

/**
 * Created by Boyad on 2018/1/16.
 */

public interface ControlCenterWindow {
    BookReadListener getViewListener();

    int getBatteryLevel();
    void close();
    void showMenu();
}
