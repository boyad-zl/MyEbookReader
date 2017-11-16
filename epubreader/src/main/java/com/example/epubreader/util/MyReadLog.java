package com.example.epubreader.util;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * 日志工具类
 * 
 * @author zhangsl
 * 
 */
public class MyReadLog {
	private static final String TAG = "MyReader";

	/**
	 * debug info (only log default info include method and line number)
	 */
	public static void d() {
		if (MyReadSetting.LOG_ENABLE) {
			Log.d(TAG, buildMessage(false, ""));
		}
	}

	/**
	 * debug info
	 * @param msg
	 */
	public static void d(String msg) {
		if (MyReadSetting.LOG_ENABLE) {
			Log.d(TAG, buildMessage(false, msg));
		}
	}

	/**
	 * debug info
	 * @param formatStr
	 * @param values
	 */
	public static void d(String formatStr, Object... values) {
		if (MyReadSetting.LOG_ENABLE) {
			Log.d(TAG, buildMessage(false, String.format(formatStr, values)));
		}
	}
	
	/**
	 * debug stack info
	 * @param formatStr
	 * @param values
	 */
	public static void dStack(String formatStr, Object... values) {
		if (MyReadSetting.LOG_ENABLE) {
			Log.d(TAG, buildMessage(true, String.format(formatStr, values)));
		}
	}

	/**
	 * normal info
	 * @param msg
	 */
	public static void i(String msg) {
		if (MyReadSetting.LOG_ENABLE) {
			Log.i(TAG, buildMessage(false, msg));
		}
	}

	/**
	 * error info
	 * @param msg
	 * @param error
	 */
	public static void e(String msg, Throwable error) {
		if (MyReadSetting.LOG_ENABLE) {
			Log.e(TAG, buildMessage(false, msg), error);
		}
	}

	/**
	 * warning info
	 * @param msg
	 */
	public static void w(String msg) {
		if (MyReadSetting.LOG_ENABLE) {
			Log.w(TAG, buildMessage(false, msg));
		}
	}

	/**
	 * 构建日志信息
	 * @param logStack  是否打印当前线程的堆栈信息
	 * @param msg       日志文本
	 * @return
	 */
	private static String buildMessage(boolean logStack, String msg) {
		StackTraceElement caller = new Throwable().fillInStackTrace().getStackTrace()[2];
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		
		StringBuilder sb = new StringBuilder();
		String className = caller.getClassName();
		className = className.substring(className.lastIndexOf(".") + 1);
		sb.append(className).append(".")
				.append(caller.getMethodName()).append("(): [")
				.append(caller.getLineNumber() + "] ").append(msg).append("\n");

		if (logStack) {
			for (StackTraceElement ste : stackTraceElements) {
				sb.append(ste).append("\n");
			}
		}
		return sb.toString();
	}

    /**
     * 写入log到文件(MyReadSetting.LOG_DEBUG为false时直接返回，即上线时不写日志到本地文件中)
     */
    public static void writetLogToFile(final List<String> strList, final String fileName, final boolean isAppend) {
        if (strList == null || strList.isEmpty() || !MyReadSetting.LOG_ENABLE) return;
        new Thread() {
            public void run() {
                File file = new File(MyReadSetting.FILE_PATH.PATH_LOG);
                if (!file.exists()) {
                    file.mkdirs();
                }
                String fname = fileName;
                if (TextUtils.isEmpty(fileName)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                    fname = sdf.format(new Date()) + ".log";
                }
                FileWriter fw = null;
                try {
                    fw = new FileWriter(new File(file.getAbsolutePath() + "/" + fname), isAppend);
                    int size = strList.size();
                    for (int i = 0; i < size; i++) {
                        fw.append(strList.get(i)).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fw != null) {
                        try {
                            fw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();

    }
}
