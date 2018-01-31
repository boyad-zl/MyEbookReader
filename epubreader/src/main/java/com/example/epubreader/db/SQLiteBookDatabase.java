package com.example.epubreader.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import com.example.epubreader.util.MyReadLog;

import java.util.ArrayList;
import java.util.List;

/**
 * DataBase
 * Created by Boyad on 2018/1/10.
 */

final class SQLiteBookDatabase extends BookDatabase {
    private final SQLiteDatabase myDataBase;

    public SQLiteBookDatabase(Context context) {
        myDataBase = context.openOrCreateDatabase("booklist.db", Context.MODE_PRIVATE, null);
        migrate();
    }

    private void migrate() {
        final int version = myDataBase.getVersion();
        final int currentVersion = 1;
        if (version >= currentVersion) {
            return;
        }
        myDataBase.beginTransaction();
        switch (version) {
            case 0:
                createTables();
                break;
        }
        myDataBase.setTransactionSuccessful();
        myDataBase.setVersion(currentVersion);
        myDataBase.endTransaction();

        myDataBase.execSQL("VACUUM");
    }

    /**
     * 需要储存到数据库里面东西有
     * 书： 书的id（主键）,书名, 封面的路径， 书的文件路径,阅读位置，书的阅读进度（浮点小数），上次于阅读时间，作者
     * 书签： 阅读的位置，书的id
     */
    private void createTables() {
        myDataBase.execSQL(
                "CREATE TABLE IF NOT EXISTS Books(" +
                        "book_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "path VARCHAR(200) NOT NULL UNIQUE," +
                        "title VARCHAR(50) NOT NULL," +
                        "cover_path VARCHAR(200)," +
                        "read_position VARCHAR(200)," +
                        "read_percent REAL," +
                        "last_read_time REAL," +
                        "author VARCHAR(50))"
        );
    }

    public void addBook(Book book) {
        MyReadLog.i("addBook");
        try {
            myDataBase.execSQL("insert into Books (path, title, cover_path, author) values (" +
                    "'" + book.getFilePath() + "', '" + book.title + "','" + book.coverFile + "', '" + book.authors+ "')");
        } catch (SQLiteConstraintException e) {
            // 文件中存在相同的路径
            MyReadLog.i("相同的路径");
        }
    }

    public List<Book> listBook() {
        List<Book> books = new ArrayList<>();
        final Cursor cursor = myDataBase.rawQuery("SELECT Books.* FROM Books GROUP BY book_id ORDER BY last_read_time DESC", null);
        while (cursor.moveToNext()) {
            Book book = new Book(cursor.getString(1));
            book.setBookId(cursor.getInt(0));
            book.title = cursor.getString(2);
            book.coverFile = cursor.getString(3);
            book.bookPositionStr = cursor.getString(4);
            book.progress = cursor.getFloat(5);
            books.add(book);
            MyReadLog.d("bookId:%s,title: %s, bookPosition : %s, bookPercent : %s", book.getBookId(), book.title, book.bookPositionStr, book.progress);
        }
        cursor.close();
        return books;
    }

    public String getBookPosition(int bookId) {
        Cursor cursor = myDataBase.rawQuery("SELECT read_position FROM Books WHERE book_id = " + bookId, null);
        String position  = "";
        while (cursor.moveToNext()) {
            position =  cursor.getString(1);
        }
        cursor.close();
        return position;
    }

    public void saveReadPosition(int bookID, String positionStr, float progress) {
        String sqlStr;
        long lastReadTime = System.currentTimeMillis();
        if (progress == -1) {
            sqlStr = "update Books set read_position = '" + positionStr + "' , last_read_time = " + lastReadTime + " where book_id = " + bookID;
        } else {
            sqlStr = "update Books Set read_position ='" + positionStr + "',read_percent = " + progress + ", last_read_time = "+ lastReadTime + " where book_id = " + bookID;
        }
        myDataBase.execSQL(sqlStr);
    }
}
