package com.example.epubreader.view.book;

import com.example.epubreader.book.tag.BookBasicControlTag;
import com.example.epubreader.util.BookAttributeUtil;
import com.example.epubreader.util.MyReadLog;
import com.example.epubreader.view.book.element.BookTextBaseElement;

import java.util.ArrayList;

/**
 * 记录页面内每行的信息
 * Created by Boyad on 2017/11/8.
 */

public class BookLineInfo {
    int lineHeight;
    int x;
    int y;
    public float lineHeightRate;
    public int topGap, bottomGap;
    private int lineWidth;
    ArrayList<BookTextBaseElement> elements;

    public BookLineInfo(int lineWidth, int lineInfoStartX, int lineInfoStartY) {
        this.lineWidth = lineWidth;
        this.x = lineInfoStartX;
        this.y = lineInfoStartY;
        elements = new ArrayList<>();
    }

    /**
     * 设置行宽
     *
     * @param lineWidth
     */
    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * 获取行宽
     *
     * @return
     */
    public int getLineWidth() {
        return lineWidth;
    }

    /**
     * 获取行高
     *
     * @return
     */
    public int getLineHeight() {
        return lineHeight;
    }

    /**
     * 往行信息里面添加显示元素
     *
     * @param element
     */
    public void addTextElement(BookTextBaseElement element) {
        elements.add(element);
    }

    /**
     * 重建行信息
     * 调整各元素的间距
     *
     * @param gap
     */
    public void rebuildLineInfo(int gap) {
//        MyReadLog.i("gap ->" + gap + "， size is " + elements.size());
        if (elements.size() > 1) {
            int averageGap = gap / elements.size() ;
            int excessGap = gap % elements.size();
            for (int i = 0; i < elements.size(); i++) {
                BookTextBaseElement element = elements.get(i);
                element.x = element.x + averageGap * i;
                if (i + 1 > excessGap) {
                    element.x = element.x + excessGap;
                } else {
                    element.x = element.x + i;
                }
            }
        }
    }

    /**
     * 设置行高度
     *
     * @param height
     */
    public void setLineHeight(int height) {
        lineHeight = height;
    }

    /**
     * 根据text-align的值排布元素
     *
     * @param textAlign
     */
    public void alignLineInfo(byte textAlign) {
//        MyReadLog.i("textAlign" + textAlign);
        switch (textAlign) {
            case BookAttributeUtil.TEXT_ALIGN_LEFT:
            case BookAttributeUtil.TEXT_ALIGN_JUSTIFY:
            case BookAttributeUtil.TEXT_ALIGN_INHERIT:
                break;
            case BookAttributeUtil.TEXT_ALIGN_CENTER:
                if (elements.size() > 0) {
                    BookTextBaseElement firstElement = elements.get(0);
                    BookTextBaseElement lastElement = elements.get(elements.size() - 1);
                    int rightGap = lineWidth - lastElement.x - lastElement.width;
                    if (rightGap > firstElement.x) {
                        int moveX = (rightGap - firstElement.x) / 2;
                        for (BookTextBaseElement element : elements) {
                            element.x = element.x + moveX;
                        }
                    }
                }
                break;
            case BookAttributeUtil.TEXT_ALIGN_RIGHT:
                if (elements.size() > 0){
                    BookTextBaseElement lastElement = elements.get(elements.size() - 1);
                    int moveX = (lineWidth - lastElement.x - lastElement.width);
                    for (BookTextBaseElement element : elements) {
                        element.x = element.x + moveX;
                    }
                }
                break;
        }
    }

    /**
     * 行坐标上移
     *
     * @param moveY 上移的距离
     */
    public void moveUp(int moveY) {
        y = y - moveY;
        if (elements.size() > 0 ) {
            for (int i = 0; i < elements.size(); i++) {
                BookTextBaseElement element = elements.get(i);
                element.y = element.y - moveY;
            }
        }
    }


    /**
     * 设置元素的y坐标
     */
    public void resetVerticalPosition() {

    }
}
