package com.boyad.epubreader.util;

/**
 * Created by Boyad on 2018/1/11.
 */

public class BookConstant {
    public static final String ACTION_CONFIG_OPTION_CHANGE = "bookReader.config_service.option_change_action";

    // 字体大小
    public static final String CONFIG_NAME_FONT_SIZE = "font_size";
    public static final int CONFIG_VALUE_DEFAULT_FONT_SIZE = 20;

    // 是否是黑夜模式
    public static final String CONFIG_NAME_DAY_MODEL = "day_model";
    public static final boolean CONFIG_VALUE_DEFAULT_DAY_MODEL = true;

    // 默认动画类型
    public static final String CONFIG_NAME_ANIMATION_TYPE = "animation_type";
    public static final String CONFIG_VALUE_DEFAULT_ANIMATION_TYPE = "slide";

    // 白天亮度信息
    public static final String CONFIG_NAME_BRIGHTNESS_DAY = "brightness_day";
    public static final int CONFIG_VALUE_DEFAULT_BRIGHTNESS_DAY = 150;

    // 黑夜亮度信息
    public static final String CONFIG_NAME_BRIGHTNESS_NIGHT = "brightness_night";
    public static final int CONFIG_VALUE_DEFAULT_BRIGHTNESS_NIGHT = 10;

    // 字体背景配置
    public static final String CONFIG_NAME_THEME = "theme";
    public static final int CONFIG_VALUE_DEFAULT_THEME_TYPE = 1;

    // 自定义字体颜色
    public static final String CONFIG_NAME_CUSTOM_FONT_COLOR = "custom_font_color";
    public static final int CONFIG_VALUE_DEFAULT_CUSTOM_FONT_COLOR = 0x000000;

    //自定义背景颜色
    public static final String CONFIG_NAME_CUSTOM_BG_COLOR = "custom_bg_color";
    public static final int CONFIG_VALUE_DEFAULT_CUSTOM_BG_COLOR = 0xFFFFFF;

    // 是否横屏
    public static final String CONFIG_NAME_SCREEN_DIRECTION = "screen_direction";
    public static final boolean CONFIG_VALUE_DEFAULT_SCREEN_DIRECTION_PORTRAIT = true;

    public static final String CONFIG_NAME_BRIGHTNESS_AUTO_DAY = "brightness_auto_day";
    public static final boolean CONFIG_VALUE_DEFAULT_BRIGHTNESS_AUTO_DAY = true;

    public static final String CONFIG_NAME_BRIGHTNESS_AUTO_NIGHT = "brightness_auto_night";
    public static final boolean CONFIG_VALUE_DEFAULT_BRIGHTNESS_AUTO_NIGHT = false;
}
