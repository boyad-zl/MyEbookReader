package com.boyad.epubreader.view.book;

import android.graphics.Bitmap;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.boyad.epubreader.book.css.BookTagAttribute;
import com.boyad.epubreader.book.tag.BodyControlTag;
import com.boyad.epubreader.util.BookAttributeUtil;
import com.boyad.epubreader.util.BookUIHelper;
import com.boyad.epubreader.view.book.element.BookTextBaseElement;

import java.util.ArrayList;

/**
 * 页面
 * Created by Boyad on 2017/11/9.
 */

public class BookPage {
    private int pageWidth;
    private int pageHeight;
    public int lGap, rGap, tGap, bGap; //左右上下的间距
    BodyControlTag bodyControlTag;

    protected Bitmap backgrounImage;
    protected int backgroundColor;

    private String startPosition; // 开始的位置
    private String endPosition; // 结束的位置

    private ArrayMap<String, BookTagAttribute> attributeMap;
    protected ArrayList<BookLineInfo> lineInfos = new ArrayList<>(); // 行信息

    /**
     * 获取指定宽度的页面， 页面高度不做限制
     *
     * @param controlTag
     * @param pageWidth
     */
    public BookPage(BodyControlTag controlTag, int pageWidth) {
        bodyControlTag = controlTag;
        this.pageWidth = pageWidth;
        attributeMap = controlTag.getAttributeMap();
//        setGap();
    }

    /**
     * 获取指定宽度和高度的页面
     *
     * @param controlTag
     * @param pageWidth
     * @param pageHeight
     */
    public BookPage(BodyControlTag controlTag, int pageWidth, int pageHeight) {
        bodyControlTag = controlTag;
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        attributeMap = controlTag.getAttributeMap();
//        setGap();
    }

    public BookPage(BookPage page, int pageHeight) {
        this.tGap = page.tGap;
        this.lGap = page.lGap;
        this.rGap = page.rGap;
        this.bGap = page.bGap;
        this.pageWidth = page.pageWidth;
        this.pageHeight = pageHeight;
        this.bodyControlTag = page.bodyControlTag;
        this.attributeMap = page.attributeMap;
    }

    /**
     * 获取边距
     */
    public void setGap() {
        lGap = rGap = BookUIHelper.dp2px(15);
        tGap = BookUIHelper.dp2px(35);
        bGap = BookUIHelper.dp2px(30);

        lGap = lGap + BookAttributeUtil.getMargin(attributeMap, BookAttributeUtil.POSITION_LEFT, pageWidth)
                + BookAttributeUtil.getPadding(attributeMap, BookAttributeUtil.POSITION_LEFT, pageWidth);
        rGap = rGap + BookAttributeUtil.getMargin(attributeMap, BookAttributeUtil.POSITION_RIGHT, pageWidth)
                + BookAttributeUtil.getPadding(attributeMap, BookAttributeUtil.POSITION_RIGHT, pageWidth);
        bGap = bGap + BookAttributeUtil.getMargin(attributeMap, BookAttributeUtil.POSITION_BOTTOM, pageWidth)
                + BookAttributeUtil.getPadding(attributeMap, BookAttributeUtil.POSITION_BOTTOM, pageWidth);
        tGap = tGap + BookAttributeUtil.getMargin(attributeMap, BookAttributeUtil.POSITION_TOP, pageWidth)
                + BookAttributeUtil.getPadding(attributeMap, BookAttributeUtil.POSITION_TOP, pageWidth);
//        MyReadLog.d("left = %d, right = %d, top = %d, bottom = %d",lGap,rGap,tGap,bGap);
    }

    /**
     * 添加行信息
     *
     * @param lineInfo
     */
    public void addLineInfo(BookLineInfo lineInfo) {
        if (lineInfos.isEmpty()) setStartPosition(lineInfo.elements.get(0).getPosition());
        lineInfos.add(lineInfo);
    }

    public void finishAdd() {
        if (!lineInfos.isEmpty()) {
            BookLineInfo lastLineInfo = lineInfos.get(lineInfos.size() - 1);
            if (lastLineInfo != null && !lastLineInfo.elements.isEmpty()) {
                setEndPosition(lastLineInfo.elements.get(lastLineInfo.elements.size() - 1).getPosition());
            }
        }
    }

    /**
     * 获取行数量
     *
     * @return
     */
    public int getLineSize() {
        return lineInfos.size();
    }

    /**
     * 重新分配坐标，
     * 当页面被切割以后，将行坐标重新计算
     */
    void resetPosition() {
        if (lineInfos.size() > 0) {
            BookLineInfo firstLineInfo = lineInfos.get(0);
            int moveY = firstLineInfo.y - tGap;
            if (moveY > 0) {
                for (int i = 0; i < lineInfos.size(); i++) {
                    BookLineInfo lineInfo = lineInfos.get(i);
                    lineInfo.moveUp(moveY);
                }
            }
        }
    }

    /**
     * 获取行信息
     *
     * @param i
     * @return
     */
    public BookLineInfo getLindInfo(int i) {
        return lineInfos.get(i);
    }

    public int getPageHeight() {
        return pageHeight;
    }

    public int getPageWidth() {
        return pageWidth;
    }

    public String getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(String startPosition) {
        this.startPosition = startPosition;
    }

    public String getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(String endPosition) {
        this.endPosition = endPosition;
    }

    /**
     * todo test : 判断元素是否在首元素和末元素之间
     * 当前页面是否包含某元素
     *
     * @param readPosition 阅读的位置
     * @return
     */
    public boolean containElement(BookReadPosition readPosition) {
//        MyReadLog.d("startPosition = %s, endPosition = %s , readPosition = %s", startPosition, endPosition, readPosition.toString());
        String contentElementPosition = readPosition.getContentIndex();
//        MyReadLog.i("contentElementPosition = " + contentElementPosition);
        int contentElmentIndex = readPosition.getElementIndex();
        if (TextUtils.isEmpty(contentElementPosition) || contentElementPosition.equals("0")) {
            return true;
        } else {
//            MyReadLog.d("startPosition = %s, endPosition = %s", startPosition, endPosition);
            int startElementIndex = Integer.valueOf(startPosition.substring(startPosition.indexOf("/") + 1));
            int endElementIndex = Integer.valueOf(endPosition.substring(endPosition.indexOf("/") + 1));

            if (contentElementPosition.equals(startPosition.substring(0, startPosition.indexOf("/")))) {
                return startElementIndex <= contentElmentIndex;
            }
            if (contentElementPosition.equals(endPosition.substring(0, endPosition.indexOf("/")))) {
                return endElementIndex >= contentElmentIndex;
            }
            String[] startElementPositionStrs = startPosition.substring(0, startPosition.indexOf("/")).split(":");
            String[] endElementPositionStrs = endPosition.substring(0, startPosition.indexOf("/")).split(":");
            String[] readPositionStrs = contentElementPosition.split(":");

            int maxLength = Math.max(startElementPositionStrs.length, endElementPositionStrs.length);
            int depth = 1;
            boolean isContained = true;
            boolean needCompareWithStart = true;
            boolean needCompareWithEnd = true;

            while (depth < maxLength) {
                int startDepthValue = depth < startElementPositionStrs.length ? Integer.valueOf(startElementPositionStrs[depth]) : -1;
                int endDepthValue = depth < endElementPositionStrs.length ? Integer.valueOf(endElementPositionStrs[depth]) : -1;
                int positionDepthValue = Integer.valueOf(readPositionStrs[depth]);
                if ((needCompareWithStart && positionDepthValue < startDepthValue)
                        || (needCompareWithEnd && positionDepthValue > endDepthValue)) {
                    isContained = false;
                    break;
                }
                if (needCompareWithStart && startDepthValue != -1) {
                    needCompareWithStart = positionDepthValue == startDepthValue;
                }
                if (needCompareWithEnd && endDepthValue != -1) {
                    needCompareWithEnd = positionDepthValue == endDepthValue;
                }
                if (!needCompareWithStart && !needCompareWithEnd) {
                    break;
                }
                depth++;
            }
            if (isContained) {
                if (needCompareWithStart) {
                    return startElementIndex <= contentElmentIndex;
                }

                if (needCompareWithEnd) {
                    return endElementIndex >= contentElmentIndex;
                }
            }
            return isContained;
        }
    }

    /**
     * 获取书签信息
     * 即page的前60个文字
     *
     * @return
     */
    public String getMarkStr() {
        int lineIndex = 0;
        StringBuilder stringBuilder = new StringBuilder();
        boolean getMarkSummaryStrSuccess = false;
        BookTextBaseElement lastLineFirstElement = null;
        while (lineIndex < lineInfos.size()) {
            BookLineInfo lineInfo = lineInfos.get(lineIndex);
            if (lastLineFirstElement != null && !lastLineFirstElement.isInOneParagraph(lineInfo.elements.get(0))) {
                stringBuilder.append("\n");
            }
            lastLineFirstElement = lineInfo.elements.get(0);
            for (int i = 0; i < lineInfo.elements.size(); i++) {
                BookTextBaseElement element = lineInfo.elements.get(i);
                stringBuilder.append(element.getContentStr());
                if (stringBuilder.length() > 59) {
                    getMarkSummaryStrSuccess = true;
                    break;
                }
            }
            if (getMarkSummaryStrSuccess) {
                break;
            }
            lineIndex++;
        }
        stringBuilder.append("...");
        return stringBuilder.toString();
    }
}
