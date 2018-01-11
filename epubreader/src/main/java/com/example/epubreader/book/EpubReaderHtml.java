package com.example.epubreader.book;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.example.epubreader.ReaderApplication;
import com.example.epubreader.book.css.BookCSSAttributeSet;
import com.example.epubreader.book.css.BookTagAttribute;
import com.example.epubreader.book.tag.BodyControlTag;
import com.example.epubreader.book.tag.BookBasicControlTag;
import com.example.epubreader.book.tag.ImageControlTag;
import com.example.epubreader.book.toc.TocElement;
import com.example.epubreader.util.BookAttributeUtil;
import com.example.epubreader.util.MyReadLog;
import com.example.epubreader.view.book.BookLineInfo;
import com.example.epubreader.view.book.BookPage;
import com.example.epubreader.view.book.BookReadPosition;
import com.example.epubreader.view.book.element.BookTextBaseElement;
import com.example.epubreader.view.book.element.BookTextImageElement;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * 用于加载html文件，并解析，文件
 * Created by Boyad on 2017/11/2.
 */
public class EpubReaderHtml {
    private static final String HTML_TAG_NAME_HEAD = "head";
    private static final String HTML_TAG_NAME_BODY = "body";

    public BookModel bookModel;
    public BookContentElement mainContentElement; // 主显示内容（主要为标签为body的主元素）
    private volatile BookContentElement contentElement; //用于计算各元素；

    private BookCSSAttributeSet currentAttributeSet; // 用到的CSS
    private ArrayList<String> cssArrayList; //用于记录用到的css文件

    private volatile ArrayList<BookPage> bookPages = new ArrayList<>(); //保存html生成的页面信息
    private ArrayList<BookTextBaseElement> textElements;
    private ArrayMap<String, String> idPositions = new ArrayMap<>(); //存放id对应的位置

    private int htmlIndex;
    private boolean needShow;

    public EpubReaderHtml(BookModel bookModel) {
        this.bookModel = bookModel;
    }

    private void parseHtml(InputStream inputStream) {
//        cssArrayList.clear(); //TODO TEST
//        currentAttributeSet.reset();
//        contentElement.getContentElements().clear();
        long startTime = System.currentTimeMillis();
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new InputStreamReader(inputStream));
            parser.defineEntityReplacementText("nbsp", "&nbsp;");
            String opfDir = bookModel.getOpfDir();
            int type = parser.getEventType();
            final byte start = 0;
            final byte doing = 1;
            final byte stop = 2;
            byte readHeadProgress = start;
            byte readBodyProgress = start;
            while (type != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (type) {
                    case XmlPullParser.START_TAG:
                        if (readHeadProgress == start && tagName.equals(HTML_TAG_NAME_HEAD)) {
                            readHeadProgress = doing;
                            break;
                        }
                        if (readHeadProgress == doing) {
                            if (tagName.equals("link")) {
                                String rel = parser.getAttributeValue(null, "rel");
                                if (rel != null && !TextUtils.isEmpty(rel)) {
                                    if (rel.equals("stylesheet")) {
                                        String hrefStr = parser.getAttributeValue(null, "href");
                                        if (hrefStr != null && !TextUtils.isEmpty(hrefStr)) {
                                            if (!TextUtils.isEmpty(opfDir)) {
                                                if (hrefStr.startsWith("..")) {
                                                    hrefStr = hrefStr.replace("..", opfDir);
                                                } else {
                                                    hrefStr = opfDir + "/" + hrefStr;
                                                }
//                                                MyReadLog.i("hrefStr =  " + hrefStr);
                                                if (cssArrayList == null) {
                                                    cssArrayList = new ArrayList();
                                                }
                                                cssArrayList.add(hrefStr);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (readBodyProgress == start && tagName.equals(HTML_TAG_NAME_BODY)) {
                            readBodyProgress = doing;
                            if (contentElement == null) {
                                int count = parser.getAttributeCount();
                                ArrayMap<String, String> attributeMaps = new ArrayMap<>();
                                for (int i = 0; i < count; i++) {
                                    String key = parser.getAttributeName(i);
                                    String value = parser.getAttributeValue(i).trim();
                                    if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                                        attributeMaps.put(key, value);
                                    }
                                }
                                BookBasicControlTag controlTag = BookTagManagerFactory.createControlTagByArrayMap(HTML_TAG_NAME_BODY, attributeMaps, currentAttributeSet);
                                contentElement = new BookContentElement(controlTag);
                            }
                            break;
                        }
                        if (readBodyProgress == doing) {
                            int count = parser.getAttributeCount();
                            ArrayMap<String, String> attributeMaps = new ArrayMap<>();
                            for (int i = 0; i < count; i++) {
                                String key = parser.getAttributeName(i);
                                String value = parser.getAttributeValue(i).trim();
                                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                                    attributeMaps.put(key, value);
                                }
                            }
                            BookBasicControlTag controlTag = BookTagManagerFactory.createControlTagByArrayMap(tagName, attributeMaps, currentAttributeSet);
                            if (controlTag instanceof ImageControlTag) {
                                ((ImageControlTag) controlTag).setImageData(bookModel.getImageInputStream(((ImageControlTag) controlTag).getImagePathStr()));
                            }
                            BookContentElement childElement = new BookContentElement(contentElement, controlTag);
                            contentElement.addControlElement(childElement);
                            contentElement = childElement;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if (!parser.isWhitespace() && readBodyProgress == doing) {
                            String contentText = parser.getText().trim();
                            if (contentElement != null && !TextUtils.isEmpty(contentText)) {
                                contentElement.addTextElement(contentText);
                            }
//                            MyReadLog.i("contentText = " + contentText);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (readHeadProgress == doing && tagName.equals(HTML_TAG_NAME_HEAD)) {
                            readHeadProgress = stop;
                            if (bookModel.cssAttributeSet != null && bookModel.cssAttributeSet.getSingleCSSSets().size() > 0) {
                                currentAttributeSet = bookModel.cssAttributeSet.getNewCSSAttribute(cssArrayList);
                            }
                        } else if (readBodyProgress == doing) {
                            if (tagName.equals(HTML_TAG_NAME_BODY)) {
                                readBodyProgress = stop;
                                break;
                            } else {
                                if (contentElement != null) {
                                    contentElement = contentElement.getParent();
                                }
                            }
                        }
                        break;
                }
                type = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        MyReadLog.i("parseHtml cost time is " + (System.currentTimeMillis() - startTime));
    }

    /**
     * 加载流
     *
     * @param needShow 是否用于展示，如果needShow 为true，则需要计算align，和计算剩余边距分配
     * @param htmlIndex
     */
    public void loadHtmlInputStream(int htmlIndex, boolean needShow) {
        this.needShow = needShow;
        this.htmlIndex = htmlIndex;
        if (htmlIndex < 0 || htmlIndex > bookModel.getSpinSize() - 1) return;
        long startTime = System.currentTimeMillis();
        InputStream inputStream = bookModel.getTextContent(htmlIndex);
        if (inputStream == null) return;
        parseHtml(inputStream);
        mainContentElement = contentElement;

        textElements = mainContentElement.getAllTextElements();
        idPositions.putAll(mainContentElement.getIdPosition());
        buildBookPages();

        long parseHtmlTime = System.currentTimeMillis();
//        MyReadLog.i("parseTocFile html cost  is  " + (parseHtmlTime - startTime) + ", cut to pages cost  " + (System.currentTimeMillis() - parseHtmlTime));
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
        if (mainContentElement.getControlTag() instanceof BodyControlTag) {
            BodyControlTag controlTag = (BodyControlTag) mainContentElement.getControlTag();
            int pageWidth = ReaderApplication.getInstance().getWindowSize().widthPixels;
            int pageHeight = ReaderApplication.getInstance().getWindowSize().heightPixels;
            BookPage page = new BookPage(controlTag, pageWidth);
            page.setGap();
            int lineInfoStartX = page.lGap;
            int lineInfoStartY = page.tGap;
            elementIndex = 0;
            BookPage childPage = null;
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
                }
//                MyReadLog.i("build line cost " + (System.currentTimeMillis() - buildLineStartTime));
                lineInfoStartY = lineInfoStartY + lineInfo.getLineHeight();
                lineInfoStartX = page.lGap;
            }

            if (!bookPages.contains(childPage) && childPage != null) {
                childPage.finishAdd();
                bookPages.add(childPage);
            }
//            MyReadLog.i("line size is " + page.getLineSize() + ", cost time is  " + (System.currentTimeMillis() - startTime));
        }
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
    private float lineHeightRate = 1.2f;

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
        int alignOffset = 0;

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
                if (!needAlignOffset) alignOffset = 0;
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
                if (alignOffset == 0) {
                    alignOffset = BookAttributeUtil.getVerticalAlign(attributeSet, lastElement == null ? BookAttributeUtil.getFontSize(paragraphAttributeSet) : lastElement.height, elementHeight);
                }
//                MyReadLog.i("alignOffset = " + alignOffset);
                element.y = element.y - alignOffset;
            }
//            MyReadLog.i("lineInfoStartX = " + lineInfoStartX + ", elementWidth = " + elementWidth);

            if (startX + lineWidth - rightGap < lineInfoStartX) {
//                MyReadLog.d("lineWidth = %d, rightGap = %d, element.x = %d, startX = %d", lineWidth, rightGap, element.x, startX);
//                long startRebuildTime = System.currentTimeMillis();
                if (needShow) {
                    lineInfo.rebuildLineInfo(startX + lineWidth - rightGap - element.x);
                }
//                MyReadLog.i("rebuild line info cost time " + (System.currentTimeMillis() - startRebuildTime));
                break;
            }

            lineInfo.addTextElement(element);
            lastElement = element;
            elementIndex++;
        }
        if (needShow && elementIndex >= textElements.size()) {
//            MyReadLog.i("elementIndex >= textElements.size()");
            lineInfo.alignLineInfo(BookAttributeUtil.getTextAlign(attributeSet));
        }
//        long startSetLineHeight = System.currentTimeMillis();
        lineInfo.setLineHeight();
//        MyReadLog.i("build line cost time is " + (System.currentTimeMillis() - startTime) + ", set line height cost " + (System.currentTimeMillis() - startSetLineHeight));
        return lineInfo;
    }

    public ArrayList<BookPage> getPages() {
        return bookPages;
    }

    public ArrayMap<String, String> getIdPositions() {
        return idPositions;
    }

    /**
     * 获取tocElement的页码信息
     * @param startIndex
     */
    public void getIndexInTocElement(int startIndex) {
        ArrayMap<String, Integer> currentHtmlTocments = bookModel.getHtmlTocElement(htmlIndex);
        if (currentHtmlTocments != null) {
            for (int i = 0; i < currentHtmlTocments.size(); i++) {
                String inHtmlId = currentHtmlTocments.keyAt(i);
                TocElement childElement = bookModel.tocElement.getElementAt(currentHtmlTocments.valueAt(i), true);
                if (!TextUtils.isEmpty(inHtmlId.trim())) {
                    String inHtmlPosition = idPositions.get(inHtmlId);
//                    MyReadLog.i(inHtmlPosition);
                    childElement.setPosition(inHtmlPosition);
                    String[] positions = inHtmlPosition.split("/");
                    BookReadPosition position;
                    if (positions.length > 1) {
                        position = new BookReadPosition(htmlIndex, positions[0], Integer.valueOf(positions[1]));
                    } else {
                        position = new BookReadPosition(htmlIndex, positions[0], 0);
                    }
                    childElement.setPageIndex(startIndex +  1);
                    for (int j = 0; j < bookPages.size(); j++) {
                        BookPage bookPage = bookPages.get(j);
                        if (bookPage.containElement(position)){
                            childElement.setPageIndex(startIndex + 1 + j);
                            break;
                        }
                    }
                } else {
                    childElement.setPosition("0:0:0/0");
                    childElement.setPageIndex(startIndex +  1);
                }
//                currentHtmlTocments.valueAt(i).setPosition(idPositions.get(currentHtmlTocments.keyAt(i)));
            }
        }
    }

    public int getHtmlIndex() {
        return htmlIndex;
    }
}
