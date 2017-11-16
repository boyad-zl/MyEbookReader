package com.example.epubreader.view.book.element;

import android.graphics.Paint;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.SimpleArrayMap;

import com.example.epubreader.book.BookContentElement;
import com.example.epubreader.book.css.BookTagAttribute;
import com.example.epubreader.util.MyReadLog;

/**
 * 绘制在bitmap上的元素
 * Created by Boyad on 2017/11/8.
 */

public abstract class BookTextBaseElement {
    public int x, y;
    public int width, height; // 元素的宽高
    BookContentElement contentElement;
    int index = 0;

    public BookTextBaseElement(BookContentElement contentElement) {
        this.contentElement = contentElement;
    }

    /**
     * 是否位置发生改变
     *
     * @param other
     * @return
     */
    public boolean isPositionChange(BookTextBaseElement other) {
        if (other == null) return true;
        if (contentElement == null) MyReadLog.i("contentElement is null!!!!");
        return !this.contentElement.getPosition().equals(other.contentElement.getPosition());
    }

    public abstract int getWidth(Paint paint);

    public abstract int getHeight(Paint paint);

    /**
     * 获取所有属性（包含父标签的属性）
     *
     * @return
     */
    public ArrayMap<String, BookTagAttribute> getAttributeSet() {
        ArrayMap<String, BookTagAttribute> result = new ArrayMap<>();
        String position = contentElement.getPosition();
//        MyReadLog.i(position);
        String[] positions = position.split(":");
        for (int i = positions.length - 1; i > 0; i--) {
            BookContentElement parent = contentElement.getParent(i);
//            for (int j = 0; j < parent.getControlTag().getAttributeMap().size(); j++) {
//                result.put(parent.getControlTag().getAttributeMap().keyAt(j), parent.getControlTag().getAttributeMap().valueAt(j));
//            }
            SimpleArrayMap<String, BookTagAttribute> arrayMap = parent.getControlTag().getAttributeMap();
            result.putAll(arrayMap);
        }
        if (contentElement.getControlTag() != null) {
            SimpleArrayMap<String, BookTagAttribute> currentContentElementAttributes = contentElement.getControlTag().getAttributeMap();
            result.putAll(currentContentElementAttributes);
//            for (int i = 0; i < contentElement.getControlTag().getAttributeMap().size(); i++) {
//                result.put(contentElement.getControlTag().getAttributeMap().keyAt(i), contentElement.getControlTag().getAttributeMap().valueAt(i));
//            }
        }
        return result;
    }

    /**
     * 设置位置
     *
     * @param index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * 是否是段落开始的位置
     *
     * @return
     */
    public boolean isParagraphStart() {
        if (index > 0) return false;
//        MyReadLog.i("index = " + index);
        return contentElement.isCurrentElementParagraphStart();
    }

    /**
     * 判断是否两个元素在同一个段落中
     *
     * @param other
     * @return
     */
    public boolean isInOneParagraph(BookTextBaseElement other) {
        if (other == null) return true;
        BookContentElement paragraph = contentElement.getTextParagraphElement();
        BookContentElement otherParagraph = other.contentElement.getTextParagraphElement();
        if (paragraph == null || otherParagraph == null) {
            return false;
        } else {
            return paragraph.getPosition().equals(otherParagraph.getPosition());
        }
    }
}