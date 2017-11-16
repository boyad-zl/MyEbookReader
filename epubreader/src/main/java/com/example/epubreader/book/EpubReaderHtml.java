package com.example.epubreader.book;

import android.graphics.Paint;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.example.epubreader.ReaderApplication;
import com.example.epubreader.book.css.BookCSSAttributeSet;
import com.example.epubreader.book.css.BookSingleCSSSet;
import com.example.epubreader.book.css.BookTagAttribute;
import com.example.epubreader.book.tag.BodyControlTag;
import com.example.epubreader.book.tag.BookBasicControlTag;
import com.example.epubreader.util.BookAttributeUtil;
import com.example.epubreader.util.BookStingUtil;
import com.example.epubreader.util.MyReadLog;
import com.example.epubreader.view.book.BookLineInfo;
import com.example.epubreader.view.book.BookPage;
import com.example.epubreader.view.book.element.BookTextBaseElement;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

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
    public File realFile;
    private BookResourceFile CSSFile;
    public BookContentElement mainContentElement; // 主显示内容（主要为标签为body的主元素）
    private volatile BookContentElement contentElement; //用于计算各元素；

    private int depth = -1;// 记录当前元素的深度
    private BookCSSAttributeSet currentAttributeSet; // 用到的CSS
    private ArrayList<String> cssArrayList; //用于记录用到的css文件


    private volatile ArrayList<BookPage> bookPages = new ArrayList<>(); //保存html生成的页面信息
    private ArrayList<BookTextBaseElement> textElements;
    private Paint paint = new Paint();

    public EpubReaderHtml(File realFile) {
        this.realFile = realFile;
    }

    public EpubReaderHtml(BookModel bookModel) {
        this.bookModel = bookModel;
    }

    /**
     * 加载流
     *
     * @param inputStream
     */
    public void loadHtmlInputStream(InputStream inputStream) {
        StringBuffer stringBuffer = new StringBuffer();
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
            buildBookPages();
            if (mainContentElement == null) {
                MyReadLog.i("mainContentElement is null");
            } else {
                MyReadLog.i("主元素里面的元素数量是 " + mainContentElement.getElementSize());
                MyReadLog.i("主元素里面子元素内的元素数量是 " + mainContentElement.getIndex(0).getElementSize());
                MyReadLog.i("css 大小" + currentAttributeSet.getSingleCSSSets().size());
                if (currentAttributeSet != null && currentAttributeSet.getSingleCSSSets() != null && currentAttributeSet.getSingleCSSSets().size() > 0) {
                    for (int i = 0; i < currentAttributeSet.getSingleCSSSets().size(); i++) {
                        BookSingleCSSSet singleCSSSet = currentAttributeSet.getSingleCSSSets().valueAt(i);
                        String name = currentAttributeSet.getSingleCSSSets().keyAt(i);
                        MyReadLog.i(name + " unDefault class size : " + singleCSSSet.classes.size());
                        MyReadLog.i(name + " default class size : " + singleCSSSet.rootClasses.size());
                    }
                }
            }
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
                String content = line.substring(index, nextStartBracketIndex);
                content = content.replaceAll("\\n", " ").replaceAll(" +", " "); // 将换号符号换成空格，再将多个空格换成一个空格
                contentElement.addTextElement(content);
                index = nextStartBracketIndex;
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

    public void buildBookPages() {
        if (mainContentElement == null || mainContentElement.getElementSize() < 0) return;
        textElements = mainContentElement.getTextElements();
        if (mainContentElement.getControlTag() instanceof BodyControlTag) {
            BodyControlTag controlTag = (BodyControlTag) mainContentElement.getControlTag();
            MyReadLog.i(controlTag.getAttributeStr());

            int pageWidth = ReaderApplication.getInstance().getWindowSize().widthPixels;
            int pageHeight = ReaderApplication.getInstance().getWindowSize().heightPixels;
            BookPage page = new BookPage(controlTag, pageWidth);

            // TODO TEST
            BookLineInfo lastLineInfo = null; // 上一行信息
//            BookLineInfo lineInfo = null; // 当前行信息

            int lineInfoStartX = page.lGap;
            int lineInfoStartY = page.tGap;
            elementIndex = 0;
            MyReadLog.i("pageWidth = " + pageWidth + ", lGap = " + lineInfoStartX + ", tGap = " + lineInfoStartY);
            while (elementIndex < textElements.size()) {
                BookLineInfo lineInfo = buildBookLineInfo(pageWidth - page.rGap, lineInfoStartX, lineInfoStartY);
                page.addLineInfo(lineInfo);
                lastLineInfo = lineInfo;
                lineInfoStartY = lineInfoStartY + lastLineInfo.getLineHeight();
                lineInfoStartX = page.lGap;
            }
            MyReadLog.i("line size is " + page.getLineSize());
            bookPages = page.cutToPages(pageHeight);
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

    private BookLineInfo buildBookLineInfo(int pageWidth, int lineInfoStartX, int lineInfoStartY) {
        BookLineInfo lineInfo = new BookLineInfo(pageWidth, lineInfoStartX, lineInfoStartY);
        ArrayMap<String, BookTagAttribute> attributeSet = null;
        BookTextBaseElement lastElement = null;
        int lineWidth = lineInfo.getLineWidth();
        int lineHeight = 0;
        boolean isFirstElement = true;
        int leftGap = 0;
        int rightGap = 0;
        int maxTextHeight = 0;
//        float lineHeightRate  = 1.2f;
//        MyReadLog.i("elementIndex --->" + elementIndex + ", element size is" + textElements.size());
        while (elementIndex < textElements.size()) {
            BookTextBaseElement element = textElements.get(elementIndex);
            if (attributeSet == null || element.isPositionChange(lastElement)) {
                if (!element.isInOneParagraph(lastElement)) {
                    if (lastElement != null) {
                        // todo test 添加下边距
                        lineInfo.bottomGap = 0;
                    }
                    lineInfo.alignLineInfo(BookAttributeUtil.getTextAlign(attributeSet));
                    break;
                }
                attributeSet = element.getAttributeSet(); // 获取显示元素的属性信息
                lineInfo.lineHeightRate = BookAttributeUtil.getLineHeight(attributeSet); //解析line-height;
                setPaint(attributeSet);
                int paragraphWidth = BookAttributeUtil.getWidth(attributeSet, pageWidth);
                if (paragraphWidth == -1) {
                    lineWidth = pageWidth;
                } else {
                    lineWidth = paragraphWidth;
                }
                lineInfo.setLineWidth(lineWidth);
                leftGap = BookAttributeUtil.getMargin(attributeSet, BookAttributeUtil.POSITION_LEFT, pageWidth)
                        + BookAttributeUtil.getPadding(attributeSet, BookAttributeUtil.POSITION_LEFT, pageWidth);
                rightGap = BookAttributeUtil.getMargin(attributeSet, BookAttributeUtil.POSITION_RIGHT, pageWidth)
                        + BookAttributeUtil.getPadding(attributeSet, BookAttributeUtil.POSITION_RIGHT, pageWidth);
            }
            int elementWidth = element.getWidth(paint);
            int elementHeight = element.getHeight(paint);
            if (element.isParagraphStart()) {
                lineInfo.topGap = BookAttributeUtil.getMargin(attributeSet, BookAttributeUtil.POSITION_TOP, BookAttributeUtil.ONE_EM_LENGTH)
                        + BookAttributeUtil.getPadding(attributeSet, BookAttributeUtil.POSITION_TOP, BookAttributeUtil.ONE_EM_LENGTH);
                // todo test : text-indent 获取方式尚有偏差
                int indentGap = BookAttributeUtil.getTextIndent(attributeSet, lineInfo.getLineWidth(), BookAttributeUtil.getFontSize(attributeSet));
//                MyReadLog.i("indentGap ->" + indentGap);
                element.x = lineInfoStartX + indentGap;
//                element.y = lineInfoStartY + marginTop + elementHeight + (int) ((lineHeightRate - 1) * elementHeight / 2);
//                lineInfoStartY = lineInfoStartY + marginTop;
            } else {
                element.x = lineInfoStartX;
//                element.y = lineInfoStartY + elementHeight + (int) ((lineHeightRate - 1) * elementHeight / 2);
            }
            element.y = lineInfoStartY; // todo test vertical-align 处理
            if (elementWidth == -1) {
                lineInfoStartX = lineWidth;
            } else {
                lineInfoStartX = element.x + elementWidth;
            }

            // 获取左边距信息
            if (isFirstElement) {
                element.x = element.x + leftGap;
                lineInfoStartX = element.x + elementWidth;
            }
//            MyReadLog.i("lineInfoStartX = " + lineInfoStartX + ", elementWidth = " + elementWidth);

            if (lineWidth - rightGap < lineInfoStartX) {
                lineInfo.rebuildLineInfo(lineWidth - rightGap - element.x);
                break;
            }

            if (elementHeight > lineHeight) {
                lineHeight = elementHeight;
                lineInfo.setLineHeight(lineHeight);
            }
            if (isFirstElement) {
                isFirstElement = false;
            }
            lineInfo.addTextElement(element);
            lastElement = element;
            elementIndex++;
        }
        lineInfo.resetVerticalPosition();
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
}
