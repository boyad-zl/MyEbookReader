package com.example.epubreader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * DataBase
 * Created by Boyad on 2018/1/10.
 */

final class SQLiteBookDatabase extends BookDataBase {
    private final SQLiteDatabase myDataBase ;

    public SQLiteBookDatabase(Context context) {
        myDataBase = context.openOrCreateDatabase("books.db",Context.MODE_PRIVATE, null);
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
     * 属性：文字大小，白天/黑夜模式， 翻页动画 ,亮度信息（尚未确定）
     * 书签： 阅读的位置，书的id
     */
    private void createTables() {
        myDataBase.execSQL(
                "CREATE TABLE IF NOT EXISTS Books(" +
                        "book_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "encoding TEXT," +
                        "title TEXT NOT NULL," +
                        "book_cover_path TEXT UNIQUE NOT NULL"+
                        "file_path  UNIQUE NOT NULL" +
                        "read_position TEXT NOT NULL" +
                        "read_percent REAL" +
                        "last_read_time TEXT NOT NULL" +
                        "author TEXT)"
        );

        myDataBase.execSQL(
                "CREATE TABLE IF NOT EXITS Options(" +
                        "font_size INTEGER ," +
                        "is_day_model BLOB," +
                        "page_turn_type INTEGER)"
        );
    }
}
