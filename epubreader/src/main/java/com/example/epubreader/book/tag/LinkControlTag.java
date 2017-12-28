package com.example.epubreader.book.tag;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.example.epubreader.util.BookAttributeUtil;
import com.example.epubreader.util.BookStingUtil;
import com.example.epubreader.util.MyReadLog;

/** todo test 链接信息添加位置未确定
 * 链接标签 a
 * Created by Boyad on 2017/11/7.
 */

public class LinkControlTag extends BookBasicControlTag {
    private static final String ATTRIBUTE_NAME_HREF = "href";
    private String  hrefStr;
    public LinkControlTag(String tagName, String attributeStr) {
        super(tagName, attributeStr);
    }

    public LinkControlTag(String tagName, ArrayMap<String, String> allAttribute) {
        super(tagName, allAttribute);
    }

    @Override
    protected void getOtherAttribute(String attributeStr) {
        super.getOtherAttribute(attributeStr);
        if (allAttribute == null) {
            int hrefIndex = attributeStr.indexOf(ATTRIBUTE_NAME_HREF);
            if (hrefIndex > -1) {
                hrefStr = BookStingUtil.getDataValue(attributeStr, "\"", "\"", hrefIndex);
            }
        } else {
            hrefStr = allAttribute.get(ATTRIBUTE_NAME_HREF);
        }
    }

    public String getHrefStr() {
        return hrefStr;
    }
}
