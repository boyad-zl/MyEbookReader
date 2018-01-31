package com.example.epubreader.view.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewConfiguration;

import com.example.epubreader.BookControlCenter;
import com.example.epubreader.R;
import com.example.epubreader.ReaderApplication;
import com.example.epubreader.util.BookUIHelper;
import com.example.epubreader.util.MyReadLog;
import com.example.epubreader.view.book.BookDummyAbstractView;
import com.example.epubreader.view.book.BookViewEnums;
import com.example.epubreader.view.widget.animation.AnimationProvider;
import com.example.epubreader.view.widget.animation.cul.CurlAnimationProvider;
import com.example.epubreader.view.widget.animation.NoneAnimationProvider;
import com.example.epubreader.view.widget.animation.ShiftAnimationProvider;
import com.example.epubreader.view.widget.animation.SlideAnimationProvider;
import com.example.epubreader.view.widget.animation.SlideOldStyleAnimationProvider;

import java.util.List;

import static com.example.epubreader.view.widget.BookReaderSurfaceView.DrawHandler.MESSAGE_DRAW_CURRENT_PAGE;

/**
 * Created by Boyad on 2017/11/21.
 */

public class BookReaderSurfaceView extends SurfaceView implements View.OnLongClickListener, BookReadListener, SurfaceHolder.Callback {
    private Context mContext;
    private Paint myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint selectedPaint = new Paint();
//    private PageBitmapManagerImpl pageBitmapManager = new PageBitmapManagerImpl(this);
    private PageBitmapManagerImpl pageBitmapManager;
    private HandlerThread drawHandlerThread;
    private DrawHandler drawHandler;
    //    private SurfaceHolder mHolder;
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

    public BookReaderSurfaceView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public BookReaderSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public BookReaderSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        MyReadLog.i("view init !!!");
        pageBitmapManager = new PageBitmapManagerImpl();
        SurfaceHolder mHolder = getHolder();
        mHolder.addCallback(this);
        selectedPaint.setColor(Color.argb(0x58, 0, 0, 0xff));

        drawHandlerThread = new HandlerThread("drawHandlerThread");
        drawHandlerThread.setPriority(Thread.MAX_PRIORITY);
        setFocusableInTouchMode(true);
        setDrawingCacheEnabled(false);
        setOnLongClickListener(this);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        MyReadLog.i("surfaceCreate");
        drawHandlerThread.start();
        drawHandler = new DrawHandler(drawHandlerThread.getLooper());
        drawHandler.setDrawHolder(holder);
//        drawHandler.sendEmptyMessage(MESSAGE_DRAW_CURRENT_PAGE);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        MyReadLog.i("******surfaceChanged");
        pageBitmapManager.setSize(getWidth(), getHeight());
//        BookDummyAbstractView dummyView = ReaderApplication.getInstance().getDummyView();
//        canvasRect = new Rect(0, 0, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

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
            if (animator.inProgress()) {
                drawHandler.sendEmptyMessage(DrawHandler.MESSAGE_DRAW_ANIMATION_PAGES);
            } else {
                drawHandler.sendEmptyMessage(DrawHandler.MESSAGE_DRAW_CURRENT_PAGE);
            }
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
        if (animation.inProgress()) {
            drawHandler.sendEmptyMessage(DrawHandler.MESSAGE_DRAW_ANIMATION_PAGES);
        } else {
            drawHandler.sendEmptyMessage(DrawHandler.MESSAGE_DRAW_CURRENT_PAGE);
        }
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
//            postInvalidate();
            if (animator.inProgress()) {
                drawHandler.sendEmptyMessage(DrawHandler.MESSAGE_DRAW_ANIMATION_PAGES);
            } else {
                drawHandler.sendEmptyMessage(DrawHandler.MESSAGE_DRAW_CURRENT_PAGE);
            }
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
            if (animator.inProgress()) {
                drawHandler.sendEmptyMessage(DrawHandler.MESSAGE_DRAW_ANIMATION_PAGES);
            } else {
                drawHandler.sendEmptyMessage(DrawHandler.MESSAGE_DRAW_CURRENT_PAGE);
            }
        }

    }

    @Override
    public void repaint() {
        MyReadLog.i("repaint");
//        postInvalidate();
        if (drawHandler != null) {
            drawHandler.sendEmptyMessage(MESSAGE_DRAW_CURRENT_PAGE);
        }
    }

    public void drawOnBitmap(Bitmap bitmap, int pageIndex) {
//        BookDummyAbstractView dummyView = ReaderApplication.getInstance().getDummyView();
        BookDummyAbstractView dummyView = BookControlCenter.Instance().getCurrentView();
//        if (dummyView == null) MyReadLog.i("dummyView is null!!!!!");
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
        Message message = drawHandler.obtainMessage();
        message.what = DrawHandler.MESSAGE_DRAW_SELECTED_REGION;
        message.obj = rects;
        drawHandler.sendMessage(message);
    }

    public void setPageBitmapManager(PageBitmapManagerImpl pageBitmapManager) {
        this.pageBitmapManager = pageBitmapManager;
    }

    public void onPause() {
        drawHandler.reset();
    }

    public void onDestroy() {
        drawHandlerThread.quit();
    }

    class DrawHandler extends Handler {
        static final int MESSAGE_DRAW_CURRENT_PAGE = 0; // 绘制当前页面
        static final int MESSAGE_DRAW_CURRENT_PAGE_FOOT_SCHEDULE = 1; // 按时绘制当前页面底部信息
        static final int MESSAGE_DRAW_SELECTED_REGION = 2; // 按时绘制当前页面底部信息
        static final int MESSAGE_DRAW_ANIMATION_PAGES = 3; // 按时绘制当前页面底部信息
        long lastAnimationTime = -1;
        SurfaceHolder drawHolder;

        public DrawHandler(Looper looper) {
            super(looper);
        }

        public void setDrawHolder(SurfaceHolder drawHolder) {
            this.drawHolder = drawHolder;
        }

        @Override
        public void handleMessage(Message msg) {
            MyReadLog.i("MSG.what = " + msg.what);
            switch (msg.what) {
                case MESSAGE_DRAW_CURRENT_PAGE:
                    drawCurrentPage();
                    break;
                case MESSAGE_DRAW_CURRENT_PAGE_FOOT_SCHEDULE:

                    break;
                case MESSAGE_DRAW_SELECTED_REGION:
                    List<Rect> rects = (List<Rect>) msg.obj;
                    drawInternalSelectedRegion(rects);
                    break;
                case MESSAGE_DRAW_ANIMATION_PAGES:
                    drawAnimationPages();
                    break;

            }
        }


        /**
         * 绘制动画时的页面
         */
        private void drawAnimationPages() {
            Canvas canvas = null;
//                    MyReadLog.i("MESSAGE_DRAW_ANIMATION_PAGES");
            long currentStartTime = System.currentTimeMillis();
//            final BookDummyAbstractView view = ReaderApplication.getInstance().getDummyView();
            final BookDummyAbstractView view = BookControlCenter.Instance().getCurrentView();
            final AnimationProvider animator = getAnimationProvider();
            final AnimationProvider.Mode oldMode = animator.getMode();
            animator.doStep();
            long doStepTime = System.currentTimeMillis();
            long startLockTime = 0;
            long lockTime = 0;
            long unlockTime = 0;
            if (animator.inProgress()) {
                try {
                    synchronized (drawHolder) {
                        startLockTime = System.currentTimeMillis();
//                        canvas = drawHolder.lockCanvas();
                        canvas = getDrawCanvas(drawHolder);
                        lockTime = System.currentTimeMillis();
                        if (canvas != null) {
                            animator.draw(canvas);
                        }
                    }
                } catch (Exception e) {

                } finally {
                    if (canvas != null) {
                        drawHolder.unlockCanvasAndPost(canvas);
                    }
                }
                unlockTime = System.currentTimeMillis();
                if (animator.getMode().Auto) {
                    sendEmptyMessage(MESSAGE_DRAW_ANIMATION_PAGES);
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
//                        Canvas canvas = null;
                try {
                    synchronized (drawHolder) {
                        startLockTime = System.currentTimeMillis();
                        canvas = drawHolder.lockCanvas();
                        lockTime = System.currentTimeMillis();
                        if (canvas != null) {
                            canvas.drawBitmap(pageBitmapManager.getBitmap(PAGE_POSITION_INDEX_CURRENT), 0, 0, myPaint);
                        }
                    }
                } catch (Exception e) {

                } finally {
                    if (canvas != null) {
                        drawHolder.unlockCanvasAndPost(canvas);
                    }
                }
                unlockTime = System.currentTimeMillis();
            }
            long start = System.currentTimeMillis();
            if (lastAnimationTime != -1) {
                MyReadLog.i("动画之间的间距时间是 " + (start - lastAnimationTime) + ", 本次绘制耗时 " + (start - currentStartTime)
                        + " , 消息传递耗时 " + (currentStartTime - lastAnimationTime) + ", doStep = " + (doStepTime - currentStartTime)
                        + ", lockCanvas = " + (lockTime - startLockTime) + ", Unlock = " + (unlockTime - lockTime));
            }
            lastAnimationTime = start;

        }

        /**
         * 绘制选择高亮区域
         * @param rects
         */
        private void drawInternalSelectedRegion(List<Rect> rects) {
            Canvas canvas = null;
            if (rects == null) return;
            try {
                synchronized (drawHolder) {
                    canvas = drawHolder.lockCanvas(null);
//                          canvas.setBitmap(pageBitmapManager.getBitmap(0));
//                          canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                    canvas.drawBitmap(pageBitmapManager.getBitmap(PAGE_POSITION_INDEX_CURRENT), 0, 0, myPaint);

                    if (startCursorBitmap == null) {
                        startCursorBitmap = ((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.icon_reading_shared_selection_indicator_start)).getBitmap();
                        srcRect = new Rect(0, 0, startCursorBitmap.getWidth(), startCursorBitmap.getHeight());
                    }
                    if (endCursorBitmap == null) {
                        endCursorBitmap = ((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.icon_reading_shared_selection_indicator_end)).getBitmap();
                        srcRect = new Rect(0, 0, endCursorBitmap.getWidth(), endCursorBitmap.getHeight());
                    }

                    for (int i = 0; i < rects.size(); i++) {
                        Rect rect = rects.get(i);
                        canvas.drawRect(rect, selectedPaint);
                        if (i == 0) {
                            startCursorDestRect.left = rect.left - BookUIHelper.dp2px(8f);
                            startCursorDestRect.top = rect.top - BookUIHelper.dp2px(16f) * startCursorBitmap.getHeight() / startCursorBitmap.getWidth();
                            startCursorDestRect.right = rect.left + BookUIHelper.dp2px(8f);
                            startCursorDestRect.bottom = rect.top;
                            canvas.drawBitmap(startCursorBitmap, srcRect, startCursorDestRect, myPaint);
                        }
                        if (i == rects.size() - 1) {
                            endCursorDestRect.left = rect.right - BookUIHelper.dp2px(8f);
                            endCursorDestRect.top = rect.bottom;
                            endCursorDestRect.right = rect.right + BookUIHelper.dp2px(8f);
                            endCursorDestRect.bottom = rect.bottom + BookUIHelper.dp2px(16f) * endCursorBitmap.getHeight() / endCursorBitmap.getWidth();
                            canvas.drawBitmap(endCursorBitmap, srcRect, endCursorDestRect, myPaint);
                        }
                    }
                }
            } catch (Exception e) {

            } finally {
                drawHolder.unlockCanvasAndPost(canvas);
            }
        }


        /**
         * 绘制当前页面
         */
        private void drawCurrentPage() {
            Canvas canvas = null;
            try {
                synchronized (drawHolder) {
                    canvas = drawHolder.lockCanvas();
                    canvas.drawBitmap(pageBitmapManager.getBitmap(PAGE_POSITION_INDEX_CURRENT), 0, 0, myPaint);
                }
            } catch (Exception e) {

            } finally {
                if (canvas != null) {
                    drawHolder.unlockCanvasAndPost(canvas);
                }
            }
        }

        private Canvas getDrawCanvas(SurfaceHolder holder) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                return holder.lockHardwareCanvas();
            } else {
                return holder.lockCanvas();
            }
        }

        /**
         * 重置消息
         */
        public void reset() {
            removeMessages(MESSAGE_DRAW_CURRENT_PAGE);
            removeMessages(MESSAGE_DRAW_CURRENT_PAGE_FOOT_SCHEDULE);
            removeMessages(MESSAGE_DRAW_SELECTED_REGION);
            removeMessages(MESSAGE_DRAW_ANIMATION_PAGES);
        }
    }
}
