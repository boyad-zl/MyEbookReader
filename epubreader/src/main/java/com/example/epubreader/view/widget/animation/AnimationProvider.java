package com.example.epubreader.view.widget.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.example.epubreader.ReaderApplication;
import com.example.epubreader.util.MyReadLog;
import com.example.epubreader.view.book.BookViewEnums;
import com.example.epubreader.view.widget.PageBitmapManager;

/**
 * Created by Boyad on 2017/12/20.
 */

public abstract class AnimationProvider {
    protected final static int PAGE_INDEX_POSITION_CURRENT = 0;
    protected final static int PAGE_INDEX_POSITION_PREVIOUS = -1;
    protected final static int PAGE_INDEX_POSITION_NEXT = 1;

    public static enum Mode {
        NoScrolling(false),
        PreManualScrolling(false),
        ManualScrolling(false),
        AnimatedScrollingForward(true),
        AnimatedScrollingBackward(true);

        public final boolean Auto;

        Mode(boolean auto) {
            Auto = auto;
        }
    }

    private Mode myMode = Mode.NoScrolling;
    private final PageBitmapManager myBitmapManager;
    protected int myStartX, myStartY, myEndX, myEndY;
    protected int myWidth, myHeight;
    protected BookViewEnums.Direction myDirection;
    protected float mySpeed;
    protected Integer myColorLevel;

    public AnimationProvider(PageBitmapManager myBitmapManager) {
        this.myBitmapManager = myBitmapManager;
    }

    public Mode getMode() {
        return myMode;
    }

    protected int getScrollingShift() {
        return myDirection.isHorizontal ? myEndX - myStartX : myEndY - myStartY;
    }

    public final void setup(BookViewEnums.Direction direction, int width, int height, Integer colorLevel) {
        myDirection = direction;
        myWidth = width;
        myHeight = height;
        myColorLevel = colorLevel;
    }

    public boolean isProgress() {
        switch (myMode) {
            case NoScrolling:
            case PreManualScrolling:
                return false;
            default:
                return true;
        }
    }

    public final void terminate() {
        myMode = Mode.NoScrolling;
        mySpeed = 0;
//        myDrawInfos.clear();
    }

    public final void startManualScrolling(int x, int y) {
        if (!myMode.Auto) {
            myMode = Mode.PreManualScrolling;
            myEndX = myStartX = x;
            myEndY = myStartY = y;
        }
    }

    private final Mode detectManualMode() {
        final int dX = Math.abs(myStartX - myEndX);
        final int dY = Math.abs(myStartY - myEndY);
        int dpi = ReaderApplication.getInstance().getWindowSize().densityDpi;
        if (myDirection.isHorizontal) {
            if (dY > dpi / 2 && dY > dX) {
                return Mode.NoScrolling;
            } else if (dX > dpi / 10) {
                return Mode.ManualScrolling;
            }
        } else {
            if (dX > dpi / 2 && dX > dY) {
                return Mode.NoScrolling;
            } else if (dY > dpi / 10) {
                return Mode.ManualScrolling;
            }
        }
        return Mode.PreManualScrolling;
    }

    public final void scrollTo(int x, int y) {
        switch (myMode) {
            case ManualScrolling:
                myEndX = x;
                myEndY = y;
                break;
            case PreManualScrolling:
                myEndX = x;
                myEndY = y;
                myMode = detectManualMode();
                break;
        }
    }

    public final void startAnimatedScrolling(int x, int y, int speed) {
        if (myMode != Mode.ManualScrolling) {
            return;
        }
        if (getPageToScrollTo(x, y) == PAGE_INDEX_POSITION_CURRENT) {
            return;
        }
        speed = 50;
        final int dpi = ReaderApplication.getInstance().getWindowSize().densityDpi;
        final int diff = myDirection.isHorizontal ? x - myStartX : y - myStartY;
        final int minDiff = myDirection.isHorizontal
                ? (myWidth > myHeight ? myWidth / 4 : myWidth / 3)
                : (myHeight > myWidth ? myHeight / 4 : myHeight / 3);
        boolean forward = Math.abs(diff) > Math.min(minDiff, dpi / 2);

        myMode = forward ? Mode.AnimatedScrollingForward : Mode.AnimatedScrollingBackward;

        float velocity = speed;
//        if (myDrawInfos.size() > 1) {
//            int duration = 0;
//            for (DrawInfo info : myDrawInfos) {
//                duration += info.Duration;
//            }
//            duration /= myDrawInfos.size();
//            final long time = System.currentTimeMillis();
//            myDrawInfos.add(new DrawInfo(x, y, time, time + duration));
//            velocity = 0;
//            for (int i = 1; i < myDrawInfos.size(); ++i) {
//                final DrawInfo info0 = myDrawInfos.get(i - 1);
//                final DrawInfo info1 = myDrawInfos.get(i);
//                final float dX = info0.X - info1.X;
//                final float dY = info0.Y - info1.Y;
//                velocity += FloatMath.sqrt(dX * dX + dY * dY) / Math.max(1, info1.Start - info0.Start);
//            }
//            velocity /= myDrawInfos.size() - 1;
//            velocity *= duration;
//            velocity = Math.min(100, Math.max(15, velocity));
//        }
//        myDrawInfos.clear();

        if (getPageToScrollTo() == PAGE_INDEX_POSITION_PREVIOUS) {
            forward = !forward;
        }

        switch (myDirection) {
            case up:
            case rightToLeft:
                mySpeed = forward ? -velocity : velocity;
                break;
            case leftToRight:
            case down:
                mySpeed = forward ? velocity : -velocity;
                break;
        }
        MyReadLog.i("startAnimatedScrollingInternal");
        startAnimatedScrollingInternal((int) mySpeed);
    }

    public void startAnimatedScrolling(int pageIndex, Integer x, Integer y, int speed) {
        if (myMode.Auto) {
            return;
        }
        terminate();
        speed = 50;
        myMode = Mode.AnimatedScrollingForward;
        switch (myDirection) {
            case up:
            case rightToLeft:
                mySpeed = pageIndex == PAGE_INDEX_POSITION_NEXT ? -50 : 50;
                break;
            case leftToRight:
            case down:
                mySpeed = pageIndex == PAGE_INDEX_POSITION_NEXT ? 50 : -50;
                break;
        }
        setupAnimatedScrollingStart(x, y);
        startAnimatedScrollingInternal(speed);
    }

    protected abstract void startAnimatedScrollingInternal(int speed);
    protected abstract void setupAnimatedScrollingStart(Integer x, Integer y);

    public boolean inProgress() {
        switch (myMode) {
            case NoScrolling:
            case PreManualScrolling:
                return false;
            default:
                return true;
        }
    }

    public abstract void doStep();

    public final void draw(Canvas canvas) {
//        long start = System.currentTimeMillis();
        setFilter();
        drawInternal(canvas);
//        MyReadLog.i("draw a frame cost time is " + (System.currentTimeMillis() - start));
    }

    protected abstract void drawInternal(Canvas canvas);
    protected abstract void setFilter();

    public final int getPageToScrollTo() {
        return getPageToScrollTo(myEndX, myEndY);
    }

    public abstract int getPageToScrollTo(int x, int y);

    protected Bitmap getBitmapFrom() {
        return myBitmapManager.getBitmap(PAGE_INDEX_POSITION_CURRENT);
    }

    protected Bitmap getBitmapTo() {
        return myBitmapManager.getBitmap(getPageToScrollTo());
    }

    protected void drawBitmapFrom(Canvas canvas, int x, int y, Paint paint) {
        myBitmapManager.drawBitmap(canvas, x, y, PAGE_INDEX_POSITION_CURRENT, paint);
    }

    protected void drawBitmapTo(Canvas canvas, int x, int y, Paint paint) {
        myBitmapManager.drawBitmap(canvas, x, y, getPageToScrollTo(), paint);
    }
}
