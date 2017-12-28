package com.example.epubreader.book;

import android.support.v4.util.ArrayMap;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;

import com.example.epubreader.book.css.BookTagAttribute;
import com.example.epubreader.book.tag.BodyControlTag;
import com.example.epubreader.book.tag.BookBasicControlTag;
import com.example.epubreader.book.tag.DivisionControlTag;
import com.example.epubreader.book.tag.ImageControlTag;
import com.example.epubreader.book.tag.LineBreakControlTag;
import com.example.epubreader.book.tag.LinkControlTag;
import com.example.epubreader.book.tag.ParagraphControlTag;
import com.example.epubreader.util.StringWidthMeasureHelper;
import com.example.epubreader.view.book.element.BookTextBaseElement;
import com.example.epubreader.view.book.element.BookTextImageElement;
import com.example.epubreader.view.book.element.BookTextLineBreakElement;
import com.example.epubreader.view.book.element.BookTextNbspElement;
import com.example.epubreader.view.book.element.BookTextWordElement;

import java.util.ArrayList;

/**
 * 用于存放标签里面的内容
 * Created by boyad on 2017/11/3.
 */
public class BookContentElement {
    private String content; // 字符串内容
    private BookBasicControlTag controlTag; // 标签名称
    public volatile ArrayList<BookContentElement> contentElements;
    private BookContentElement parent = null;
    private String position = "0";
    private int index = 0;
    private ArrayList<BookTextBaseElement> words;

    /**
     * 构成纯文字的元素
     *
     * @param content
     */
    public BookContentElement(BookContentElement parent, String content) {
        this.parent = parent;
        this.content = content;
    }

    public BookContentElement(BookBasicControlTag controlTag) {
        this.controlTag = controlTag;
        this.content = "";
    }

    public BookContentElement(BookContentElement parent, BookBasicControlTag controlTag) {
        this.parent = parent;
        this.controlTag = controlTag;
    }

    /**
     * 向当前元素里面添加字符串
     *
     * @param contentChar
     */
    public void addTextElement(String contentChar) {
        if (contentElements == null) {
//            MyReadLog.i("contentElements is null");
            contentElements = new ArrayList<>();
        }
        BookContentElement bookContentElement = new BookContentElement(this, contentChar);
        bookContentElement.setIndex(contentElements.size());
//        bookContentElement.handleWordElements();
        bookContentElement.getOnlyWordElements();
//        MyReadLog.i("size = " + contentElements.size());
        contentElements.add(bookContentElement);

    }


    /**
     * 向当前元素中添加子元素
     *
     * @param element
     */
    public void addControlElement(BookContentElement element) {
        if (contentElements == null) {
//            MyReadLog.i("contentElements is null");
            contentElements = new ArrayList<>();
        }
//        MyReadLog.i("size = " + contentElements.size());
        element.setIndex(contentElements.size());
        contentElements.add(element);
    }


    /**
     * 设置元素位置
     *
     * @param index
     */
    public void setIndex(int index) {
        this.index = index;
        if (parent != null) {
            position = parent.getPosition() + ":" + index;
        }
    }

    /**
     * 当前元素是文字还是其他元素
     *
     * @return
     */
    public boolean isElementText() {
        return !TextUtils.isEmpty(content);
    }

    /**
     * 获取父元素
     *
     * @return
     */
    public BookContentElement getParent() {
        return this.parent;
    }


    /**
     * 获取当前元素的某上层次的元素
     *
     * @param level
     * @return
     */
    public BookContentElement getParent(int level) {
        if (level == 1) {
            return getParent();
        }
        if (level > 1) {
            return getParent().getParent(level - 1);
        } else {
            return null;
        }
    }

    /**
     * 获取元素数量
     *
     * @return
     */
    public int getElementSize() {
//        if (contentElements == null) MyReadLog.i("contentElements 是空！！！");
        return contentElements == null ? 0 : contentElements.size();
    }

    /**
     * 获取指定位置的元素
     *
     * @param i
     * @return
     */
    public BookContentElement getIndex(int i) {
        return contentElements == null ? null : contentElements.get(i);
    }

    public BookBasicControlTag getControlTag() {
        return controlTag;
    }

    public ArrayList<BookContentElement> getContentElements() {
        return contentElements;
    }

    public String getPosition() {
        return position;
    }

    public String getTextContent() {
        return content;
    }

    /**
     *  处理content
     * 将String转换成 ArrayList<BookTextBaseElement> words， 并测量每个BookTextBaseElement的baseWidth
     * @see
     */
    public void getOnlyWordElements() {
        if (words == null) {
            words = new ArrayList<>();
        }
        if (!TextUtils.isEmpty(content)) {
            char[] contentChars = content.toCharArray();
            int index = 0;
            StringBuffer sb = new StringBuffer();
            BookTextBaseElement element;
            int lastCharType = -1;
            boolean needBreakFromLastChar = false; // 是否需要从上一个元素开始将StringBuffer里面的字符串转化成BookTextWordElement
            boolean needIgnoreCurrentChar = false; // 是否需要忽略当前元素
            while (index < contentChars.length) {
                if (contentChars[index] != '&') {
                    sb.append(contentChars[index]);
                    int currentCharType = Character.getType(contentChars[index]);
                    if (lastCharType != -1 && sb.length() > 0) {
                        switch (currentCharType) {
                            case Character.DECIMAL_DIGIT_NUMBER:
                                if (lastCharType != Character.DECIMAL_DIGIT_NUMBER) {
                                    needBreakFromLastChar = true;
                                }
                                break;
                            case Character.OTHER_LETTER:
                                if (lastCharType != Character.START_PUNCTUATION
                                        && lastCharType != Character.INITIAL_QUOTE_PUNCTUATION
                                        && lastCharType != Character.SPACE_SEPARATOR) {
                                    needBreakFromLastChar = true;
                                }
                                break;
                            case Character.UPPERCASE_LETTER:
                            case Character.LOWERCASE_LETTER:
//                        case Character.CONNECTOR_PUNCTUATION:
                                if (lastCharType != Character.UPPERCASE_LETTER && lastCharType != Character.LOWERCASE_LETTER) {
                                    needBreakFromLastChar = true;
                                }
                                break;
                            case Character.START_PUNCTUATION:
                            case Character.INITIAL_QUOTE_PUNCTUATION:
                                if (lastCharType != Character.START_PUNCTUATION && lastCharType != Character.INITIAL_QUOTE_PUNCTUATION) {
                                    needBreakFromLastChar = true;
                                }
                                break;
                            case Character.END_PUNCTUATION:
                            case Character.FINAL_QUOTE_PUNCTUATION:
                                break;
                            case Character.SPACE_SEPARATOR:
                            case Character.CONTROL:
                                if (lastCharType == Character.CONTROL || lastCharType == Character.SPACE_SEPARATOR){
                                    needIgnoreCurrentChar = true;
                                }
                                needBreakFromLastChar = true;
                                break;
                            case Character.OTHER_PUNCTUATION:
                            case Character.OTHER_SYMBOL:
                                break;
                            case Character.DASH_PUNCTUATION:
                                if (lastCharType != Character.DASH_PUNCTUATION)
                                    needBreakFromLastChar = true;
                                break;
                            default:
                                break;
                        }
                        if (needBreakFromLastChar) {
                            String word = sb.substring(0, sb.length() - 1);
                            element = new BookTextWordElement(word, this);
//                        element.setBaseWidth(paint.measureText(word));
                            element.setBaseWidth(StringWidthMeasureHelper.getStringWidth(word));
                            element.setIndex(index - sb.length() + 1);
                            sb.delete(0, sb.length() - 1);
                            if (!needIgnoreCurrentChar) {
                                words.add(element);
                            }
                            needIgnoreCurrentChar = false;
                            needBreakFromLastChar = false;
                        }
                    }
                    lastCharType = currentCharType;
                    index++;
                } else {
                    if (sb.length() > 0) {
                        element = new BookTextWordElement(sb.toString(), this);
                        element.setIndex(index - sb.length());
                        element.setBaseWidth(StringWidthMeasureHelper.getStringWidth(sb.toString()));
                        sb.setLength(0);
                        words.add(element);
                    }
                    if (contentChars.length - index >= 6 && (String.valueOf(contentChars, index, 6).equals("&nbsp;"))) {
//                    MyReadLog.i("subString = " + String.valueOf(contentChars, index, index + 6) + " index = " + index + "index + 6 = " + (index + 6));
                        element = new BookTextNbspElement(this);
                        element.setIndex(index);
                        index = index + 6;
                        lastCharType = -1;
                    } else {
                        element = new BookTextWordElement("&", this);
                        element.setIndex(index);
                        element.setBaseWidth(StringWidthMeasureHelper.getCharWidth('&'));
                        index = index + 1;
                        lastCharType = Character.OTHER_PUNCTUATION;
                    }
                    words.add(element);
                }
            }
            if (sb.length() > 0) {
                element = new BookTextWordElement(sb.toString(), this);
                element.setIndex(index - sb.length() + 1);
                element.setBaseWidth(StringWidthMeasureHelper.getStringWidth(sb.toString()));
                sb.setLength(0);
                words.add(element);
            }
        }
    }

    /**
     * 处理 content 不是空的BookContentElement 将其内部的字符串转换成 ArrayList<BookTextBaseElement> words,
     * 每个并设置好BookTextBaseElement的baseWidth
     *
     * @return
     */
    public void handleWordElements() {
        if (words == null) {
            words = new ArrayList<>();
        }
        if (!TextUtils.isEmpty(content)) {
            char[] contentChars = content.toCharArray();
            float[] charWidths = StringWidthMeasureHelper.getStringCharWidths(content);
            int index = 0;
            StringBuffer sb = new StringBuffer();
            BookTextBaseElement element;
            int lastCharType = -1;
            boolean needBreakFromLastChar = false; // 是否需要从上一个元素开始将StringBuffer里面的字符串转化成BookTextWordElement
            while (index < contentChars.length) {
                if (contentChars[index] != '&') {
                    sb.append(contentChars[index]);
                    int currentCharType = Character.getType(contentChars[index]);
                    if (lastCharType != -1 && sb.length() > 0) {

                        switch (currentCharType) {
                            case Character.DECIMAL_DIGIT_NUMBER:
                                if (lastCharType != Character.DECIMAL_DIGIT_NUMBER) {
                                    needBreakFromLastChar = true;
                                }
                                break;
                            case Character.OTHER_LETTER:
                                if (lastCharType != Character.START_PUNCTUATION
                                        && lastCharType != Character.INITIAL_QUOTE_PUNCTUATION
                                        && lastCharType != Character.SPACE_SEPARATOR) {
                                    needBreakFromLastChar = true;
                                }

                                break;
                            case Character.UPPERCASE_LETTER:
                            case Character.LOWERCASE_LETTER:
//                        case Character.CONNECTOR_PUNCTUATION:
                                if (lastCharType != Character.UPPERCASE_LETTER && lastCharType != Character.LOWERCASE_LETTER) {
                                    needBreakFromLastChar = true;
                                }
                                break;
                            case Character.START_PUNCTUATION:
                            case Character.INITIAL_QUOTE_PUNCTUATION:
                                if (lastCharType != Character.START_PUNCTUATION && lastCharType != Character.INITIAL_QUOTE_PUNCTUATION) {
                                    needBreakFromLastChar = true;
                                }
                                break;
                            case Character.END_PUNCTUATION:
                            case Character.FINAL_QUOTE_PUNCTUATION:
                                break;
                            case Character.SPACE_SEPARATOR:
                                needBreakFromLastChar = true;
                                break;
                            case Character.OTHER_PUNCTUATION:
                            case Character.OTHER_SYMBOL:
                                break;
                            case Character.DASH_PUNCTUATION:
                                if (lastCharType != Character.DASH_PUNCTUATION)
                                    needBreakFromLastChar = true;
                                break;
                            default:
                                break;
                        }
                        if (needBreakFromLastChar) {
                            String word = sb.substring(0, sb.length() - 1);
                            element = new BookTextWordElement(word, this);
//                        element.setBaseWidth(paint.measureText(word));
                            float baseWidth = 0;
                            for (int i = index - sb.length() + 1; i < index; i++) {
                                baseWidth += charWidths[i];
                            }
                            element.setBaseWidth(baseWidth);
                            element.setIndex(index - sb.length() + 1);
                            sb.delete(0, sb.length() - 1);
                            words.add(element);
                            needBreakFromLastChar = false;
                        }
                    }
                    lastCharType = currentCharType;
                    index++;
                } else {
                    if (sb.length() > 0) {
                        element = new BookTextWordElement(sb.toString(), this);
                        element.setIndex(index - sb.length());
                        float baseWidth = 0;
                        for (int i = index - sb.length() + 1; i < index; i++) {
                            baseWidth += charWidths[i];
                        }
                        element.setBaseWidth(baseWidth);
                        sb.setLength(0);
                        words.add(element);
                    }
                    if (contentChars.length - index >= 6 && (String.valueOf(contentChars, index, 6).equals("&nbsp;"))) {
//                    MyReadLog.i("subString = " + String.valueOf(contentChars, index, index + 6) + " index = " + index + "index + 6 = " + (index + 6));
                        element = new BookTextNbspElement(this);
                        element.setIndex(index);
                        index = index + 6;
                        lastCharType = -1;
                    } else {
                        element = new BookTextWordElement("&", this);
                        element.setIndex(index);
                        element.setBaseWidth(StringWidthMeasureHelper.getCharWidth('&'));
                        index = index + 1;
                        lastCharType = Character.OTHER_PUNCTUATION;
                    }
                    words.add(element);
                }
            }
            if (sb.length() > 0) {
                element = new BookTextWordElement(sb.toString(), this);
                element.setIndex(index - sb.length() + 1);
//            element.setBaseWidth(paint.measureText(sb.toString()));
                element.setBaseWidth(StringWidthMeasureHelper.getStringWidth(sb.toString()));
                sb.setLength(0);
                words.add(element);
            }
        }
    }

    /**
     * 获取用户显示的元素（文字和图片）
     *
     * @return
     */
    public ArrayList<BookTextBaseElement> getAllTextElements() {
        ArrayList<BookTextBaseElement> result = new ArrayList<>();
        if (!TextUtils.isEmpty(content)) {
//            ArrayList<BookTextBaseElement> words = wo;
            if (words != null && words.size() > 0) result.addAll(words);
        } else {
            if (controlTag instanceof ImageControlTag) {
                result.add(new BookTextImageElement((ImageControlTag) controlTag, this));
            } else if (controlTag instanceof LineBreakControlTag) {
                result.add(new BookTextLineBreakElement(this));
            } else {
                if (contentElements != null && contentElements.size() > 0) {
                    for (int i = 0; i < contentElements.size(); i++) {
                        result.addAll(contentElements.get(i).getAllTextElements());
                    }
                }
            }
        }
        return result;
    }


    /**
     * 是否当前元素是段落开始
     *
     * @return
     */

    public boolean isCurrentElementParagraphStart() {
        if (parent == null) return true;
        if (parent.getControlTag() instanceof ParagraphControlTag || parent.getControlTag() instanceof DivisionControlTag) {
            return index == 0;
        } else {
            return parent.isCurrentElementParagraphStart();
        }
    }

    /**
     * 获取段落信息
     *
     * @return
     */
    public BookContentElement getTextParagraphElement() {
        if (this.controlTag instanceof BodyControlTag) return null;
        if (this.controlTag instanceof ParagraphControlTag || this.controlTag instanceof DivisionControlTag) {
            return this;
        } else {
            return this.parent.getTextParagraphElement();
        }
    }

    /**
     * 获取段落或者div标签的属性
     *
     * @return
     */
    public ArrayMap<String, BookTagAttribute> getParagraphAttribute() {
        if (controlTag != null && (controlTag instanceof ParagraphControlTag || controlTag instanceof DivisionControlTag)) {
            return controlTag.getAttributeMap();
        } else {
            if (parent == null) return null;
            return parent.getParagraphAttribute();
        }
    }

    /**
     * 是否是链接
     *
     * @return
     */
    public boolean isLink() {
        if (controlTag != null && controlTag instanceof LinkControlTag) {
            return true;
        } else {
            if (parent == null) return false;
            return parent.isLink();
        }
    }

    public SimpleArrayMap<String, String> getIdPosition() {
        SimpleArrayMap<String, String> result = new SimpleArrayMap<>();
        if (controlTag != null){
            String idStr = controlTag.getId();
            if (!TextUtils.isEmpty(idStr)) {
                result.put(idStr, getFirstWordOrImgElementPosition());
            }
        }

        if (contentElements != null && contentElements.size() > 0) {
            for (int i = 0; i < contentElements.size(); i++) {
                result.putAll(contentElements.get(i).getIdPosition());
            }
        }
        return result;
    }

    /**
     * 获取第一个展示的文字或图片的位置信息
     * @return
     */
    public String getFirstWordOrImgElementPosition() {
        String position = "";
        if (!TextUtils.isEmpty(content)) {
            return words.get(0).getPosition();
        } else if (controlTag instanceof ImageControlTag) {
            return getPosition();
        } else if ( contentElements != null &&!contentElements.isEmpty() ) {
            return contentElements.get(0).getFirstWordOrImgElementPosition();
        }
        return position;
    }

    public String getHrefPath() {
        if (controlTag != null && controlTag instanceof LinkControlTag) {
            return ((LinkControlTag) controlTag).getHrefStr();
        } else {
            if (parent == null) return "";
            return parent.getHrefPath();
        }
    }

}
