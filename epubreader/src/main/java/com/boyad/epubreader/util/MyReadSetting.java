package com.boyad.epubreader.util;

import android.os.Environment;

import com.boyad.epubreader.BuildConfig;

import java.io.File;

/**
 * Created by zjb on 2017/10/31.
 */

public class MyReadSetting {
    public static final boolean LOG_ENABLE = BuildConfig.LOG_ENABLE;

    public static final class FILE_PATH {
        public static final String PATH_BASE = getSDPath() + "/MyBookReader";

        public static final String PATH_LOG = PATH_BASE + "/MyBookReader";

        /**
         * 获取根目录
         * @return
         */
        private static String getSDPath() {
            File sdDir = null;
            boolean isSDCardExit = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
            if (isSDCardExit){
                sdDir = Environment.getExternalStorageDirectory();
            } else {
                return  "";
            }

            return sdDir.getPath();
        }
    }
}
