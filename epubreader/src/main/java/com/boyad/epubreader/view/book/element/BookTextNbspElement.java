package com.boyad.epubreader.view.book.element;

import android.graphics.Paint;

import com.boyad.epubreader.book.BookContentElement;
import com.boyad.epubreader.util.BookStingUtil;

/**
 * Created by Boyad on 2017/11/9.
 */

public class BookTextNbspElement extends BookTextBaseElement {

    public BookTextNbspElement(BookContentElement contentElement) {
        super(contentElement);
    }

    @Override
    public int getWidth(int fontSize, int maxWidth, int maxHeight) {
        width = (int) (.25f*fontSize + 0.5f);
        height = fontSize;
//        width = (int)(paint.measureText("  ", 0, 2) + 0.5f);
//        width = BookStingUtil.getStringWidth(fontSize, ("  ").toCharArray(), 0, 2, );
//        height = fontSize;
        return width;
    }

    @Override
    public int getHeight(int maxHeight) {
        return height;
    }

    @Override
    public void measureSize(int fontSize, Paint paint) {
        width = BookStingUtil.getStringWidth(fontSize, ("  ").toCharArray(), 0, 2, paint);
        height = fontSize;
    }

    @Override
    public String getContentStr() {
        return " ";
    }
}
