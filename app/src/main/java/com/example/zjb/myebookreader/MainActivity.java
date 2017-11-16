package com.example.zjb.myebookreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        findViewById(R.id.show_files).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.show_files:
                goToFiles();
                break;
        }
    }

    /**
     * 跳转到文件界面
     */
    private void goToFiles() {
        Intent intent = new Intent();
        intent.setClass(this, EpubFilesActivity.class);
        startActivity(intent);
    }
}
