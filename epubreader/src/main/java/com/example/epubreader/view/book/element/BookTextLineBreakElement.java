package com.example.epubreader.view.book.element;

import android.graphics.Paint;
import android.support.v4.util.ArrayMap;

import com.example.epubreader.book.BookContentElement;
import com.example.epubreader.book.css.BookCSSAttributeSet;
import com.example.epubreader.book.css.BookTagAttribute;

/**
 * Created by Boyad on 2017/11/10.
 */
public class BookTextLineBreakElement extends BookTextBaseElement {
    public BookTextLineBreakElement(BookContentElement contentElement) {
        super(contentElement);
    }

    @Override
    public int getWidth(int fontSize, int maxWidth, int maxHeight) {
        width = -1;
        height = fontSize;
        return width;
    }

    @Override
    public int getHeight(int maxHeight) {
        return height;
    }

    @Override
    public void measureSize(int fontSize, Paint paint) {
        width = -1;
        height = fontSize;
    }
}
