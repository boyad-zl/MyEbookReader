package com.example.epubreader.view.book.element;

import android.graphics.Paint;
import android.support.v4.util.ArrayMap;

import com.example.epubreader.book.BookContentElement;
import com.example.epubreader.book.css.BookTagAttribute;
import com.example.epubreader.util.MyReadLog;

/**
 * Created by Boyad on 2017/11/9.
 */

public class BookTextWordElement extends BookTextBaseElement {
    private final char[] data;
    private final int offset;
    private final int length;


    public BookTextWordElement(String s, BookContentElement bookContentElement) {
        super(bookContentElement);
        data = s.toCharArray();
        offset = 0;
        length = data.length;
//        this.contentElement = contentElement;
    }

    public boolean isASpace() {
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(data[i])){
                return false;
            }
        }
        return true;
    }


    public char[] getData() {
        return data;
    }

    public BookContentElement getContentElement() {
        return contentElement;
    }

    @Override
    public int getWidth(Paint paint) {
        width = (int)(paint.measureText(data, 0 , length) + 0.5f);
        return width;
    }

    @Override
    public int getHeight(Paint paint) {
        height = (int)(paint.getTextSize() + 0.5f);
        return height;
    }

    public int getLength() {
        return length;
    }
}

