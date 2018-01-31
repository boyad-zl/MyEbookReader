package com.example.epubreader.db;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Boyad on 2017/12/5.
 */
public class Book implements Parcelable {
    public String title; // 电子书的名称
    public String rights; //版权描叙
    public String description; // 电子书的内容介绍
    public String coverFile;

    private String filePath;
    public String bookPositionStr;
    public float progress;
    private int bookId;
    public long lastReadTime;
    public String authors;

    public Book(String filePath) {
        this.filePath = filePath;
    }

    protected Book(Parcel in) {
        title = in.readString();
        rights = in.readString();
        description = in.readString();
        coverFile = in.readString();
        filePath = in.readString();
        bookPositionStr = in.readString();
        bookId = in.readInt();
        progress = in.readFloat();
        lastReadTime = in.readLong();
        authors = in.readString();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public String getFilePath() {
        return filePath;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId (int bookId) {
        this.bookId = bookId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(rights);
        dest.writeString(description);
        dest.writeString(coverFile);
        dest.writeString(filePath);
        dest.writeString(bookPositionStr);
        dest.writeInt(bookId);
        dest.writeFloat(progress);
        dest.writeLong(lastReadTime);
        dest.writeString(authors);
    }


    public void addAuthor(String author) {
        if (TextUtils.isEmpty(authors)) {
            authors = author;
        } else {
            authors = authors + "/" + author;
        }
    }
}
