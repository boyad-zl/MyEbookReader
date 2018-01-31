package com.boyad.epubreader.book.toc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.boyad.epubreader.R;
import com.boyad.epubreader.util.BookUIHelper;

/**
 * Created by Boyad on 2017/11/30.
 */

public class TocAdapter extends BaseAdapter {

    private TocElement mainElement;
    private Context mContext;
    private int depthGap;

    public TocAdapter(TocElement mainElement, Context mContext) {
        this.mainElement = mainElement;
        mainElement.setOpened(true);
        depthGap = BookUIHelper.dp2px(3);
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
//        MyReadLog.i("count = " + mainElement.getCount());
        return mainElement.getCount();
    }

    @Override
    public TocElement getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final View contentView = LayoutInflater.from(mContext).inflate(R.layout.item_catalog, parent, false);
        TextView catalogText = contentView.findViewById(R.id.item_catalog_name);
        TextView catalogOpenBtn = contentView.findViewById(R.id.item_catalog_open);
        TextView pageIndexTxt =  contentView.findViewById(R.id.item_catalog_page_index);
        catalogOpenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TocElement element = (TocElement) contentView.getTag();
                element.setOpened(!element.isOpened());
//                MyReadLog.i("position = " + position);
                notifyDataSetChanged();
            }
        });
        TocElement tocElement = mainElement.getElementAt(position, false);
        contentView.setPadding(tocElement.getDepth() * depthGap, 0, 0, 0);
        catalogText.setText(tocElement.getName());
        catalogOpenBtn.setEnabled(tocElement.canOpen());
        if (tocElement.canOpen()) {
            catalogOpenBtn.setText(tocElement.isOpened() ? "-" : "+");
        } else {
            catalogOpenBtn.setText("");
        }
//        MyReadLog.i("tocElement.getPageIndex() = " + tocElement.getPageIndex());
        if (tocElement.getPageIndex() == -1) {
            pageIndexTxt.setVisibility(View.GONE);
        } else {
            pageIndexTxt.setVisibility(View.VISIBLE);
            pageIndexTxt.setText("" + tocElement.getPageIndex());
        }
        contentView.setTag(tocElement);
        return contentView;
    }
}
