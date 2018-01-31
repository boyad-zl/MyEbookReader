package com.example.epubreader;

import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.example.epubreader.book.BookModel;
import com.example.epubreader.db.LibraryShadow;
import com.example.epubreader.util.BookUIHelper;
import com.example.epubreader.util.MyReadLog;
import com.example.epubreader.view.book.BookDummyAbstractView;
import com.example.epubreader.view.book.BookDummyView;
import com.example.epubreader.view.book.BookReadPosition;
import com.example.epubreader.view.widget.BookReadListener;
import com.example.epubreader.view.widget.PageBitmapManagerImpl;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.io.File;
import java.io.FileFilter;
import java.lang.ref.SoftReference;
import java.util.regex.Pattern;

/**
 * Created by zjb on 2017/10/31.
 */

public class ReaderApplication extends Application {
    private static ReaderApplication sInstance;
    private BookModel bookModel;
    private BookDummyAbstractView dummyView;
    //    private BookReadListener myWidget;
    private DisplayMetrics dm;
    private int coreNumber; // 手机CPU的数量
    private RefWatcher watcher;
    private SoftReference<BookReadListener> myWidget;
    private PageBitmapManagerImpl pageBitmapManager;
    private LibraryShadow libraryShadow;

    /**
     * 初始化
     */
    @Override
    public void onCreate() {
        super.onCreate();
        MyReadLog.i("Application init:UId=" + Process.myUid() + ", Pid = " + Process.myPid() + ", Tid = " + Process.myTid());
        if (sInstance == null) {
            sInstance = this;
            watcher = LeakCanary.install(this);
            coreNumber = getNumCores();
            MyReadLog.i("coreNumber = " + coreNumber);
            libraryShadow = new LibraryShadow(this);
        }
    }

    /**
     * todo test获取手机CPU核数
     *
     * @return
     */
    private int getNumCores() {
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }
        try {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(new CpuFilter());
            return files.length;
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * 获取屏幕宽高
     *
     * @return
     */
    public DisplayMetrics getWindowSize() {
        if (dm == null) {
            dm = new DisplayMetrics();
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(dm);
        }
        return dm;
    }

    public static ReaderApplication getInstance() {
        return sInstance;
    }

    public RefWatcher getWatcher() {
        return watcher;
    }

    public LibraryShadow getLibraryShadow() {
        return libraryShadow;
    }

    public void resetSize(DisplayMetrics dm) {
        this.dm = dm;
    }
}
