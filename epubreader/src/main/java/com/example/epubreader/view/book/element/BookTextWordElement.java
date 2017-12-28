package com.example.epubreader.view.book.element;

import android.graphics.Paint;
import android.support.v4.util.ArrayMap;

import com.example.epubreader.ReaderApplication;
import com.example.epubreader.book.BookContentElement;
import com.example.epubreader.book.css.BookTagAttribute;
import com.example.epubreader.util.BookStingUtil;
import com.example.epubreader.util.MyReadLog;

import java.util.ArrayList;

/**
 * Created by Boyad on 2017/11/9.
 */

public class BookTextWordElement extends BookTextBaseElement {
    private final char[] data;
    private final int offset;
    private final int length;
    private boolean isSingleChinese = false;

    public BookTextWordElement(String s, BookContentElement bookContentElement) {
        super(bookContentElement);
        data = s.toCharArray();
        offset = 0;
        length = data.length;
        isSingleChinese = false;
//        this.contentElement = contentElement;
    }

    public BookTextWordElement(char c, BookContentElement bookContentElement, boolean isSingleChinese) {
        super(bookContentElement);
        data = new char[]{c};
        offset = 0;
        length = data.length;
        this.isSingleChinese = isSingleChinese;
//        this.contentElement = contentElement;
    }

    public BookTextWordElement(String s, BookContentElement bookContentElement, boolean isSingleChinese ) {
        super(bookContentElement);
        data = s.toCharArray();
        offset = 0;
        length = data.length;
        this.isSingleChinese = isSingleChinese;
//        MyReadLog.i("isSingleChinese");
//        this.contentElement = contentElement;
    }


    public boolean isASpace() {
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(data[i])) {
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
    public int getWidth(int fontSize, int maxWidth, int maxHeight) {
//        width = BookStingUtil.getStringWidth(fontSize, data, 0, length);
//        descent = (int) (ReaderApplication.getInstance().getWindowSize().density * 3 + 0.5f);
//        height = fontSize;
        width = (int) ((baseWidth / 100) * fontSize + 0.5f);
        descent = (int) (ReaderApplication.getInstance().getWindowSize().density * 3 + 0.5f);
        height = fontSize;
        return width;
    }

    @Override
    public int getHeight(int maxHeight) {
        return height;
    }

    public int getLength() {
        return length;
    }

    public void measureSize(int fontSize, Paint paint) {
        if (isSingleChinese) {
            width = fontSize;
        } else {
            width = (int) paint.measureText(data, 0, length);
        }
//        width = BookStingUtil.getStringWidth(fontSize, data, 0, length, paint);
//        width = getStringWidth(fontSize, data, 0, length, paint);
//        MyReadLog.i("width = " + width);
        descent = (int) (ReaderApplication.getInstance().getWindowSize().density * 3 + 0.5f);
        height = fontSize;
    }

    public  int getStringWidth(int fontSize, char[] content, int start, int length) {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        return (int) paint.measureText(content, start, length);
    }


}

