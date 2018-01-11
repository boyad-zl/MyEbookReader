package com.example.epubreader.view.book.element;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v4.util.ArrayMap;

import com.example.epubreader.book.BookContentElement;
import com.example.epubreader.book.css.BookTagAttribute;
import com.example.epubreader.book.tag.ImageControlTag;
import com.example.epubreader.util.BookAttributeUtil;
import com.example.epubreader.util.MyReadLog;

import java.io.InputStream;

/**
 * Created by Boyad on 2017/11/9.
 */

public class BookTextImageElement extends BookTextBaseElement {
    ImageControlTag controlTag;
    private int[] realSize;
    private float rate; // 宽高比
    private ArrayMap<String, BookTagAttribute> attributeArrayMap;
    private String pathStr;

    public BookTextImageElement(ImageControlTag controlTag, BookContentElement contentElement) {
        super(contentElement);
        this.controlTag = controlTag;
        realSize = controlTag.getRealSize();

        // 获取宽高比
        if (realSize[0] == 0 || realSize[1] == 0) {
            rate = 0;
        } else {
            rate = ((float) realSize[0]) / realSize[1];
        }

        pathStr = controlTag.getImagePathStr();
    }

    /**
     * 此构造方法用于创建封面页
     *
     * @param realSize
     */
    public BookTextImageElement(int[] realSize, int width, int height, String pathStr) {
        super(null);
        this.realSize = realSize;
//        realSize = controlTag.getRealSize();
        // 获取宽高比
        if (realSize[0] == 0 || realSize[1] == 0) {
            rate = 0;
        } else {
            rate = ((float) realSize[0]) / realSize[1];
        }
        this.width = width;
        this.height = height;
        this.pathStr = pathStr;
//        attributeArrayMap = getAttributeSet();
    }


    /**
     * 创建图片
     *
     * @return
     */
    public Bitmap createBitmap(InputStream inputStream) {
        MyReadLog.i("createBitmap~~~~");
        Bitmap newBitmap = null;
        if (width > 0 && height > 0) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Matrix matrix = new Matrix();
            matrix.postScale((float) width / bitmap.getWidth(), (float) height / bitmap.getHeight());
            newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//            MyReadLog.i("newBitmap : width =" + newBitmap.getWidth() + " , height = " + newBitmap.getHeight());
        }
        return newBitmap;
    }

    @Override
    public int getWidth(int fontSize, int maxWidth, int maxHeight) {
        width = BookAttributeUtil.getWidth(attributeArrayMap, maxWidth);
        height = BookAttributeUtil.getHeight(attributeArrayMap, maxHeight);
        if (rate == 0) {
            width = 0;
            height = 0;
//            MyReadLog.i("rate is 0");
        } else {
            float pageWHRate = ((float) maxWidth) / maxHeight;
            if (width == -1 && height == -1) {
//                MyReadLog.i("width,height is auto: " +maxWidth + " : " + maxHeight);
                if (pageWHRate <= rate) {
                    width = realSize[0] >= maxWidth ? maxWidth : realSize[0];
                    height = (int) ((float) width / rate);
                } else {
                    height = realSize[1] >= maxHeight ? maxHeight : realSize[1];
                    width = (int) ((float) height * rate);
                }
            } else if (width == -1) {
//                MyReadLog.i("width is auto");
                height = height >= maxHeight ? maxHeight : height;
                width = (int) ((float) height * rate);
            } else if (height == -1) {
//                MyReadLog.i("height is auto");
                width = width >= maxWidth ? maxWidth : width;
                height = (int) ((float) width / rate);
            }
        }
//        MyReadLog.i("width = " + width + ", height = " + height);
        return width;
    }

    @Override
    public int getHeight(int maxHeight) {
        return height;
    }

    /**
     * 获取图片的路径
     *
     * @return
     */
    public String getImagePath() {
        return pathStr;
    }

    @Override
    public void measureSize(int fontSize, Paint paint) {

    }
}
