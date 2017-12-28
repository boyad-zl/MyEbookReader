package com.example.epubreader.book.css;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.example.epubreader.book.BookResourceFile;
import com.example.epubreader.util.MyReadLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.ZipFile;

/**
 * 存放所有css里面的数据
 * Created by Boyad on 2017/11/5.
 */

public class BookCSSAttributeSet {
    private ArrayMap<String, BookSingleCSSSet> singleCSSSets;

    private int depth = 0;
    private String tagNames = "";
    private String[] tags;
    private boolean isReaderAnnotation = false;

    private BookCSSAttributeSet() {
        singleCSSSets = new ArrayMap<>();
    }

    public BookCSSAttributeSet(ZipFile zipFile, ArrayMap<String, BookResourceFile> cssFileArrayMap) throws IOException {
        singleCSSSets = new ArrayMap<>();
        for (int i = 0; i < cssFileArrayMap.size(); i++) {
            BookResourceFile cssFile = cssFileArrayMap.valueAt(i);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipFile.getEntry(cssFile.inFilePath))));
            BookSingleCSSSet singleCSSSet = new BookSingleCSSSet(cssFile.inFilePath);
            String line = "";
            BookClassSet classSet = null;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (!TextUtils.isEmpty(line)) {
                    //对于css中出现的注释字段进行处理
                    int startAnnotation = line.indexOf("/*");
                    int endAnnotation = line.indexOf("*/");
                    if (startAnnotation > -1) {
                        if (endAnnotation > -1) {
                            continue;
                        } else {
                            line = line.substring(0, endAnnotation).trim();
                            isReaderAnnotation = true;
                        }
                    }
                    if (isReaderAnnotation) {
                        if (endAnnotation > -1) {
                            line = line.substring(endAnnotation + 2).trim();
                        } else {
                            continue;
                        }
                    }

//                    MyReadLog.i(line);
                    char[] chars = line.toCharArray();
                    int charIndex = 0;
                    while (charIndex < chars.length) {
//                        MyReadLog.i("depth = " + depth +   ", index" +  charIndex+ " ：" + chars[charIndex]);
                        if (depth == 0) { //加载class的命名即...{
                            if (chars[charIndex] == '.') {
                                int startBraceIndex = line.indexOf("{", charIndex);
                                String className;
                                if (startBraceIndex > -1) {
                                    className = line.substring(charIndex + 1, startBraceIndex).trim();
                                    depth++;
                                    charIndex = startBraceIndex + 1;
                                } else {
                                    className = line.substring(charIndex + 1).trim();
                                    charIndex = line.length();
                                }
//                                MyReadLog.i(className);
                                tagNames = "";
                                classSet = new BookClassSet(className);
                            } else if (chars[charIndex] == '{') {
                                if (!TextUtils.isEmpty(tagNames)) {
                                    tags = tagNames.split(",");
                                }
                                tagNames = "";
                                depth++;
                                charIndex++;
                            } else {
                                int startBraceIndex = line.indexOf("{", charIndex);
                                int periodIndex = line.indexOf(".", charIndex); // .的位置索引
                                if (startBraceIndex < 0) {
                                    // 当前行中没有出现 { 符号
                                    if (periodIndex < 0) {
                                        // 当行中没有出现 '.'
                                        tagNames = line.substring(charIndex).trim();
                                    } else {
                                        // 当行中没有出现 '.'
                                        String tag = line.substring(charIndex, periodIndex).trim();
                                        String className = line.substring(periodIndex + 1).trim();
//                                        MyReadLog.i(tag + " : " + className);
                                        classSet = new BookClassSet(className, tag);
                                    }
                                    charIndex = line.length();
                                } else {
                                    // 当前行中出现了 { 符号
                                    if (periodIndex < 0 || periodIndex > startBraceIndex) {
                                        tagNames = tagNames + line.substring(charIndex, startBraceIndex).trim();
//                                        MyReadLog.i(tagNames);
                                        tags = tagNames.split(",");
                                    } else {  // 例如：p,h1 { 或者 p{
                                        String tag = line.substring(charIndex, periodIndex).trim();
                                        String className = line.substring(periodIndex + 1, startBraceIndex).trim();
//                                        MyReadLog.i(tag + " : " + className);
                                        classSet = new BookClassSet(className, tag);
                                    }
                                    tagNames = "";
                                    depth++;
                                    charIndex = startBraceIndex + 1;
                                }
                            }
                        } else {
                            if (chars[charIndex] == '}') {
                                if (tags == null) {
//                                    MyReadLog.i("singleCSSSet is null ? " + (singleCSSSet == null) + " ,\n classSet is null  ? " + (classSet == null));
                                    if (classSet != null) {
                                        singleCSSSet.addClass(classSet);
                                    }
                                }

                                tags = null;
                                depth--;
                                charIndex++;
                            } else {
                                BookTagAttribute tagAttribute = null;
                                int semicolonIndex = line.indexOf(";", charIndex);
                                int colonIndex = line.indexOf(":", charIndex);

                                if (semicolonIndex < 0 || colonIndex < 0) {
                                    charIndex = line.length();
                                } else {
                                    if (semicolonIndex < colonIndex) {
                                        charIndex = line.length();
                                    } else {
                                        String attributeName = line.substring(charIndex, colonIndex).trim().toLowerCase();
                                        String attributeValue = line.substring(colonIndex + 1, semicolonIndex).trim().toLowerCase();
//                                        MyReadLog.i(attributeName  + " : " + attributeValue);
                                        tagAttribute = new BookTagAttribute(attributeName, attributeValue);
                                        charIndex = semicolonIndex + 1;
                                    }
                                }

                                if (tags == null) {
                                    if (classSet != null && tagAttribute != null) {
                                        classSet.addAttribute(tagAttribute);
                                    }
                                } else { //将属性赋值给基础属性
                                    if (tagAttribute != null) {
                                        singleCSSSet.loadDefaultAttribute(tags, tagAttribute);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            depth = 0;
//            MyReadLog.i(cssFile.inFilePath);
            singleCSSSets.put(cssFile.inFilePath, singleCSSSet);
//            MyReadLog.i("----------");
//            for (int j = 0; j < singleCSSSet.classes.size(); j++) {
//                BookClassSet clz = singleCSSSet.classes.valueAt(j);
//                MyReadLog.i("size is " + clz.attributes.size());
//            }
        }
    }

    /**
     * 获取某class的属性
     *
     * @param classStr
     * @return
     */
    public BookClassSet getBookClass(String classStr, String tag) {
        classStr = classStr.trim();
        BookClassSet bookClassSet = null;
        if (singleCSSSets.size() > 0) {
            bookClassSet = new BookClassSet(classStr, tag);
            getDefaultAttribute(bookClassSet, tag);
            if (!TextUtils.isEmpty(classStr)) {
                String[] classValues = classStr.split(" ");
                bookClassSet = new BookClassSet(classStr);
//                MyReadLog.i("singleCSSSets.size() = " + singleCSSSets.size() + ", 第一个singleCssSet size " + singleCSSSets.valueAt(0).classes.size());
                for (int j = 0; j < singleCSSSets.size(); j++) {
                    BookSingleCSSSet singleCssSt = singleCSSSets.valueAt(j);
                    if (singleCssSt != null) {
                        for (int i = 0; i < classValues.length; i++) {
                            String classValue = classValues[i].trim();
                            if ((!TextUtils.isEmpty(classValue)) && singleCssSt.containClass(classValue, tag)) {
                                bookClassSet.addAttribute(singleCssSt.getBookClassSet(classValue, tag));
                            }
                        }
                    }
                }
            }
        }
        return bookClassSet;
    }

    /**
     * 获取默认属性
     *
     * @param bookClassSet
     */
    private void getDefaultAttribute(BookClassSet bookClassSet, String tagName) {
        for (int i = 0; i < singleCSSSets.size(); i++) {
            BookSingleCSSSet singleCSSSet = singleCSSSets.valueAt(i);
            if (singleCSSSet != null) {
                bookClassSet.addAttribute(singleCSSSet.getDefaultTagAttributes(tagName));
            }
        }
    }

    public BookCSSAttributeSet getNewCSSAttribute(ArrayList<String> cssArrayList) {
        BookCSSAttributeSet bookCSSAttributeSet = new BookCSSAttributeSet();
        if (cssArrayList != null && cssArrayList.size() > 0) {
            for (int i = 0; i < cssArrayList.size(); i++) {
                String cssFileId = cssArrayList.get(i).trim();
                if (!TextUtils.isEmpty(cssFileId)) {
                    if (this.singleCSSSets.containsKey(cssFileId)) {
//                        MyReadLog.i("cssFiledId = " + cssFileId + ", singCssSet size " + singleCSSSets.get(cssFileId).classes.size());
//                        for (int j = 0; j < singleCSSSets.get(cssFileId).classes.size(); j++) {
//                            BookClassSet clz = singleCSSSets.get(cssFileId).classes.valueAt(j);
//                            MyReadLog.i("size is " + clz.attributes.size());
//                        }
                        bookCSSAttributeSet.singleCSSSets.put(cssFileId, new BookSingleCSSSet(singleCSSSets.get(cssFileId)));
                    }
                }
            }
        }
        return bookCSSAttributeSet;
    }

    public ArrayMap<String, BookSingleCSSSet> getSingleCSSSets() {
        return singleCSSSets;
    }

    /**
     * 获取默认属性
     *
     * @param tagName
     * @return
     */
    public ArrayMap<String, BookTagAttribute> getDefaultAttributeMap(String tagName) {
        BookClassSet defaultClassSet = new BookClassSet("", tagName);
        getDefaultAttribute(defaultClassSet, tagName);
        return defaultClassSet.attributes;
    }

    public void reset() {
        singleCSSSets.clear();
    }
}
