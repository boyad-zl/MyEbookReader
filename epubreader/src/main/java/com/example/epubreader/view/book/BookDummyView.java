package com.example.epubreader.view.book;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.SparseArray;

import com.example.epubreader.ReaderApplication;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 虚拟的绘制类，用于绘制图片Bitmap上的信息
 * Created by Boyad on 2017/11/8.
 */

public class BookDummyView extends BookDummyAbstractView {
    public static final int PAGE_POSITION_INDEX_PREVIOUS = -1;
    public static final int PAGE_POSITION_INDEX_CURRENT = 0;
    public static final int PAGE_POSITION_INDEX_NEXT = 1;
    private BookModel myBookModel;
    private ArrayList<BookPage> pages;
    private BookPage previousPage, currentPage, nextPage;
    private int currentPageIndex;
    private int currentPageKey;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean isSelectedTextRegion = false; // 是否在选择中


    public final ExecutorService prepareService = Executors.newSingleThreadExecutor(new PriorityHighThreadFactory());
    private SparseArray<ArrayList<BookPage>> pageArray = new SparseArray<>();
    private SparseArray<ArrayMap<String, String>> idArrays = new SparseArray<>();
    private BookCoverPage coverPage;

    public BookDummyView(ReaderApplication application) {
        super(application);
        this.myBookModel = application.getBookModel();
    }

    /**
     * 链接跳转
     *
     * @param href
     */
    public void jumpLinkHref(String href) {
        boolean needRepaint = false;
        boolean webLinkJump = false;
        int pageIndex = currentPageKey;

        String[] hrefChips = href.split("#");
        if (hrefChips.length == 2) {
            String idInPage = hrefChips[1];
            MyReadLog.i("idInPage = " + idInPage);
            if (!TextUtils.isEmpty(hrefChips[0].trim())) {
                pageIndex = myBookModel.getSpinePageIndex(hrefChips[0].trim());
                gotoPagePosition(pageIndex, idInPage);
                return;
            } else {
                ArrayMap<String, String> ids = idArrays.get(pageIndex);
                if (ids != null && ids.size() > 0) {
                    String positionStr = ids.get(idInPage);
                    MyReadLog.i("position = " + positionStr);
                    ArrayList<BookPage> pages = pageArray.get(pageIndex);
                    String[] positionInfo = positionStr.split("/");
                    if (pages != null && pages.size() > 0) {
                        BookReadPosition position = new BookReadPosition(pageIndex, positionInfo[0], Integer.valueOf(positionInfo[1]));
                        for (int i = 0; i < pages.size(); i++) {
                            BookPage page = pages.get(i);
                            if (page.containElement(position)) {
                                currentPage = page;
                                if (currentPageIndex != i) {
                                    needRepaint = true;
                                    currentPageIndex = i;
                                }
                                MyReadLog.i("currentPageIndex = " + i);
                                setPageCurrentAndPreviousIndex();
                                break;
                            }
                        }
                    }

                } else {

                }
            }

            if (needRepaint) {
                ReaderApplication.getInstance().getMyWidget().reset();
                ReaderApplication.getInstance().getMyWidget().repaint();
            }
        } else {
//            MyReadLog.i("is web url ? " + BookStingUtil.isWebUrl(hrefChips[0]));
            if (hrefChips.length == 1 && !BookStingUtil.isWebUrl(hrefChips[0])) {
                pageIndex = myBookModel.getSpinePageIndex(hrefChips[0].trim());
                if (pageIndex > -1) {
                    gotoPagePosition(pageIndex, "");
                }
                return;
            } else {
                webLinkJump = true;
            }
        }

        if (webLinkJump) {
            // 是否需要打开浏览器进行浏览 网址是href
            MyReadLog.i("打开网址：" + href);
        }
    }

    /**
     * todo test 暂时才有用的是一旦跳转就全部重新加载Html，待优化
     * 跳转到指定页面的指定id位置
     *
     * @param pageIndex html 的位置
     * @param idInPage  在html中 的id
     */
    private void gotoPagePosition(int pageIndex, String idInPage) {
        pageArray.clear();
        idArrays.clear();
        MyReadLog.i("prepare");
        preparePage(new BookReadPosition(pageIndex, "0", 0));
        ArrayList<BookPage> pages = pageArray.get(pageIndex);
        ArrayMap<String, String> idMaps = idArrays.get(pageIndex);
        if (!TextUtils.isEmpty(idInPage) && idMaps != null && idMaps.size() > 0 && pages != null && pages.size() > 0) {
            String positionStr = idMaps.get(idInPage);
            MyReadLog.i("position = " + positionStr);
            String[] positionInfo = positionStr.split("/");
            BookReadPosition position = new BookReadPosition(pageIndex, positionInfo[0], Integer.valueOf(positionInfo[1]));
            for (int i = 0; i < pages.size(); i++) {
                BookPage page = pages.get(i);
                if (page.containElement(position)) {
                    currentPage = page;
                    if (currentPageIndex != i) {
                        currentPageIndex = i;
                    }
                    MyReadLog.i("currentPageIndex = " + i);
                    setPageCurrentAndPreviousIndex();
                    break;
                }
            }
        }
        application.getMyWidget().reset();
        application.getMyWidget().repaint();
    }

    private int lineIndexInPage;
    private int elementIndexInLine;

    /**
     * 查找点击处的元素（精确地），可能会找不到元素
     *
     * @param x
     * @param y
     * @param isPrecise 是否是精确查找
     * @return
     */
    private BookTextBaseElement findSelectedElement(int x, int y, boolean isPrecise) {
        BookTextBaseElement selectedElement = null;
        if (y > currentPage.tGap && y < currentPage.getPageHeight() - currentPage.bGap) {
            if ((x < currentPage.lGap || x > currentPage.getPageWidth() - currentPage.rGap) && isPrecise) {
                return selectedElement;
            }
            for (int i = 0; i < currentPage.getLineSize(); i++) {
                BookLineInfo lineInfo = currentPage.getLindInfo(i);
                lineIndexInPage = i;
                if ((lineInfo.y <= y && lineInfo.y + lineInfo.getLineHeight() >= y && lineInfo.elements.size() > 0)
                        || (i == 0 && !isPrecise && lineInfo.y > y)
                        || (i == currentPage.getLineSize() - 1 && !isPrecise && lineInfo.y + lineInfo.getLineHeight() < y)) {
                    for (int j = 0; j < lineInfo.elements.size(); j++) {
                        BookTextBaseElement element = lineInfo.elements.get(j);
                        if (j == 0 && !isPrecise && element.x > x) {
                            selectedElement = element;
//                            MyReadLog.i(element.getPosition());
                            elementIndexInLine = j;
                            return selectedElement;
                        }
                        if (j == lineInfo.elements.size() - 1 && !isPrecise && element.x + element.width < x) {
                            selectedElement = element;
//                            MyReadLog.i(element.getPosition());
                            elementIndexInLine = j;
                            return selectedElement;
                        }
                        if (element.x <= x && element.x + element.width >= x) {
//                                && element.y <= y && element.y + element.height >= y) {
//                            MyReadLog.d("x = %d, y = %d, element.x = %d, element.y = %d, element.width = %d, element.height = %d", x,y,element.x, element.y,element.width,element.height);
                            selectedElement = element;
//                            MyReadLog.i(element.getPosition());
                            elementIndexInLine = j;
                            return selectedElement;
                        }

                    }
                }
            }
        }
        return selectedElement;
    }

    @Override
    public boolean canScroll(boolean isForward) {
        if (isForward) {
            return nextPage != null;
        } else {
//            MyReadLog.i("previous page is null ? - >" + (previousPage == null));
            return previousPage != null;
        }
    }

    @Override
    public synchronized void onScrollingFinished(final int pageIndex) {
//        MyReadLog.i("onScrollingFinished");
        boolean needAddPage = false;
        if (pageIndex == PAGE_POSITION_INDEX_NEXT) {
            if (isCalculatePages) {
                movePageSum++;
            } else {
                currentPageNum++;
            }
        } else if (pageIndex == PAGE_POSITION_INDEX_PREVIOUS) {
            if (isCalculatePages) {
                movePageSum--;
            } else {
                currentPageNum--;
            }
        }
        if (pageIndex == PAGE_POSITION_INDEX_NEXT) {
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
        } else if (pageIndex == PAGE_POSITION_INDEX_PREVIOUS) {
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
        } else {
            needAddPage = false;
        }

        if (needAddPage) {
            prepareService.execute(new Runnable() {
                @Override
                public void run() {
                    int htmlIndex = -1;
                    if (pageIndex == PAGE_POSITION_INDEX_NEXT) {
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
                        html.loadHtmlInputStream(htmlIndex, true);
                        pageArray.put(htmlIndex, html.getPages());
                        idArrays.put(htmlIndex, html.getIdPositions());
                        MyReadLog.i("put finished ==time=" + (System.currentTimeMillis() - startTime) + "=====" +
                                "pages size is " + html.getPages().size() +
                                " size change to " + pageArray.size());
                    }

                    if (pageArray.size() > 5) {
                        if (pageIndex == PAGE_POSITION_INDEX_NEXT) {
                            pageArray.removeAt(0);
                            idArrays.removeAt(0);
                        } else {
                            pageArray.removeAt(pageArray.size() - 1);
                            idArrays.removeAt(pageArray.size() - 1);
                        }
                        MyReadLog.i("pageArray size is" + pageArray.size());
                    }
                }
            });
        }
        String readPositionStr = currentPageKey + "-" + currentPage.getStartPosition();
        MyReadLog.i(readPositionStr);
        myBookModel.saveReadPosition(readPositionStr);
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
    public synchronized void paint(Bitmap bitmap, int pagePosition) {
        Canvas canvas = new Canvas(bitmap);
        if (pages != null && pages.size() > 0) {
            BookPage bookPage;
            if (myBookModel.getCoverPage() != null) {
                bookPage = myBookModel.getCoverPage();
            } else {
                bookPage = pages.get(0);
            }
            drawPage(bookPage, canvas);
            drawFootView(canvas, bookPage, PAGE_POSITION_INDEX_CURRENT);
        } else {
//            preparePage(myBookModel.getReadPosition());
            BookPage bookPage;
            switch (pagePosition) {
                case PAGE_POSITION_INDEX_PREVIOUS:
                    bookPage = previousPage;
                    break;
                case PAGE_POSITION_INDEX_CURRENT:
                    bookPage = currentPage;
                    break;
                case PAGE_POSITION_INDEX_NEXT:
                    bookPage = nextPage;
                    break;
                default:
                    bookPage = currentPage;
                    break;
            }
            drawPage(bookPage, canvas);
            drawFootView(canvas, bookPage, pagePosition);
            drawChapterInfo(canvas, bookPage);
        }
    }

    /**
     * 绘制章节相关信息
     *
     * @param canvas
     * @param bookPage
     */
    private void drawChapterInfo(Canvas canvas, BookPage bookPage) {
        if (myBookModel.tocElement == null) return;
        String chapterStr = TextUtils.isEmpty(myBookModel.book.title) ? " " : myBookModel.book.title;
        String startPosition = bookPage.getStartPosition(); // 获取开始的位置信息

//       chapterStr = startPosition;
        canvas.drawText(chapterStr, bookPage.lGap, bookPage.tGap - 8, paint);
    }


    @Override
    public void setCoverPage(BookCoverPage coverPage) {
        this.coverPage = coverPage;
        currentPage = coverPage;
    }

    /**
     * 绘制底部信息
     */
    private void drawFootView(Canvas canvas, BookPage page, int pageIndex) {
        paint.setTextSize(BookUIHelper.dp2px(12f));
        paint.setColor(Color.GRAY);
        // 绘制 时间
        int timeY = page.getPageHeight() - page.bGap / 2 + (int) (paint.getTextSize() / 2);
        canvas.drawText(BookStingUtil.getTimeStr(System.currentTimeMillis()), page.lGap, timeY, paint);

        // 绘制电池电量
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);
        RectF rectF = new RectF();
        rectF.top = timeY - 15;
        rectF.bottom = timeY;
        rectF.left = page.lGap + 7 * 12;
        rectF.right = rectF.left + 29;
        canvas.drawRect(rectF, paint);
        canvas.drawRoundRect(rectF, 3f, 3f, paint);

        paint.setStrokeWidth(3f);
        canvas.drawLine(rectF.left - 1, rectF.top + 5, rectF.left - 1, rectF.bottom - 5, paint);

        paint.setStyle(Paint.Style.FILL);
        RectF bRect = new RectF();
        bRect.top = rectF.top + 2;
        bRect.bottom = rectF.bottom - 2;
        bRect.left = rectF.left + 2;
        bRect.right = bRect.left + 10;
        canvas.drawRoundRect(bRect, 2f, 2f, paint);

        // 绘制页码相关信息
        if (!isCalculatePages && totalPages > 1) {
            String pageNumInfo;
            if (pageIndex == PAGE_POSITION_INDEX_PREVIOUS) {
                pageNumInfo = (currentPageNum - 1) + "/" + totalPages;
            } else if (pageIndex == PAGE_POSITION_INDEX_NEXT) {
                pageNumInfo = (currentPageNum + 1) + "/" + totalPages;
            } else {
                pageNumInfo = currentPageNum + "/" + totalPages;
            }
            paint.setTextSize(BookUIHelper.dp2px(12f));
            canvas.drawText(pageNumInfo, page.getPageWidth() / 2, timeY, paint);
        }
    }

    /**
     * 绘制页面信息
     *
     * @param bookPage
     */
    private void drawPage(BookPage bookPage, Canvas canvas) {
//        MyReadLog.i("drawPage");
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
            MyReadLog.i("imagePath = " + imagePath);
            Bitmap bitmap = ((BookTextImageElement) bookTextBaseElement).createBitmap(myBookModel.getImageInputStream(imagePath));
            if (bitmap != null) {
//                MyReadLog.i("imagePath = " + imagePath + "， bitmap size height = " + bitmap.getHeight() + ", width = " + bitmap.getWidth() + "elementWidth =  " + bookTextBaseElement.width + ", elementHeight = " + bookTextBaseElement.height);
                canvas.drawBitmap(bitmap, bookTextBaseElement.x, bookTextBaseElement.y, imagePaint);
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
    @Override
    public synchronized void preparePage(BookReadPosition readPosition) {
        MyReadLog.i("---------------preparePage----------------------");
        pageArray.clear();
        idArrays.clear();
//        if (pageArray.size() == 0) {
        int startIndex = readPosition.getPagePosition() - 2 > 0 ? readPosition.getPagePosition() - 2 : 0;
        int loadSum = (myBookModel.getSpinSize() - 1) - readPosition.getPagePosition() > 2
                ? readPosition.getPagePosition() + 3 - startIndex
                : (myBookModel.getSpinSize()) - startIndex;
        MyReadLog.d("startIndex = %d, loadSum = %d", startIndex, loadSum);
        long startTime = System.currentTimeMillis();
        for (int i = startIndex; i < startIndex + loadSum; i++) {
            EpubReaderHtml html = new EpubReaderHtml(myBookModel);
            html.loadHtmlInputStream(i, true);
            pageArray.put(i, html.getPages());
            idArrays.put(i, html.getIdPositions());
        }
        MyReadLog.i("prepare cost " + (System.currentTimeMillis() - startTime));
        MyReadLog.i("pageArray size is  " + pageArray.size());

        currentPageKey = readPosition.getPagePosition();
        MyReadLog.i("currentPageKey = " + currentPageKey + ", pageArray size is " + pageArray.size());
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
        setPageCurrentAndPreviousIndex();
//        }
    }

    private void setPageCurrentAndPreviousIndex() {
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

    private int startLineIndex, startElementIndex, endLineIndex, endElementIndex;
    private int startElementX, endElementX, startElementY, endElementY;
    private BookTextBaseElement startElement, endElement;
    private ArrayList<Rect> rects = new ArrayList<>();
    private boolean isLastActionMoveEndCursor = true;
    private int currentLineInfoHeight;

    @Override
    public boolean onFingerLongPress(int x, int y) {
        MyReadLog.i("onFingerLongPress");
        if (y < currentPage.tGap || y > currentPage.getPageHeight() - currentPage.bGap) {
            return false;
        }
        rects.clear();
        isSelectedTextRegion = true;
        startElement = endElement = findSelectedElement(x, y, false);
        startElementX = startElement.x;
        endElementX = startElementX + endElement.width;
        // 保存其实位置的索引
        startLineIndex = lineIndexInPage;
        startElementIndex = elementIndexInLine;

        BookLineInfo selectedLine = currentPage.getLindInfo(startLineIndex);
        startElementY = endElementY = selectedLine.y;
        currentLineInfoHeight = selectedLine.getLineHeight();
        rects.add(new Rect(startElement.x, selectedLine.y, startElement.x + startElement.width, selectedLine.y + selectedLine.getLineHeight()));
        application.getMyWidget().drawSelectedRegion(rects);
        return true;
    }

    @Override
    public void onFingerReleaseAfterLongPress(int x, int y) {
        MyReadLog.i("onFingerReleaseAfterLongPress");
        // 弹出菜单选择界面
    }

    @Override
    public void onFingerRelease(int x, int y) {
//        MyReadLog.i("onFingerRelease");
        application.getMyWidget().startAnimatedScrolling(x, y);
    }

    @Override
    public void onFingerSingleTap(int x, int y) {   //点击一下
        MyReadLog.i("onFingerSingleTap");
        BookTextBaseElement selectedElement = findSelectedElement(x, y, true);
        if (selectedElement != null) {
            String href = selectedElement.getLinkHref().trim();
            MyReadLog.i("href = " + href);
            if (!TextUtils.isEmpty(href)) {
                jumpLinkHref(href);
                return;
            } else {
                if (selectedElement instanceof BookTextImageElement) {
                    //  TODO test 展示大图
                    MyReadLog.i("selectedElement = BookTextImageElement");
                    return;
                }
            }
        }

        if (isSelectedTextRegion) {
            isSelectedTextRegion = false;
            isLastActionMoveEndCursor = true;
            application.getMyWidget().repaint();
            return;
        }
        // 暂时添加 事件
        boolean forward = x > application.getWindowSize().widthPixels / 2;
        application.getMyWidget().startAnimatedScrolling(forward ? PAGE_POSITION_INDEX_NEXT : PAGE_POSITION_INDEX_PREVIOUS, x, y, Direction.rightToLeft);
    }


    @Override
    public void onFingerDoubleTap(int x, int y) { // 双击
        MyReadLog.i("onFingerDoubleTap");
    }

    @Override
    public void onFingerMoveAfterLongPress(int x, int y) {
        // 长安拖动
        // 判断是否移动的是尾部游标
        boolean isMoveEndCursor;
        if (isLastActionMoveEndCursor) { //上一次移动是否是移动结束游标
            if (y < startElementY) {
                isMoveEndCursor = false;
            } else if (y > startElementY + currentLineInfoHeight) {
                isMoveEndCursor = true;
            } else {
                if (x < startElementX) {
                    isMoveEndCursor = false;
                } else if (x > startElementX + startElement.width) {
                    isMoveEndCursor = true;
                } else {
                    return;
                }
            }
        } else {
            if (y < endElementY) {
                isMoveEndCursor = false;
            } else if (y > endElementY + currentLineInfoHeight) {
                isMoveEndCursor = true;
            } else {
                if (x < endElementX) {
                    isMoveEndCursor = false;
                } else if (x > endElementX + endElement.width) {
                    isMoveEndCursor = true;
                } else {
                    return;
                }
            }
        }

//        MyReadLog.i("isMoveEndCursor = " +isMoveEndCursor);
        if (isMoveEndCursor != isLastActionMoveEndCursor) {
            if (isMoveEndCursor) {
//                startElement = endElement;
                BookLineInfo lineInfo = currentPage.getLindInfo(endLineIndex);
                if (endElementIndex == lineInfo.elements.size() - 1) {
                    startElementIndex = 0;
                    startLineIndex = endLineIndex + 1;
                    BookLineInfo nextLineInfo = currentPage.getLindInfo(startLineIndex);
                    startElementY = nextLineInfo.y + nextLineInfo.getLineHeight();
                    startElementX = nextLineInfo.x;
                    startElement = nextLineInfo.elements.get(0);
                } else {
                    startElementX = endElement.x;
                    startElementY = endElementY;
                    startElementIndex = endElementIndex + 1;
                    startLineIndex = endLineIndex;
                    startElement = lineInfo.elements.get(startElementIndex);
                }
            } else {
                if (startElementIndex == 0) {
                    endLineIndex = startLineIndex - 1;
                    BookLineInfo lineInfo = currentPage.getLindInfo(endLineIndex);
                    endElementIndex = lineInfo.elements.size() - 1;
                    endElementY = lineInfo.y + lineInfo.getLineHeight();
                    endElementX = lineInfo.x + lineInfo.getLineWidth();
                    endElement = lineInfo.elements.get(endElementIndex);
                } else {
                    endElementX = startElement.x;
                    endLineIndex = startLineIndex;
                    endElementIndex = startElementIndex - 1;
                    endElementY = startElementY;
                    BookLineInfo lineInfo = currentPage.getLindInfo(endLineIndex);
                    endElement = lineInfo.elements.get(endElementIndex);
                }
            }
            isLastActionMoveEndCursor = isMoveEndCursor;
        }

        rects.clear();

        if (isMoveEndCursor) {
            endElement = findSelectedElement(x, y, false);
            if (endElement == null) return;
            endLineIndex = lineIndexInPage;
            endElementIndex = elementIndexInLine;
        } else {
            startElement = findSelectedElement(x, y, false);
            if (startElement == null) return;
            startLineIndex = lineIndexInPage;
            startElementIndex = elementIndexInLine;
        }

//        MyReadLog.d("startLineIndex = %d, endLieIndex = %d, startElementIndex = %d , endElementIndex = %d", startLineIndex, endLineIndex, startElementIndex, endElementIndex);
        for (int i = startLineIndex; i < endLineIndex + 1; i++) {
            BookLineInfo lineInfo = currentPage.getLindInfo(i);
            int lineHeight = lineInfo.getLineHeight();
            Rect rect = new Rect(lineInfo.getRealStartX(), lineInfo.y, lineInfo.getRealEndX(), lineInfo.y + lineHeight);
            if (i == startLineIndex) {
                rect.left = startElement.x;
                MyReadLog.i("startX = " + rect.left + ", endX = " + lineInfo.getRealEndX());
            }
            if (i == endLineIndex) {
                rect.right = endElement.x + endElement.width;
            }
            rects.add(rect);
        }
        application.getMyWidget().drawSelectedRegion(rects);
    }

    @Override
    public void onFingerPress(int x, int y) {
        MyReadLog.i("onFingerPress");
//        Direction direction = Direction.rightToLeft;
        application.getMyWidget().startManualScrolling(x, y, Direction.rightToLeft);
    }

    @Override
    public void onFingerMove(int x, int y) {  // 移动
//        MyReadLog.i("onFingerMove");
        application.getMyWidget().scrollManuallyTo(x, y);
    }

    /**
     * 计算所有的的页面总数量
     */
    private volatile int totalPages;
    private volatile int currentPageNum; //当前页面的页码
    private volatile boolean isCalculatePages = true;
    private volatile int movePageSum = 0;

    public void calculateTotalPages() {
        isCalculatePages = true;
        long start = System.currentTimeMillis();
        int nowPageHtmlKey = currentPageKey;
        int nowPageIndex = currentPageIndex;
        int oldPageNum = 0;
        totalPages = 0;
        for (int i = 0; i < myBookModel.getSpinSize(); i++) {
            EpubReaderHtml html = new EpubReaderHtml(myBookModel);
            html.loadHtmlInputStream(i, false);
            html.getIndexInTocElement(totalPages);
            if (i == nowPageHtmlKey) {
                oldPageNum = totalPages + 1 + nowPageIndex;
            }
            totalPages = totalPages + html.getPages().size();
        }
        isCalculatePages = false;
        currentPageNum = oldPageNum + movePageSum;
        MyReadLog.i("calculateTotalPages cost " + (System.currentTimeMillis() - start) + "，totalPages =  " + totalPages);
    }

//    @Override
//    public void reset() {
//        pageArray.clear();
//        idArrays.clear();
//        preparePage(myBookModel.getReadPosition());
//    }
}
