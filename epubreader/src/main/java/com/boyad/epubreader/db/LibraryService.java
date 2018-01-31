package com.boyad.epubreader.db;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.List;

public class LibraryService extends Service {
   private static SQLiteBookDatabase ourDatabase;
    private static final Object ourDatabaseLock = new Object();

    private volatile LibraryImplementation myLibrary;

    @Override
    public void onStart(Intent intent, int startId) {
        onStartCommand(intent, 0 , startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
       return myLibrary;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (ourDatabaseLock) {
            ourDatabase = new SQLiteBookDatabase(LibraryService.this);
        }
        myLibrary = new LibraryImplementation();
    }

    private class LibraryImplementation extends LibraryInterface.Stub {
        @Override
        public void removeBook(Book book) throws RemoteException {
//            ourDatabase.removeBook();
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            ourDatabase.addBook(book);
        }

        @Override
        public List<Book> listBook() throws RemoteException {
           return ourDatabase.listBook();
        }

        @Override
        public String getReadPosition(int bookId) throws RemoteException {
            return ourDatabase.getBookPosition(bookId);
        }

        @Override
        public void saveReadPosition(int bookID, String postionStr, float progress){
            ourDatabase.saveReadPosition(bookID, postionStr, progress);
        }
    }
}
