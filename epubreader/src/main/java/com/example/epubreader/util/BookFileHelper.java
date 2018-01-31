package com.example.epubreader.util;

import android.os.Environment;

import com.example.epubreader.ReaderApplication;

import java.io.File;

/**
 * Created by Boyad on 2018/1/15.
 */

public class BookFileHelper {
    public final static String PATH_BASE = getSDPath() + "/LittleFurBook";

    public final static String PATH_COVER = PATH_BASE + "/cover";

    private static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
            return sdDir.getPath();
        } else {
            return ReaderApplication.getInstance().getCacheDir().getAbsolutePath();
        }
    }
}
