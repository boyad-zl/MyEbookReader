package com.example.epubreader.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.epubreader.R;
import com.example.epubreader.ReaderApplication;
import com.example.epubreader.book.BookModel;
import com.example.epubreader.book.BookResourceFile;
import com.example.epubreader.book.toc.TocAdapter;
import com.example.epubreader.book.toc.TocElement;
import com.example.epubreader.config.ConfigShadow;
import com.example.epubreader.util.BookAttributeUtil;
import com.example.epubreader.util.BookConstract;
import com.example.epubreader.util.BookSettings;
import com.example.epubreader.util.MyReadLog;
import com.example.epubreader.view.widget.BookReaderGLSurfaceView;
import com.example.epubreader.view.widget.BookReaderView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReaderActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private String bookPath;
    private BookModel bookModel;
    private ImageView epubPic;
//    private BookReaderSurfaceView reader;
    private BookReaderView reader;
    private BookReaderGLSurfaceView glReader;
    private TextView dayOrNightBtn;
    private TextView coverShowBtn;
    private boolean isShow = false;
    private Bitmap coverBitmap;
    private TextView catalogBtn;
    private View catalogView;
    private ListView catalogListView;
    private TocAdapter mAdapter;
    private boolean isShowCatalog = false;
    private boolean isCul;
    private TextView culBtn;
    private BroadcastReceiver mBroadcastReceiver;


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

        culBtn = (TextView) findViewById(R.id.btn_turn_cul);
        culBtn.setOnClickListener(this);

//        reader = (BookReaderSurfaceView) findViewById(R.id.main_reader_view);
        reader = (BookReaderView) findViewById(R.id.main_reader_view);
        glReader = (BookReaderGLSurfaceView) findViewById(R.id.main_reader_gl_view);

        initReceiver();
    }

    /**
     * 初始化
     */
    private void initReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                onReceiveBroadcast(action, intent.getExtras());
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        List<String> needAddActions = getReceiverFilterActions();
        if (needAddActions != null) {
            int size = needAddActions.size();
            for (int i = 0; i < size; i++) {
                intentFilter.addAction(needAddActions.get(i));
            }
        }
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void onReceiveBroadcast(String action, Bundle extras) {
        switch (action){
            case BookConstract.ACTION_CONFIG_OPTION_CHANGE:
                String group = (String) extras.get("group");
                String name = (String) extras.get("name");
                String value = (String) extras.get("value");

                break;
        }
    }

    private List<String> getReceiverFilterActions() {
        return Arrays.asList(BookConstract.ACTION_CONFIG_OPTION_CHANGE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        isCul = BookSettings.getPageTurnAnimation();
        updateCulBtn();
        controlReadView(isCul);
        MyReadLog.i("==========onResume===========");
    }

    private void controlReadView(boolean isCul) {
        if (isCul) {
            glReader.setVisibility(View.VISIBLE);
            reader.setVisibility(View.GONE);
            ReaderApplication.getInstance().setMyWidget(glReader);
        } else {
            reader.setVisibility(View.VISIBLE);
            glReader.setVisibility(View.GONE);
            ReaderApplication.getInstance().setMyWidget(reader);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
//        reader.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
//        reader.onDestroy();
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
        } else if (i == R.id.btn_turn_cul) {
            toggleCulAnimation();
        }
    }

    private void toggleCulAnimation() {
        isCul = !isCul;
        updateCulBtn();
        controlReadView(isCul);
        ReaderApplication.getInstance().getMyWidget().repaint();
    }

    private void updateCulBtn() {
       culBtn.setTextColor(isCul ? Color.RED : Color.DKGRAY);
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
//            epubPic.setImageBitmap(reader.getPageBitmapManager().getBitmap(1));
//            epubPic.setVisibility(View.VISIBLE);
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
