package com.boyad.epubreader.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.boyad.epubreader.BookControlCenter;
import com.boyad.epubreader.BookReadControlCenter;
import com.boyad.epubreader.R;
import com.boyad.epubreader.ReaderApplication;
import com.boyad.epubreader.db.Book;
import com.boyad.epubreader.util.BookAttributeUtil;
import com.boyad.epubreader.util.BookBrightUtil;
import com.boyad.epubreader.util.BookConstant;
import com.boyad.epubreader.util.BookContentDrawHelper;
import com.boyad.epubreader.util.BookSettings;
import com.boyad.epubreader.util.BookUIHelper;
import com.boyad.epubreader.util.MyReadLog;
import com.boyad.epubreader.util.StringWidthMeasureHelper;
import com.boyad.epubreader.view.widget.BookReadListener;
import com.boyad.epubreader.view.widget.BookReaderGLSurfaceView;
import com.boyad.epubreader.view.widget.BookReaderView;
import com.boyad.epubreader.view.widget.ControlCenterWindow;
import com.boyad.epubreader.view.widget.ReaderMenuDialog;
import com.boyad.epubreader.view.widget.SelectionPanelDialog;

import java.util.Arrays;
import java.util.List;

public class ReaderActivity extends AppCompatActivity implements View.OnClickListener, ControlCenterWindow {
    //    private BookModel bookModel;
    //    private BookReaderSurfaceView reader;
    private BookReaderView reader;
    private BookReaderGLSurfaceView glReader;
    private boolean isCul;
    private TextView culBtn;
    private BroadcastReceiver mBroadcastReceiver;
    private BookReadControlCenter controlCenter;
    private Book book;
    private volatile BookReadListener currentListener;
    private ReaderMenuDialog menuDialog;
    private int batteryLevel;
    private PowerManager.WakeLock mWakeLock;
    private boolean mWakeLockToCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyReadLog.i("==========onCreate===========");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_reader);
        boolean isHaveNavigationBar = BookUIHelper.isNavigationBarShowing();
        MyReadLog.i("isHaveNavigationBar = " + isHaveNavigationBar);
        BookContentDrawHelper.setNavigationBarHeight(isHaveNavigationBar ? BookUIHelper.getNavigationBarHeight() : 0);
        final Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                MyReadLog.i("visibility = " + visibility);
                if (visibility == 0) {
                    MyReadLog.i("状态栏显示");
                } else {
                    MyReadLog.i("状态栏隐藏");
                    int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE|
                            //布局位于状态栏下方
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|
                            //全屏
                            View.SYSTEM_UI_FLAG_FULLSCREEN|
                            //隐藏导航栏
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                    if (Build.VERSION.SDK_INT>=19){
                        uiOptions |= 0x00001000;
                    }else{
                        uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
                    }
                    window.getDecorView().setSystemUiVisibility(uiOptions);
                }
            }
        });

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        int newUiOptions = getWindow().getDecorView().getSystemUiVisibility();
//        newUiOptions ^= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
//        if (Build.VERSION.SDK_INT >= 14) newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//        if (Build.VERSION.SDK_INT >= 16) {
//            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
//            newUiOptions ^= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
//        }
//        if (Build.VERSION.SDK_INT >= 19){
//            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE;
//        }

        ReaderApplication.getInstance().getWatcher().watch(this);
        // 初始化绘制工具类，赋予基础参数
        initReceiver();
        initBookContentDrawHelper();
        book = getIntent().getParcelableExtra("BOOK");
        culBtn = (TextView) findViewById(R.id.btn_turn_cul);
        culBtn.setOnClickListener(this);
//        reader = (BookReaderSurfaceView) findViewById(R.id.main_reader_view);
        reader = (BookReaderView) findViewById(R.id.main_reader_view);
        glReader = (BookReaderGLSurfaceView) findViewById(R.id.main_reader_gl_view);
        currentListener = reader;

        controlCenter = (BookReadControlCenter) BookReadControlCenter.Instance();
        if (controlCenter == null) {
            controlCenter = new BookReadControlCenter();
        }
        reader.setPageBitmapManager(controlCenter.getPageBitmapManager());
        glReader.setPageBitmapManager(controlCenter.getPageBitmapManager());
        controlCenter.setWindow(this);
        controlCenter.openBook(book);
    }

    private void initBookContentDrawHelper() {
//        Typeface typeface = Typeface.createFromAsset(getAssets(), "goodNight.ttf");
        Typeface typeface = Typeface.createFromAsset(getAssets(), "FZYouH.ttf");
        StringWidthMeasureHelper.setMeasureTypeface(typeface);
        BookAttributeUtil.setEmSize(BookSettings.getFontSize());
        boolean dayModel = BookSettings.getDayModel();
        BookContentDrawHelper.setDayModel(dayModel);

        boolean isBrightAuto;
        int brightness;
        if (dayModel) {
            isBrightAuto = BookSettings.isBrightnessAutoDay();
            brightness = BookSettings.getBrightnessDay();
        } else {
            isBrightAuto = BookSettings.isBrightnessAutoNight();
            brightness = BookSettings.getBrightnessNight();
        }
        if (isBrightAuto) {
            BookBrightUtil.startAutoBrightness(this);
        } else {
            BookBrightUtil.setBrightness(this, brightness);
        }

        BookContentDrawHelper.setFontBgTheme(BookSettings.getTheme());

        boolean screenDirectionPortrait = BookSettings.isScreenDirection();
        if (!screenDirectionPortrait) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    /**
     * 初始化
     */
    private void initReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                onReceiveBroadcast(action, intent.getExtras());
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        List<String> needAddActions = getReceiverFilterActions();
        if (needAddActions != null) {
            int size = needAddActions.size();
            for (int i = 0; i < size; i++) {
                intentFilter.addAction(needAddActions.get(i));
            }
        }
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void onReceiveBroadcast(String action, Bundle extras) {
        switch (action) {
            case Intent.ACTION_BATTERY_CHANGED:
                int level = extras.getInt("level", 0);
                int scale = extras.getInt("scale", 100);
                batteryLevel = (level * 100 / scale);
                break;
        }
    }

    private List<String> getReceiverFilterActions() {
        return Arrays.asList(BookConstant.ACTION_CONFIG_OPTION_CHANGE, Intent.ACTION_BATTERY_CHANGED);
    }

    @Override
    protected void onResume() {
        super.onResume();

        isCul = BookSettings.getPageTurnAnimation();
        updateCulBtn();
        controlReadView(isCul);
        MyReadLog.i("==========onResume===========");

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void controlReadView(final boolean isCul) {

        if (isCul) {
            glReader.setVisibility(View.VISIBLE);
            currentListener = glReader;
            reader.setVisibility(View.GONE);
        } else {
            reader.setVisibility(View.VISIBLE);
            glReader.setVisibility(View.GONE);
            currentListener = reader;
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
//        reader.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
//        reader.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_turn_cul) {
            toggleCulAnimation();
        }
    }

    private void toggleCulAnimation() {
        BookControlCenter.Instance().getViewListener().repaint();
        isCul = !isCul;
        updateCulBtn();
        controlReadView(isCul);
//        ReaderApplication.getInstance().getMyWidget().repaint();
    }

    private void updateCulBtn() {
        culBtn.setTextColor(isCul ? Color.RED : Color.DKGRAY);
    }


    @Override
    public BookReadListener getViewListener() {
        return currentListener;
    }

    @Override
    public int getBatteryLevel() {
        return batteryLevel;
    }


    @Override
    public void close() {

    }

    @Override
    public void showMenu() {
        if (menuDialog == null) {
            menuDialog = new ReaderMenuDialog();
        }
        menuDialog.initWithContext(this).showDialog();
    }

    private SelectionPanelDialog selectionPanel;

    public void showSelectionPanel(List<Rect> rects) {
        MyReadLog.i("rects size is " + rects.size());
//        AlertDialog.Builder builder =  new AlertDialog.Builder(this);
//        AlertDialog dialog = builder.setView(LayoutInflater.from(this).inflate(R.layout.selection_panel_view, null)).create();
//        dialog.show();
        if (selectionPanel == null) {
            selectionPanel = new SelectionPanelDialog();
            selectionPanel.initWithContext(this);
        }
        if (selectionPanel.isDialogShowing()) {
            selectionPanel.updateSelections(rects);
        } else {
            selectionPanel.showDialog();
        }
    }

    public final void createWakeLock() {
        if (mWakeLockToCreate) {
            synchronized (this) {
                if (mWakeLockToCreate) {
                    mWakeLockToCreate = false;
                    mWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyReader");
                    mWakeLock.acquire();
                }
            }
        }
    }

    private final void switchWakeLock(boolean on) {
        if (on) {
            if (mWakeLock == null) {
                mWakeLockToCreate = true;
            }
        } else {
            if (mWakeLock != null) {
                synchronized (this) {
                    if (mWakeLock != null) {
                        mWakeLock.release();
                        mWakeLock = null;
                    }
                }
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        switchWakeLock(hasFocus);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        MyReadLog.i("onConfigurationChanged!!!  height " + dm.heightPixels + ", width = " + dm.widthPixels);
        ReaderApplication.getInstance().resetSize(dm);
    }
}
