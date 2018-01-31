/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package com.boyad.epubreader.view.widget.animation.cul;

import android.graphics.*;

import com.boyad.epubreader.util.MyReadLog;
import com.boyad.epubreader.view.widget.PageBitmapManager;
import com.boyad.epubreader.view.widget.animation.AnimationProvider;
import com.boyad.epubreader.view.widget.animation.cul.pageflip.OnPageFlipListener;
import com.boyad.epubreader.view.widget.animation.cul.pageflip.Page;
import com.boyad.epubreader.view.widget.animation.cul.pageflip.PageFlip;
import com.boyad.epubreader.view.widget.animation.cul.pageflip.PageFlipState;

public final class CurlAnimationProvider extends AnimationProvider implements OnPageFlipListener {
    private PageFlip mPageFlip;

    public CurlAnimationProvider(PageBitmapManager bitmapManager) {
        super(bitmapManager);
    }

    public void setPageFlip(PageFlip pageFlip) {
        this.mPageFlip = pageFlip;
        mPageFlip.setListener(this);
    }

    @Override
    protected void drawInternal(Canvas canvas) {
        long start = System.currentTimeMillis();
        mPageFlip.deleteUnusedTextures();
        Page page = mPageFlip.getFirstPage();
        if (getPageToScrollTo() == PAGE_INDEX_POSITION_NEXT) {
            if (!page.isSecondTextureSet()) {
                MyReadLog.i("!page.isSecondTextureSet()");
                page.setSecondTexture(getBitmapTo());
            }
        } else if (!page.isFirstTextureSet()) {
            MyReadLog.i("!page.isFirstTextureSet()");
            page.setFirstTexture(getBitmapTo());
        }
        mPageFlip.drawFlipFrame();
        MyReadLog.i("draw frame cost " + (System.currentTimeMillis() - start));
    }

    @Override
    public int getPageToScrollTo(int x, int y) {
        if (myDirection == null) {
            return PAGE_INDEX_POSITION_CURRENT;

        }

        if (myStartX == myEndX && myStartY == myEndY) {
            switch (myDirection) {
                case leftToRight:
                    return myStartX < myWidth / 2 ? PAGE_INDEX_POSITION_NEXT : PAGE_INDEX_POSITION_PREVIOUS;
                case rightToLeft:
                    return myStartX < myWidth / 2 ? PAGE_INDEX_POSITION_PREVIOUS : PAGE_INDEX_POSITION_NEXT;
                case up:
                    return myStartY < myHeight / 2 ? PAGE_INDEX_POSITION_PREVIOUS : PAGE_INDEX_POSITION_NEXT;
                case down:
                    return myStartY < myHeight / 2 ? PAGE_INDEX_POSITION_NEXT : PAGE_INDEX_POSITION_PREVIOUS;
            }
        } else {
            if (myStartX >= myEndX) {
                return PAGE_INDEX_POSITION_NEXT;
            } else {
                return PAGE_INDEX_POSITION_PREVIOUS;
            }
        }
        return PAGE_INDEX_POSITION_CURRENT;
    }

    @Override
    protected void startAnimatedScrollingInternal(int speed) {
//		doStep();
    }

    @Override
    protected void setupAnimatedScrollingStart(Integer x, Integer y) {
        if (x == null || y == 0) {
            if (myDirection.isHorizontal) {
                x = mySpeed < 0 ? myWidth : 0;
                y = 0;
            } else {
                x = 0;
                y = mySpeed < 0 ? myHeight : 0;
            }
        }
        myStartX = myEndX = x;
        myStartY = myEndY = y;
        mPageFlip.onFingerDown(myStartX, myStartY);
        MyReadLog.i("getScrollTo = " + getPageToScrollTo() + ", mywidth / 4 = " + (myWidth / 4));
        if (getPageToScrollTo() == PAGE_INDEX_POSITION_NEXT) {
            mPageFlip.onFingerUp(myStartX, myStartY, 1200);
//            mPageFlip.onFingerMove(x - myWidth / 4, y);
//            mPageFlip.onFingerUp(x - myWidth / 4, y, 600);
        } else if (getPageToScrollTo() == PAGE_INDEX_POSITION_PREVIOUS) {
            mPageFlip.onFingerUp(myStartX, myStartY, 700);
//            mPageFlip.onFingerMove(x + myWidth / 4, y);
//            mPageFlip.onFingerUp(x + myWidth / 4, y, 600);
        }
    }

    @Override
    public void doStep() {
        if (!getMode().Auto) {
            return;
        }

        boolean isAnimating = mPageFlip.animating();
        if (isAnimating) {
//			MyReadLog.i("state : " + mPageFlip.getFlipState());
        } else {
            final PageFlipState state = mPageFlip.getFlipState();
//			MyReadLog.i("PageFlipState = " + state);
            if (state == PageFlipState.END_WITH_FORWARD) {
                mPageFlip.getFirstPage().setFirstTextureWithSecond();
            }
            terminate();
        }
    }

//	@Override
//	public void drawFooterBitmapInternal(Canvas canvas, Bitmap footerBitmap, int voffset) {
//		canvas.drawBitmap(footerBitmap, 0, voffset, myPaint);
//	}

    @Override
    protected void setFilter() {
//		ViewUtil.setColorLevel(myPaint, myColorLevel);
//		ViewUtil.setColorLevel(myBackPaint, myColorLevel);
//		ViewUtil.setColorLevel(myEdgePaint, myColorLevel);
    }

    @Override
    public boolean canFlipForward() {
        return true;
    }

    @Override
    public boolean canFlipBackward() {
        mPageFlip.getFirstPage().setSecondTextureWithFirst();
        return true;
    }
}
