package com.example.epubreader.view.book;

import android.graphics.Bitmap;
import android.support.v4.util.ArrayMap;

import com.example.epubreader.book.css.BookTagAttribute;
import com.example.epubreader.book.tag.BodyControlTag;
import com.example.epubreader.util.BookAttributeUtil;
import com.example.epubreader.util.MyReadLog;

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
    public static final byte POSITION_LEFT = 0;
    public static final byte POSITION_TOP = 1;
    public static final byte POSITION_RIGHT = 2;
    public static final byte POSITION_BOTTOM = 3;

    private Bitmap backgrounImage;

    private int backgroundColor;
    private String startPosition; // 开始的位置
    private String endPosition; // 结束的位置

    private ArrayMap<String, BookTagAttribute> attributeMap;
    private ArrayList<BookLineInfo> lineInfos = new ArrayList<>(); // 行信息

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
        setGap();
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
        setGap();
    }

    /**
     * 获取边距
     */
    private void setGap() {
        lGap = rGap = 100;
        tGap = 150;
        bGap = 50;

//        lGap = BookAttributeUtil.getMargin(attributeMap, BookAttributeUtil.POSITION_LEFT, pageWidth)
//                + BookAttributeUtil.getPadding(attributeMap, BookAttributeUtil.POSITION_LEFT, pageWidth);
//        rGap = BookAttributeUtil.getMargin(attributeMap, BookAttributeUtil.POSITION_RIGHT, pageWidth)
//                + BookAttributeUtil.getPadding(attributeMap, BookAttributeUtil.POSITION_RIGHT, pageWidth);
//        bGap = BookAttributeUtil.getMargin(attributeMap, BookAttributeUtil.POSITION_BOTTOM, pageWidth)
//                + BookAttributeUtil.getPadding(attributeMap, BookAttributeUtil.POSITION_BOTTOM, pageWidth);
//        tGap = BookAttributeUtil.getMargin(attributeMap, BookAttributeUtil.POSITION_TOP, pageWidth)
//                + BookAttributeUtil.getPadding(attributeMap, BookAttributeUtil.POSITION_TOP, pageWidth);
//        MyReadLog.d("left = %d, right = %d, top = %d, bottom = %d",lGap,rGap,tGap,bGap);
    }

    /**
     * 获取页面的边距信息
     *
     * @param position
     * @return
     */
    private int getGap(Byte position) {
        switch (position) {
            case POSITION_LEFT:
                return lGap;
            case POSITION_TOP:
                return tGap;
            case POSITION_RIGHT:
                return rGap;
            case POSITION_BOTTOM:
                return tGap;
            default:
                return 0;
        }
    }

    /**
     * 添加行信息
     *
     * @param lineInfo
     */
    public void addLineInfo(BookLineInfo lineInfo) {
        lineInfos.add(lineInfo);
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
     * 将页面切成几个指定页面高度的页面
     *
     * @param height 页面的指定高度
     * @return
     */
    public ArrayList<BookPage> cutToPages(int height) {
        ArrayList<BookPage> pages = new ArrayList<>();
        BookPage page = null;
        int linesY = tGap;
        int lineIndex = 0;
        while (lineIndex < lineInfos.size()) {
            if (page == null) {
                page = new BookPage(bodyControlTag, pageWidth, height);
            }
            BookLineInfo lineInfo = lineInfos.get(lineIndex);

            if (linesY + lineInfo.lineHeight > height - bGap) {
                page.resetPosition();
                pages.add(page);
                page = null;
                linesY = tGap;
            } else {
                page.addLineInfo(lineInfos.get(lineIndex));
                lineIndex++;
                linesY = linesY + lineInfo.lineHeight;
            }
        }
        if (page != null) {
            page.resetPosition();
            pages.add(page);
        }
        MyReadLog.i("pages size is " + pages.size());
        return pages;
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
}
