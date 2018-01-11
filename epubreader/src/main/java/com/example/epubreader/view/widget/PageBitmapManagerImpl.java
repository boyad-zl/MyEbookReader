package com.example.epubreader.view.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.example.epubreader.ReaderApplication;
import com.example.epubreader.util.MyReadLog;

/**
 * Created by Boyad on 2017/12/14.
 */

public class PageBitmapManagerImpl implements PageBitmapManager {
    private final int SIZE = 2;
    private final Bitmap[] myBitmaps = new Bitmap[SIZE];
    private final int[] myPageIndexes = new int[SIZE];

    private int myWidth ;
    private int myHeight;

//    private final BookReaderSurfaceView myWidget;

    public PageBitmapManagerImpl() {

    }

    public void setSize(int w, int h) {
        if (myWidth != w  || myHeight != h) {
            myWidth = w;
            myHeight = h;
            for (int i = 0; i < SIZE; i++) {
                myBitmaps[i] = null;
                myPageIndexes[i] = -2;
            }
            System.gc();
            System.gc();
            System.gc();
        }
    }

    @Override
    public Bitmap getBitmap(int pageIndex) {
        for (int i = 0; i < SIZE; i++) {
            if (pageIndex == myPageIndexes[i]){
                return myBitmaps[i];
            }
        }
        final int iIndex = getInternalIndex(pageIndex);
        myPageIndexes[iIndex] = pageIndex;
        if (myBitmaps[iIndex] == null) {
            try {
                myBitmaps[iIndex] = Bitmap.createBitmap(myWidth, myHeight, Bitmap.Config.RGB_565);
            } catch (OutOfMemoryError e) {
                System.gc();
                System.gc();
                myBitmaps[iIndex] = Bitmap.createBitmap(myWidth, myHeight, Bitmap.Config.RGB_565);
            }
        }
        MyReadLog.i("myWidget.drawOnBitmap");
//        myWidget.drawOnBitmap(myBitmaps[iIndex], pageIndex);
        ReaderApplication.getInstance().getMyWidget().drawOnBitmap(myBitmaps[iIndex], pageIndex);
        return myBitmaps[iIndex];
    }

    @Override
    public void drawBitmap(Canvas canvas, int x, int y, int pageIndex, Paint paint) {
        canvas.drawBitmap(getBitmap(pageIndex), x, y, paint);
    }


    private int getInternalIndex(int pageIndex) {
        for (int i = 0; i < SIZE; i++) {
            if (myPageIndexes[i] == -2) {
                return i;
            }
        }
        for (int i = 0; i < SIZE; i++) {
            if (myPageIndexes[i] != 0) {
                return i;
            }
        }
        throw new RuntimeException("That's impossible!");
    }

    void reset() {
        for (int i = 0; i < SIZE; i++) {
            myPageIndexes[i] = -2;
        }
    }

    void shift(boolean isForward) {
        for (int i = 0; i < SIZE; i++) {
            if (myPageIndexes[i] == -2) {
                continue;
            }
            if (isForward) {
                myPageIndexes[i] = myPageIndexes[i] - 1;
            } else {
                if (myPageIndexes[i] + 1 >= 2) {
                    myPageIndexes[i] = -2;
                } else {
                    myPageIndexes[i] = myPageIndexes[i] + 1;
                }
            }
        }
    }
}
