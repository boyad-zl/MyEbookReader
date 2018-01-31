package com.example.epubreader.view.book;

import android.graphics.Bitmap;

import com.example.epubreader.BookControlCenter;
import com.example.epubreader.ReaderApplication;

import java.util.ArrayList;

/**
 * Created by Boyad on 2017/11/21.
 */

public abstract class BookDummyAbstractView implements BookViewEnums{
    public final BookControlCenter controlCenter;

    public BookDummyAbstractView(BookControlCenter controlCenter) {
        this.controlCenter = controlCenter;
    }

    public abstract boolean canScroll(boolean isForward);
    public abstract void paint(Bitmap bitmap, int pagePosition);
    public abstract void onScrollingFinished(int pageIndex);

    public abstract void setPages(ArrayList<BookPage> pages);
    public abstract void setCoverPage(BookCoverPage coverPage);
    public abstract void calculateTotalPages();

//    public abstract void reset();

    public abstract void jumpLinkHref(String href);

    public abstract void preparePage(BookReadPosition position);

    public abstract void onFingerPress(int x, int y);
    public abstract void onFingerMove(int x, int y);
    public abstract void onFingerRelease(int x, int y);
    public abstract boolean onFingerLongPress(int myPressedX, int myPressedY);

    public abstract void onFingerSingleTap(int x, int y);
    public abstract void onFingerDoubleTap(int x, int y);
    public abstract void onFingerMoveAfterLongPress(int x, int y);
    public abstract void onFingerReleaseAfterLongPress(int x, int y);

}
