package com.example.epubreader.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zjb on 2017/11/3.
 */

public class BookStingUtil {
    private static final String REG_EX = "[\\w]"; // 用于正则匹配数字或者字母

    /**
     * 获取MetData的值
     *
     * @param line 需要判断的字符串
     * @param s    值前面的标志符号
     * @param s1   值后面的表示符号
     * @param startIndex 开始查找的位置
     */
    public static String getDataValue(String line, String s, String s1, int startIndex) {
        String result;
        int firstIndex = line.indexOf(s, startIndex);
        if (firstIndex < 0) return "";
        int secondIndex = line.indexOf(s1, firstIndex + 1);
        if (secondIndex < 0) return "";
        result = line.substring(firstIndex + 1, secondIndex);
        return result;
    }

    /**
     * 将字符串转换成两位小数
     * @param number
     * @return
     */
    public static float getDoubleDecimalNumber(String number){
        float result = 0;
        try {
            result = Float.valueOf(number) ;
            int result100 = (int) (result * 100);
            result = (float) result100 / 100;
        } catch (NumberFormatException e) {
            result = 0;
        }
        return result;
    }

    // 根据Unicode编码完美的判断中文汉字和符号
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    // 根据Unicode编码完美的判断中文汉字和符号
    public static boolean isOnlyChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
            return true;
        }
        return false;
    }

    /**
     * 校验某个字符是否是a-z、A-Z、_、0-9
     *
     * @param c 被校验的字符
     * @return true 代表符合条件
     */
    public static boolean isWord(char c) {
        Pattern p = Pattern.compile(REG_EX);
        Matcher m = p.matcher("" + c);
        return m.matches();
    }

}
