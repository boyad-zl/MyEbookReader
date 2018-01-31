package com.example.epubreader.view.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.example.epubreader.BookControlCenter;
import com.example.epubreader.BookReadControlCenter;
import com.example.epubreader.R;
import com.example.epubreader.book.toc.TocAdapter;
import com.example.epubreader.book.toc.TocElement;
import com.example.epubreader.util.BookUIHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Boyad on 2018/1/18.
 */

public class BookChapterMarkDialog extends DialogFragment implements AdapterView.OnItemClickListener {
    private FragmentActivity mActivity;
    private Dialog mDialog;
    private ViewPager viewPager;
    private ListView chapterListView;
    private TocElement mainTocElement;

    public BookChapterMarkDialog() {
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            dialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION );
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    public BookChapterMarkDialog initWithContext(Context context) {
        if (context instanceof FragmentActivity) {
            mActivity = (FragmentActivity) context;
        }
        return this;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mActivity == null) return null;
        if (mDialog == null) {
            mDialog = onCreateDialog(mActivity, savedInstanceState);
        }
        updateBeforeShow();
        return mDialog;
    }

    private void updateBeforeShow() {
        mainTocElement = ((BookReadControlCenter) BookControlCenter.Instance()).getChapterToc();
        TocAdapter chapterAdapter = new TocAdapter(mainTocElement, mActivity);
        chapterListView.setAdapter(chapterAdapter);
    }

    private Dialog onCreateDialog(FragmentActivity mActivity, Bundle savedInstanceState) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_chapter_mark, null);
        BookUIHelper.setNotFastClickListener(view.findViewById(R.id.dialog_chapter_mark_empty_view), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        viewPager = (ViewPager) view.findViewById(R.id.dialog_chapter_mark_view_pager);
        initViewPager();
        Dialog dialog = new Dialog(mActivity, R.style.FloatTranslucentDimStyle);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setWindowAnimations(R.style.leftDialogAnim);
        return dialog;
    }

    private void initViewPager() {
        List<View> viewList = new ArrayList<>();
        chapterListView = new ListView(mActivity);
        chapterListView.setBackgroundColor(Color.WHITE);
        chapterListView.setOnItemClickListener(this);
        viewList.add(chapterListView);

        ViewPagerAdapter mAdapter = new ViewPagerAdapter(viewList, mActivity);
        viewPager.setAdapter(mAdapter);
    }

    public boolean isDialogShowing() {
        return getDialog() != null && getDialog().isShowing();
    }

    public final void showDialog() {
        if (isDialogShowing() || mActivity == null) return;
        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        if (this.isAdded()) {
            ft.show(this).commitAllowingStateLoss();
        } else {
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.add(this, getClass().getCanonicalName());
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent == chapterListView) {
            TocElement selectedElement = mainTocElement.getElementAt(position, false);
            String path = selectedElement.getPath();
            if (!TextUtils.isEmpty(path)) {
                BookControlCenter.Instance().getCurrentView().jumpLinkHref(path);
                BookControlCenter.Instance().getViewListener().reset();
                BookControlCenter.Instance().getViewListener().repaint();
            }
            dismiss();
        }
    }

    private class ViewPagerAdapter extends PagerAdapter{
        protected List<? extends View> mViewList;
        protected Context mContext;
        private int mCount;

        public ViewPagerAdapter(List<? extends View> mViewList, Context mContext) {
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

}
