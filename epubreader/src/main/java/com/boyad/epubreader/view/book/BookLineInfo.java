package com.boyad.epubreader.view.book;

import com.boyad.epubreader.util.BookAttributeUtil;
import com.boyad.epubreader.view.book.element.BookTextBaseElement;
import com.boyad.epubreader.view.book.element.BookTextImageElement;

import java.util.ArrayList;

/**
 * 记录页面内每行的信息
 * Created by Boyad on 2017/11/8.
 */

public class BookLineInfo {
    int lineHeight, lineWidth;
    int x;
    int y;
    public float lineHeightRate;
    public int topGap, bottomGap;
    ArrayList<BookTextBaseElement> elements;

    private int textElementTopY, textElementBottomY, contentElementTopY, contentElementBottomY;
    private int maxTextElementHeight= 0;
    private int maxOtherElementHeight = 0;

    private int realStartX,realEndX, realStartY, realEndY; //实际开始的X

    public BookLineInfo(int lineWidth, int lineInfoStartX, int lineInfoStartY) {
        this.lineWidth = lineWidth;
        this.x = lineInfoStartX;
        this.y = lineInfoStartY;
        elements = new ArrayList<>();
        textElementTopY = textElementBottomY = contentElementTopY = contentElementBottomY = lineInfoStartY;
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

    public int getRealStartX() {
        return realStartX;
    }

    public int getRealEndX() {
        return realEndX;
    }

    /**
     * 往行信息里面添加显示元素
     *
     * @param element
     */
    public void addTextElement(BookTextBaseElement element) {
        if (elements.isEmpty()) {
            realStartX = element.x;
        }
        realEndX = element.x + element.width;
        elements.add(element);
        if (element instanceof BookTextImageElement) {
//            MyReadLog.i("图片element height = " + element.height);
            contentElementTopY = Math.min(contentElementTopY, element.y);
            contentElementBottomY = Math.max(contentElementBottomY, element.y + element.height);
            maxOtherElementHeight = Math.max(maxOtherElementHeight, element.height);
        } else {
            contentElementTopY = Math.min(contentElementTopY, element.y - element.height);
            contentElementBottomY = Math.max(contentElementBottomY, element.y);
            textElementTopY = Math.min(textElementTopY, element.y - element.height);
            textElementBottomY = Math.max(textElementBottomY, element.y);

            maxTextElementHeight = Math.max(maxTextElementHeight, element.height);
        }

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
            realEndX = x + lineWidth;
            int averageGap = gap / elements.size();
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
     * TODO TEST ：设置行高存在问题
     * 设置行高度
     *
     */
    public void setLineHeight() {
        int lineHeightGap = (int) ((lineHeightRate - 1) * maxTextElementHeight / 2);
        int textBaseLineYOffset ;
//        MyReadLog.i("top or bottom gap is " + lineHeightGap);
        if (maxOtherElementHeight < maxTextElementHeight) {
//            if (maxOtherElementHeight == 0) {
//                lineHeight = (int) (lineHeightRate * maxTextElementHeight) + topGap + bottomGap;
//                for (int i = 0; i < elements.size(); i++) {
//                    BookTextBaseElement element = elements.get(i);
//                    element.y = element.y + maxTextElementHeight + topGap + lineHeightGap;
//                }
//            } else {
//                int textBaseLineYOffset ;
                if (contentElementTopY  < textElementTopY - lineHeightGap) {
                    lineHeight = textElementBottomY - contentElementTopY;
                } else {
                    lineHeight = lineHeightGap + maxTextElementHeight;
                }
                lineHeight = lineHeight + topGap;
                textBaseLineYOffset = lineHeight ;
                if (contentElementBottomY > contentElementBottomY + lineHeightGap){
                    lineHeight = lineHeight + (contentElementBottomY - textElementBottomY);
                } else {
                    lineHeight = lineHeight + lineHeightGap ;
                }
                lineHeight = lineHeight + bottomGap;

                for (int i = 0; i < elements.size(); i++) {
                    BookTextBaseElement element = elements.get(i);
                    element.y = element.y + textBaseLineYOffset ;
                }
//            }
        } else {
//            int textBaseLineYOffset ;
            if (contentElementTopY  < textElementTopY - lineHeightGap) {
                lineHeight = textElementBottomY - contentElementTopY;
            } else {
                lineHeight = lineHeightGap + maxTextElementHeight;
            }
            lineHeight = lineHeight + topGap;
            textBaseLineYOffset = lineHeight ;
            if (contentElementBottomY > contentElementBottomY + lineHeightGap){
                lineHeight = lineHeight + (contentElementBottomY - textElementBottomY);
            } else {
                lineHeight = lineHeight + lineHeightGap;
            }
            lineHeight = lineHeight + bottomGap;

            for (int i = 0; i < elements.size(); i++) {
                BookTextBaseElement element = elements.get(i);
                element.y = element.y + textBaseLineYOffset ;
            }
        }
//        MyReadLog.i("y = " + y + " , height = " + lineHeight + " ,  textBaseLineYOffset = " + textBaseLineYOffset + " , topGap = " + topGap);
//        MyReadLog.d("contentElementTopY = %d, contentElementBottomY = %d, textElementTopY = %d, textElementBottomY = %d", contentElementTopY, contentElementBottomY, textElementTopY, textElementBottomY);
//        if (contentElementTopY < textElementTopY) {
//            lineHeight = lineHeight - (int) ((lineHeightRate - 1) / 2 * height);
//        }
//        if (contentElementBottomY > textElementBottomY) {
//            lineHeight = lineHeight - (int) ((lineHeightRate - 1) / 2 * height);
//        }
//        if (elements.size() == 1) {
//            BookTextBaseElement element = elements.get(0);
//            if (!(element instanceof BookTextImageElement)) {
//            } else {
//            }
//                element.y = element.y + height;
//            lineHeight = height + topGap + bottomGap;
//        } else {
//            for (int i = 0; i < elements.size(); i++) {
//                BookTextBaseElement element = elements.get(i);
//                if (element instanceof BookTextImageElement) {
//                    MyReadLog.i("need top line height ?  " + (textElementTopY <= contentElementTopY));
//                    MyReadLog.i("need bottom line height ?  " + (contentElementBottomY <= textElementBottomY));
//                }
//                element.y = element.y + height + topGap;
//                if (textElementTopY <= contentElementTopY) {
//                    element.y = element.y + (int) ((lineHeightRate - 1) * height / 2);
//                }
//            }
//        }
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
                    if (elements.size() == 1) {
                        int rightGap = lineWidth - (firstElement.x - x) - firstElement.width;
                        if (rightGap > firstElement.x - x) {
                            int offset = (rightGap - (firstElement.x - x)) / 2;
                            firstElement.x = firstElement.x + offset;
                            realStartX = realStartX + offset;
                            realEndX = realEndX + offset;
                        }
                    } else {
                        BookTextBaseElement lastElement = elements.get(elements.size() - 1);
                        int rightGap = lineWidth - (lastElement.x - x) - lastElement.width;
                        if (rightGap > firstElement.x - x) {
                            int moveX = (rightGap - (firstElement.x - x)) / 2;
                            realStartX = realStartX + moveX;
                            realEndX = realEndX + moveX;
                            for (BookTextBaseElement element : elements) {
                                element.x = element.x + moveX;
                            }
                        }
                    }
                }
                break;
            case BookAttributeUtil.TEXT_ALIGN_RIGHT:
                if (elements.size() > 0) {
                    BookTextBaseElement lastElement = elements.get(elements.size() - 1);
                    int moveX = (lineWidth + x - lastElement.x - lastElement.width);
                    realStartX = realStartX + moveX;
                    realEndX = realEndX + moveX;
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
        if (elements.size() > 0) {
            for (int i = 0; i < elements.size(); i++) {
                BookTextBaseElement element = elements.get(i);
                element.y = element.y - moveY;
            }
        }
    }
}
