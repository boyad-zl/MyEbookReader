package com.example.epubreader.util;
import android.support.v4.util.ArrayMap;

import com.example.epubreader.ReaderApplication;
import com.example.epubreader.book.css.BookTagAttribute;

/**
 * 属性哦工具类
 * Created by Boyad on 2017/11/10.
 */

public class BookAttributeUtil {
    public static final byte POSITION_LEFT = 0;
    public static final byte POSITION_TOP = 1;
    public static final byte POSITION_RIGHT = 2;
    public static final byte POSITION_BOTTOM = 3;

    public static final byte TEXT_ALIGN_LEFT = 0;
    public static final byte TEXT_ALIGN_RIGHT = 1;
    public static final byte TEXT_ALIGN_CENTER = 2;
    public static final byte TEXT_ALIGN_JUSTIFY = 3;
    public static final byte TEXT_ALIGN_INHERIT = 4;

    private static final float LINE_HEIGHT_NORMAL = 1.2F;

    public static int ONE_EM_LENGTH ;
    public static int settingFontSize = 20;


    /**
     * 设置1em的大小
     * @param size
     */
    public static void setEmSize(int size) {
        settingFontSize = size;
        MyReadLog.i("settingFontSize = " + settingFontSize);
        ONE_EM_LENGTH = (int) (ReaderApplication.getInstance().getWindowSize().density * settingFontSize);
    }

    public static int getEmSize() {
       return settingFontSize;
    }

    /**
     * 获取长度信息
     *
     * @param lengthStr
     * @param lineWidth
     * @return
     */
    public static int getLength(String lengthStr, int lineWidth) {
        int size = ONE_EM_LENGTH;
        if (lengthStr.equals("auto")) {
            return -1;
        }
        try {
            if (lengthStr.endsWith("px")) {
                size = Integer.valueOf(lengthStr.substring(0, lengthStr.indexOf("px")));
            } else if (lengthStr.endsWith("em")) {
                String sizeStr = lengthStr.substring(0, lengthStr.indexOf("em"));
                size = (int) (Float.valueOf(sizeStr) * ONE_EM_LENGTH);
            } else if (lengthStr.endsWith("%")) {
                size = Integer.valueOf(lengthStr.substring(0, lengthStr.indexOf("%")));
                size = lineWidth * size / 100;
            } else {
                size = (int)(Float.valueOf(lengthStr) + 0);
            }
        } catch (NumberFormatException e) {
            size = ONE_EM_LENGTH;
        }
        return size;
    }

    /**
     * 获取字体大小
     *
     * @param
     * @return
     */
    public static int getFontSize(ArrayMap<String, BookTagAttribute> attributeArrayMap) {
        BookTagAttribute attribute = attributeArrayMap.get("font-size");
        if (attribute == null) {
            return getLength("1em", ONE_EM_LENGTH);
        } else {
            return getLength(attribute.valueStr, ONE_EM_LENGTH);
        }
    }


    /**
     * 获取首行缩进大小
     *
     * @param
     * @return
     */
    public static int getTextIndent(ArrayMap<String, BookTagAttribute> attributeArrayMap , int max, int fontSize) {
        BookTagAttribute attribute = attributeArrayMap.get("text-indent");
        if (attribute == null) {
            return 0;
        } else {
            int indexEM = attribute.valueStr.indexOf("em");
            if (indexEM > -1){
                try {
                    float size = Float.valueOf(attribute.valueStr.substring(0, indexEM));
                    return (int) (size * fontSize);
                }catch (NumberFormatException e) {
                }
            }
            return getLength(attribute.valueStr, max);
        }
    }

    /**
     * 获取margin属性
     * @param attributeArrayMap
     * @param position
     * @param max
     * @return
     */
    public static int getMargin(ArrayMap<String, BookTagAttribute> attributeArrayMap, byte position, int max) {
        BookTagAttribute attribute = null;
        switch (position) {
            case POSITION_TOP:
                attribute = attributeArrayMap.get("margin-top");
                break;
            case POSITION_LEFT:
                attribute = attributeArrayMap.get("margin-left");
                break;
            case POSITION_RIGHT:
                attribute = attributeArrayMap.get("margin-right");
                break;
            case POSITION_BOTTOM:
                attribute = attributeArrayMap.get("margin-bottom");
                break;
        }
        if (attribute == null) {
            return 0;
        } else {
            return getLength(attribute.valueStr, max);
        }

    }

    /**
     * 获取padding属性
     * @param attributeArrayMap
     * @param position
     * @param max
     * @return
     */
    public static int getPadding(ArrayMap<String, BookTagAttribute> attributeArrayMap, byte position, int max) {
        BookTagAttribute attribute = null;
        switch (position) {
            case POSITION_TOP:
                attribute = attributeArrayMap.get("padding-top");
                break;
            case POSITION_LEFT:
                attribute = attributeArrayMap.get("padding-left");
                break;
            case POSITION_RIGHT:
                attribute = attributeArrayMap.get("padding-right");
                break;
            case POSITION_BOTTOM:
                attribute = attributeArrayMap.get("padding-bottom");
                break;
        }
        if (attribute == null) {
            return 0;
        } else {
            return getLength(attribute.valueStr, max);
        }

    }

    /**
     * 获取宽度
     * @param attributeSet
     * @param pageWidth
     * @return
     */
    public static int getWidth(ArrayMap<String, BookTagAttribute> attributeSet, int pageWidth) {
        BookTagAttribute attribute = attributeSet.get("width");
        if (attribute != null) {
            if (attribute.valueStr.equals("auto")) return -1;
            return getLength(attribute.valueStr, pageWidth);
        }
        return -1;
    }

    /**
     * 获取宽度
     * @param attributeSet
     * @param pageHeight
     * @return
     */
    public static int getHeight(ArrayMap<String, BookTagAttribute> attributeSet, int pageHeight) {
        BookTagAttribute attribute = attributeSet.get("height");
        if (attribute != null) {
            if (attribute.valueStr.equals("auto")) return -1;
            return getLength(attribute.valueStr, pageHeight);
        }
        return -1;
    }

    /**
     * 获取text-align属性
     * @param attributeSet
     */
    public static byte getTextAlign(ArrayMap<String, BookTagAttribute> attributeSet) {
        BookTagAttribute attribute = attributeSet.get("text-align");
        if (attribute == null) return TEXT_ALIGN_JUSTIFY;
//        MyReadLog.i(attribute.valueStr);
        switch (attribute.valueStr){
            case "left":
                return TEXT_ALIGN_LEFT;
            case "right":
                return TEXT_ALIGN_RIGHT;
            case "center":
                return TEXT_ALIGN_CENTER;
            case "justify":
                return TEXT_ALIGN_JUSTIFY;
            case "inherit":
                return TEXT_ALIGN_INHERIT;
            default:
                return TEXT_ALIGN_JUSTIFY;
        }
    }

    /**
     * 获取line-height属性
     * @param attributeSet
     * @return
     */
    public static float getLineHeight(ArrayMap<String, BookTagAttribute> attributeSet) {
        float rate = LINE_HEIGHT_NORMAL;
//        if (true) return 2.0f;
        if (attributeSet == null) return rate;
        BookTagAttribute attribute = attributeSet.get("line-height");
        if (attribute == null) return rate;
        if (attribute.valueStr.equals("normal")) return rate;
        try {
            if (attribute.valueStr.endsWith("%")){
                rate = Float.valueOf(attribute.valueStr.substring(0, attribute.valueStr.indexOf("%"))) / 100 ;
            } else {
                rate = Float.valueOf(attribute.valueStr);
            }
        } catch (NumberFormatException e){
        }
        return rate;
    }

    /**
     * 获取字体加粗属性（font-weight）
     * normal 时weight = 400， bold时 weight = 800
     * @param attributeSet
     * @return
     */
    public static boolean getBold(ArrayMap<String, BookTagAttribute> attributeSet) {
        boolean needBold = false;
        if (attributeSet == null) return needBold;
        BookTagAttribute attribute = attributeSet.get("line-height");
        if (attribute != null) {
            if (attribute.valueStr.equals("normal")) {
                needBold = false;
            } else if (attribute.valueStr.equals("bold")) {
                needBold = true;
            } else {
                try {
                    float boldSize = Float.valueOf(attribute.valueStr);
                   needBold = boldSize > 400;
                }catch (NumberFormatException e){
                    needBold = false;
                }
            }
        }
        return needBold;
    }

    /**
     * 获取字体样式属性（normal，italic, oblique）
     * @param attributeSet
     * @return
     */
    public static boolean getItalic(ArrayMap<String, BookTagAttribute> attributeSet) {
        boolean needItalic = false;
        if (attributeSet == null) return needItalic;
        BookTagAttribute attribute = attributeSet.get("font-style");
        if (attribute != null) {
            if (attribute.valueStr.equals("italic") || attribute.valueStr.equals("oblique")) {
                needItalic = true;
            }
        }
        return needItalic;
    }


    /**
     * 获取vertical-align属性
     * @param attributeSet
     * @param lastElementHeight 上一个元素的高度
     * @param elementHeight 本元素的高度
     * @return
     */
    public static int getVerticalAlign(ArrayMap<String, BookTagAttribute> attributeSet, int lastElementHeight, int elementHeight) {
        int offsetY = 0;
        if (attributeSet == null) return  offsetY;
        BookTagAttribute attribute = attributeSet.get("vertical-align");
        if (attribute == null) return offsetY;
        switch (attribute.valueStr) {
            case "super":
                offsetY = lastElementHeight - elementHeight;
                break;
            case "middle":
                offsetY = (lastElementHeight - elementHeight) / 2 ;
                break;
            case "baseline":
            case "sub":
            default:
                offsetY = 0;
                break;
        }
        return offsetY;
    }

    /**
     * 是否需要vertical-align的偏移
     * @param attributeSet
     * @return
     */
    public static boolean needVerticalAlignOffset(ArrayMap<String, BookTagAttribute> attributeSet) {
        if (attributeSet == null) return  false;
        BookTagAttribute attribute = attributeSet.get("vertical-align");
        if (attribute == null || attribute.valueStr.equals("baseline") ) {
            return false;
        } else {
            return true;
        }
    }
}
