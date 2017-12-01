package com.example.epubreader.book.tag;

import android.text.TextUtils;

import com.example.epubreader.util.BookAttributeUtil;
import com.example.epubreader.util.BookStingUtil;
import com.example.epubreader.util.MyReadLog;

/** todo test 链接信息添加位置未确定
 * 链接标签 a
 * Created by Boyad on 2017/11/7.
 */

public class LinkControlTag extends BookBasicControlTag {
    private String idStr, hrefStr;
    public LinkControlTag(String tagName, String attributeStr) {
        super(tagName, attributeStr);
    }

    @Override
    protected void getOtherAttribute(String attributeStr) {
        super.getOtherAttribute(attributeStr);
        if (TextUtils.isEmpty(attributeStr)) return;
        int idIndex = attributeStr.indexOf("id");
        if (idIndex > -1) {
            idStr = BookStingUtil.getDataValue(attributeStr, "\"", "\"", idIndex);
//            MyReadLog.i("idStr = " + idStr);
        }

        int hrefIndex = attributeStr.indexOf("href");
        if (hrefIndex > -1) {
            hrefStr = BookStingUtil.getDataValue(attributeStr, "\"", "\"", hrefIndex);
            if (!TextUtils.isEmpty(hrefStr) && hrefStr.startsWith("#")) {
                hrefStr = hrefStr.substring(1);
            } else {
                hrefStr = "";
            }
//            MyReadLog.i("hrefStr = " + hrefStr);
        }
    }

}
