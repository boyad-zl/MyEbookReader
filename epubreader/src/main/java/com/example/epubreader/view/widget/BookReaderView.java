package com.example.epubreader.view.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.example.epubreader.BookControlCenter;
import com.example.epubreader.BookReadControlCenter;
import com.example.epubreader.ReaderApplication;
import com.example.epubreader.book.BookResourceFile;
import com.example.epubreader.util.MyReadLog;
import com.example.epubreader.view.ReaderActivity;
import com.example.epubreader.view.book.BookDummyAbstractView;
import com.example.epubreader.view.book.BookViewEnums;
import com.example.epubreader.view.widget.animation.AnimationProvider;
import com.example.epubreader.view.widget.animation.cul.CurlAnimationProvider;
import com.example.epubreader.view.widget.animation.NoneAnimationProvider;
import com.example.epubreader.view.widget.animation.ShiftAnimationProvider;
import com.example.epubreader.view.widget.animation.SlideAnimationProvider;
import com.example.epubreader.view.widget.animation.SlideOldStyleAnimationProvider;

import java.util.List;


/**
 * Created by Boyad on 2018/1/8.
 */

public class BookReaderView extends View implements View.OnLongClickListener, BookReadListener {

    private Context mContext;
    private Paint myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint selectedPaint = new Paint();
    private PageBitmapManagerImpl pageBitmapManager ;
    private Bitmap startCursorBitmap;
    private Bitmap endCursorBitmap;
    private int cursorBitmapWidth, cursorBitmapHeight;
    private Rect srcRect;
    private Rect startCursorDestRect = new Rect();
    private Rect endCursorDestRect = new Rect();
    private Rect canvasRect;

    public static final int PAGE_POSITION_INDEX_PREVIOUS = -1;
    public static final int PAGE_POSITION_INDEX_CURRENT = 0;
    public static final int PAGE_POSITION_INDEX_NEXT = 1;

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
        MyReadLog.i("view init !!!");
//        pageBitmapManager = new PageBitmapManagerImpl();
        selectedPaint.setColor(Color.argb(0x58, 0, 0, 0xff));
        setFocusableInTouchMode(true);
        setDrawingCacheEnabled(false);
        setOnLongClickListener(this);
    }

    private long lastDrawTime ;
    @Override
    protected void onDraw(Canvas canvas) {
        if (mContext instanceof ReaderActivity) {
            ((ReaderActivity) mContext).createWakeLock();
        } else {
            System.err.println("view's context is not ReaderActivity");
        }
        pageBitmapManager.setSize(getWidth(), getHeight());
        if (getAnimationProvider().inProgress()) {
            onDrawInScrolling(canvas);
//            long FinishDrawTime = System.currentTimeMillis();
//            MyReadLog.i("动画之间的时间间距是" + (FinishDrawTime - lastDrawTime));
//            lastDrawTime = FinishDrawTime;
        } else {
            onDrawStatic(canvas);
        }
    }

    private void onDrawStatic(Canvas canvas) {
        canvas.drawBitmap(pageBitmapManager.getBitmap(PAGE_POSITION_INDEX_CURRENT), 0, 0, myPaint);
    }

    private void onDrawInScrolling(Canvas canvas) {
//                    MyReadLog.i("MESSAGE_DRAW_ANIMATION_PAGES");
//        final BookDummyAbstractView view = ReaderApplication.getInstance().getDummyView();
        final BookDummyAbstractView view = BookControlCenter.Instance().getCurrentView();
        final AnimationProvider animator = getAnimationProvider();
        final AnimationProvider.Mode oldMode = animator.getMode();
        animator.doStep();
        if (animator.inProgress()) {
            animator.draw(canvas);
            if (animator.getMode().Auto) {
                postInvalidate();
            }
//                            drawFooter(canvas, animator);
        } else {
            switch (oldMode) {
                case AnimatedScrollingForward: {
                    final int index = animator.getPageToScrollTo();
                    boolean isForward = index == PAGE_POSITION_INDEX_NEXT;
                    pageBitmapManager.shift(isForward);
                    view.onScrollingFinished(index);
//                                    ZLApplication.Instance().onRepaintFinished();
                    break;
                }
                case AnimatedScrollingBackward:
                    view.onScrollingFinished(PAGE_POSITION_INDEX_CURRENT);
                    break;
            }
            onDrawStatic(canvas);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        MyReadLog.d("onSizeChanged, w = %d, h = %d, oldw = %d, oldh = " + w, h, oldw, oldh);
        getAnimationProvider().terminate();
        if (myScreenIsTouched) {
            myScreenIsTouched = false;
            BookControlCenter.Instance().getCurrentView().onScrollingFinished(PAGE_POSITION_INDEX_CURRENT);
        }
        if ((w > h && oldw < oldh) || (w < h && oldw > oldh)){
            MyReadLog.i("需要重新绘制！");
            BookControlCenter.Instance().getCurrentView().preparePage(null);
            reset();
            repaint();
            ((BookReadControlCenter) BookControlCenter.Instance()).calculateTotalPages();
        }
    }

    private class LongClickRunnable implements Runnable {
        @Override
        public void run() {
            if (performLongClick()) {
                myLongClickPerformed = true;
            }
        }
    }

    private volatile LongClickRunnable myPendingLongClickRunnable;
    private volatile boolean myLongClickPerformed;

    private void postLongClickRunnable() {
        myLongClickPerformed = false;
        myPendingPress = false;
        if (myPendingLongClickRunnable == null) {
            myPendingLongClickRunnable = new LongClickRunnable();
        }
        postDelayed(myPendingLongClickRunnable, 2 * ViewConfiguration.getLongPressTimeout());
    }

    private class ShortClickRunnable implements Runnable {
        @Override
        public void run() {
//            final BookDummyAbstractView view = ReaderApplication.getInstance().getDummyView();
            final BookDummyAbstractView view = BookControlCenter.Instance().getCurrentView();
            view.onFingerSingleTap(myPressedX, myPressedY);
            myPendingPress = false;
            myPendingShortClickRunnable = null;
        }

    }

    private volatile ShortClickRunnable myPendingShortClickRunnable;
    private volatile boolean myPendingPress;
    private volatile boolean myPendingDoubleTap;
    private boolean myScreenIsTouched;
    private int myPressedX, myPressedY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
//        BookDummyAbstractView dummyView = ReaderApplication.getInstance().getDummyView();
        BookDummyAbstractView dummyView = BookControlCenter.Instance().getCurrentView();
        switch (event.getAction()) {
            case MotionEvent.ACTION_CANCEL:
                MyReadLog.i("MotionEvent.ACTION_CANCEL");
                break;
            case MotionEvent.ACTION_DOWN:
                if (myPendingShortClickRunnable != null) {
                    removeCallbacks(myPendingShortClickRunnable);
                    myPendingShortClickRunnable = null;
                    myPendingDoubleTap = true;
                } else {
                    postLongClickRunnable();
                    myPendingPress = true;
                }
                myScreenIsTouched = true;
                myPressedX = x;
                myPressedY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (myPendingDoubleTap) {
                    dummyView.onFingerDoubleTap(x, y);
                } else if (myLongClickPerformed) {
                    dummyView.onFingerReleaseAfterLongPress(x, y);
                } else {
                    if (myPendingLongClickRunnable != null) {
                        removeCallbacks(myPendingLongClickRunnable);
                        myPendingLongClickRunnable = null;
                    }
                    if (myPendingPress) {
                        //  TODO TEST 是否支持双击
//                        if () {
//                            if (myPendingShortClickRunnable == null) {
//                                myPendingShortClickRunnable = new ShortClickRunnable();
//                            }
//                            postDelayed(myPendingShortClickRunnable, ViewConfiguration.getDoubleTapTimeout());
//                        } else {
                        dummyView.onFingerSingleTap(x, y);
//                        }
                    } else {
                        dummyView.onFingerRelease(x, y);
                    }
                }
                myPendingPress = false;
                myPendingDoubleTap = false;
                myScreenIsTouched = false;
//                dummyView.onFingerRelease(x, y);
                break;
            case MotionEvent.ACTION_MOVE: {
                final int slop = ViewConfiguration.get(mContext).getScaledTouchSlop();
                final boolean isAMove = Math.abs(myPressedX - x) > slop || Math.abs(myPressedY - y) > slop;
                if (isAMove) {
                    myPendingDoubleTap = false;
                }

                if (myLongClickPerformed) {
                    dummyView.onFingerMoveAfterLongPress(x, y);
                } else {
                    if (myPendingPress) {
                        if (isAMove) {
                            if (myPendingShortClickRunnable != null) {
                                removeCallbacks(myPendingShortClickRunnable);
                                myPendingShortClickRunnable = null;
                            }
                            if (myPendingLongClickRunnable != null) {
                                removeCallbacks(myPendingLongClickRunnable);
                            }
                            dummyView.onFingerPress(myPressedX, myPressedY);
                            myPendingPress = false;
                        }
                    }
                    if (!myPendingPress) {
                        dummyView.onFingerMove(x, y);
                    }
                }
                break;
            }
        }
        return true;
    }

    @Override
    public boolean onLongClick(View v) {
//        BookDummyAbstractView dummyView = ReaderApplication.getInstance().getDummyView();
        BookDummyAbstractView dummyView = BookControlCenter.Instance().getCurrentView();
        return dummyView.onFingerLongPress(myPressedX, myPressedY);
    }

    @Override
    public void startManualScrolling(int x, int y, BookViewEnums.Direction direction) {
        final AnimationProvider animator = getAnimationProvider();
//		animator.setup(direction, getWidth(), getMainAreaHeight(), myColorLevel);
        animator.setup(direction, getWidth(), getHeight(), null);
        animator.startManualScrolling(x, y);
    }

    @Override
    public void scrollManuallyTo(int x, int y) {
//        final BookDummyAbstractView dummyView = ReaderApplication.getInstance().getDummyView();
        final BookDummyAbstractView dummyView = BookControlCenter.Instance().getCurrentView();
        final AnimationProvider animator = getAnimationProvider();
        int pageIndex = animator.getPageToScrollTo(x, y);
        MyReadLog.i("scrollManuallyTo");
        if (dummyView.canScroll(pageIndex == PAGE_POSITION_INDEX_NEXT)) {
            animator.scrollTo(x, y);
            postInvalidate();
        }
    }

    @Override
    public void startAnimatedScrolling(int x, int y) {
//        final BookDummyAbstractView dummyView = ReaderApplication.getInstance().getDummyView();
        final BookDummyAbstractView dummyView = BookControlCenter.Instance().getCurrentView();
        final AnimationProvider animation = getAnimationProvider();
        if (!dummyView.canScroll(animation.getPageToScrollTo(x, y) == PAGE_POSITION_INDEX_NEXT)) {
            animation.terminate();
            return;
        }
        MyReadLog.i("startAnimatedScrolling ==== x   y ");
        animation.startAnimatedScrolling(x, y, 25);
        postInvalidate();
    }


    @Override
    public void startAnimatedScrolling(int pageIndex, int x, int y, BookViewEnums.Direction direction) {
//        final BookDummyAbstractView dummyView = ReaderApplication.getInstance().getDummyView();
        final BookDummyAbstractView dummyView = BookControlCenter.Instance().getCurrentView();
        if (pageIndex == PAGE_POSITION_INDEX_CURRENT || !dummyView.canScroll(pageIndex == PAGE_POSITION_INDEX_NEXT)) {
            return;
        }
        final AnimationProvider animator = getAnimationProvider();
//		animator.setup(direction, getWidth(), getMainAreaHeight(), myColorLevel);
        animator.setup(direction, getWidth(), getHeight(), null);
        animator.startAnimatedScrolling(pageIndex, x, y, 25);
        if (animator.getMode().Auto) {
            postInvalidate();
        }
    }

    @Override
    public void startAnimatedScrolling(int pageIndex, BookViewEnums.Direction direction) {
//        final BookDummyAbstractView dummyView = ReaderApplication.getInstance().getDummyView();
        final BookDummyAbstractView dummyView = BookControlCenter.Instance().getCurrentView();
        if (pageIndex == PAGE_POSITION_INDEX_CURRENT || !dummyView.canScroll(pageIndex == PAGE_POSITION_INDEX_NEXT)) {
            return;
        }
        final AnimationProvider animator = getAnimationProvider();
//		animator.setup(direction, getWidth(), getMainAreaHeight(), myColorLevel);
        animator.setup(direction, getWidth(), getHeight(), null);
        animator.startAnimatedScrolling(pageIndex, null, null, 15);
        if (animator.getMode().Auto) {
            postInvalidate();
        }

    }

    @Override
    public void repaint() {
        MyReadLog.i("repaint");
        postInvalidate();
    }

    @Override
    public void drawOnBitmap(Bitmap bitmap, int pageIndex) {
        BookDummyAbstractView dummyView = BookControlCenter.Instance().getCurrentView();
        dummyView.paint(bitmap, pageIndex);
    }

    private AnimationProvider myAnimationProvider;
    private BookViewEnums.Animation myAnimationType;

    private AnimationProvider getAnimationProvider() {
        final BookViewEnums.Animation type = BookViewEnums.Animation.slide;
        if (myAnimationProvider == null || myAnimationType != type) {
            myAnimationType = type;
            switch (type) {
                case none:
                    myAnimationProvider = new NoneAnimationProvider(pageBitmapManager);
                    break;
                case curl:
                    myAnimationProvider = new CurlAnimationProvider(pageBitmapManager);
                    break;
                case slide:
                    myAnimationProvider = new SlideAnimationProvider(pageBitmapManager);
                    break;
                case slideOldStyle:
                    myAnimationProvider = new SlideOldStyleAnimationProvider(pageBitmapManager);
                    break;
                case shift:
                    myAnimationProvider = new ShiftAnimationProvider(pageBitmapManager);
                    break;
            }
        }
        return myAnimationProvider;
    }

    @Override
    public void reset() {
        pageBitmapManager.reset();
    }

    @Override
    public void drawSelectedRegion(List<Rect> rects) {

    }

    public void setPageBitmapManager(PageBitmapManagerImpl pageBitmapManager) {
        this.pageBitmapManager = pageBitmapManager;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                MyReadLog.i("key down KEYCODE_VOLUME_UP");
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                MyReadLog.i("key down KEYCODE_VOLUME_DOWN");
                break;
        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                MyReadLog.i("key up KEYCODE_VOLUME_UP");
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                MyReadLog.i("key up KEYCODE_VOLUME_DOWN");
                break;
        }
        return true;
    }
}
