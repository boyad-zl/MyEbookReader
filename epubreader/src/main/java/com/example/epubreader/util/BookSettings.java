package com.example.epubreader.util;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.TextUtils;

import com.example.epubreader.ReaderApplication;

/**
 * SharedPreference配置存取类（保存可能会更新的配置）
 * @author zhangsl
 *
 */
public class BookSettings {
    private static final String SETTING_NAME = "book_settings"; // 配置名

    private static final String READ_BOOK_POSITION = "read_book_position"; // 标识手机的唯一id


    /**
     * 从SharedPreferences中删除指定的key
     * @param key
     */
    private static void removeKey(String key) {
    	if (TextUtils.isEmpty(key)) return;
    	SharedPreferences pref = ReaderApplication.getInstance().getSharedPreferences(SETTING_NAME, 0);
        Editor editor = pref.edit();
        editor.remove(key);
        editor.commit();
    }
    
    /**
     * 向SharedPreferences中写入String值
     * @param key
     * @param value
     */
    private static void setString(String key, String value) {
        SharedPreferences pref = ReaderApplication.getInstance().getSharedPreferences(SETTING_NAME, 0);
        Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }
    
    /**
     * 从SharedPreferences中读取String值，提供默认值
     * @param key
     * @param defaultValue
     * @return
     */
    private static String getString(String key, String defaultValue) {
        SharedPreferences pref = ReaderApplication.getInstance().getSharedPreferences(SETTING_NAME, 0);
        return pref.getString(key, defaultValue);
    }

    /**
     * 从SharedPreferences中读取String值
     * @param key
     * @return
     */
    private static String getString(String key) {
        return getString(key, "");
    }
    
    /**
     * 向SharedPreferences中写入Boolean值
     * @param key
     * @param value
     */
	private static void setBoolean(String key, boolean value) {
        SharedPreferences pref = ReaderApplication.getInstance().getSharedPreferences(SETTING_NAME, 0);
        Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    
    /**
     * 从SharedPreferences中读取Boolean值，提供默认值
     * @param key
     * @param defaultValue
     * @return
     */
    private static boolean getBoolean(String key, boolean defaultValue) {
        SharedPreferences pref = ReaderApplication.getInstance().getSharedPreferences(SETTING_NAME, 0);
        return pref.getBoolean(key, defaultValue);
    }
    
    /**
     * 从SharedPreferences中读取Boolean值
     * @param key
     * @return
     */
	private static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }
    
    /**
     * 向SharedPreferences中写入int值
     * @param key
     * @param value
     */
    private static void setInt(String key, int value) {
        SharedPreferences pref = ReaderApplication.getInstance().getSharedPreferences(SETTING_NAME, 0);
        Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.commit();
    }
    
    /**
     * 从SharedPreferences中读取int值
     * @param key
     * @return
     */
	private static int getInt(String key) {
        SharedPreferences pref = ReaderApplication.getInstance().getSharedPreferences(SETTING_NAME, 0);
        return pref.getInt(key, 0);
    }
    
    /**
     * 从SharedPreferences中读取int值，提供默认值
     * @param key
     * @param defaultValue
     * @return
     */
	private static int getInt(String key, int defaultValue) {
        SharedPreferences pref = ReaderApplication.getInstance().getSharedPreferences(SETTING_NAME, 0);
        return pref.getInt(key, defaultValue);
    }
    
    /**
     * 从SharedPreferences中读取long值，提供默认值
     * @param key
     * @param defaultValue
     * @return
     */
	private static long getLong(String key, long defaultValue) {
        SharedPreferences pref = ReaderApplication.getInstance().getSharedPreferences(SETTING_NAME, 0);
        return pref.getLong(key, defaultValue);
    }
    
    /**
     * 向SharedPreferences中写入long值
     * @param key
     * @param value
     */
	private static void setLong(String key, long value) {
        SharedPreferences pref = ReaderApplication.getInstance().getSharedPreferences(SETTING_NAME, 0);
        Editor editor = pref.edit();
        editor.putLong(key, value);
        editor.commit();
    }
    
    /**
	 * 获取manifest中的metaData
	 * @param metaKey
	 * @return
	 */
    public static String getMetaValue(String metaKey) {
        Bundle metaData = null;
        String apiKey = null;
        if (metaKey == null) {
        	return null;
        }
        try {
            ApplicationInfo ai = ReaderApplication.getInstance().getPackageManager().getApplicationInfo(
            		ReaderApplication.getInstance().getPackageName(), PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
            	apiKey = metaData.getString(metaKey);
            }
        } catch (NameNotFoundException e) {

        }
        return apiKey;
    }

    public static void setReadPosition(String positionStr) {
        setString(READ_BOOK_POSITION, positionStr);
    }

    public static String getReadBookPosition(){
        return getString(READ_BOOK_POSITION, "");
    }
}
