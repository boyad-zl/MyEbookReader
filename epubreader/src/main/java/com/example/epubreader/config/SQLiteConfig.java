package com.example.epubreader.config;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.RemoteException;

import com.example.epubreader.util.BookConstract;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Boyad on 2018/1/11.
 */

public class SQLiteConfig extends ConfigInterface.Stub {
    private final Service myService;

    private final SQLiteDatabase myDataBase;
    private final SQLiteStatement myGetValueStatement;
    private final SQLiteStatement mySetValueStatement;
//    private final SQLiteStatement myUnsetValueStatement;
//    private final SQLiteStatement myDeleteGroupValueStatement;

    public SQLiteConfig(Service myService) {
        this.myService = myService;
        myDataBase = myService.openOrCreateDatabase("config.db", Context.MODE_PRIVATE, null);
        switch (myDataBase.getVersion()) {
            case 0:
                myDataBase.execSQL("CREATE TABLE IF NOT EXISTS config(" +
                        "name VARCHAR PRIMARY KEY," +
                        "value VARCHAR)" );
//                        "PRIMARY KEY (groupName, name)");
//                "groupName VARCHAR," +

                break;
        }
        myDataBase.setVersion(1);
//        myGetValueStatement = myDataBase.compileStatement("SELECT value FROM config WHERE groupName = ? AND name = ?");
//        mySetValueStatement = myDataBase.compileStatement("INSERT OR REPLACE INTO config (groupName, name, value) VALUE (?, ?, ?)");
//        myUnsetValueStatement = myDataBase.compileStatement("DELETE FROM config WHERE groupName = ? AND name = ?");
//        myDeleteGroupValueStatement = myDataBase.compileStatement("DELETE FROM config WHERE groupName = ? ");
        myGetValueStatement = myDataBase.compileStatement("SELECT value FROM config WHERE name = ?");
        mySetValueStatement = myDataBase.compileStatement("INSERT OR REPLACE INTO config (name, value) VALUES (?, ?)");

        initDataBase();

    }

    /**
     * 插入初始化数据
     */
    private void initDataBase() {
        myDataBase.beginTransaction();
        mySetValueStatement.bindString(1,BookConstract.CONFIG_NAME_DAY_MODEL);
        mySetValueStatement.bindString(2,BookConstract.CONFIG_VALUE_DAY_MODEL);
        mySetValueStatement.execute();

        mySetValueStatement.bindString(1,BookConstract.CONFIG_NAME_FONT_SIZE);
        mySetValueStatement.bindString(2,BookConstract.CONFIG_VALUE_FONT_SIZE);
        mySetValueStatement.execute();

        myDataBase.setTransactionSuccessful();
        myDataBase.endTransaction();
        myDataBase.execSQL("VACUUM");

    }

    /**
    @Override
    public List<String> listGroup() throws RemoteException {
        final LinkedList<String> list = new LinkedList<>();
        final Cursor cursor = myDataBase.rawQuery("SELECT DISTINCT groupName FROM config", null);
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0));
        }
        cursor.close();
        return list;
    }

    @Override
    public List<String> listName(String group) throws RemoteException {
        final LinkedList<String> list = new LinkedList<>();
        final Cursor cursor = myDataBase.rawQuery("SELECT name FROM config WHERE groupName = ?", new String[]{group});
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0));
        }
        cursor.close();
        return list;
    }
*/
    @Override
//    public String getValue(String group, String name) throws RemoteException {
    public String getValue(String name) throws RemoteException {
        if (!myDataBase.isOpen()) {
            myService.openOrCreateDatabase("config.db",Context.MODE_PRIVATE, null);
            myDataBase.beginTransaction();
        }
//        myGetValueStatement.bindString(1, group);
        myGetValueStatement.bindString(1, name);
        try {
            return myGetValueStatement.simpleQueryForString();
        } catch (SQLException e) {
            return  null;
        }
    }

    @Override
//    public void setValue(String group, String name, String value) throws RemoteException {
    public void setValue(String name, String value) throws RemoteException {
//        mySetValueStatement.bindString(1, group);
//        mySetValueStatement.bindString(2, name);
//        mySetValueStatement.bindString(3, value);
        mySetValueStatement.bindString(1, name);
        mySetValueStatement.bindString(2, value);
        try {
            mySetValueStatement.execute();
//            sendChangeEvent(group, name, value);
            sendChangeEvent(name, value);
        } catch (SQLException e) {
            
        }

    }


/**
    @Override
    public void unsetValue(String group, String name) throws RemoteException {
        myUnsetValueStatement.bindString(1,group);
        myUnsetValueStatement.bindString(2,name);

        try {

            myUnsetValueStatement.execute();
            sendChangeEvent(group,name,null);
        } catch (SQLException e) {
        }
    }

    @Override
    public void removeGroup(String name) throws RemoteException {
        myDeleteGroupValueStatement.bindString(1, name);
        try {
            myDeleteGroupValueStatement.execute();
        } catch (SQLException e) {

        }
    }

    @Override
    public List<String> requestAllValueForGroup(String group) throws RemoteException {
        try {
            final List<String> pairs = new LinkedList<>();
            final Cursor cursor = myDataBase.rawQuery("SELECT name, value FROM config WHERE groupName = ?", new String[]{group});
            while (cursor.moveToNext()) {
                pairs.add(cursor.getString(0) + "\000" + cursor.getString(1));
            }
            cursor.close();
            return pairs;
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }
*/
    // todo test 发送修改属性的广播
//    private void sendChangeEvent(String group, String name, String value) {
    private void sendChangeEvent( String name, String value) {
        myService.sendBroadcast(new Intent(BookConstract.ACTION_CONFIG_OPTION_CHANGE)
                .putExtra("name", name)
                .putExtra("value", value));
//        .putExtra("group", group)
    }
}
