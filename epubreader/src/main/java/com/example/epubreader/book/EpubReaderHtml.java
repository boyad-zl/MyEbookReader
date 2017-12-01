package com.example.epubreader.book;

import android.graphics.Paint;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.example.epubreader.ReaderApplication;
import com.example.epubreader.book.css.BookCSSAttributeSet;
import com.example.epubreader.book.css.BookTagAttribute;
import com.example.epubreader.book.tag.BodyControlTag;
import com.example.epubreader.book.tag.BookBasicControlTag;
import com.example.epubreader.book.tag.ImageControlTag;
import com.example.epubreader.util.BookAttributeUtil;
import com.example.epubreader.util.BookStingUtil;
import com.example.epubreader.util.MyReadLog;
import com.example.epubreader.view.book.BookLineInfo;
import com.example.epubreader.view.book.BookPage;
import com.example.epubreader.view.book.element.BookTextBaseElement;
import com.example.epubreader.view.book.element.BookTextImageElement;
import com.example.epubreader.view.book.element.BookTextWordElement;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 用于加载html文件，并解析，文件
 * Created by Boyad on 2017/11/2.
 */
public class EpubReaderHtml {
    private static final byte PARSE_NO_START = 0;
    private static final byte PARSE_DOING = 1;
    private static final byte PARSE_END = 2;
    private byte isReadHead = PARSE_NO_START;
    private byte isReadBody = PARSE_NO_START;

    public BookModel bookModel;
    private BookResourceFile CSSFile;
    public BookContentElement mainContentElement; // 主显示内容（主要为标签为body的主元素）
    private volatile BookContentElement contentElement; //用于计算各元素；

    private int depth = -1;// 记录当前元素的深度
    private BookCSSAttributeSet currentAttributeSet; // 用到的CSS
    private ArrayList<String> cssArrayList; //用于记录用到的css文件


    private volatile ArrayList<BookPage> bookPages = new ArrayList<>(); //保存html生成的页面信息
    private ArrayList<BookTextBaseElement> textElements;
    private Paint paint = new Paint();

    public EpubReaderHtml(BookModel bookModel) {
        this.bookModel = bookModel;
    }


    public void parseHtmlByPull(InputStream inputStream) {
        if (inputStream == null) return;
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new InputStreamReader(inputStream));
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {

                MyReadLog.i("getEventType ->" + parser.getEventType() + ", getDepth =" + parser.getDepth());
                if (parser.getEventType() == XmlPullParser.START_TAG) {
                    String taName = parser.getName();
                    String classStr = parser.getAttributeValue(null, "class");
                    String styleStr = parser.getAttributeValue(null, "style");
                    MyReadLog.d("Start:   tagName = %s, classStr = %s, styleStr = %s ", taName, classStr, styleStr);

                    if (taName.toLowerCase().equals("p")) {

                        MyReadLog.i("nextText =" + parser.nextText());

                        MyReadLog.i("nextTag =" + parser.nextTag() + ", getDepth = " + parser.getDepth());
                    }

                } else if (parser.getEventType() == XmlPullParser.END_TAG) {
                    String tagName = parser.getName();
                    MyReadLog.i("End : tagName = " + tagName);
                }
                parser.next();

            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载流
     *
     * @param inputStream
     */
    public void loadHtmlInputStream(InputStream inputStream) {
        if (inputStream == null) return;
        long startTime = System.currentTimeMillis();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();

                // 解析head部分
                if (isReadHead == PARSE_NO_START && line.contains("<head")) {
                    isReadHead = PARSE_DOING;
                }
                if (isReadHead == PARSE_DOING) {
                    addNeedCSSFile(line);
                    if (line.contains("</head>")) {
                        isReadHead = PARSE_END;
                        // 将当前文件需要用到的CSS文件转换成BookCSSAttributeSet
                        currentAttributeSet = bookModel.cssAttributeSet.getNewCSSAttribute(cssArrayList);
                    }
                }

                // 解析body部分
                if (isReadBody == PARSE_NO_START && line.contains("<body")) {
                    isReadBody = PARSE_DOING;
                }
                if (isReadBody == PARSE_DOING) {
//                    MyReadLog.i(line);
                    addToContent(line);
                    if (line.contains("</body>")) {
                        isReadBody = PARSE_END;
                        if (contentElement == null) MyReadLog.i("contentElement is null");
                        mainContentElement = contentElement;
                    }
                }
            }
            long parseHtmlTime = System.currentTimeMillis();

            buildBookPages();
            MyReadLog.i("parseTocFile html cost  is  " + (parseHtmlTime - startTime) + ", cut to pages cost  " + (System.currentTimeMillis() - parseHtmlTime));
//            if (mainContentElement == null) {
//                MyReadLog.i("mainContentElement is null");
//            } else {
//                MyReadLog.i("主元素里面的元素数量是 " + mainContentElement.getElementSize());
//                MyReadLog.i("主元素里面子元素内的元素数量是 " + mainContentElement.getIndex(0).getElementSize());
//                MyReadLog.i("css 大小" + currentAttributeSet.getSingleCSSSets().size());
//                if (currentAttributeSet != null && currentAttributeSet.getSingleCSSSets() != null && currentAttributeSet.getSingleCSSSets().size() > 0) {
//                    for (int i = 0; i < currentAttributeSet.getSingleCSSSets().size(); i++) {
//                        BookSingleCSSSet singleCSSSet = currentAttributeSet.getSingleCSSSets().valueAt(i);
//                        String name = currentAttributeSet.getSingleCSSSets().keyAt(i);
//                        MyReadLog.i(name + " unDefault class size : " + singleCSSSet.classes.size());
//                        MyReadLog.i(name + " default class size : " + singleCSSSet.rootClasses.size());
//                    }
//                }
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO TEST: 暂时不识别注释
     * 添加信息到需要显示的内容中
     *
     * @param line
     */
    private void addToContent(String line) {
        if (TextUtils.isEmpty(line)) return;
        char[] element = line.toCharArray();
        int index = 0;
//        MyReadLog.i(line);
        while (index < element.length) {
//            MyReadLog.i("index: " + index + " : " + element[index] + "next char " + element[index + 1] + " , depth = " + depth);
            if (element[index] == '<') {
                int endBracketIndex = line.indexOf(">", index + 1);  // '<'后的'>'的索引位置
                if (element[index + 1] == '/') {
                    // 判断 "</...>" 情况
                    index = endBracketIndex + 1;
//                    MyReadLog.i("depth = " + depth + " : " + contentElement.getPosition() + ", tag = " + contentElement.getControlTag().getTagName());
                    depth--;
                    if (depth > -1) {
                        BookContentElement parentContentElement = contentElement.getParent();
                        contentElement = parentContentElement;
                    }
                } else if (element[endBracketIndex - 1] == '/') {
                    // 判断 <.../>
                    int spaceIndex = line.indexOf(" ", index);
                    String tagName;
                    String attributeStr = "";
                    if (spaceIndex != -1 && spaceIndex < endBracketIndex - 1) {
                        // 例如<img class= ".." src = ".."/>
                        tagName = line.substring(index + 1, spaceIndex).trim().toLowerCase();
                        attributeStr = line.substring(spaceIndex, endBracketIndex - 1).trim();
//                        MyReadLog.i("tagName : "+tagName+", attribute--> " + attributeStr);
                    } else {
                        // 例如<br/>
                        tagName = line.substring(index + 1, endBracketIndex - 1).trim().toLowerCase();
                    }
//                    MyReadLog.i("这里是元素开始:" + tagName);
                    BookBasicControlTag controlTag = BookTagManagerFactory.createControlTag(tagName, TextUtils.isEmpty(attributeStr) ? "" : attributeStr, currentAttributeSet);
                    if (controlTag instanceof ImageControlTag) {
                        ((ImageControlTag) controlTag).setImageData(bookModel.getImageInputStream(((ImageControlTag) controlTag).getImagePathStr()));
                    }
                    BookContentElement childElement = new BookContentElement(contentElement, controlTag);
                    contentElement.addControlElement(childElement);
                    index = endBracketIndex + 1;
                } else {
                    // 判断 <...>
                    int spaceIndex = line.indexOf(" ", index);
                    String tagName;
                    String attributeStr = "";
                    if (spaceIndex != -1 && spaceIndex < endBracketIndex) {
                        // 例如<p class= ".." style = "..">
                        tagName = line.substring(index + 1, spaceIndex).trim().toLowerCase();
                        attributeStr = line.substring(spaceIndex, endBracketIndex).trim();
//                        MyReadLog.i("attribute-->" + attributeStr);
                    } else {
                        // 例如<p>,或者<h1>
                        tagName = line.substring(index + 1, endBracketIndex).trim().toLowerCase();
                    }
//                    MyReadLog.i("这里是元素开始:" + tagName);
                    BookBasicControlTag controlTag = BookTagManagerFactory.createControlTag(tagName, TextUtils.isEmpty(attributeStr) ? "" : attributeStr, currentAttributeSet);
                    if (depth == -1) {
//                        MyReadLog.i("开始阅读阅读body");
                        contentElement = new BookContentElement(controlTag);
                    } else {
                        BookContentElement childElement = new BookContentElement(contentElement, controlTag);
                        contentElement.addControlElement(childElement);
                        contentElement = childElement;
                    }
                    depth++;
                    index = endBracketIndex + 1;
                }
            } else {
                int nextStartBracketIndex = line.indexOf("<", index);
                String content;
                if (nextStartBracketIndex > -1) {
                    content = line.substring(index, nextStartBracketIndex);
                    index = nextStartBracketIndex;

                } else {
                    content = line.substring(index);
                    index = line.length();
                }
                content = content.replaceAll("\\n", " ").replaceAll(" +", " "); // 将换号符号换成空格，再将多个空格换成一个空格
                if (!TextUtils.isEmpty(content.trim())) {
                    contentElement.addTextElement(content.trim());
                }

            }
        }
    }

    /**
     * todo test : 暂时做统一处理 没有区分js其他的信息
     * 获取需要用到的CSS
     *
     * @param line
     */
    private void addNeedCSSFile(String line) {
        if (line.startsWith("<link") && line.contains("href")) {
            String hrefValue = BookStingUtil.getDataValue(line, "\"", "\"", line.indexOf("href")).trim();

            String opfDir = bookModel.getOpfDir();
            if (!TextUtils.isEmpty(opfDir)) {
                if (hrefValue.startsWith("..")) {
                    hrefValue = hrefValue.replace("..", opfDir);
                } else {
                    hrefValue = opfDir + "/" + hrefValue;
                }
            }
            MyReadLog.i(hrefValue);
            if (cssArrayList == null) {
                cssArrayList = new ArrayList();
            }
            cssArrayList.add(hrefValue);
        }
    }

    /**
     * 将mainContentElement转换成行信息
     */
    private int elementIndex = 0;

    public synchronized void buildBookPages() {
//        MyReadLog.i("buildBookPages");
        if (mainContentElement == null || mainContentElement.getElementSize() < 0) return;
        bookPages.clear();
        long startTime = System.currentTimeMillis();
        textElements = mainContentElement.getTextElements(); // TODO TEST  此处耗时较长 待优化
        long startMeasureElementTime = System.currentTimeMillis();
        measureTextElementsSize();
        MyReadLog.i("getTextElements cost " + (System.currentTimeMillis() - startTime) + ", measure element cost " + (System.currentTimeMillis() - startMeasureElementTime));
        if (mainContentElement.getControlTag() instanceof BodyControlTag) {
            BodyControlTag controlTag = (BodyControlTag) mainContentElement.getControlTag();
            int pageWidth = ReaderApplication.getInstance().getWindowSize().widthPixels;
            int pageHeight = ReaderApplication.getInstance().getWindowSize().heightPixels;
            BookPage page = new BookPage(controlTag, pageWidth);
//            BookLineInfo lastLineInfo; // 上一行信息
            int lineInfoStartX = page.lGap;
            int lineInfoStartY = page.tGap;
            elementIndex = 0;
            BookPage childPage = null ;
//            MyReadLog.i("pageWidth = " + pageWidth + ", lGap = " + lineInfoStartX + ", tGap = " + lineInfoStartY);
            while (elementIndex < textElements.size()) {
//                long buildLineStartTime = System.currentTimeMillis();
                if (childPage == null) {
                    childPage = new BookPage(page, pageHeight);
                }
                BookLineInfo lineInfo = buildBookLineInfo(pageWidth - page.rGap - page.lGap, pageHeight - page.tGap - page.bGap, lineInfoStartX, lineInfoStartY);
                if (lineInfoStartY + lineInfo.getLineHeight() > pageHeight - page.bGap) {
                    childPage.finishAdd();
                    bookPages.add(childPage);
                    childPage = new BookPage(page, pageHeight);
                    lineInfo.moveUp(lineInfoStartY - page.tGap);
                    childPage.addLineInfo(lineInfo);
                    lineInfoStartY = page.tGap;
                } else {
                    childPage.addLineInfo(lineInfo);
//                    page.addLineInfo(lineInfo);
                }
//                MyReadLog.i("build line cost " + (System.currentTimeMillis() - buildLineStartTime));
//                lastLineInfo = lineInfo;
                lineInfoStartY = lineInfoStartY + lineInfo.getLineHeight();
                lineInfoStartX = page.lGap;
            }

            if (!bookPages.contains(childPage) && childPage != null){
                childPage.finishAdd();
                bookPages.add(childPage);
            }
            MyReadLog.i("line size is " + page.getLineSize() + ", cost time is  " + (System.currentTimeMillis() - startTime));
//            bookPages = page.cutToPages(pageHeight);
        }
    }

    /**
     * 测量 textElements内的文字大小
     */

    private void measureTextElementsSize() {
        measureOnMainThread();
//          TODO test 多线程效果不明显，暂时放弃
        if (true) return;
        int threadCount = ReaderApplication.getInstance().getCoreNumber();
        ThreadPoolExecutor measureSizeService = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount, new PriorityHighThreadFactory());
//        MyReadLog.i("size = " + textElements.size());
        int threadMeasureSize = textElements.size() / threadCount;
        int remainderSize = textElements.size() % threadCount;

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {
            int startIndex = i * threadMeasureSize + Math.min(i, remainderSize);
            int endIndex = startIndex + threadMeasureSize + (i < remainderSize ? 0 : -1);
//            MyReadLog.d("startIndex = %d, endIndex = %s", startIndex, endIndex);
            measureSizeService.execute(new MeasureElementSizeRunnable(textElements, startIndex, endIndex));
        }
//        }

        while (measureSizeService.getCompletedTaskCount() < threadCount) {
            try {
//                MyReadLog.i("measureSizeService.getCompletedTaskCount() = " + measureSizeService.getCompletedTaskCount() + " / " + threadCount);
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        attributeSet = null;
//        lastElement = null;
        MyReadLog.i("measureOnThreadPool cost " + (System.currentTimeMillis() - startTime));
    }

    private void measureOnMainThread() {
        long startTime = System.currentTimeMillis();
        Paint paint = new Paint();
        for (int i = 0; i < textElements.size(); i++) {
            BookTextBaseElement element = textElements.get(i);
            if (!(element instanceof BookTextImageElement)) {
                if (attributeSet == null || element.isPositionChange(lastElement)) {
                    attributeSet = element.getAttributeSet();
                    fontSize = BookAttributeUtil.getFontSize(attributeSet);
                    paint.setTextSize(fontSize);
                }
//                BookTextWordElement wordElement = (BookTextWordElement) element;
                element.measureSize(fontSize, paint);
                lastElement = element;
            }
        }
        attributeSet = null;
        lastElement = null;
        MyReadLog.i("measureOnMainThread cost " + (System.currentTimeMillis() - startTime));
    }

    /**
     * 构建行信息
     *
     * @param pageWidth
     * @param lineInfoStartX
     * @param lineInfoStartY
     * @return
     */
    private ArrayMap<String, BookTagAttribute> attributeSet = null;
    private BookTextBaseElement lastElement = null;
    private ArrayMap<String, BookTagAttribute> paragraphAttributeSet = null;
    private int leftGap, rightGap;
    private int fontSize;
    private float lineHeightRate  = 1.2f;

    private BookLineInfo buildBookLineInfo(int pageWidth, int pageHeight, int lineInfoStartX, int lineInfoStartY) {
        int startX = lineInfoStartX;
//        ArrayMap<String, BookTagAttribute> attributeSet = null;
//        BookTextBaseElement lastElement = null;
//        ArrayMap<String, BookTagAttribute> paragraphAttributeSet = null;
        BookLineInfo lineInfo = new BookLineInfo(pageWidth, lineInfoStartX, lineInfoStartY);
        lineInfo.lineHeightRate = lineHeightRate;
        int lineWidth = lineInfo.getLineWidth();
        boolean isFirstElement = true;
//        int leftGap = 0;
//        int rightGap = 0;

//        MyReadLog.i("elementIndex --->" + elementIndex + ", element size is" + textElements.size());
//        long startTime = System.currentTimeMillis();
        boolean needAlignOffset = false;

        while (elementIndex < textElements.size()) {
            BookTextBaseElement element = textElements.get(elementIndex);
            if (attributeSet == null || element.isPositionChange(lastElement)) {
//                long attributeInitTime = System.currentTimeMillis();
                if (!element.isInOneParagraph(lastElement)) {
                    if (lastElement != null) {
                        lineInfo.bottomGap = BookAttributeUtil.getMargin(lastElement.getAttributeSet(), BookAttributeUtil.POSITION_BOTTOM, pageHeight)
                                + BookAttributeUtil.getPadding(lastElement.getAttributeSet(), BookAttributeUtil.POSITION_BOTTOM, pageHeight);
                    }
                    lineInfo.alignLineInfo(BookAttributeUtil.getTextAlign(attributeSet));
//                    MyReadLog.i("alignLineInfo");
                    lastElement = null;
                    paragraphAttributeSet = null;
                    break;
                }
                attributeSet = element.getAttributeSet(); // 获取显示元素的属性信息
                if (paragraphAttributeSet == null) {
                    paragraphAttributeSet = element.getParagraphAttributeSet();
                }
                lineHeightRate = BookAttributeUtil.getLineHeight(attributeSet); //解析line-height;
                lineInfo.lineHeightRate = lineHeightRate;
                fontSize = BookAttributeUtil.getFontSize(attributeSet);
                int paragraphWidth = BookAttributeUtil.getWidth(attributeSet, pageWidth);
                if (paragraphWidth == -1 || element instanceof BookTextImageElement) {
//                    lineWidth = pageWidth;
//                    MyReadLog.i("paragraphWidth :" + lineWidth );
                } else {
                    MyReadLog.i("设置了段落宽度");
                    lineWidth = paragraphWidth;
                    lineInfo.setLineWidth(lineWidth);
                }
                needAlignOffset = BookAttributeUtil.needVerticalAlignOffset(attributeSet);
                leftGap = BookAttributeUtil.getMargin(attributeSet, BookAttributeUtil.POSITION_LEFT, pageWidth)
                        + BookAttributeUtil.getPadding(attributeSet, BookAttributeUtil.POSITION_LEFT, pageWidth);
                rightGap = BookAttributeUtil.getMargin(attributeSet, BookAttributeUtil.POSITION_RIGHT, pageWidth)
                        + BookAttributeUtil.getPadding(attributeSet, BookAttributeUtil.POSITION_RIGHT, pageWidth);
//                MyReadLog.i("attribute init  cost " + (System.currentTimeMillis() - attributeInitTime));
            }

            int indentGap = 0;
            if (element.isParagraphStart()) {
                lineInfo.topGap = BookAttributeUtil.getMargin(attributeSet, BookAttributeUtil.POSITION_TOP, pageHeight)
                        + BookAttributeUtil.getPadding(attributeSet, BookAttributeUtil.POSITION_TOP, pageHeight);
                indentGap = BookAttributeUtil.getTextIndent(attributeSet, lineInfo.getLineWidth(), BookAttributeUtil.getFontSize(paragraphAttributeSet));
//                MyReadLog.i("indentGap ->" + indentGap);
                element.x = lineInfoStartX + indentGap;
//                pageWidth = pageWidth - indentGap;
//                startX = startX + indentGap;
            } else {
                element.x = lineInfoStartX;
            }

//            if (element instanceof BookTextImageElement){
//                MyReadLog.i("pageWidth  " + pageWidth);
//            }
            // todo test 获取元素的宽高
            int allowMaxWidth = pageWidth - indentGap - rightGap - leftGap;
            int elementWidth = element.getWidth(fontSize, allowMaxWidth, pageHeight);
            int elementHeight = element.getHeight(pageHeight);

            if (elementWidth == -1) {
                lineInfoStartX = lineWidth - rightGap;
            } else {
                lineInfoStartX = element.x + elementWidth;
            }

            // 获取左边距信息
            if (isFirstElement) {
                element.x = element.x + leftGap;
                lineInfoStartX = element.x + elementWidth;
                isFirstElement = false;
            }
            if (element instanceof BookTextImageElement) {
                element.y = lineInfoStartY - elementHeight;
            } else {
                element.y = lineInfoStartY - element.descent;
            }
            if (needAlignOffset) {
                element.y = element.y - BookAttributeUtil.getVerticalAlign(attributeSet, lastElement == null ? BookAttributeUtil.getFontSize(paragraphAttributeSet) : lastElement.height, elementHeight);
            }
//            MyReadLog.i("lineInfoStartX = " + lineInfoStartX + ", elementWidth = " + elementWidth);

            if (startX + lineWidth - rightGap < lineInfoStartX) {
//                MyReadLog.d("lineWidth = %d, rightGap = %d, element.x = %d, startX = %d", lineWidth, rightGap, element.x, startX);
//                long startRebuildTime = System.currentTimeMillis();
                lineInfo.rebuildLineInfo(startX + lineWidth - rightGap - element.x);
//                MyReadLog.i("rebuild line info cost time " + (System.currentTimeMillis() - startRebuildTime));
                break;
            }

            lineInfo.addTextElement(element);
            lastElement = element;
            elementIndex++;
        }
        if (elementIndex >= textElements.size()) {
//            MyReadLog.i("elementIndex >= textElements.size()");
            lineInfo.alignLineInfo(BookAttributeUtil.getTextAlign(attributeSet));
        }
//        long startSetLineHeight = System.currentTimeMillis();
        lineInfo.setLineHeight();
//        MyReadLog.i("build line cost time is " + (System.currentTimeMillis() - startTime) + ", set line height cost " + (System.currentTimeMillis() - startSetLineHeight));
        return lineInfo;
    }

    /**
     * 获取paint ，用于测量element的长度
     *
     * @param attributeArrayMap
     * @return
     */
    public void setPaint(ArrayMap<String, BookTagAttribute> attributeArrayMap) {
        paint.setTextSize(BookAttributeUtil.getFontSize(attributeArrayMap));
    }

    public ArrayList<BookPage> getPages() {
        return bookPages;
    }


    private class MeasureElementSizeRunnable implements Runnable {
        private ArrayList<BookTextBaseElement> elements;
        private int startIndex, endIndex;
        private Paint elementPaint;
        private ArrayMap<String, BookTagAttribute> attributeArrayMap = null;
        private int elementFontSize;
        private BookTextBaseElement lastTextElement;

        public MeasureElementSizeRunnable(ArrayList<BookTextBaseElement> elements, int startIndex, int endIndex) {
            this.elements = elements;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            elementPaint = new Paint();
        }

        @Override
        public void run() {
            for (int i = startIndex; i < endIndex + 1; i++) {
                BookTextBaseElement element = elements.get(i);
                if (element instanceof BookTextWordElement) {
                    if (attributeArrayMap == null || element.isPositionChange(lastTextElement)) {
                        attributeArrayMap = element.getAttributeSet();
                        elementFontSize = BookAttributeUtil.getFontSize(attributeArrayMap);
                        elementPaint.setTextSize(elementFontSize);
                    }
                    element.measureSize(elementFontSize, elementPaint);
                    lastTextElement = element;
                }
            }
        }
    }
}
