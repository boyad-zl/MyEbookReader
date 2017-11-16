package com.example.epubreader.book;

import android.text.TextUtils;

import com.example.epubreader.book.tag.BodyControlTag;
import com.example.epubreader.book.tag.BookBasicControlTag;
import com.example.epubreader.book.tag.DivisionControlTag;
import com.example.epubreader.book.tag.ImageControlTag;
import com.example.epubreader.book.tag.LineBreakControlTag;
import com.example.epubreader.book.tag.ParagraphControlTag;
import com.example.epubreader.util.MyReadLog;
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
        if (contentElements == null) MyReadLog.i("contentElements 是空！！！");
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
     * 获取用户显示的元素（文字和图片）
     *
     * @return
     */
    public ArrayList<BookTextBaseElement> getTextElements() {
        ArrayList<BookTextBaseElement> result = new ArrayList<>();

        if (!TextUtils.isEmpty(content)) {
            char[] contentChars = content.toCharArray();
            int index = 0;

            boolean isReadLetter = false;
            StringBuffer sb = new StringBuffer();
            while (index < contentChars.length) {
                if (contentChars[index] != '&') {
                    if (contentChars[index] < 'a' || contentChars[index] > 'Z') {
                        BookTextWordElement element = new BookTextWordElement(String.valueOf(contentChars[index]), this);
                        element.setIndex(index);
                        result.add(element);
                        index++;
                    } else {
                        isReadLetter = true;
                    }
                    if (isReadLetter) {
                        sb.append(contentChars[index]);
                        if (index + 1 >= contentChars.length || (index + 1 < contentChars.length && (contentChars[index] < 'a' || contentChars[index] > 'Z'))) {
                            BookTextWordElement element = new BookTextWordElement(String.valueOf(contentChars[index]), this);
                            element.setIndex(index);
                            result.add(element);
                            isReadLetter = false;
                            sb.setLength(0);
                        }
                        index++;
                    }
                } else {
                    BookTextBaseElement element;
                    int semicolonIndex = content.indexOf(";", index);
                    if (semicolonIndex > -1 && (semicolonIndex - index) < 9) {
                        String characterEntryStr = (semicolonIndex == contentChars.length - 1)
                                ? content.substring(index)
                                : content.substring(index, semicolonIndex + 1);
                        switch (characterEntryStr) {
                            case "&nbsp;":
                                index = semicolonIndex + 1;
                                element = new BookTextNbspElement(this);
                                break;
                            default:
                                MyReadLog.i(String.valueOf(contentChars[index]));
                                element = new BookTextWordElement(String.valueOf(contentChars[index]), this);
                                index++;
                                break;
                        }
                    } else {
                        element = new BookTextWordElement(String.valueOf(contentChars[index]), this);
                    }
                    element.setIndex(index);
                    result.add(element);
                }
            }
        } else {
            if (controlTag instanceof ImageControlTag) {
                result.add(new BookTextImageElement((ImageControlTag) controlTag, this));
            } else if (controlTag instanceof LineBreakControlTag) {
                result.add(new BookTextLineBreakElement(this));
            } else {
                if (contentElements != null && contentElements.size() > 0) {
                    for (int i = 0; i < contentElements.size(); i++) {
                        result.addAll(contentElements.get(i).getTextElements());
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
        if (parent.getControlTag() instanceof ParagraphControlTag || parent.getControlTag() instanceof DivisionControlTag){
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
}
