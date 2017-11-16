package com.example.epubreader.view.book.element;

import android.graphics.Paint;
import android.support.v4.util.ArrayMap;

import com.example.epubreader.book.BookContentElement;
import com.example.epubreader.book.css.BookCSSAttributeSet;
import com.example.epubreader.book.css.BookTagAttribute;

/**
 * Created by Boyad on 2017/11/9.
 */

public class BookTextNbspElement extends BookTextBaseElement {

    public BookTextNbspElement(BookContentElement contentElement) {
        super(contentElement);
    }

    @Override
    public int getWidth(Paint paint) {
        return (int)(paint.measureText("  ", 0, 2) + 0.5f);
    }

    @Override
    public int getHeight(Paint pain) {
        return (int) (pain.getTextSize() + 0.5f);
    }

}
