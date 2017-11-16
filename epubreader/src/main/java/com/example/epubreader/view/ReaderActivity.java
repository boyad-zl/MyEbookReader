package com.example.epubreader.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.epubreader.R;
import com.example.epubreader.ReaderApplication;
import com.example.epubreader.book.BookModel;
import com.example.epubreader.book.EpubReaderHtml;
import com.example.epubreader.view.book.BookDummyView;

public class ReaderActivity extends AppCompatActivity {


    private String bookPath;
    private TextView info;

    private ImageView epubPic;
    private TextView contentView;
    private BookModel bookModel;
    private ImageView pageImg;
    private BookDummyView bookDummyView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);


        Intent intent = getIntent();
        bookPath = intent.getStringExtra("BOOK_PATH");
        bookModel = new BookModel(bookPath);
        bookDummyView = new BookDummyView(ReaderApplication.getInstance());
        info = (TextView) findViewById(R.id.reader_tv);

        epubPic = (ImageView) findViewById(R.id.reader_img);
        contentView = (TextView) findViewById(R.id.reader_content);
        pageImg = (ImageView) findViewById(R.id.reader_page_img);

    }

    @Override
    protected void onResume() {
        super.onResume();
        info.setText("bookPath--->" + bookPath);

        epubPic.postDelayed(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap =  bookModel.getBitmap(bookModel.bookCover);
                if (bitmap != null) {
                    epubPic.setImageBitmap(bitmap);
                }
            }
        }, 1000);

        contentView.postDelayed(new Runnable() {
            @Override
            public void run() {
                contentView.setText("开始展示内容");
                contentView.append("\n");
                EpubReaderHtml htmlContent = new EpubReaderHtml(bookModel);
                htmlContent.loadHtmlInputStream(bookModel.getTextContent());
                bookDummyView.setPages(htmlContent.getPages());
                Bitmap bitmap = Bitmap.createBitmap(ReaderApplication.getInstance().getWindowSize().widthPixels,
                                                    ReaderApplication.getInstance().getWindowSize().heightPixels,
                                                    Bitmap.Config.RGB_565);
                bookDummyView.paint(bitmap);
                pageImg.setImageBitmap(bitmap);
            }
        }, 1000);

        // TODO TEST:如果图片过大的话，会消耗时间较长可以考虑异步加载图片
//        long startLoadCover = System.currentTimeMillis();
//        Bitmap coverBitmap = BitmapFactory.decodeStream();
//        epubPic.setImageBitmap(coverBitmap);
//        MyReadLog.i("Load book Cover cost time is " + (System.currentTimeMillis() - startLoadCover));
//
//        StringBuffer sb = new StringBuffer();
//        String contentPath = textFileArrayMap.get(spinContent[10]).inFilePath;
//        MyReadLog.i("contentPath: " + contentPath);
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipFile.getEntry(contentPath))));
//        String contentLine;
//        while ((contentLine = bufferedReader.readLine()) != null) {
//            sb.append(contentLine + "\n");
//        }
//        contentView.setText(sb);
    }
}
