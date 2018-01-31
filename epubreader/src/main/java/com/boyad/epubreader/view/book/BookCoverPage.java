package com.boyad.epubreader.view.book;

import android.util.DisplayMetrics;

import com.boyad.epubreader.ReaderApplication;
import com.boyad.epubreader.book.tag.BodyControlTag;
import com.boyad.epubreader.view.book.element.BookTextImageElement;

/**
 * 创建专属于封面的页面，用于展示封面
 * Created by Boyad on 2017/12/12.
 */

public final class BookCoverPage extends BookPage {
    private static BookCoverPage mIntance;
    private static String coverPath;

    private BookCoverPage(BodyControlTag controlTag, int pageWidth, int pageHeight, int[] coverSize, String coverPath) {
        super(controlTag, pageWidth, pageHeight);
        tGap = bGap = rGap = lGap = 0;
        BookTextImageElement imageElement = new BookTextImageElement(coverSize, pageWidth, pageHeight, coverPath);
        BookLineInfo lineInfo = new BookLineInfo(pageWidth, 0 , 0);
        lineInfo.addTextElement(imageElement);
        lineInfos.add(lineInfo);
    }

    public static BookCoverPage createBookCoverPage(String path, int[] coverSize) {
        DisplayMetrics metrics = ReaderApplication.getInstance().getWindowSize();
        BodyControlTag bodyControlTag = new BodyControlTag("body", "");
        mIntance = new BookCoverPage(bodyControlTag, metrics.widthPixels, metrics.heightPixels, coverSize, path);
        return mIntance;
    }
}
