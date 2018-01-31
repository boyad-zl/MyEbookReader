package com.boyad.epubreader.util;

import com.boyad.epubreader.view.book.BookReadPosition;

/**
 * Created by Boyad on 2018/1/17.
 */

public class BookPositionUtil {
    public static BookReadPosition string2Position(String positionStr) {
        int pageIndex = positionStr.indexOf("-");
        int charIndex = positionStr.indexOf("/");
        if (pageIndex != -1 && charIndex != -1 && (pageIndex < charIndex)) {
            try {
                int pageNum = Integer.valueOf(positionStr.substring(0, pageIndex));
                String positionInPage = positionStr.substring(pageIndex + 1, charIndex);
                int charNum = Integer.valueOf(positionStr.substring(charIndex + 1));
                return new BookReadPosition(pageNum, positionInPage, charNum);
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            return null;
        }
    }
}
