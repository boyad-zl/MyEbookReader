package com.boyad.epubreader.db;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.List;

/**
 * Created by Boyad on 2018/1/12.
 */

public class LibraryShadow implements ServiceConnection {
    private Context mContext;
    private LibraryInterface mInterface;

    public LibraryShadow(Context mContext) {
        this.mContext = mContext;
        mContext.bindService(new Intent(mContext, LibraryService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        synchronized (this) {
            mInterface = LibraryInterface.Stub.asInterface(service);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mInterface = null;
    }

    public void addBook(Book book) {
        if (mInterface == null) {
            return;
        }
        try {
            mInterface.addBook(book);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public List<Book> listBook() {
        if (mInterface == null) {
            return null;
        }
        try {
            return mInterface.listBook();
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getBookPosition(int bookId) {
        if (mInterface == null) {
            return "";
        } else {
            try {
                return mInterface.getReadPosition(bookId);
            } catch (RemoteException e) {
                return "";
            }
        }
    }

    public void saveReadPosition(int bookId, String positionStr, float progress) {
        if (mInterface != null) {

            try {
                mInterface.saveReadPosition(bookId, positionStr, progress);
            } catch (RemoteException e) {

            }
        }
    }
}
