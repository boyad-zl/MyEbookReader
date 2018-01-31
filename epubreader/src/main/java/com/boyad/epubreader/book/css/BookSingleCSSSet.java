package com.boyad.epubreader.book.css;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

/**
 * 单一CSS文件的实体类
 * Created by Boyad on 2017/11/6.
 */
public class BookSingleCSSSet {
    private String name;
    public ArrayMap<String, BookClassSet> classes;
    public ArrayMap<String, BookClassSet> rootClasses; // 保存默认属性


    public BookSingleCSSSet(String name) {
        this.name = name;
        rootClasses = new ArrayMap<>();
        classes = new ArrayMap<>();
    }

    public BookSingleCSSSet(BookSingleCSSSet other) {
        this.name = other.name;
        this.classes = other.classes;
        this.rootClasses = other.rootClasses;
    }


    public void addClass(BookClassSet classSet){
        if (classSet.className == null || TextUtils.isEmpty(classSet.className)) {
            classes.put("." + classSet.getClassName(), classSet);
        } else {
            classes.put(classSet.tagName + "." + classSet.getClassName(), classSet);
        }
    }

    public void loadDefaultAttribute(String[] tags, BookTagAttribute tagAttribute) {
        for (int i = 0; i < tags.length; i++) {
            String tagName = tags[i].trim().toLowerCase();
            if (!TextUtils.isEmpty(tagName)){
                if (!rootClasses.containsKey(tagName)){
                    rootClasses.put(tagName, new BookClassSet("", tagName));
                }
                BookClassSet classSet = rootClasses.get(tagName);
                classSet.addAttribute(tagAttribute);
            }
        }
    }

    /**
     * 获取tag标签定义的默认属性
     * @param tag
     * @return
     */
    public BookClassSet getDefaultTagAttributes(String tag) {
//        ArrayMap<String, BookTagAttribute> attributes = new ArrayMap<>();
        if (rootClasses != null && rootClasses.containsKey(tag)){
            return rootClasses.get(tag);
        }
        return null;
    }

    /**
     * 在css中是否存在某样式
     * @param className
     * @param tag
     * @return
     */
    public boolean containClass(String className, String tag) {
        return classes.containsKey(tag + "." + className) || classes.containsKey("." + className);
    }

    /**
     * 获取指定样式
     * @param classValue
     * @param tag
     * @return
     */
    public BookClassSet getBookClassSet(String classValue, String tag) {
        String key = tag + "." + classValue;
        if (classes.containsKey(key)) {
            return classes.get(key);
        } else {
            return classes.get(("." + classValue));
        }
    }
}
