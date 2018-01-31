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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;

import com.boyad.epubreader.view.widget.PageBitmapManager;


public final class SlideAnimationProvider extends SimpleAnimationProvider {
	private final Paint myDarkPaint = new Paint();
	private final Paint myPaint = new Paint();

	public SlideAnimationProvider(PageBitmapManager bitmapManager) {
		super(bitmapManager);
	}

	private void setDarkFilter(int visible, int full) {
		visible = full; // add by jerry (hide dark effect)

		int darkColorLevel = 145 + 100 * Math.abs(visible) / full;
		if (myColorLevel != null) {
			darkColorLevel = darkColorLevel * myColorLevel / 0xFF;
		}
//		ViewUtil.setColorLevel(myDarkPaint, darkColorLevel);
	}

	@Override
	protected void setFilter() {
//		ViewUtil.setColorLevel(myPaint, myColorLevel);
	}

	private void drawShadowHorizontal(Canvas canvas, int left, int right, int dY) {
		final GradientDrawable.Orientation orientation = dY > 0
			? GradientDrawable.Orientation.BOTTOM_TOP
			: GradientDrawable.Orientation.TOP_BOTTOM;
		final int[] colors = new int[] { 0x46000000, 0x00000000 };
		final GradientDrawable gradient = new GradientDrawable(orientation, colors);
		gradient.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		gradient.setDither(true);
		if (dY > 0) {
			gradient.setBounds(left, dY - 16, right, dY);
		} else {
			gradient.setBounds(left, myHeight + dY, right, myHeight + dY + 16);
		}
		gradient.draw(canvas);
	}

	private void drawShadowVertical(Canvas canvas, int top, int bottom, int dX) {
		final GradientDrawable.Orientation orientation = dX > 0
			? GradientDrawable.Orientation.RIGHT_LEFT
			: GradientDrawable.Orientation.LEFT_RIGHT;
		final int[] colors = new int[] { 0x46000000, 0x00000000 };
		final GradientDrawable gradient = new GradientDrawable(orientation, colors);
		gradient.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		gradient.setDither(true);
		if (dX > 0) {
			gradient.setBounds(dX - 16, top, dX, bottom);
		} else {
			gradient.setBounds(myWidth + dX, top, myWidth + dX + 16, bottom);
		}
		gradient.draw(canvas);
	}

	@Override
	protected void drawInternal(Canvas canvas) {
//		long start = System.currentTimeMillis();
		if (myDirection.isHorizontal) {
			final int dX = myEndX - myStartX;
			setDarkFilter(dX, myWidth);
			drawBitmapTo(canvas, 0, 0, myDarkPaint);
			drawBitmapFrom(canvas, dX, 0, myPaint);
			drawShadowVertical(canvas, 0, myHeight, dX);
		} else {
			final int dY = myEndY - myStartY;
			setDarkFilter(dY, myHeight);
			drawBitmapTo(canvas, 0, 0, myDarkPaint);
			drawBitmapFrom(canvas, 0, dY, myPaint);
			drawShadowHorizontal(canvas, 0, myWidth, dY);
		}
//		MyReadLog.i("draw a frame cost time is " + (System.currentTimeMillis() - start));
	}

	private void drawBitmapInternal(Canvas canvas, Bitmap bm, int left, int right, int height, int voffset, Paint paint) {
		canvas.drawBitmap(
			bm,
			new Rect(left, 0, right, height),
			new Rect(left, voffset, right, voffset + height),
			paint
		);
	}

//	@Override
//	protected void drawFooterBitmapInternal(Canvas canvas, Bitmap footerBitmap, int voffset) {
//		if (myDirection.IsHorizontal) {
//			final int dX = myEndX - myStartX;
//			setDarkFilter(dX, myWidth);
//			final int h = footerBitmap.getHeight();
//			if (dX > 0) {
//				drawBitmapInternal(canvas, footerBitmap, 0, dX, h, voffset, myDarkPaint);
//				drawBitmapInternal(canvas, footerBitmap, dX, myWidth, h, voffset, myPaint);
//			} else {
//				drawBitmapInternal(canvas, footerBitmap, myWidth + dX, myWidth, h, voffset, myDarkPaint);
//				drawBitmapInternal(canvas, footerBitmap, 0, myWidth + dX, h, voffset, myPaint);
//			}
//			drawShadowVertical(canvas, voffset, voffset + h, dX);
//		} else {
//			canvas.drawBitmap(footerBitmap, 0, voffset, myPaint);
//		}
//	}
}
