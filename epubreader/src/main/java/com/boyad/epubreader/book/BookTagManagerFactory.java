package com.boyad.epubreader.book;

import android.support.v4.util.ArrayMap;

import com.boyad.epubreader.book.css.BookCSSAttributeSet;
import com.boyad.epubreader.book.tag.BodyControlTag;
import com.boyad.epubreader.book.tag.BookBasicControlTag;
import com.boyad.epubreader.book.tag.DivisionControlTag;
import com.boyad.epubreader.book.tag.ImageControlTag;
import com.boyad.epubreader.book.tag.LineBreakControlTag;
import com.boyad.epubreader.book.tag.LinkControlTag;
import com.boyad.epubreader.book.tag.ParagraphControlTag;
import com.boyad.epubreader.book.tag.SpanControlTag;

/**
 * 标签工厂类
 * 用于保存标签信息
 * Created by Boyad on 2017/11/3.
 */

public class BookTagManagerFactory {
    public static BookBasicControlTag createControlTag(String name, String attributeStr, BookCSSAttributeSet attributeSet) {
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
                controlTag =  new ParagraphControlTag(name, attributeStr);
                break;
            case "div":
                controlTag = new DivisionControlTag(name, attributeStr);
                break;
            case "body":
                controlTag = new BodyControlTag(name, attributeStr);
                break;
            case "img":
            case "image":
                controlTag = new ImageControlTag(name, attributeStr);
                break;
            case "a":
                controlTag = new LinkControlTag(name, attributeStr);
                break;
            case "span":
                controlTag = new SpanControlTag(name, attributeStr);
                break;
            default:
                controlTag = null;
        }
        if (controlTag != null) {
            controlTag.setNeedAttribute(attributeSet);
        }
        return controlTag;
    }

    public static BookBasicControlTag createControlTagByArrayMap(String name, ArrayMap attributeMaps, BookCSSAttributeSet attributeSet) {
        BookBasicControlTag controlTag = null;
        switch (name) {
            case "h1":
            case "h2":
            case "h3":
            case "h4":
            case "h5":
            case "h6":
            case "p":
                controlTag =  new ParagraphControlTag(name, attributeMaps);
                break;
            case "br":
                controlTag = new LineBreakControlTag(name, attributeMaps);
                break;
            case "div":
                controlTag = new DivisionControlTag(name, attributeMaps);
                break;
            case "body":
                controlTag = new BodyControlTag(name, attributeMaps);
                break;
            case "img":
            case "image":
                controlTag = new ImageControlTag(name, attributeMaps);
                break;
            case "a":
                controlTag = new LinkControlTag(name, attributeMaps);
                break;
            case "span":
                controlTag = new SpanControlTag(name, attributeMaps);
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
