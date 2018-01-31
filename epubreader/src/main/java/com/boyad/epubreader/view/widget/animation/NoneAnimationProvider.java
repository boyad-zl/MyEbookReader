package com.boyad.epubreader.view.widget.animation;

import android.graphics.Canvas;

import com.boyad.epubreader.view.widget.PageBitmapManager;

/**
 * Created by Boyad on 2017/12/20.
 */
public class NoneAnimationProvider extends AnimationProvider {
    public NoneAnimationProvider(PageBitmapManager bitmapManager) {
        super(bitmapManager);
    }

    @Override
    public int getPageToScrollTo(int x, int y) {
        if (myDirection == null) {
            return 0;
        }

        switch (myDirection) {
            case rightToLeft:
                return myStartX < x ? PAGE_INDEX_POSITION_PREVIOUS : PAGE_INDEX_POSITION_NEXT;
            case leftToRight:
                return myStartX < x ? PAGE_INDEX_POSITION_NEXT: PAGE_INDEX_POSITION_PREVIOUS;
            case up:
                return myStartY < y ? PAGE_INDEX_POSITION_PREVIOUS : PAGE_INDEX_POSITION_NEXT;
            case down:
                return myStartY < y ? PAGE_INDEX_POSITION_NEXT :PAGE_INDEX_POSITION_PREVIOUS;
        }
        return 0;
    }


    @Override
    protected void startAnimatedScrollingInternal(int speed) {
    }

    @Override
    protected void setupAnimatedScrollingStart(Integer x, Integer y) {
        if (myDirection.isHorizontal) {
            myStartX = mySpeed < 0 ? myWidth : 0;
            myEndX = myWidth - myStartX;
            myEndY = myStartY = 0;
        } else {
            myEndX = myStartX = 0;
            myStartY = mySpeed < 0 ? myHeight : 0;
            myEndY = myHeight - myStartY;
        }

    }

    @Override
    public void doStep() {

    }

    @Override
    protected void drawInternal(Canvas canvas) {

    }

    @Override
    protected void setFilter() {

    }
}
