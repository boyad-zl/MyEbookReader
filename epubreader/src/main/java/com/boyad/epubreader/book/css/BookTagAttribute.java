package com.boyad.epubreader.book.css;

/**
 * 标签属性
 * Created by Boyad on 2017/11/4.
 */

public class BookTagAttribute {
    public String nameStr; // 属性名称
    public String valueStr; // 属性的值

    public BookTagAttribute(String name, String value) {
        this.nameStr = name;
        this.valueStr = value;
    }

    public String getAttributeName() {
        return nameStr;
    }

}
