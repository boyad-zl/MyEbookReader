package com.example.epubreader.util;

import android.graphics.Paint;

import org.w3c.dom.CharacterData;

import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zjb on 2017/11/3.
 */

public class BookStingUtil {
    private final static SimpleDateFormat hourMinuteFormat = new SimpleDateFormat("HH:mm");
    public static final int SYMBOL_START = 0; // 开始的标点符号
    public static final int SYMBOL_END = 1;  // 结束的标点符号
    public static final int SYMBOL_NORMAL = 2; // 普通的标点符号
    public static final int SYMBOL_OTHER = 3; // 其他的标点符号（单独占一个位置的标点符号）
    public static final String HTML_REGEX = "^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\\\/])+$" ;


    /**
     * 获取MetData的值
     *
     * @param line       需要判断的字符串
     * @param s          值前面的标志符号
     * @param s1         值后面的表示符号
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
     *
     * @param number
     * @return
     */
    public static float getDoubleDecimalNumber(String number) {
        float result = 0;
        try {
            result = Float.valueOf(number);
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

    // 根据Unicode编码完美的判断中文汉字
    public static boolean isOnlyChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B) {
//            MyReadLog.i("char = " + c);
            return true;
        }
        return false;
    }

    /**
     * 判断字符是否是数字
     * @param c
     * @return
     */
    public static  boolean isNumber(char c) {
        if (c >= '0' && c <= '9'){
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断字符是否是字母
     * @param c
     * @return
     */
    public static boolean isLetter(char c) {
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')){
            return true;
        } else {
            return false;
        }
    }

    /**
     * 字符是否是中文标点符号
     * @param c
     * @return
     */
    public static boolean isOnlyChineseSymbols(char c) {
//        Character character = Character.valueOf(c);
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
//            MyReadLog.i("char = " + c);

            return true;
        }
        return false;
    }

    /**
     * 将时间转换成HH：mm格式
     *
     * @param time
     * @return
     */
    public static String getTimeStr(long time) {
        return hourMinuteFormat.format(time);
    }


    /**
     * 获得指定字体大小的字符串宽度
     *
     * @param fontSize
     * @param content  内容
     * @param start    开始位置
     * @param length   长度
     * @return
     */
    public static int getStringWidth(int fontSize, char[] content, int start, int length, Paint paint) {
//        paint.setTextSize(fontSize);
        return (int) paint.measureText(content, start, length);
    }

    /**
     * 判断字符串是否是网址
     * @param hrefChip
     * @return
     */
    public static boolean isWebUrl(String hrefChip) {
        boolean isURl ;
        Pattern pat = Pattern.compile(HTML_REGEX);
        Matcher mat = pat.matcher(hrefChip.trim());
        isURl = mat.matches();
        return isURl;
    }
}
