package com.boyad.epubreader.book.tag;

import android.support.v4.util.ArrayMap;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;

import com.boyad.epubreader.book.css.BookCSSAttributeSet;
import com.boyad.epubreader.book.css.BookClassSet;
import com.boyad.epubreader.util.BookStingUtil;
import com.boyad.epubreader.book.css.BookTagAttribute;


/**
 * Basic Tag class
 * Created by Boyad on 2017/11/3.
 */

public class BookBasicControlTag {
    private static final String ATTRIBUTE_NAME_CLASS = "class";
    private static final String ATTRIBUTE_NAME_STYLE = "style";
    private static final String ATTRIBUTE_NAME_ID = "id";
    private String tagName; // 标签名称
    private String attributeStr; // 属性字段
    ArrayMap<String, String> allAttribute; // 所有属性
    ArrayMap<String, BookTagAttribute> attributeMap = new ArrayMap<>(); //储存标签内的属性信息
    private BookCSSAttributeSet bookCSSAttributeSet;
    private String id;


    public BookBasicControlTag(String tagName, String attributeStr) {
        this.tagName = tagName;
        this.attributeStr = attributeStr;
    }

    public BookBasicControlTag(String tagName, ArrayMap<String, String> allAttribute) {
        this.tagName = tagName;
        this.allAttribute = allAttribute;
    }

    /**
     * 设置html里获取到的属性集合
     *
     * @param bookCSSAttributeSet
     */
    public void setNeedAttribute(BookCSSAttributeSet bookCSSAttributeSet) {
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
        if (bookCSSAttributeSet != null) {
            attributeMap = bookCSSAttributeSet.getDefaultAttributeMap(tagName);
        }
//        MyReadLog.i("attribute map size is " + attributeMap.size());
        if (TextUtils.isEmpty(attributeStr) && (allAttribute == null || allAttribute.size() <= 0)) {
//            MyReadLog.i("return");
            return;
        }
//        MyReadLog.i("not return");
        String classValue = "";
        String styleValue = "";
        if (allAttribute == null) {
            int classIndex = attributeStr.indexOf(ATTRIBUTE_NAME_CLASS);
            if (classIndex > -1)
                classValue = BookStingUtil.getDataValue(attributeStr, "\"", "\"", classIndex).trim();
            int styleIndex = attributeStr.indexOf(ATTRIBUTE_NAME_STYLE);
            if (styleIndex > -1) {
                styleValue = BookStingUtil.getDataValue(attributeStr, "\"", "\"", styleIndex).trim();
            }
        } else {
            classValue = allAttribute.get(ATTRIBUTE_NAME_CLASS);
            styleValue = allAttribute.get(ATTRIBUTE_NAME_STYLE);
        }
        if (!TextUtils.isEmpty(classValue) && bookCSSAttributeSet != null) {
            BookClassSet classSet = bookCSSAttributeSet.getBookClass(classValue, tagName);
//            MyReadLog.i("classSet size is " + classSet.attributes.size());
            if (classSet != null ) {
                attributeMap.putAll((SimpleArrayMap<String, BookTagAttribute>) classSet.attributes);
            }
        }

        if (!TextUtils.isEmpty(styleValue)) {
            String[] styles = styleValue.split(";");
            if (styles.length > 0) {
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
        if (allAttribute == null) {
            int idIndex = attributeStr.indexOf(ATTRIBUTE_NAME_ID);
            if (idIndex > -1) {
                id = attributeStr.substring(idIndex);
            }
        } else {
            id = allAttribute.get(ATTRIBUTE_NAME_ID);
        }
    }

    public String getId() {
        return id;
    }

    public String getTagName() {
        return tagName;
    }

    public String getAttributeStr() {
        return attributeStr;
    }

    /**
     * 获取标签的属性
     *
     * @return
     */
    public ArrayMap<String, BookTagAttribute> getAttributeMap() {
        return attributeMap;
    }
}
