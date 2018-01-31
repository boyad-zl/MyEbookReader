package com.boyad.epubreader.book.tag;

import android.support.v4.util.ArrayMap;

/**
 * br 标签用于换行
 * Created by Boyad on 2017/11/7.
 */

public class LineBreakControlTag extends BookBasicControlTag {
    public LineBreakControlTag(String tagName, String attributeStr) {
        super(tagName, attributeStr);
    }

    public LineBreakControlTag(String tagName, ArrayMap<String, String> allAttribute) {
        super(tagName, allAttribute);
    }
}
