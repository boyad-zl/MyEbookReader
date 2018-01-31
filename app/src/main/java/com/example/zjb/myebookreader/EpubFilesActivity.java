package com.example.zjb.myebookreader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.epubreader.ReaderApplication;
import com.example.epubreader.db.Book;
import com.example.epubreader.util.EpubPullParserUtil;
import com.example.epubreader.util.MyReadLog;
import com.example.epubreader.view.ReaderActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

public class EpubFilesActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, ViewPager.OnPageChangeListener {

    private ListView mFileListView;
    private List<File> epubFilsList;
    private ViewPager mViewPager;
    private DefaultViewPagerAdapter mViewPagerAdapter;
    private GridView bookGridView;
    private BookGridAdapter bookAdapter;
    private EpubFilesListAdapter fileAdapter;
    private List<Book> books;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epub_files);
        mViewPager = (ViewPager) findViewById(R.id.epub_view_pager);
        List<View> mViewList = new ArrayList<>();
        mFileListView = new ListView(this);
        mFileListView.setOnItemClickListener(this);
        mViewList.add(mFileListView);

        bookGridView = new GridView(this);
        bookGridView.setNumColumns(3);
        bookAdapter = new BookGridAdapter(this);
        bookGridView.setAdapter(bookAdapter);
        bookGridView.setOnItemClickListener(this);
        mViewList.add(bookGridView);

        mViewPagerAdapter = new DefaultViewPagerAdapter(mViewList, this);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setCurrentItem(1);
//                mFileListView = (ListView) findViewById(R.id.epub_list_view);
    }


    @Override
    protected void onResume() {
        super.onResume();
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        Log.i("Reader", "sdCardExist = " + sdCardExist);
        File sdDir = null;
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        }

        FilenameFilter epubFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".epub");
            }
        };
        File[] epubs = sdDir.listFiles(epubFilter);
        if (epubs != null) {
            Log.i("Reader", "epubs size is " + epubs.length);
        }

        epubFilsList = new ArrayList<>();
        for (int i = 0; i < epubs.length; i++) {
            epubFilsList.add(epubs[i]);
        }
        fileAdapter = new EpubFilesListAdapter((ArrayList<File>) epubFilsList, this);
        mFileListView.setAdapter(fileAdapter);

        books = ReaderApplication.getInstance().getLibraryShadow().listBook();
        bookAdapter.setBooks(books);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent == mFileListView) {
            getMetaInfo(epubFilsList.get(position).getAbsolutePath());
        } else {
            Intent intent = new Intent();
            intent.setClass(this, ReaderActivity.class);
//        intent.setClass(this, TestActivity.class);
            intent.putExtra("BOOK", bookAdapter.getData().get(position));
            intent.putExtra("BOOK_PATH", epubFilsList.get(position).getAbsolutePath());
            startActivity(intent);
        }
    }

    private void getMetaInfo(String absolutePath) {
        try {
            ZipFile epubZipFile = new ZipFile(absolutePath);
            Book book = new Book(absolutePath);
            EpubPullParserUtil.parseOnlyMetaFile(epubZipFile, book);
//            ReaderApplication.getInstance().getLibraryShadow().getBook(book);
            ReaderApplication.getInstance().getLibraryShadow().addBook(book);
            MyReadLog.i("Book: title = " + book.title + ",Path = " + absolutePath + ", coverPath = " + book.coverFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 1) {
            books = ReaderApplication.getInstance().getLibraryShadow().listBook();
            bookAdapter.setBooks(books);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class EpubFilesListAdapter extends BaseAdapter {
        private ArrayList<File> data;
        private Context mContext;

        public EpubFilesListAdapter(ArrayList<File> data, Context mContext) {
            this.data = data;
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View contentView = LayoutInflater.from(mContext).inflate(R.layout.item_epub_file, parent, false);
            TextView nameText = contentView.findViewById(R.id.item_epub_name);
            nameText.setText(data.get(position).getName());
            return contentView;
        }
    }

    private class DefaultViewPagerAdapter extends PagerAdapter {
        protected List<? extends View> mViewList;
        protected Context mContext;
        private int mCount;

        public DefaultViewPagerAdapter(List<? extends View> mViewList, Context mContext) {
            this.mViewList = mViewList;
            this.mContext = mContext;
            mCount = mViewList == null ? 0 : mViewList.size();
        }

        @Override
        public int getCount() {
            return mCount;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return object == view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mViewList.get(position);
            if (view.getParent() != null) {
                container.removeView(view);
            } else {
                container.addView(view);
            }
            return view;
        }
    }

    private class BookGridAdapter extends BaseAdapter {
        private List<Book> books;
        private Context mContext;

        public BookGridAdapter(Context mContext) {
            this.mContext = mContext;
        }

        public void setBooks(List<Book> books) {
            this.books = books;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return books == null ? 0 : books.size();
        }

        @Override
        public Object getItem(int position) {
            return books == null ? null : books.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_book, null);
            TextView title = (TextView) itemView.findViewById(R.id.item_book_title);
            ImageView coverImg = (ImageView) itemView.findViewById(R.id.item_book_cover);
            title.setText(books.get(position).title);
            if (TextUtils.isEmpty(books.get(position).coverFile)) {
                coverImg.setBackgroundColor(Color.GRAY);
            } else {
                try {
                    Bitmap coverBitmap = BitmapFactory.decodeStream(new FileInputStream(books.get(position).coverFile));
                    coverImg.setImageBitmap(coverBitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    coverImg.setBackgroundColor(Color.GRAY);
                } catch (OutOfMemoryError e){
//                    Bitmap coverBitmap = BitmapFactory.decodeStream(new FileInputStream(books.get(position).coverFile));
//                    coverImg.setImageBitmap(coverBitmap);
                }

            }
            return itemView;
        }

        public List<Book> getData() {
            return books;
        }
    }
}
