package com.boyad.epubreader.view.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.boyad.epubreader.BookControlCenter;
import com.boyad.epubreader.BookReadControlCenter;
import com.boyad.epubreader.R;
import com.boyad.epubreader.util.BookAttributeUtil;
import com.boyad.epubreader.util.BookBrightUtil;
import com.boyad.epubreader.util.BookContentDrawHelper;
import com.boyad.epubreader.util.BookSettings;
import com.boyad.epubreader.util.BookUIHelper;
import com.boyad.epubreader.util.MyReadLog;

/**
 * Created by Boyad on 2018/1/17.
 */

public class ReaderMenuDialog extends DialogFragment implements SeekBar.OnSeekBarChangeListener {
    private static final float UNENABLE_ALPHA = 0.5F;
    private static final float ENABLE_ALPHA = 1F;
    protected FragmentActivity mActivity;
    private Dialog mDialog;
    private DialogInterface.OnCancelListener mCancelListener;
    private DialogInterface.OnDismissListener mDismissListener;
    private SeekBar chapterSeekBar;
    private View fontSizeView;
    private View titleView;
    private View brightnessView;
    private View otherView;
    private View mainView;
    private TextView largerFontSizeBtn;
    private TextView smallerFontSizeBtn;
    private int fontSize;
    private boolean isDayModel;
    private ImageView dayModelBtn;
    private TextView systemBrightBtn;
    private SeekBar brightnessSeekBar;
    private int currentBrightness;
    private boolean isBrightAuto;
    private BookChapterMarkDialog chapterMarkDialog;

    public ReaderMenuDialog() {
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            window.setLayout(getWindowWidth(), getWindowHeight());

        }
    }

    public ReaderMenuDialog initWithContext(Context context) {
        if (context instanceof FragmentActivity) {
            mActivity = (FragmentActivity) context;
//            BookUIHelper.setForceStatusBarColor(mActivity, context.getResources().getColor(R.color.menu_dark_gray_bg));
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

    private Dialog onCreateDialog(FragmentActivity mActivity, Bundle savedInstanceState) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_read_book_menu, null);
        Dialog dialog = new Dialog(mActivity, R.style.FloatTranslucentStyle);
        initMainView(view);
        initChapterView(view);
        initFontSizeView(view);
        initBrightnessView(view);
        initOtherView(view);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    private void initOtherView(View view) {
        BookUIHelper.setNotFastClickListener(view.findViewById(R.id.menu_other_screen_direction_btn), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int requestedOrientation = mActivity.getRequestedOrientation();
                MyReadLog.i("requestedOrientation = " +requestedOrientation);
                if(requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    BookSettings.setScreenDirection(true);
                } else if(requestedOrientation ==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    BookSettings.setScreenDirection(false);
                }
                mActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_FULLSCREEN|
                                                                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|
                                                                            View.SYSTEM_UI_FLAG_IMMERSIVE);
                dismiss();
            }
        });
    }

    private void initBrightnessView(View view) {
        systemBrightBtn = (TextView) view.findViewById(R.id.menu_brightness_system_setting_btn);
        BookUIHelper.setNotFastClickListener(systemBrightBtn, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBrightAuto = !isBrightAuto;
                if (isBrightAuto) {
                    BookBrightUtil.startAutoBrightness(mActivity);
                } else {
//                    BookBrightUtil.stopAutoBrightness(mActivity);
                    BookBrightUtil.setBrightness(mActivity, currentBrightness);
                }
                if (isDayModel) {
                    BookSettings.setBrightnessAutoDay(isBrightAuto);
                } else {
                    BookSettings.setBrightnessAutoNight(isBrightAuto);
                }
                systemBrightBtn.setTextColor(mActivity.getResources().getColor(isBrightAuto ? R.color.default_red : R.color.default_gray));
            }
        });
        brightnessSeekBar = (SeekBar) view.findViewById(R.id.menu_brightness_seek_bar);
        brightnessSeekBar.setMax(BookBrightUtil.BOOK_READ_MAX_BRIGHTNESS - BookBrightUtil.BOOK_READ_MIN_BRIGHTNESS);
        brightnessSeekBar.setOnSeekBarChangeListener(this);
    }

    private void initFontSizeView(View view) {
        largerFontSizeBtn = (TextView) view.findViewById(R.id.menu_font_size_larger_btn);
        smallerFontSizeBtn = (TextView) view.findViewById(R.id.menu_font_size_smaller_btn);
        BookUIHelper.setNotFastClickListener(largerFontSizeBtn, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFontSize(true);
            }
        });

        BookUIHelper.setNotFastClickListener(smallerFontSizeBtn, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFontSize(false);
            }
        });
    }

    private void updateFontSize(boolean larger) {
            if (larger && fontSize == BookAttributeUtil.MIN_SETTING_FONT_SIZE) {
                smallerFontSizeBtn.setEnabled(true);
                smallerFontSizeBtn.setAlpha(ENABLE_ALPHA);
            } else if (!larger && fontSize == BookAttributeUtil.MAX_SETTING_FONT_SIZE) {
                largerFontSizeBtn.setEnabled(true);
                largerFontSizeBtn.setAlpha(ENABLE_ALPHA);
            }
            fontSize = fontSize + (larger ? 2 : -2);
            if (fontSize == BookAttributeUtil.MAX_SETTING_FONT_SIZE) {
                largerFontSizeBtn.setEnabled(false);
                largerFontSizeBtn.setAlpha(UNENABLE_ALPHA);
            } else if (fontSize == BookAttributeUtil.MIN_SETTING_FONT_SIZE) {
                smallerFontSizeBtn.setEnabled(false);
                smallerFontSizeBtn.setAlpha(UNENABLE_ALPHA);
            }

            MyReadLog.i("updateFontSize");
            BookSettings.setFontSize(fontSize);
            BookAttributeUtil.setEmSize(fontSize);
            BookControlCenter.Instance().getCurrentView().preparePage(null);
            BookControlCenter.Instance().getViewListener().reset();
            BookControlCenter.Instance().getViewListener().repaint();
    }

    private void initChapterView(View view) {
    }

    /**
     * 初始化主菜单
     */
    private void initMainView(View view) {
        titleView = view.findViewById(R.id.menu_top_title_view);
        mainView = view.findViewById(R.id.menu_main_view);
        BookUIHelper.setNotFastClickListener(view.findViewById(R.id.menu_empty_view), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        BookUIHelper.setNotFastClickListener(view.findViewById(R.id.menu_exit_btn), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                mActivity.finish();
            }
        });

        BookUIHelper.setNotFastClickListener(view.findViewById(R.id.menu_mark_btn), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BookReadControlCenter) BookControlCenter.Instance()).addBookMark();
                dismiss();
            }
        });

        BookUIHelper.setNotFastClickListener(view.findViewById(R.id.menu_search_btn), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        dayModelBtn = (ImageView) view.findViewById(R.id.menu_day_model_btn);
        BookUIHelper.setNotFastClickListener(dayModelBtn, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDayModel();
            }
        });

        BookUIHelper.setNotFastClickListener(view.findViewById(R.id.menu_previous_chapter_btn), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyReadLog.i("上一章");
            }
        });

        BookUIHelper.setNotFastClickListener(view.findViewById(R.id.menu_next_chapter_btn), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyReadLog.i("下一章");
            }
        });

        chapterSeekBar = (SeekBar) view.findViewById(R.id.menu_chapter_seek_bar);
        chapterSeekBar.setOnSeekBarChangeListener(this);

        BookUIHelper.setNotFastClickListener(view.findViewById(R.id.menu_chapter_list_btn), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyReadLog.i("打开目录明细");
                if (chapterMarkDialog == null) {
                    chapterMarkDialog = new BookChapterMarkDialog();
                    chapterMarkDialog.initWithContext(mActivity);
                }
                dismiss();
                chapterMarkDialog.showDialog();
            }
        });

        fontSizeView = view.findViewById(R.id.menu_font_size_view);
        BookUIHelper.setNotFastClickListener(view.findViewById(R.id.menu_font_size_btn), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//                mDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

                titleView.setVisibility(View.GONE);
                mainView.setVisibility(View.GONE);
                fontSizeView.setVisibility(View.VISIBLE);
            }
        });

        brightnessView = view.findViewById(R.id.menu_brightness_view);
        BookUIHelper.setNotFastClickListener(view.findViewById(R.id.menu_brightness_btn), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                mDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

                titleView.setVisibility(View.GONE);
                mainView.setVisibility(View.GONE);
                brightnessView.setVisibility(View.VISIBLE);
            }
        });

        otherView = view.findViewById(R.id.menu_other_view);
        BookUIHelper.setNotFastClickListener(view.findViewById(R.id.menu_other_btn), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                mDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                titleView.setVisibility(View.GONE);
                mainView.setVisibility(View.GONE);
                otherView.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * 修改白天模式
     */
    private void changeDayModel() {
        isDayModel = !isDayModel;

        if (isDayModel) {
            isBrightAuto = BookSettings.isBrightnessAutoDay();
            currentBrightness = BookSettings.getBrightnessDay();
        } else {
            isBrightAuto = BookSettings.isBrightnessAutoNight();
            currentBrightness = BookSettings.getBrightnessNight();
        }
        if (isBrightAuto) {
            BookBrightUtil.startAutoBrightness(mActivity);
        } else {
//            BookBrightUtil.stopAutoBrightness(mActivity);
            BookBrightUtil.setBrightness(mActivity, currentBrightness);
        }
        systemBrightBtn.setTextColor(mActivity.getResources().getColor(isBrightAuto ? R.color.default_red : R.color.default_gray));
        brightnessSeekBar.setProgress(currentBrightness + BookAttributeUtil.MIN_SETTING_FONT_SIZE);

        BookSettings.setDayModel(isDayModel);
        BookContentDrawHelper.setDayModel(isDayModel);
        dayModelBtn.setImageDrawable(mActivity.getResources().getDrawable(!isDayModel ? R.drawable.icon_day_model : R.drawable.icon_night_model));
        BookControlCenter.Instance().getViewListener().reset();
        BookControlCenter.Instance().getViewListener().repaint();
    }


    private void updateBeforeShow() {
//        BookUIHelper.setForceStatusBarColor(mActivity, mActivity.getResources().getColor(R.color.menu_dark_gray_bg));
//        mDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        int uiOptions = mActivity.getWindow().getDecorView().getSystemUiVisibility();
//        MyReadLog.i("uiOptions = " + uiOptions);
        uiOptions = uiOptions^View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOptions = uiOptions^View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        uiOptions = uiOptions^View.SYSTEM_UI_FLAG_IMMERSIVE;
//        MyReadLog.i(" later uiOptions = " + uiOptions);
        mActivity.getWindow().getDecorView().setSystemUiVisibility(uiOptions);

        Animation up2DownAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,0,
                Animation.RELATIVE_TO_SELF, -1,Animation.RELATIVE_TO_SELF, 0);
        up2DownAnimation.setDuration(300);

        Animation down2UpAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,0,
                Animation.RELATIVE_TO_SELF, 2,Animation.RELATIVE_TO_SELF, 1);
        up2DownAnimation.setDuration(300);
        titleView.setVisibility(View.VISIBLE);
        mainView.setVisibility(View.VISIBLE);
        titleView.setAnimation(up2DownAnimation);
        mainView.setAnimation(down2UpAnimation);

        fontSizeView.setVisibility(View.GONE);
        brightnessView.setVisibility(View.GONE);
        otherView.setVisibility(View.GONE);

        if (fontSize == 0) {
            fontSize = BookSettings.getFontSize();
            if (fontSize == BookAttributeUtil.MAX_SETTING_FONT_SIZE) {
                largerFontSizeBtn.setEnabled(false);
                largerFontSizeBtn.setAlpha(UNENABLE_ALPHA);
            } else if (fontSize == BookAttributeUtil.MIN_SETTING_FONT_SIZE) {
                smallerFontSizeBtn.setEnabled(false);
                smallerFontSizeBtn.setAlpha(UNENABLE_ALPHA);
            }
        }

        isDayModel = BookSettings.getDayModel();
        dayModelBtn.setImageDrawable(mActivity.getResources().getDrawable(!isDayModel ? R.drawable.icon_day_model : R.drawable.icon_night_model));

        if (isDayModel) {
            isBrightAuto = BookSettings.isBrightnessAutoDay();
            currentBrightness = BookSettings.getBrightnessDay();
        } else {
            isBrightAuto = BookSettings.isBrightnessAutoNight();
            currentBrightness = BookSettings.getBrightnessNight();
        }
        systemBrightBtn.setTextColor(mActivity.getResources().getColor(isBrightAuto ? R.color.default_red : R.color.default_gray));
        brightnessSeekBar.setProgress(currentBrightness - BookAttributeUtil.MIN_SETTING_FONT_SIZE);
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


    private int getWindowHeight() {
        return ViewGroup.LayoutParams.MATCH_PARENT;
    }

    private int getWindowWidth() {
        return ViewGroup.LayoutParams.MATCH_PARENT;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar == brightnessSeekBar) {
            currentBrightness = seekBar.getProgress() + BookBrightUtil.BOOK_READ_MIN_BRIGHTNESS;
            BookBrightUtil.setBrightness(mActivity, currentBrightness);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (seekBar == brightnessSeekBar) {
            if (isBrightAuto) {
                systemBrightBtn.setTextColor(mActivity.getResources().getColor(R.color.default_gray));
//                BookBrightUtil.stopAutoBrightness(mActivity);
                if (isDayModel) {
                    BookSettings.setBrightnessAutoDay(false);
                } else {
                    BookSettings.setBrightnessAutoNight(false);
                }
            }
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar == brightnessSeekBar) {
            MyReadLog.i(" brightness ---> onStopTrackingTouch");
            if (isDayModel) {
                BookSettings.setBrightnessDay(currentBrightness);
            } else {
                BookSettings.setBrightnessNight(currentBrightness);
            }
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
//        mDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//        mDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);

    }

}
