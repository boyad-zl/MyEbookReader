package com.example.epubreader.book;

import com.example.epubreader.book.css.BookCSSAttributeSet;
import com.example.epubreader.book.tag.BodyControlTag;
import com.example.epubreader.book.tag.BookBasicControlTag;
import com.example.epubreader.book.tag.DivisionControlTag;
import com.example.epubreader.book.tag.ImageControlTag;
import com.example.epubreader.book.tag.LinkControlTag;
import com.example.epubreader.book.tag.ParagraphControlTag;
import com.example.epubreader.book.tag.SpanControlTag;

/**
 * 标签工厂类
 * 用于保存标签信息
 * Created by Boyad on 2017/11/3.
 */

public class BookTagManagerFactory {
    public static BookBasicControlTag createControlTag(String name, String attribute, BookCSSAttributeSet attributeSet) {
        BookBasicControlTag controlTag = null;
        switch (name) {
            case "h1":
            case "h2":
            case "h3":
            case "h4":
            case "h5":
            case "h6":
            case "p":
            case "br":
                controlTag =  new ParagraphControlTag(name, attribute);
                break;
            case "div":
                controlTag = new DivisionControlTag(name, attribute);
                break;
            case "body":
                controlTag = new BodyControlTag(name, attribute);
                break;
            case "img":
            case "image":
                controlTag = new ImageControlTag(name, attribute);
                break;
            case "a":
                controlTag = new LinkControlTag(name, attribute);
                break;
            case "span":
                controlTag = new SpanControlTag(name, attribute);
                break;
            default:
                controlTag = null;
        }
        if (controlTag != null) {
            controlTag.setNeedAttribute(attributeSet);
        }
        return controlTag;
    }
}
