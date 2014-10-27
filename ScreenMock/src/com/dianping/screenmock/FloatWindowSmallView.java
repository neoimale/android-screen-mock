package com.dianping.screenmock;

import java.lang.reflect.Field;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class FloatWindowSmallView extends LinearLayout {

    public static int viewWidth;
    public static int viewHeight;
    private static int statusBarHeight;

    private WindowManager windowManager;
    private WindowManager.LayoutParams mParams;

    private float xInScreen;
    private float yInScreen;

    private float xDownInScreen;
    private float yDownInScreen;

    private float xInView;
    private float yInView;

    private View layoutView;

    private int screenWidth;
    private int screenHeight;
    private float screenDensity;

    private boolean isShow;

    public FloatWindowSmallView(Context context) {
        super(context);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.float_window_small, this);
        layoutView = findViewById(R.id.small_window_layout);
        viewWidth = layoutView.getLayoutParams().width;
        viewHeight = layoutView.getLayoutParams().height;
        screenWidth = windowManager.getDefaultDisplay().getWidth();
        screenHeight = windowManager.getDefaultDisplay().getHeight();
        screenDensity = context.getResources().getDisplayMetrics().density;
    }

    public void show() {
        if (!isShow) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.type = WindowManager.LayoutParams.TYPE_PHONE;
            lp.format = PixelFormat.RGBA_8888;
            lp.alpha = 0.6f;
            lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            lp.gravity = Gravity.LEFT | Gravity.TOP;
            lp.width = viewWidth;
            lp.height = viewHeight;
            lp.x = 0;
            lp.y = screenHeight / 2;
            setParams(lp);
            windowManager.addView(this, lp);
            isShow = true;
        }
    }

    public void dismiss() {
        if (isShow) {
            windowManager.removeView(this);
            isShow = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchBegin();
                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY() - getStatusBarHeight();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                break;
            case MotionEvent.ACTION_MOVE:
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                updateViewPosition(false);
                break;
            case MotionEvent.ACTION_UP:
                touchEnd();
                if (Math.abs(xDownInScreen - xInScreen) < 3 * screenDensity
                        && Math.abs(yDownInScreen - yInScreen) < 3 * screenDensity) {
                    openBigWindow();
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void touchBegin() {
        layoutView.getBackground().setAlpha(255);
    }

    private void touchEnd() {
        layoutView.getBackground().setAlpha(80);

        int centerSceenX = screenWidth / 2;
        int x = (int) (xInScreen - xInView);
        int centerViewX = x + viewWidth / 2;
        if (centerViewX < centerSceenX) {
            x = 0;
        } else {
            x = screenWidth - viewWidth;
        }
        xInScreen = x + xInView;
        updateViewPosition(true);
    }

    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    private void updateViewPosition(Boolean animated) {
        mParams.x = (int) (xInScreen - xInView);
        mParams.y = (int) (yInScreen - yInView);
        if (animated) {
            mParams.windowAnimations = 0;
        } else {
            mParams.windowAnimations = 0;
        }
        windowManager.updateViewLayout(this, mParams);
    }

    private void openBigWindow() {
        Intent intent = new Intent("com.dianping.screenmock.MAIN_PANEL");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getContext().startActivity(intent);
    }

    private int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }

}
