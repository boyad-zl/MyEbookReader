package com.boyad.epubreader.book.tag;

import android.support.v4.util.ArrayMap;

/**
 * div 标签
 * Created by Boyad on 2017/11/3.
 */
public class DivisionControlTag extends BookBasicControlTag{
    public DivisionControlTag(String name, String attribute) {
        super(name, attribute);
    }

    public DivisionControlTag(String tagName, ArrayMap<String, String> allAttribute) {
        super(tagName, allAttribute);
    }
}
