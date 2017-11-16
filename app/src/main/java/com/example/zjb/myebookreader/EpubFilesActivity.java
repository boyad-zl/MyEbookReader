package com.example.zjb.myebookreader;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.epubreader.view.ReaderActivity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class EpubFilesActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView mListView;
    private List<File> epubFilsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epub_files);
        mListView = (ListView) findViewById(R.id.epub_list_view);
        mListView.setOnItemClickListener(this);
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
        EpubFilesListAdapter adapter = new EpubFilesListAdapter((ArrayList<File>) epubFilsList, this);
        mListView.setAdapter(adapter);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i("Reader", "position" + position);
        Toast.makeText(this, epubFilsList.get(position).getName(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setClass(this, ReaderActivity.class);
        intent.putExtra("BOOK_PATH", epubFilsList.get(position).getAbsolutePath());
        startActivity(intent);
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
}
