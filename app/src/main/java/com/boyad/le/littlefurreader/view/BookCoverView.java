package com.boyad.le.littlefurreader.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Boyad on 2018/1/15.
 */

public class BookCoverView extends View {

    private Context mContext;

    public BookCoverView(Context context) {
        super(context);
        init(context);
    }

    public BookCoverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BookCoverView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;

    }
}
