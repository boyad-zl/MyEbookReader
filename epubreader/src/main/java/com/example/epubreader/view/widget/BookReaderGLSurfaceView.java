package com.example.epubreader.view.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewConfiguration;

import com.example.epubreader.R;
import com.example.epubreader.ReaderApplication;
import com.example.epubreader.util.BookUIHelper;
import com.example.epubreader.util.MyReadLog;
import com.example.epubreader.view.book.BookDummyAbstractView;
import com.example.epubreader.view.book.BookViewEnums;
import com.example.epubreader.view.widget.animation.AnimationProvider;
import com.example.epubreader.view.widget.animation.cul.CurlAnimationProvider;
import com.example.epubreader.view.widget.animation.cul.pageflip.Page;
import com.example.epubreader.view.widget.animation.cul.pageflip.PageFlip;
import com.example.epubreader.view.widget.animation.cul.pageflip.PageFlipException;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.example.epubreader.view.widget.BookReaderGLSurfaceView.DrawHandler.MESSAGE_DRAW_ANIMATION_PAGES;


/**
 * Created by Boyad on 2018/1/8.
 */

public class BookReaderGLSurfaceView extends GLSurfaceView implements View.OnLongClickListener, BookReadListener, GLSurfaceView.Renderer {
    private Context mContext;
    private Paint myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint selectedPaint = new Paint();
    //    private PageBitmapManagerImpl pageBitmapManager = new PageBitmapManagerImpl(this);
    private PageBitmapManagerImpl pageBitmapManager;
    private HandlerThread drawHandlerThread;
    private DrawHandler drawHandler;

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
    private PageFlip mPageFlip;
    private ReentrantLock mLock;
    private final static int CUL_ANIMATION_DURATION = 600;


    public BookReaderGLSurfaceView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public BookReaderGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public void setPageBitmapManager(PageBitmapManagerImpl pageBitmapManager) {
        this.pageBitmapManager = pageBitmapManager;
    }

    private void init() {
        MyReadLog.i("view init !!!");
        drawHandler = new DrawHandler();
        mLock = new ReentrantLock();
        mPageFlip = new PageFlip(mContext);
        mPageFlip.setSemiPerimeterRatio(0.8f)
                .setMaskAlphaOfFold(0xF0)
                .setWidthRatioOfClickToFlip(0.5f)
                .setShadowWidthOfFoldEdges(5, 60, 0.3f)
                .setShadowWidthOfFoldBase(5, 80, 0.4f)
                .setPixelsOfMesh(10)
                .enableAutoPage(true);
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

//        pageBitmapManager = new PageBitmapManagerImpl();
        selectedPaint.setColor(Color.argb(0x58, 0, 0, 0xff));

        setFocusableInTouchMode(true);
        setDrawingCacheEnabled(false);
        setOnLongClickListener(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        try {
            mPageFlip.onSurfaceCreated();
        } catch (PageFlipException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        try {
            mPageFlip.onSurfaceChanged(width, height);
            pageBitmapManager.setSize(width, height);
            MyReadLog.i(" Render ---> onSurfaceChanged");
        } catch (PageFlipException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        try {
            mLock.lock();
//            MyReadLog.i("onDrawFrame");
            final AnimationProvider animator = getAnimationProvider();
            if (animator.isProgress()) {
//                MyReadLog.i("drawScrolling");
                final BookDummyAbstractView view = ReaderApplication.getInstance().getDummyView();
                final AnimationProvider.Mode oldMode = animator.getMode();
//                MyReadLog.i("Mode ："+oldMode);
                animator.doStep();
                if (animator.inProgress()) {
                    animator.draw(null);
                    if (animator.getMode().Auto) {
                        // TODO TEST 让其重画
                        drawHandler.sendEmptyMessage(MESSAGE_DRAW_ANIMATION_PAGES);
                    }
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
                    onDrawStatic();
                }
            } else {
                onDrawStatic();
            }
        } finally {
            mLock.unlock();
        }
    }

    private void onDrawStatic() {
//        MyReadLog.i("onDrawStatic!");
        mPageFlip.deleteUnusedTextures();
        Page page = mPageFlip.getFirstPage();
        Bitmap currentBitmap = pageBitmapManager.getBitmap(PAGE_POSITION_INDEX_CURRENT);
        if (currentBitmap != null) {
            page.setFirstTexture(currentBitmap);
            mPageFlip.drawPageFrame();
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
            final BookDummyAbstractView view = ReaderApplication.getInstance().getDummyView();
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
        BookDummyAbstractView dummyView = ReaderApplication.getInstance().getDummyView();
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
        BookDummyAbstractView dummyView = ReaderApplication.getInstance().getDummyView();
        return dummyView.onFingerLongPress(myPressedX, myPressedY);
    }

    @Override
    public void startManualScrolling(int x, int y, BookViewEnums.Direction direction) {
//        MyReadLog.i("startManualScrolling");
        final AnimationProvider animator = getAnimationProvider();
//		animator.setup(direction, getWidth(), getMainAreaHeight(), myColorLevel);
        animator.setup(direction, getWidth(), getHeight(), null);
        animator.startManualScrolling(x, y);
        mPageFlip.onFingerDown(x, y);
    }

    @Override
    public void scrollManuallyTo(int x, int y) {
        final BookDummyAbstractView dummyView = ReaderApplication.getInstance().getDummyView();
        final AnimationProvider animator = getAnimationProvider();
        int pageIndex = animator.getPageToScrollTo(x, y);
//        MyReadLog.i("scrollManuallyTo");
        if (dummyView.canScroll(pageIndex == PAGE_POSITION_INDEX_NEXT)) {
            animator.scrollTo(x, y);
//            if (animator.inProgress()) {
//                drawHandler.sendEmptyMessage(BookReaderSurfaceView.DrawHandler.MESSAGE_DRAW_ANIMATION_PAGES);
//            } else {
//                drawHandler.sendEmptyMessage(BookReaderSurfaceView.DrawHandler.MESSAGE_DRAW_CURRENT_PAGE);
//            }
        }
        if (mPageFlip.onFingerMove(x, y)) {
            try {
                mLock.lock();
                requestRender();
            } finally {
                mLock.unlock();
            }
        }
    }

    @Override
    public void startAnimatedScrolling(int x, int y) {
        final BookDummyAbstractView dummyView = ReaderApplication.getInstance().getDummyView();
        final AnimationProvider animation = getAnimationProvider();
        if (!dummyView.canScroll(animation.getPageToScrollTo(x, y) == PAGE_POSITION_INDEX_NEXT)) {
            animation.terminate();
            return;
        }
        MyReadLog.i("startAnimatedScrolling ==== x   y ");
        animation.startAnimatedScrolling(x, y, 25);
//        if (!mPageFlip.isAnimating()) {
            mPageFlip.onFingerUp(x, y, CUL_ANIMATION_DURATION);
            try {
                mLock.lock();
                requestRender();
            } finally {
                mLock.unlock();
            }
//        }
//        if (animation.inProgress()) {
//            drawHandler.sendEmptyMessage(BookReaderSurfaceView.DrawHandler.MESSAGE_DRAW_ANIMATION_PAGES);
//        } else {
//            drawHandler.sendEmptyMessage(BookReaderSurfaceView.DrawHandler.MESSAGE_DRAW_CURRENT_PAGE);
//        }
    }


    @Override
    public void startAnimatedScrolling(int pageIndex, int x, int y, BookViewEnums.Direction direction) {
        final BookDummyAbstractView dummyView = ReaderApplication.getInstance().getDummyView();
        if (pageIndex == PAGE_POSITION_INDEX_CURRENT || !dummyView.canScroll(pageIndex == PAGE_POSITION_INDEX_NEXT)) {
            return;
        }
        final AnimationProvider animator = getAnimationProvider();
//		animator.setup(direction, getWidth(), getMainAreaHeight(), myColorLevel);
        animator.setup(direction, getWidth(), getHeight(), null);
        animator.startAnimatedScrolling(pageIndex, x, y, 25);
        if (animator.getMode().Auto) {
//            postInvalidate();
//            if (!mPageFlip.isAnimating()) {

                try {
                    mLock.lock();
                    requestRender();
                } finally {
                    mLock.unlock();
                }
//            }
//            if (animator.inProgress()) {
//                drawHandler.sendEmptyMessage(BookReaderSurfaceView.DrawHandler.MESSAGE_DRAW_ANIMATION_PAGES);
//            } else {
//                drawHandler.sendEmptyMessage(BookReaderSurfaceView.DrawHandler.MESSAGE_DRAW_CURRENT_PAGE);
//            }
        }
    }

    @Override
    public void startAnimatedScrolling(int pageIndex, BookViewEnums.Direction direction) {
        final BookDummyAbstractView dummyView = ReaderApplication.getInstance().getDummyView();
        if (pageIndex == PAGE_POSITION_INDEX_CURRENT || !dummyView.canScroll(pageIndex == PAGE_POSITION_INDEX_NEXT)) {
            return;
        }
        final AnimationProvider animator = getAnimationProvider();
//		animator.setup(direction, getWidth(), getMainAreaHeight(), myColorLevel);
        animator.setup(direction, getWidth(), getHeight(), null);
        animator.startAnimatedScrolling(pageIndex, null, null, 15);
        if (animator.getMode().Auto) {
            if (animator.inProgress()) {
                drawHandler.sendEmptyMessage(BookReaderSurfaceView.DrawHandler.MESSAGE_DRAW_ANIMATION_PAGES);
            } else {
                drawHandler.sendEmptyMessage(BookReaderSurfaceView.DrawHandler.MESSAGE_DRAW_CURRENT_PAGE);
            }
        }
    }

    @Override
    public void repaint() {
        MyReadLog.i("repaint");
        try {
            mLock.lock();
            requestRender();
        } finally {
            mLock.unlock();
        }
    }

    public void drawOnBitmap(Bitmap bitmap, int pageIndex) {
        BookDummyAbstractView dummyView = ReaderApplication.getInstance().getDummyView();
        dummyView.paint(bitmap, pageIndex);
    }

    private AnimationProvider myAnimationProvider;
    private BookViewEnums.Animation myAnimationType;

    private AnimationProvider getAnimationProvider() {
        final BookViewEnums.Animation type = BookViewEnums.Animation.curl;
        if (myAnimationProvider == null || myAnimationType != type) {
            myAnimationType = type;
            switch (type) {
                case curl:
                    myAnimationProvider = new CurlAnimationProvider(pageBitmapManager);
                    ((CurlAnimationProvider) myAnimationProvider).setPageFlip(mPageFlip);
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

    class DrawHandler extends Handler {
        static final int MESSAGE_DRAW_ANIMATION_PAGES = 0; // 按时绘制当前页面底部信息

        public DrawHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
//            MyReadLog.i("MSG.what = " + msg.what);
            switch (msg.what) {
                case MESSAGE_DRAW_ANIMATION_PAGES:
//                    MyReadLog.i("animation pages draw");
                    try {
                        mLock.lock();
                        requestRender();
                    } finally {
                        mLock.unlock();
                    }
                    break;
                default:
                    break;
            }

        }

        /**
         * 重置消息
         */
        public void reset() {
            removeMessages(MESSAGE_DRAW_ANIMATION_PAGES);
        }
    }
}
