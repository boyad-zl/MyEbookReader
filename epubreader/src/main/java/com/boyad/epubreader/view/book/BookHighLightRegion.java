package com.boyad.epubreader.view.book;

/**
 * 高亮区域
 * Created by Boyad on 2017/12/15.
 */

public class BookHighLightRegion {
    BookReadPosition startPosition, endPosition; // 开始的元素和结束的元素

    public BookHighLightRegion(BookReadPosition position) {
        this.startPosition = position;
        this.endPosition = position;
    }

    public BookHighLightRegion(BookReadPosition start, BookReadPosition end) {
        this.startPosition = start;
        this.endPosition = end;
    }

    public void setStartPosition(BookReadPosition start) {
        this.startPosition = start;

    }

    public void setEndElement(BookReadPosition end) {
        this.endPosition = end;
    }
}
