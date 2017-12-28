package com.example.epubreader.book.tag;

import android.support.v4.util.ArrayMap;

/**
 * 段落标签类（h1, h2, h3, p）
 * Created by Boyad on 2017/11/3.
 */
public class ParagraphControlTag extends BookBasicControlTag{
    
    public ParagraphControlTag(String name, String attribute) {
        super(name, attribute);
    }

    public ParagraphControlTag(String tagName, ArrayMap<String, String> allAttribute) {
        super(tagName, allAttribute);
    }
}

