package com.example.epubreader.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.epubreader.BuildConfig;
import com.example.epubreader.R;
import com.example.epubreader.ReaderApplication;
import com.example.epubreader.book.BookModel;
import com.example.epubreader.book.BookResourceFile;
import com.example.epubreader.book.toc.TocAdapter;
import com.example.epubreader.book.toc.TocElement;
import com.example.epubreader.util.BookAttributeUtil;
import com.example.epubreader.util.MyReadLog;
import com.example.epubreader.view.widget.BookReaderView;

import java.util.IllegalFormatCodePointException;

public class ReaderActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private String bookPath;
    private BookModel bookModel;
    private ImageView epubPic;
    private BookReaderView reader;
    private TextView dayOrNightBtn;
    private TextView coverShowBtn;
    private boolean isShow = false;
    private Bitmap coverBitmap;
    private TextView catalogBtn;
    private View catalogView;
    private ListView catalogListView;
    private TocAdapter mAdapter;
    private boolean isShowCatalog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyReadLog.i("==========onCreate===========");
//        reader = new BookReaderView(this);
        setContentView(R.layout.activity_reader);
//        setContentView(reader);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        ReaderApplication.getInstance().getWatcher().watch(this);
        Intent intent = getIntent();
        bookPath = intent.getStringExtra("BOOK_PATH");
        if (ReaderApplication.getInstance().getBookModel() == null || !ReaderApplication.getInstance().getBookModel().getEpubPath().equals(bookPath)) {
            ReaderApplication.getInstance().createBookModel(bookPath);
        }
        bookModel = ReaderApplication.getInstance().getBookModel();

        findViewById(R.id.font_size_larger).setOnClickListener(this);
        findViewById(R.id.font_size_smaller).setOnClickListener(this);
        dayOrNightBtn = (TextView) findViewById(R.id.btn_day_or_night_theme);
        dayOrNightBtn.setOnClickListener(this);
        coverShowBtn = (TextView) findViewById(R.id.btn_cover_show);
        coverShowBtn.setOnClickListener(this);
        epubPic = (ImageView) findViewById(R.id.reader_img);
        catalogBtn = (TextView) findViewById(R.id.btn_catalog_show);
        catalogBtn.setOnClickListener(this);
        catalogView = findViewById(R.id.view_catalog_view);
        catalogListView = (ListView) findViewById(R.id.list_view_catalog);
        catalogListView.setOnItemClickListener(this);

        reader = (BookReaderView) findViewById(R.id.main_reader_view);
        ReaderApplication.getInstance().setMyWidget(reader);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyReadLog.i("==========onResume===========");
    }


    @Override
    protected void onPause() {
        super.onPause();
        reader.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        reader.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.font_size_larger) {
            changeFontSize(true);
        } else if (i == R.id.font_size_smaller) {
            changeFontSize(false);
        }else if (i == R.id.btn_day_or_night_theme) {
            changeDayOrNightModel();
        } else if (i == R.id.btn_cover_show) {
            showOrHidePic();
        } else if (i == R.id.btn_catalog_show) {
            showOrHideCatalog();
        }
    }

    private void showOrHideCatalog() {
        isShowCatalog = !isShowCatalog;
        if (isShowCatalog) {
            if (mAdapter == null) {
                mAdapter = new TocAdapter(bookModel.tocElement, this);
                catalogListView.setAdapter(mAdapter);
            }
        }
        catalogView.setVisibility(isShowCatalog ? View.VISIBLE : View.GONE);
    }

    private void showOrHidePic() {
        if (isShow) {
            //隐藏
           epubPic.setVisibility(View.GONE);
        } else  {
            // 显示
            if (coverBitmap == null) {
                BookResourceFile coverFile = bookModel.imageFileArrayMap.get(bookModel.bookCover);
                if (coverFile != null) {
                    coverBitmap = BitmapFactory.decodeStream(bookModel.getImageInputStream(coverFile.inFilePath));
                }
            }
            epubPic.setImageBitmap(coverBitmap);
            epubPic.setVisibility(View.VISIBLE);
        }
        isShow = !isShow;
    }


    private void changeDayOrNightModel() {
        boolean isDayModel = ReaderApplication.getInstance().getDummyView().isDayModel();
        ReaderApplication.getInstance().getDummyView().setDayModel(!isDayModel);
        dayOrNightBtn.setText(isDayModel ?  "夜": "白") ;
        ReaderApplication.getInstance().getMyWidget().reset();
        ReaderApplication.getInstance().getMyWidget().repaint();
    }

    private void changeFontSize(boolean isLarger) {
        int currentFontSize = BookAttributeUtil.getEmSize();
        BookAttributeUtil.setEmSize(currentFontSize + (isLarger ? 2 : -2));
        ReaderApplication.getInstance().getDummyView().preparePage(bookModel.getReadPosition());
        ReaderApplication.getInstance().getMyWidget().reset();
        ReaderApplication.getInstance().getMyWidget().repaint();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TocElement selectedElement = bookModel.tocElement.getElementAt(position, false);
        MyReadLog.i(selectedElement.getPath());
        ReaderApplication.getInstance().getDummyView().jumpLinkHref(selectedElement.getPath());
    }
}
