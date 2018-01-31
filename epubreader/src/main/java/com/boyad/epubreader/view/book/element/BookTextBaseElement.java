package com.boyad.epubreader.view.book.element;

import android.graphics.Paint;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.SimpleArrayMap;

import com.boyad.epubreader.book.BookContentElement;
import com.boyad.epubreader.book.css.BookTagAttribute;
import com.boyad.epubreader.util.MyReadLog;

/**
 * 绘制在bitmap上的元素
 * Created by Boyad on 2017/11/8.
 */

public abstract class BookTextBaseElement {
    public int x, y;
    public int width, height; // 元素的宽高
    public int descent; // 上下偏移
    BookContentElement contentElement;
    int index = 0;
    float baseWidth = 0;
    boolean haveHref = false;

    public BookTextBaseElement(BookContentElement contentElement) {
        this.contentElement = contentElement;
        if (contentElement != null) {
            haveHref = contentElement.isLink();
        }
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

    public abstract int getWidth(int fontSize, int maxWidth, int maxHeight);

    public abstract int getHeight(int maxHeight);

    public abstract void measureSize(int fontSize, Paint paint);

    public abstract String getContentStr();
    /**
     * 获取所有属性（包含父标签的属性）
     *
     * @return
     */
    public ArrayMap<String, BookTagAttribute> getAttributeSet() {
        ArrayMap<String, BookTagAttribute> result = new ArrayMap<>();
        if (contentElement == null) return result;
        String position = contentElement.getPosition();
//        MyReadLog.i(position);
        String[] positions = position.split(":");
        for (int i = positions.length - 1; i > 0; i--) {
            BookContentElement parent = contentElement.getParent(i);
            if (parent != null && parent.getControlTag() != null) {
                SimpleArrayMap<String, BookTagAttribute> arrayMap = parent.getControlTag().getAttributeMap();
                result.putAll(arrayMap);
            }
        }
        if (contentElement.getControlTag() != null) {
            SimpleArrayMap<String, BookTagAttribute> currentContentElementAttributes = contentElement.getControlTag().getAttributeMap();
            result.putAll(currentContentElementAttributes);
        }
        return result;
    }

    public ArrayMap<String, BookTagAttribute> getParagraphAttributeSet(){
        return contentElement.getParagraphAttribute();
    }

    /**
     * 设置位置
     *
     * @param index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    public void setBaseWidth(float baseWidth) {
        this.baseWidth = baseWidth;
    }

    /**
     * 是否是段落开始的位置
     *
     * @return
     */
    public boolean isParagraphStart() {
        if (index > 0) return false;
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

    /**
     * 判断是否拥有链接
     * @return
     */
    public boolean isHasLink(){
        return haveHref;
    }

    public String getLinkHref() {
        String href = "";
        if (haveHref) {
            href = contentElement.getHrefPath();
        }
        return href;
    }

    /**
     * 获取元素坐标位置
     * @return
     */
    public String getPosition() {
        return contentElement.getPosition() + "/" + index;
    }

    public BookContentElement getContentElement() {
        return contentElement;
    }
}