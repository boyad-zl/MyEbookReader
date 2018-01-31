package com.boyad.epubreader.book.tag;

import android.support.v4.util.ArrayMap;

/**
 * tag span
 * Created by Boyad on 2017/11/7.
 */
public class SpanControlTag extends BookBasicControlTag {
    public SpanControlTag(String name, String attribute) {
        super(name, attribute);
    }

    public SpanControlTag(String tagName, ArrayMap<String, String> allAttribute) {
        super(tagName, allAttribute);
    }
}
