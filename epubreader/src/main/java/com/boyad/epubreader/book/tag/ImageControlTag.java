package com.boyad.epubreader.book.tag;

import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.support.v4.util.ArrayMap;

import com.boyad.epubreader.util.BookStingUtil;

import java.io.InputStream;

/**
 * img/ image tag
 * Created by Boyad on 2017/11/3.
 */
public class ImageControlTag extends BookBasicControlTag {
    private static final String ATTRIBUTE_NAME_SRC = "src";
    private static final String ATTRIBUTE_NAME_ALT = "alt";
    public String altStr = "";
    public String pathStr = "";
    private Rect outPaddingRect = new Rect();
    private int[] realSize = new int[2];

    public ImageControlTag(String name, String attribute) {
        super(name, attribute);
    }


    public ImageControlTag(String tagName, ArrayMap<String, String> allAttribute) {
        super(tagName, allAttribute);
    }

    @Override
    protected void getOtherAttribute(String attributeStr) {
        super.getOtherAttribute(attributeStr);
//        if (TextUtils.isEmpty(attributeStr)) return;
        if (allAttribute == null) {
            int altIndex = attributeStr.indexOf(ATTRIBUTE_NAME_ALT);
            if (altIndex > -1) {
                altStr = BookStingUtil.getDataValue(attributeStr, "\"", "\"", altIndex);
//            MyReadLog.i("altStr = " + altStr);
            }

            int pathIndex = attributeStr.indexOf(ATTRIBUTE_NAME_SRC);
            if (pathIndex > -1) {
                pathStr = BookStingUtil.getDataValue(attributeStr, "\"", "\"", pathIndex);
//            MyReadLog.i("pathStr = " + pathStr);
            }
        } else {
            altStr = allAttribute.get(ATTRIBUTE_NAME_ALT);
            pathStr = allAttribute.get(ATTRIBUTE_NAME_SRC);
        }
    }

    /**
     * 获取实际宽高
     * @return
     */
    public void setImageData(InputStream inputStream){
        if (inputStream != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, outPaddingRect, options);
            realSize[0] = options.outWidth;
            realSize[1] = options.outHeight;
//            MyReadLog.d("width = %d, height = %d", realSize[0], realSize[1]);
        } else {
            realSize[0] = 0;
            realSize[1] = 0;
        }
    }

    /**
     * 获取图片的路径
     * @return
     */
    public String getImagePathStr() {
        return pathStr;
    }

    /**
     * 获取图片的大小
     * @return
     */
    public int[] getRealSize() {
        return realSize;
    }
}
