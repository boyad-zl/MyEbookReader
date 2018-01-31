package com.example.epubreader.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.epubreader.R;
import com.example.epubreader.ReaderApplication;
import com.example.epubreader.book.BookModel;
import com.example.epubreader.book.EpubReaderHtml;
import com.example.epubreader.view.book.BookDummyAbstractView;

public class TestActivity extends AppCompatActivity {

    private ImageView testImage;
    private String bookPath;
    private BookModel bookModel;
    private BookDummyAbstractView bookDummyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        testImage = (ImageView) findViewById(R.id.test_img);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        Intent intent = getIntent();
        bookPath = intent.getStringExtra("BOOK_PATH");
//        bookModel = new BookModel(bookPath);
//        if (ReaderApplication.getInstance().getBookModel() == null || !ReaderApplication.getInstance().getBookModel().getEpubPath().equals(bookPath)) {
//            ReaderApplication.getInstance().createBookModel(bookPath);
//        }
//        bookModel = ReaderApplication.getInstance().getBookModel();
//        bookDummyView = ReaderApplication.getInstance().getDummyView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        testImage.post(new Runnable() {
            @Override
            public void run() {
                EpubReaderHtml htmlContent = new EpubReaderHtml(bookModel);
                htmlContent.loadHtmlInputStream(2, true);
//                htmlContent.parseHtml(bookModel.getTextContent(5));
//        htmlContent.parseHtmlByPull(bookModel.getTextContent(5));
                bookDummyView.setPages(htmlContent.getPages());
                Bitmap bitmap = Bitmap.createBitmap(ReaderApplication.getInstance().getWindowSize().widthPixels,
                        ReaderApplication.getInstance().getWindowSize().heightPixels,
                        Bitmap.Config.RGB_565);
                bookDummyView.paint(bitmap, 1);
                testImage.setImageBitmap(bitmap);
            }
        });

    }
}
