package com.example.epubreader.util;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.example.epubreader.ReaderApplication;

/**
 * SharedPreference配置存取类（保存可能会更新的配置）
 *
 * @author zhangsl
 */
public class BookSettings {
    private static final String SETTING_NAME = "book_settings"; // 配置名

    private static final String READ_BOOK_POSITION = "read_book_position";
    public static final String READ_PAGE_TURN_ANIMATION_CUL = "read_page_turn_animation_cul"; // 是否是仿真翻页


    /**
     * 从SharedPreferences中删除指定的key
     *
     * @param key
     */
    private static void removeKey(String key) {
        if (TextUtils.isEmpty(key)) return;
        SharedPreferences pref = ReaderApplication.getInstance().getSharedPreferences(SETTING_NAME, 0);
        Editor editor = pref.edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * 向SharedPreferences中写入String值
     *
     * @param key
     * @param value
     */
    private static void setString(String key, String value) {
        SharedPreferences pref = ReaderApplication.getInstance().getSharedPreferences(SETTING_NAME, 0);
        Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * 从SharedPreferences中读取String值，提供默认值
     *
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
     *
     * @param key
     * @return
     */
    private static String getString(String key) {
        return getString(key, "");
    }

    /**
     * 向SharedPreferences中写入Boolean值
     *
     * @param key
     * @param value
     */
    private static void setBoolean(String key, boolean value) {
        SharedPreferences pref = ReaderApplication.getInstance().getSharedPreferences(SETTING_NAME, 0);
        Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * 从SharedPreferences中读取Boolean值，提供默认值
     *
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
     *
     * @param key
     * @return
     */
    private static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * 向SharedPreferences中写入int值
     *
     * @param key
     * @param value
     */
    private static void setInt(String key, int value) {
        SharedPreferences pref = ReaderApplication.getInstance().getSharedPreferences(SETTING_NAME, 0);
        Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * 从SharedPreferences中读取int值
     *
     * @param key
     * @return
     */
    private static int getInt(String key) {
        SharedPreferences pref = ReaderApplication.getInstance().getSharedPreferences(SETTING_NAME, 0);
        return pref.getInt(key, 0);
    }

    /**
     * 从SharedPreferences中读取int值，提供默认值
     *
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
     *
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
     *
     * @param key
     * @param value
     */
    private static void setLong(String key, long value) {
        SharedPreferences pref = ReaderApplication.getInstance().getSharedPreferences(SETTING_NAME, 0);
        Editor editor = pref.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static void setReadPosition(String positionStr) {
        setString(READ_BOOK_POSITION, positionStr);
    }

    public static String getReadBookPosition() {
        return getString(READ_BOOK_POSITION, "");
    }

    public static boolean getPageTurnAnimation() {
        return getBoolean(READ_PAGE_TURN_ANIMATION_CUL, false);
    }

    public static void setPageTurnAnimation(boolean isCul) {
        setBoolean(READ_PAGE_TURN_ANIMATION_CUL, isCul);
    }

    public static void setFontSize(int fontSize) {
        setInt(BookConstant.CONFIG_NAME_FONT_SIZE, fontSize);
    }

    public static int getFontSize() {
        return getInt(BookConstant.CONFIG_NAME_FONT_SIZE, BookConstant.CONFIG_VALUE_DEFAULT_FONT_SIZE);
    }

    public static void setDayModel(boolean isDayModel) {
        setBoolean(BookConstant.CONFIG_NAME_DAY_MODEL, isDayModel);
    }

    public static boolean getDayModel() {
        return getBoolean(BookConstant.CONFIG_NAME_DAY_MODEL, BookConstant.CONFIG_VALUE_DEFAULT_DAY_MODEL);
    }

    public static void setTurnAnimationType(String type){
        setString(BookConstant.CONFIG_NAME_ANIMATION_TYPE, type);
    }

    public static String getTurnAnimationType() {
        return getString(BookConstant.CONFIG_NAME_ANIMATION_TYPE, BookConstant.CONFIG_VALUE_DEFAULT_ANIMATION_TYPE);
    }

    public static void setBrightnessDay(int brightness) {
        setInt(BookConstant.CONFIG_NAME_BRIGHTNESS_DAY, brightness);
    }

    public static int getBrightnessDay() {
        return getInt(BookConstant.CONFIG_NAME_BRIGHTNESS_DAY, BookConstant.CONFIG_VALUE_DEFAULT_BRIGHTNESS_DAY);
    }

    public static void setBrightnessNight(int brightness) {
        setInt(BookConstant.CONFIG_NAME_BRIGHTNESS_NIGHT, brightness);
    }

    public static int getBrightnessNight() {
        return getInt(BookConstant.CONFIG_NAME_BRIGHTNESS_NIGHT, BookConstant.CONFIG_VALUE_DEFAULT_BRIGHTNESS_NIGHT);
    }

    public static void setTheme(int theme) {
        setInt(BookConstant.CONFIG_NAME_THEME, theme);
    }

    public static int getTheme(){
        return getInt(BookConstant.CONFIG_NAME_THEME,BookConstant.CONFIG_VALUE_DEFAULT_THEME_TYPE);
    }

    public static void setCustomFontColor(int color) {
        setInt(BookConstant.CONFIG_NAME_CUSTOM_FONT_COLOR, color);
    }

    public static int getCustomFontColor() {
        return getInt(BookConstant.CONFIG_NAME_CUSTOM_FONT_COLOR, BookConstant.CONFIG_VALUE_DEFAULT_CUSTOM_FONT_COLOR);
    }

    public static void setCustomBgColor(int color) {
        setInt(BookConstant.CONFIG_NAME_CUSTOM_BG_COLOR, color);
    }

    public static int getCustomBgColor() {
        return getInt(BookConstant.CONFIG_NAME_CUSTOM_BG_COLOR, BookConstant.CONFIG_VALUE_DEFAULT_CUSTOM_BG_COLOR);
    }

    public static void setScreenDirection(boolean isPortrait) {
        setBoolean(BookConstant.CONFIG_NAME_SCREEN_DIRECTION, isPortrait);
    }

    public static boolean isScreenDirection() {
        return getBoolean(BookConstant.CONFIG_NAME_SCREEN_DIRECTION, BookConstant.CONFIG_VALUE_DEFAULT_SCREEN_DIRECTION_PORTRAIT);
    }

    public static boolean isBrightnessAutoDay() {
        return getBoolean(BookConstant.CONFIG_NAME_BRIGHTNESS_AUTO_DAY, BookConstant.CONFIG_VALUE_DEFAULT_BRIGHTNESS_AUTO_DAY);
    }

    public static void setBrightnessAutoDay(boolean isAuto) {
        setBoolean(BookConstant.CONFIG_NAME_BRIGHTNESS_AUTO_DAY, isAuto);
    }

    public static boolean isBrightnessAutoNight() {
        return getBoolean(BookConstant.CONFIG_NAME_BRIGHTNESS_AUTO_NIGHT, BookConstant.CONFIG_VALUE_DEFAULT_BRIGHTNESS_AUTO_NIGHT);
    }

    public static void setBrightnessAutoNight(boolean isAuto) {
        setBoolean(BookConstant.CONFIG_NAME_BRIGHTNESS_AUTO_NIGHT, isAuto);
    }
}
