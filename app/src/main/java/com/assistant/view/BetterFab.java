package com.assistant.view;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/5/10
 * <p>
 * 功能描述 :
 */
public class BetterFab extends FloatingActionButton {
    private boolean forceHide = false;

    public BetterFab(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BetterFab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BetterFab(Context context) {
        super(context);
    }

    public boolean isForceHide() {
        return forceHide;
    }

    public void setForceHide(boolean forceHide) {
        this.forceHide = forceHide;
        if (!forceHide) {
            setVisibility(VISIBLE);
        } else {
            setVisibility(GONE);
        }
    }

    //if hide，disable animation
    public boolean canAnimation() {
        return !isForceHide();
    }
}
