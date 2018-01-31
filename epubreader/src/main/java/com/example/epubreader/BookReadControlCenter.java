package com.example.epubreader;

import android.text.TextUtils;

import com.example.epubreader.book.BookModel;
import com.example.epubreader.book.toc.TocElement;
import com.example.epubreader.db.Book;
import com.example.epubreader.util.BookPositionUtil;
import com.example.epubreader.util.MyReadLog;
import com.example.epubreader.view.book.BookDummyView;
import com.example.epubreader.view.book.BookReadPosition;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Boyad on 2018/1/16.
 */

public class BookReadControlCenter extends BookControlCenter{
    private final BookDummyView dummyView;
    private volatile BookModel bookModel;

    public BookReadControlCenter() {
        super();
        dummyView = new BookDummyView(this);
        setDummyView(dummyView);
    }

    public void openBook(Book book) {
        MyReadLog.i("openBook : id = " + book.getBookId());
        if (bookModel == null || !bookModel.getEpubPath().equals(book.getFilePath()) ) {
            bookModel = new BookModel(book);
        }
        dummyView.setBookModel(bookModel);
        bookModel.decodeEpubMeta(book.getFilePath());
        gotoStorePosition();
        BookControlCenter.Instance().getViewListener().reset();
        BookControlCenter.Instance().getViewListener().repaint();
        calculateTotalPages();
    }

    public void calculateTotalPages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MyReadLog.i("BookReadControlCenter ---- > calculateTotalPages");
                BookControlCenter.Instance().getCurrentView().calculateTotalPages();
            }
        }).start();
    }

    public TocElement getChapterToc() {
        if (bookModel == null) return null;
        return bookModel.tocElement;
    }

    private final SaverThread mSaverThread = new SaverThread();
    private volatile String mStorePosition;
    private volatile Book myStoredPositionBook;

    private void gotoStorePosition() {
        myStoredPositionBook = bookModel != null ? bookModel.book : null;
        if (myStoredPositionBook == null) {
            return;
        }
        mStorePosition = myStoredPositionBook.bookPositionStr;
//        mStorePosition = ReaderApplication.getInstance().getLibraryShadow().getBookPosition(myStoredPositionBook.getBookId());
        if (TextUtils.isEmpty(mStorePosition)) {
            mStorePosition = "0-0:0:0/0";
        }
        dummyView.gotoPosition(BookPositionUtil.string2Position(mStorePosition));
        savePosition();
    }

    public  String getReadPosition() {
        if (TextUtils.isEmpty(mStorePosition)) {
            return "0-0:0:0/0";
        } else {
            return mStorePosition;
        }
    }

    public void storeReadPosition() {
        final Book book = bookModel != null ? bookModel.book : null;
        if (book != null && myStoredPositionBook == book) {
            MyReadLog.i("storeReadPosition");
            final String positionStr = dummyView.getCurrentPageStartElementPositionStr();
            if (!TextUtils.isEmpty(positionStr)) {
                mStorePosition = positionStr;
                savePosition();
            }
        }
    }

    private void savePosition() {
        final float readProgress = dummyView.getProgress();
        synchronized (mSaverThread){
            if (!mSaverThread.isAlive()) {
                mSaverThread.start();
            }
            MyReadLog.i("savePosition :id = " +     myStoredPositionBook.getBookId());

            mSaverThread.add(new PositionSaver(myStoredPositionBook, mStorePosition, readProgress));
        }
    }

    public void addBookMark() {
        String markStr = dummyView.getCurrentPage().getMarkStr();
        long currentTime = System.currentTimeMillis(); // 当前时间
        String markPositionstr =  dummyView.getCurrentPageStartElementPositionStr();
        MyReadLog.i("markStr = " + markStr + " \n markPositionStr = " + markPositionstr);
    }

    private class SaverThread extends Thread {
        private final List<Runnable> mTasks = Collections.synchronizedList(new LinkedList<Runnable>());

        public SaverThread() {
            setPriority(MIN_PRIORITY);
        }

        void add(Runnable task) {
            mTasks.add(task);
        }

        @Override
        public void run() {
            while (true) {
                synchronized (mTasks) {
                    while (!mTasks.isEmpty()) {
                        mTasks.remove(0).run();
                    }
                }
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class PositionSaver implements Runnable{
        private final Book mBook;
        private final String positionStr;
        private final float readProgress;

        public PositionSaver(Book mBook, String positionStr, float readProgress) {
            this.mBook = mBook;
            this.positionStr = positionStr;
            this.readProgress = readProgress;
        }

        @Override
        public void run() {
            mBook.bookPositionStr = positionStr;
            if (readProgress != -1) {
                mBook.progress = readProgress;
            }
            ReaderApplication.getInstance().getLibraryShadow().saveReadPosition(mBook.getBookId(), positionStr, readProgress);
        }
    }
}
