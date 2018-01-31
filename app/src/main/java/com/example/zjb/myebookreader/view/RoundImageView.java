package com.example.zjb.myebookreader.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.example.epubreader.util.BookUIHelper;


/**
 * 圆角ImageView(isCircle为true时，为圆形图片)
 * @author zhangsl
 *
 */
public class RoundImageView extends ImageView {
    private boolean isCircle ;
	public RoundImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RoundImageView(Context context) {
		super(context);
		init();
	}

	private final RectF roundRect = new RectF();
	private float mRectRadius;
	private final Paint maskPaint = new Paint();
	private final Paint zonePaint = new Paint();

	private void init() {
		maskPaint.setAntiAlias(true);
		maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		
		zonePaint.setAntiAlias(true);
		zonePaint.setColor(Color.WHITE);
		
		mRectRadius = BookUIHelper.dp2px(2.5f);
	}

	public void setRectRadius(float radius) {
		mRectRadius = radius;
		invalidate();
	}
	
	public void setCircle(boolean isCircle) {
	    this.isCircle = isCircle;
	    invalidate();
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int w = getWidth();
		int h = getHeight();
		roundRect.set(0, 0, w, h);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.saveLayer(roundRect, zonePaint, Canvas.ALL_SAVE_FLAG);
		if (isCircle) {
		    canvas.drawRoundRect(roundRect, getWidth() / 2, getHeight() / 2, zonePaint);
		} else {
		    canvas.drawRoundRect(roundRect, mRectRadius, mRectRadius, zonePaint);
		}
		canvas.saveLayer(roundRect, maskPaint, Canvas.ALL_SAVE_FLAG);
		super.draw(canvas);
		canvas.restore();
	}

}
