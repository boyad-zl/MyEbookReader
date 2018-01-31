package com.boyad.epubreader.view.widget;

/**
 * Created by Boyad on 2018/1/16.
 */

public interface ControlCenterWindow {
    BookReadListener getViewListener();

    int getBatteryLevel();
    void close();
    void showMenu();
}
