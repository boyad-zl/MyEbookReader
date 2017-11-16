package com.example.epubreader.view.book;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.util.ArrayMap;

import com.example.epubreader.ReaderApplication;
import com.example.epubreader.book.BookContentElement;
import com.example.epubreader.book.BookModel;
import com.example.epubreader.book.css.BookTagAttribute;
import com.example.epubreader.util.BookAttributeUtil;
import com.example.epubreader.util.MyReadLog;
import com.example.epubreader.view.book.element.BookTextBaseElement;
import com.example.epubreader.view.book.element.BookTextLineBreakElement;
import com.example.epubreader.view.book.element.BookTextNbspElement;
import com.example.epubreader.view.book.element.BookTextWordElement;

import java.util.ArrayList;

/**
 * 虚拟的绘制类，用于绘制图片Bitmap上的信息
 * Created by Boyad on 2017/11/8.
 */

public class BookDummyView {
    private BookModel myBookModel;

    private ReaderApplication readerApplication;

    private BookContentElement mainBookContentElement;

    private ArrayList<BookPage> pages;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private volatile ArrayList<BookLineInfo> lineInfos = new ArrayList<>(); //当前页面转换成行信息；

    public BookDummyView(ReaderApplication context) {
        this.readerApplication = context;
    }


    public void setPages(ArrayList<BookPage> pages) {
        this.pages = pages;
    }

    /**
     * 绘制每一页展示的Bitmap
     *
     * @param bitmap
     */
    public void paint(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);
//        preparePage(mainBookContentElement); //准备页面信息；
        if (pages.size() > 0){
            BookPage bookPage = pages.get(0);
            drawPage(bookPage, canvas);
//            BookTextBaseElement firstElement = bookPage.getLindInfo(0).elements.get(0);
//            MyReadLog.i("第一行的元素数量 ： " + bookPage.getLindInfo(0).elements.size());
//            MyReadLog.i("x :"+ firstElement.x + ", Y = " + firstElement.y + ", width = " + firstElement.width + "， height = " + firstElement.height);
        }
    }

    /**
     * 绘制页面信息
     *
     * @param bookPage
     */
    private void drawPage(BookPage bookPage, Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        for (int i = 0; i < bookPage.getLineSize(); i++) {
            BookLineInfo info = bookPage.getLindInfo(i);
            drawLineInfo(info, canvas);
        }
    }

    /**
     * 绘制行信息
     *
     * @param info
     * @param canvas
     */
    private void drawLineInfo(BookLineInfo info, Canvas canvas) {
        ArrayMap<String, BookTagAttribute> attributeSet = null;
        BookTextBaseElement lastElement = null;
        for (int i = 0; i < info.elements.size(); i++) {
            BookTextBaseElement element = info.elements.get(i);
            if (attributeSet == null || element.isPositionChange(lastElement)) {
                attributeSet = element.getAttributeSet(); // 获取显示元素的属性信息
                setPaint(attributeSet);
            }
            drawElement(element, canvas);
        }
    }

    /**
     * 绘制元素
     *
     * @param
     * @param
     */
    private void drawElement(BookTextBaseElement bookTextBaseElement, Canvas canvas) {
        if (bookTextBaseElement instanceof BookTextWordElement) {
            BookTextWordElement wordElement = (BookTextWordElement) bookTextBaseElement;
//            MyReadLog.i("" + wordElement.getData()[0]);
            canvas.drawText(wordElement.getData(), 0, wordElement.getLength(), wordElement.x, wordElement.y , paint);
        } else if (bookTextBaseElement instanceof BookTextNbspElement || bookTextBaseElement instanceof BookTextLineBreakElement) {
            canvas.drawText("  ", bookTextBaseElement.x, bookTextBaseElement.y, paint);
        }
    }

    /**
     * 准备绘制相关的属性
     *
     * @param mainBookContentElement
     */
    private void preparePage(BookContentElement mainBookContentElement) {

        lineInfos.clear();

    }

    /**
     * 设置paint属性
     * @param attributeSet
     */
    private void setPaint(ArrayMap<String, BookTagAttribute> attributeSet) {
        paint.setTextSize(BookAttributeUtil.getFontSize(attributeSet));
        boolean needItalic = BookAttributeUtil.getItalic(attributeSet);
        Typeface italicTypeface = Typeface.create(Typeface.SANS_SERIF, needItalic ? Typeface.ITALIC : Typeface.NORMAL);
        paint.setTypeface(italicTypeface);
        paint.setFakeBoldText(BookAttributeUtil.getBold(attributeSet));
        paint.setColor(Color.BLACK);
    }

}
