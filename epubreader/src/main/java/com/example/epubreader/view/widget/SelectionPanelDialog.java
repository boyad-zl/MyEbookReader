package com.example.epubreader.view.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.epubreader.BookControlCenter;
import com.example.epubreader.R;
import com.example.epubreader.util.BookUIHelper;
import com.example.epubreader.util.MyReadLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Boyad on 2018/1/25.
 */

public class SelectionPanelDialog extends DialogFragment {
    ArrayList<Rect> rects;
    private FragmentActivity mActivity;
    private Dialog mDialog;
    private DialogInterface.OnCancelListener mCancelListener;
    private DialogInterface.OnDismissListener mDismissListener;
    private TextView showText;

    public SelectionPanelDialog() {
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

    public SelectionPanelDialog initWithContext(Context context) {
        if (context instanceof FragmentActivity) {
            mActivity = (FragmentActivity) context;
        }
        return this;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mCancelListener != null) mCancelListener.onCancel(dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mDismissListener != null) mDismissListener.onDismiss(dialog);
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

    }

    private Dialog onCreateDialog(FragmentActivity mActivity, Bundle savedInstanceState) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.selection_panel_view, null);
        showText = view.findViewById(R.id.selection_panel_dismiss_btn);
        BookUIHelper.setNotFastClickListener(view, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        BookUIHelper.setNotFastClickListener(showText, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        Dialog dialog = new Dialog(mActivity, R.style.FloatTranslucentDimStyle);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        return dialog;
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

    public void updateSelections(List<Rect> rects) {
        showText.setText("size = " + rects.size());
    }

}
