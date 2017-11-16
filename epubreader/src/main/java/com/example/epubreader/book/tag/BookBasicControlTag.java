package com.example.epubreader.book.tag;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.example.epubreader.book.css.BookCSSAttributeSet;
import com.example.epubreader.util.BookStingUtil;
import com.example.epubreader.book.css.BookTagAttribute;


/**
 * Basic Tag class
 * Created by Boyad on 2017/11/3.
 */

public class BookBasicControlTag {
    private String tagName; // 标签名称
    private String attributeStr; // 属性字段

    ArrayMap<String, BookTagAttribute> attributeMap = new ArrayMap<>(); //储存标签内的属性信息

    private BookCSSAttributeSet bookCSSAttributeSet;

    public BookBasicControlTag(String tagName, String attributeStr) {
        this.tagName = tagName;
        this.attributeStr = attributeStr;

    }

    /**
     * 设置html里获取到的属性集合
     * @param bookCSSAttributeSet
     */
    public void setNeedAttribute(BookCSSAttributeSet bookCSSAttributeSet){
        this.bookCSSAttributeSet = bookCSSAttributeSet;
        getCSSAndStyleAttribute();
        getOtherAttribute(attributeStr);
    }

    /**
     * todo test 先获取默认的css，在获取css里面的默认属性，在获取class定义的属性（按照先后顺序覆盖添加），在获取style里面属性
     * 获取CSS里面的属性以及Style里面的属性
     */
    private void getCSSAndStyleAttribute() {
    // 1.加载css里面的根标签的属性；
    // 2.加载定义的属性
    // 3.加载style里面定义的属性； 即style=".."中定义的属性
        attributeMap = bookCSSAttributeSet.getDefaultAttributeMap(tagName);
        if (TextUtils.isEmpty(attributeStr)) {
            return;
        }
        int classIndex = attributeStr.indexOf("class");
        if (classIndex > -1) {
            String classValue = BookStingUtil.getDataValue(attributeStr, "\"", "\"", classIndex).trim();
            if (!TextUtils.isEmpty(classValue)) {
                String[] classes = classValue.split(" ");
//                MyReadLog.i("first class is " + classes[0]);
                attributeMap = bookCSSAttributeSet.getBookClass(classValue, tagName).attributes;
            }
        }
        int styleIndex = attributeStr.indexOf("style");
        if (styleIndex > -1) {
            String styleValue  = BookStingUtil.getDataValue(attributeStr, "\"", "\"", styleIndex).trim();
            String[] styles = styleValue.split(";");
            if (styles != null && styles.length > 0) {
                for (int i = 0; i < styles.length; i++) {
                    String attributeItemStr = styles[i].trim();
                    if (!TextUtils.isEmpty(attributeItemStr)) {
                        int colonIndex = attributeItemStr.indexOf(":");
                        if (colonIndex > -1) {
                           String attributeName = attributeItemStr.substring(0, colonIndex).trim();
                           String attributeValue = attributeItemStr.substring(colonIndex + 1).trim();
                            if (!TextUtils.isEmpty(attributeName) && !TextUtils.isEmpty(attributeValue)) {
                                attributeMap.put(attributeName, new BookTagAttribute(attributeName, attributeValue));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取其他属性，例如href，id，src，alt等其他属性
     *
     * @param attributeStr
     */
    protected void getOtherAttribute(String attributeStr) {

    }

    public String getTagName() {
        return tagName;
    }

    public String getAttributeStr() {
        return attributeStr;
    }

    /**
     * 获取标签的属性
     * @return
     */
    public ArrayMap<String, BookTagAttribute> getAttributeMap() {
        return attributeMap;
    }
}
