package com.example.epubreader.view.book;

import android.graphics.Bitmap;

import com.example.epubreader.ReaderApplication;

import java.util.ArrayList;

/**
 * Created by Boyad on 2017/11/21.
 */

public abstract class BookDummyAbstractView {
    public final ReaderApplication application;
    boolean isDayModel;

    public BookDummyAbstractView(ReaderApplication application) {
        this.application = application;
    }

   public abstract void onFingerRelease(int x, int y);

    public abstract boolean canScroll(boolean isForward);

    public abstract void paint(Bitmap bitmap, int i);

    public abstract void onScrollingFinished(boolean isForward);

    public abstract void setPages(ArrayList<BookPage> pages);

    public abstract void reset();

    public boolean isDayModel() {
        return isDayModel;
    }

    public void setDayModel(boolean dayModel) {
        isDayModel = dayModel;
    }
}
