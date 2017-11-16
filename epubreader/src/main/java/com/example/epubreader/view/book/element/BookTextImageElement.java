package com.example.epubreader.view.book.element;

import android.graphics.Paint;

import com.example.epubreader.book.BookContentElement;
import com.example.epubreader.book.tag.ImageControlTag;

/**
 * Created by Boyad on 2017/11/9.
 */

public class BookTextImageElement extends BookTextBaseElement {
    ImageControlTag controlTag;
    public BookTextImageElement(ImageControlTag controlTag, BookContentElement contentElement) {
        super(contentElement);
        this.controlTag = controlTag;
    }

    @Override
    public int getWidth(Paint paint) {
        return 0;
    }

    @Override
    public int getHeight(Paint paint) {
        return 0;
    }
}
