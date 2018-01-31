package com.example.epubreader;

import android.graphics.Rect;
import android.support.v4.app.NavUtils;

import com.example.epubreader.view.book.BookDummyAbstractView;
import com.example.epubreader.view.widget.BookReadListener;
import com.example.epubreader.view.widget.ControlCenterWindow;
import com.example.epubreader.view.widget.PageBitmapManagerImpl;

import java.util.ArrayList;

/**
 * Created by Boyad on 2018/1/16.
 */

public abstract class BookControlCenter {
    private static BookControlCenter ourInstance;

    public static BookControlCenter Instance() {
        return ourInstance;
    }

    public BookControlCenter() {
        ourInstance = this;
        pageBitmapManager = new PageBitmapManagerImpl();
    }

    private volatile BookDummyAbstractView dummyAbstractView;
    private volatile ControlCenterWindow myWindow;
    protected volatile PageBitmapManagerImpl pageBitmapManager ;

    protected final void setDummyView(BookDummyAbstractView view) {
        if (view != null) {
            dummyAbstractView = view;
            final BookReadListener listener = getViewListener();
            if (listener != null) {
                listener.reset();
                listener.repaint();
            }
        }
    }

    public final BookDummyAbstractView getCurrentView() {
        return dummyAbstractView;
    }

    public final void setWindow(ControlCenterWindow window) {
        this.myWindow = window;
    }

    public final void initWindow() {
        setDummyView(dummyAbstractView);
    }


    public final BookReadListener getViewListener() {
        return myWindow != null ? myWindow.getViewListener() : null;
    }

    public PageBitmapManagerImpl getPageBitmapManager() {
        return pageBitmapManager;
    }

    public void closeWindow() {
        if (myWindow != null) {
            myWindow.close();
        }
    }

    public int getBatteryLevel() {
        return (myWindow != null) ? myWindow.getBatteryLevel() : 0;
    }

    public void showWindowMenu(){
        if (myWindow != null) {
            myWindow.showMenu();
        }
    }

}
