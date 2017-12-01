package com.example.epubreader.view.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.example.epubreader.ReaderApplication;
import com.example.epubreader.book.BookResourceFile;
import com.example.epubreader.util.MyReadLog;
import com.example.epubreader.view.book.BookDummyAbstractView;
import com.example.epubreader.view.book.BookDummyView;

/**
 * Created by Boyad on 2017/11/21.
 */

public class BookReaderView extends View implements View.OnLongClickListener, BookReadListener {
    private Context mContext;
    private Paint myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int height, width;


    public BookReaderView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public BookReaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public BookReaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    /**
     * 初始化
     */
    private void init() {

        setFocusableInTouchMode(true);
        setDrawingCacheEnabled(false);
        setOnLongClickListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        MyReadLog.i("draw");

        BookDummyAbstractView dummyView = ReaderApplication.getInstance().getDummyView();
        height = getHeight();
        width = getWidth();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//        if (dummyView == null) MyReadLog.i("dummyView is null!!!!!");
        dummyView.paint(bitmap, 0);
        canvas.drawBitmap(bitmap, 0, 0, myPaint);
    }

    private int myPressedX, myPressedY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        BookDummyAbstractView dummyView = ReaderApplication.getInstance().getDummyView();
        switch (event.getAction()) {
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_DOWN:
                myPressedX = x;
                myPressedY = y;
                break;
            case MotionEvent.ACTION_UP:
//                MyReadLog.i("onFingerRelease: x = " + x + ", y = " + y);
                dummyView.onFingerRelease(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                final int slop = ViewConfiguration.get(mContext).getScaledTouchSlop();
                final boolean isAMove = Math.abs(myPressedX - x) > slop || Math.abs(myPressedY - y) > slop;
                break;
        }
        return true;
    }

    @Override
    public boolean onLongClick(View v) {

        return false;
    }

    /**
     * TODO TEST
     * 暂时没有动画设置
     *
     * @param x
     * @param y
     */
    @Override
    public void startAnimatedScrolling(int x, int y) {
//        MyReadLog.i("startAnimatedScrolling");
        int leftRegion = width / 3;
        int rightRegion = width * 2 / 3;
        if (x >= leftRegion && x <= rightRegion) {

        } else {
            boolean isForward = true;
            if (x < leftRegion) {
                isForward = false;
            } else if (x > rightRegion) {
                isForward = true;
            }
            BookDummyAbstractView dummyView = ReaderApplication.getInstance().getDummyView();
            if (!dummyView.canScroll(isForward)) {
//                MyReadLog.i("不能往前翻页");
                return;
            }

            dummyView.onScrollingFinished(isForward);
            postInvalidate();
        }
    }


    @Override
    public void repaint() {
        postInvalidate();
    }
}
