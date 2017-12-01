package com.example.epubreader.view.book;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.util.ArrayMap;
import android.util.SparseArray;

import com.example.epubreader.ReaderApplication;
import com.example.epubreader.book.BookContentElement;
import com.example.epubreader.book.BookModel;
import com.example.epubreader.book.EpubReaderHtml;
import com.example.epubreader.book.PriorityHighThreadFactory;
import com.example.epubreader.book.css.BookTagAttribute;
import com.example.epubreader.util.BookAttributeUtil;
import com.example.epubreader.util.BookStingUtil;
import com.example.epubreader.util.BookUIHelper;
import com.example.epubreader.util.MyReadLog;
import com.example.epubreader.view.book.element.BookTextBaseElement;
import com.example.epubreader.view.book.element.BookTextImageElement;
import com.example.epubreader.view.book.element.BookTextLineBreakElement;
import com.example.epubreader.view.book.element.BookTextNbspElement;
import com.example.epubreader.view.book.element.BookTextWordElement;

import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 虚拟的绘制类，用于绘制图片Bitmap上的信息
 * Created by Boyad on 2017/11/8.
 */

public class BookDummyView extends BookDummyAbstractView {
    private BookModel myBookModel;
    private ArrayList<BookPage> pages;
    private BookPage previousPage, currentPage, nextPage;
    private int currentPageIndex;
    private int currentPageKey;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);


    public final ExecutorService prepareService = Executors.newSingleThreadExecutor(new PriorityHighThreadFactory());
    private SparseArray<ArrayList<BookPage>> pageArray = new SparseArray<>();

    public BookDummyView(ReaderApplication application) {
        super(application);
        this.myBookModel = application.getBookModel();
    }

    @Override
    public void onFingerRelease(int x, int y) {
        application.getMyWidget().startAnimatedScrolling(x, y);
    }

    @Override
    public boolean canScroll(boolean isForward) {
        if (isForward) {
            return nextPage != null;
        } else {
            MyReadLog.i("previous page is null ? - >" + (previousPage == null));
            return previousPage != null;
        }
    }

    @Override
    public void onScrollingFinished(final boolean isForward) {
//        MyReadLog.i("onScrollingFinished");
        int needPreparePageKey = 0;
        boolean needAddPage = false;
        if (isForward) {
            ArrayList<BookPage> currentPages = pageArray.get(currentPageKey);
            previousPage = currentPage;
            currentPage = nextPage;
            if (currentPageIndex == currentPages.size() - 1) {
                currentPageIndex = 0;
                currentPageKey = currentPageKey + 1;
                needAddPage = true;
                currentPages = pageArray.get(currentPageKey);
            } else {
                currentPageIndex = currentPageIndex + 1;
            }
            if (currentPageIndex == currentPages.size() - 1) {
                ArrayList<BookPage> nextPages = pageArray.get(currentPageKey + 1);
                if (currentPageKey >= myBookModel.getSpinSize() - 1) {
                    MyReadLog.i("你翻不动了");
                    nextPage = null;
                } else {
                    nextPage = nextPages.get(0);
                }
            } else {
                nextPage = currentPages.get(currentPageIndex + 1);
            }
        } else {
            ArrayList<BookPage> currentPages = pageArray.get(currentPageKey);
            nextPage = currentPage;
            currentPage = previousPage;

            if (currentPageIndex == 0) {
                currentPageKey = currentPageKey - 1;
                currentPages = pageArray.get(currentPageKey);
                currentPageIndex = currentPages.size() - 1;
                needAddPage = true;
            } else {
                currentPageIndex = currentPageIndex - 1;
            }
            if (currentPageIndex == 0) {
                ArrayList<BookPage> previousPages = pageArray.get(currentPageKey - 1);
                if (currentPageKey - 1 < 0) {
                    MyReadLog.i("你翻不动了");
                    previousPage = null;
                } else {
                    previousPage = previousPages.get(previousPages.size() - 1);
                }
            } else {
                previousPage = currentPages.get(currentPageIndex - 1);
            }
        }

        if (needAddPage) {
            prepareService.execute(new Runnable() {
                @Override
                public void run() {
                    int htmlIndex = -1;
                    if (isForward) {
                        if (currentPageKey + 2 < myBookModel.getSpinSize()) {
                            htmlIndex = currentPageKey + 2;
                        }
                    } else {
                        if (currentPageKey - 2 >= 0) {
                            htmlIndex = currentPageKey - 2;
                        }
                    }
                    if (htmlIndex != -1 && pageArray.get(htmlIndex) == null) {
                        // 添加html页面
                        long startTime = System.currentTimeMillis();
                        EpubReaderHtml html = new EpubReaderHtml(myBookModel);
                        html.loadHtmlInputStream(myBookModel.getTextContent(htmlIndex));
                        pageArray.put(htmlIndex, html.getPages());
                        MyReadLog.i("put finished ==time=" + (System.currentTimeMillis() - startTime) + "=====" +
                                "pages size is " +  html.getPages().size() +
                                " size change to " + pageArray.size() );
                    }

                    if (pageArray.size() > 5) {
                        if (isForward) {
                            pageArray.removeAt(0);
                        } else {
                            pageArray.removeAt(pageArray.size() - 1);
                        }
                        MyReadLog.i("pageArray size is" + pageArray.size());
                    }
                }
            });
        }
        String readPositionStr = currentPageKey  + "-" +currentPage.getStartPosition();
        MyReadLog.i(readPositionStr);
        myBookModel.setReadPosition(readPositionStr );
    }

    public void setPages(ArrayList<BookPage> pages) {
        this.pages = pages;
    }

    /**
     * 绘制每一页展示的Bitmap
     *
     * @param bitmap
     * @param pagePosition 页面的位置信息： -1： 上一章页面； 0： 当前页面； 1：下一个页面
     */
    @Override
    public void paint(Bitmap bitmap, int pagePosition) {
        Canvas canvas = new Canvas(bitmap);
        if (pages != null && pages.size() > 0) {
            BookPage bookPage = pages.get(0);
            drawPage(bookPage, canvas);
            drawFootView(canvas, bookPage);
        } else  {
            preparePage();
            BookPage bookPage = currentPage;
            drawPage(bookPage, canvas);
            drawFootView(canvas, bookPage);
        }
    }

    /**
     * 绘制底部信息
     */
    private void drawFootView(Canvas canvas, BookPage page) {
        paint.setTextSize(BookUIHelper.dp2px(12f));
        paint.setColor(Color.GRAY);
        int timeY = page.getPageHeight() - page.bGap / 2 + (int) (paint.getTextSize() / 2);
        canvas.drawText(BookStingUtil.getTimeStr(System.currentTimeMillis()), page.lGap, timeY, paint);

    }

    /**
     * 绘制页面信息
     *
     * @param bookPage
     */
    private void drawPage(BookPage bookPage, Canvas canvas) {
        if (isDayModel) {
            canvas.drawColor(Color.WHITE);
        } else {
            canvas.drawColor(Color.BLACK);
        }
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
                setPaint(attributeSet, element.isHasLink());
            }
            drawElement(element, canvas);
        }
//        canvas.drawLine(info.x, info.y, info.x + info.getLineWidth(), info.y, paint);
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
            canvas.drawText(wordElement.getData(), 0, wordElement.getLength(), wordElement.x, wordElement.y, paint);
        } else if (bookTextBaseElement instanceof BookTextNbspElement || bookTextBaseElement instanceof BookTextLineBreakElement) {
            canvas.drawText("  ", bookTextBaseElement.x, bookTextBaseElement.y, paint);
        } else if (bookTextBaseElement instanceof BookTextImageElement) {
            String imagePath = ((BookTextImageElement) bookTextBaseElement).getImagePath();
            Bitmap bitmap = ((BookTextImageElement) bookTextBaseElement).createBitmap(myBookModel.getImageInputStream(imagePath));
            if (bitmap != null) {
                MyReadLog.i("imagePath = " + imagePath + "， bitmap size height = " + bitmap.getHeight() + ", width = " + bitmap.getWidth() + "elementWidth =  " + bookTextBaseElement.width  + ", elementHeight = " + bookTextBaseElement.height);
                canvas.drawBitmap(bitmap, bookTextBaseElement.x , bookTextBaseElement.y, imagePaint);
                canvas.drawLine(bookTextBaseElement.x, bookTextBaseElement.y, bookTextBaseElement.x + bookTextBaseElement.width, bookTextBaseElement.y, paint);
                canvas.drawLine(bookTextBaseElement.x, bookTextBaseElement.y + bookTextBaseElement.height, bookTextBaseElement.x + bookTextBaseElement.width, bookTextBaseElement.y + bookTextBaseElement.height, paint);
            }
        }
    }

    /**
     * 准备绘制相关的属性
     *
     * @param
     */
    private void preparePage() {
        BookReadPosition readPosition = myBookModel.getReadPosition();
        if (pageArray.size() == 0) {
            int startIndex = readPosition.getPagePosition() - 2 > 0 ? readPosition.getPagePosition() - 2 : 0;
            int loadSum = (myBookModel.getSpinSize() - 1) - readPosition.getPagePosition() > 2
                    ? readPosition.getPagePosition() + 3 - startIndex
                    : (myBookModel.getSpinSize()) - startIndex;
            MyReadLog.d("startIndex = %d, loadSum = %d", startIndex, loadSum);
            long startTime = System.currentTimeMillis();
            for (int i = startIndex; i < startIndex + loadSum; i++) {
                EpubReaderHtml html = new EpubReaderHtml(myBookModel);
                html.loadHtmlInputStream(myBookModel.getTextContent(i));
                pageArray.put(i, html.getPages());
            }
            MyReadLog.i("prepare cost " + (System.currentTimeMillis() - startTime));
            MyReadLog.i("pageArray size is  " + pageArray.size());

            currentPageKey = readPosition.getPagePosition();
            ArrayList<BookPage> currentHtmlPages = pageArray.get(currentPageKey);
            for (int i = 0; i < currentHtmlPages.size(); i++) {
                BookPage bookPage = currentHtmlPages.get(i);
                if (bookPage.containElement(readPosition)) {
                    currentPage = bookPage;
                    currentPageIndex = i;
                    break;
                }
            }

            MyReadLog.i("currentPageKey = " + currentPageKey + " , currentPageIndex = " + currentPageIndex);
            ArrayList<BookPage> currentPages = pageArray.get(currentPageKey);

            if (currentPageKey == 0 && currentPageIndex == 0) {
                previousPage = null;
            } else {
                if (currentPageIndex == 0) {
                    ArrayList<BookPage> lastPages = pageArray.get(currentPageKey - 1);
                    if (lastPages != null && lastPages.size() > 0) {
                        previousPage = lastPages.get(lastPages.size() - 1);
                    } else {
                        MyReadLog.i("~~~~~~~~~~~~~~~~~last pages is null ?" + (lastPages == null));
                    }
                } else {
                    previousPage = currentPages.get(currentPageIndex - 1);
                }
            }

            if (currentPageKey >= myBookModel.getSpinSize() - 1 && currentPageIndex >= currentPages.size() - 1) {
                nextPage = null;
            } else {
                if (currentPageIndex >= currentPages.size() - 1) {
                    ArrayList<BookPage> nextPages = pageArray.get(currentPageKey + 1);
                    if (nextPages != null && nextPages.size() > 0) {
                        nextPage = nextPages.get(0);
                    }
                } else {
                    nextPage = currentPages.get(currentPageIndex + 1);
                }
            }
        }
    }

    /**
     * 设置paint属性
     *
     * @param attributeSet
     */
    private void setPaint(ArrayMap<String, BookTagAttribute> attributeSet, boolean isHasLink) {
        paint.setTextSize(BookAttributeUtil.getFontSize(attributeSet));
        boolean needItalic = BookAttributeUtil.getItalic(attributeSet);
        Typeface italicTypeface = Typeface.create(Typeface.SANS_SERIF, needItalic ? Typeface.ITALIC : Typeface.NORMAL);
        paint.setTypeface(italicTypeface);
        paint.setFakeBoldText(BookAttributeUtil.getBold(attributeSet));
        paint.setUnderlineText(isHasLink);
        if (isDayModel) {
            paint.setColor(isHasLink ? Color.BLUE : Color.BLACK);
        } else {
            paint.setColor(isHasLink ? Color.YELLOW : Color.WHITE);
        }
    }

    @Override
    public void reset() {
        pageArray.clear();
        preparePage();
    }
}
