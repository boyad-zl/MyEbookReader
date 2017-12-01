package com.example.epubreader.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.epubreader.R;
import com.example.epubreader.ReaderApplication;
import com.example.epubreader.book.BookModel;
import com.example.epubreader.book.BookResourceFile;
import com.example.epubreader.util.BookAttributeUtil;
import com.example.epubreader.util.MyReadLog;
import com.example.epubreader.view.widget.BookReaderView;

public class ReaderActivity extends AppCompatActivity implements View.OnClickListener {
    private String bookPath;
    private BookModel bookModel;
    private ImageView epubPic;
    private BookReaderView reader;
    private TextView dayOrNightBtn;
    private TextView coverShowBtn;
    private boolean isShow = false;
    private Bitmap coverBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyReadLog.i("==========onCreate===========");
        setContentView(R.layout.activity_reader);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

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

        reader = (BookReaderView) findViewById(R.id.main_reader_view);
        ReaderApplication.getInstance().setMyWidget(reader);
    }

    @Override
    protected void onResume() {
        super.onResume();
       // MyReadLog.i("==========onResume===========");
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
        }if (i == R.id.btn_cover_show) {
            showOrHidePic();
        }
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
        ReaderApplication.getInstance().getMyWidget().repaint();
    }

    private void changeFontSize(boolean isLarger) {
        int currentFontSize = BookAttributeUtil.getEmSize();
        BookAttributeUtil.setEmSize(currentFontSize + (isLarger ? 2 : -2));
        ReaderApplication.getInstance().getDummyView().reset();
        ReaderApplication.getInstance().getMyWidget().repaint();
    }
}
