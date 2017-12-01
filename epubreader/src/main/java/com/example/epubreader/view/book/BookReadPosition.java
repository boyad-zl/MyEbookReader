package com.example.epubreader.view.book;

/**
 * Created by Boyad on 2017/11/21.
 */

public class BookReadPosition {
    private int pagePosition; // 所在html的位置
    private String contentIndex ; // contentElement的位置信息
    private int elementIndex; // 在contentElement 中的索引位置

    public BookReadPosition(int pagePosition, String contentIndex, int elementIndex) {
        this.pagePosition = pagePosition;
        this.contentIndex = contentIndex;
        this.elementIndex = elementIndex;
    }

    public int getPagePosition() {
        return pagePosition;
    }

    public String getContentIndex() {
        return contentIndex;
    }

    public int getElementIndex() {
        return elementIndex;
    }

    public void setPagePosition(int pagePosition) {
        this.pagePosition = pagePosition;
    }

    public void setContentIndex(String contentIndex) {
        this.contentIndex = contentIndex;
    }

    public void setElementIndex(int elementIndex) {
        this.elementIndex = elementIndex;
    }

    @Override
    public String toString() {
        return pagePosition + "-" + contentIndex + "/" + elementIndex;
    }

}
