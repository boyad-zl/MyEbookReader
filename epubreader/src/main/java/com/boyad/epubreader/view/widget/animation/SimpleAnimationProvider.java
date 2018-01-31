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

package com.boyad.epubreader.view.widget.animation;

import com.boyad.epubreader.view.widget.PageBitmapManager;


abstract class SimpleAnimationProvider extends AnimationProvider {
//	private float mySpeedFactor;

	SimpleAnimationProvider(PageBitmapManager bitmapManager) {
		super(bitmapManager);
	}

	@Override
	public int getPageToScrollTo(int x, int y) {
		if (myDirection == null) {
			return PAGE_INDEX_POSITION_CURRENT;
		}

		switch (myDirection) {
			case rightToLeft:
				return myStartX < x ? PAGE_INDEX_POSITION_PREVIOUS : PAGE_INDEX_POSITION_NEXT;
			case leftToRight:
				return myStartX < x ? PAGE_INDEX_POSITION_NEXT : PAGE_INDEX_POSITION_PREVIOUS;
			case up:
				return myStartY < y ? PAGE_INDEX_POSITION_PREVIOUS : PAGE_INDEX_POSITION_NEXT;
			case down:
				return myStartY < y ? PAGE_INDEX_POSITION_NEXT : PAGE_INDEX_POSITION_PREVIOUS;
		}
		return PAGE_INDEX_POSITION_CURRENT;
	}

	@Override
	protected void setupAnimatedScrollingStart(Integer x, Integer y) {
		if (x == null || y == null) {
			if (myDirection.isHorizontal) {
				x = mySpeed < 0 ? myWidth : 0;
				y = 0;
			} else {
				x = 0;
				y = mySpeed < 0 ? myHeight : 0;
			}
		}
		myEndX = myStartX = x;
		myEndY = myStartY = y;
	}

	@Override
	protected void startAnimatedScrollingInternal(int speed) {
//		mySpeedFactor = (float)Math.pow(1.5, 0.25 * speed);
//		mySpeedFactor = (float)Math.pow(1.5,  speed);
//		mySpeed *= mySpeedFactor;
		doStep();
	}

	@Override
	public final void doStep() {
		if (!getMode().Auto) {
			return;
		}

		switch (myDirection) {
			case leftToRight:
				myEndX -= (int)mySpeed;
				break;
			case rightToLeft:
				myEndX += (int)mySpeed;
				break;
			case up:
				myEndY += (int)mySpeed;
				break;
			case down:
				myEndY -= (int)mySpeed;
				break;
		}
//		MyReadLog.i("myStartX = " + myStartX + ", myEndX = " + myEndX + ", getScrollingShift()" + getScrollingShift());

		final int bound;
		if (getMode() == Mode.AnimatedScrollingForward) {
			bound = myDirection.isHorizontal ? myWidth : myHeight;
		} else {
			bound = 0;
		}
		if (mySpeed > 0) {
			if (getScrollingShift() >= bound) {
				if (myDirection.isHorizontal) {
					myEndX = myStartX + bound;
				} else {
					myEndY = myStartY + bound;
				}
				terminate();
				return;
			}
		} else {
			if (getScrollingShift() <= -bound) {
				if (myDirection.isHorizontal) {
					myEndX = myStartX - bound;
				} else {
					myEndY = myStartY - bound;
				}
				terminate();
				return;
			}
		}
//		mySpeed *= mySpeedFactor;
	}

}
