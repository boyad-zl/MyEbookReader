package com.boyad.epubreader.book.css;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

/**
 * CSS 里的class属性
 * Created by Boyad on 2017/11/6.
 */

public class BookClassSet {
    public String className;
    public ArrayMap<String, BookTagAttribute> attributes;
    public String tagName = "";  //指定专用的tag

    public BookClassSet(String className) {
        this.className = className;
        attributes = new ArrayMap<>();
    }

    public BookClassSet(String className, String tag) {
        this.className = className;
        if (tag != null || !TextUtils.isEmpty(tag)) {
            this.tagName = tag;
        }
        attributes = new ArrayMap<>();

    }

    public String getClassName() {
        return className;
    }

    /**
     * 往attribute中添加属性数据
     * @param attribute
     */
    public void addAttribute(BookTagAttribute attribute) {
        attributes.put(attribute.getAttributeName(), attribute);
    }

    /**
     * 往attribute中添加属性数据
     * @param classSet
     */
    public void addAttribute(BookClassSet classSet) {
        if (classSet != null && classSet.attributes != null){
            if (classSet.attributes.size() > 0) {
                for (int i = 0; i <classSet.attributes.size(); i++) {
                    attributes.put(classSet.attributes.keyAt(i), classSet.attributes.valueAt(i));
                }
            }
        }
    }

}
