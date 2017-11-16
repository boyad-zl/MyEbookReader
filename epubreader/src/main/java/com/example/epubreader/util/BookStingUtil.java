package com.example.epubreader.util;

/**
 * Created by zjb on 2017/11/3.
 */

public class BookStingUtil {

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
}
