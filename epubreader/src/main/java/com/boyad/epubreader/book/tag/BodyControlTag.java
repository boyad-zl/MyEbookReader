package com.boyad.epubreader.book.tag;

import android.support.v4.util.ArrayMap;

/**
 * body tag
 * Created by Boyad on 2017/11/3.
 */
public class BodyControlTag extends BookBasicControlTag {

    public BodyControlTag(String name, String attribute) {
        super(name, attribute);
    }

    public BodyControlTag(String tagName, ArrayMap<String, String> allAttribute) {
        super(tagName, allAttribute);
    }
}
