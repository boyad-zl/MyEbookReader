package com.example.epubreader.book;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Boyad on 2017/12/5.
 */
public class Book {
    public String title; // 电子书的名称
    public String language; //语言
    public List<String> authors; //作者
    public long UUID;
    public String rights; //版权描叙
    public String description; // 电子书的内容介绍
    public long ISBN;
    public String maker; //电子书制作者

    public void addAuthor(String s) {
        if (authors == null) {
            authors = new ArrayList<>();
        }
        authors.add(s);
    }
}
