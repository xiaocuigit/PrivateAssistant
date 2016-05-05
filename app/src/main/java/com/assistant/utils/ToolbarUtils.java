package com.assistant.utils;

import android.support.v7.widget.Toolbar;

import com.assistant.R;
import com.assistant.ui.activity.BaseActivity;

/**
 * 作者 : xiaocui
 * <p/>
 * 版本 : 1.0
 * <p/>
 * 创建日期 : 2016/3/26
 * <p/>
 * 功能描述 :
 */
public class ToolbarUtils {

    public static void initToolbar(Toolbar toolbar, BaseActivity activity){

        if (toolbar == null || activity == null)
            return;
        toolbar.setBackgroundColor(activity.getColorPrimary());
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextColor(activity.getColorById(R.color.action_bar_title_color));
        toolbar.collapseActionView();
        activity.setSupportActionBar(toolbar);
        if (activity.getSupportActionBar() != null){
            activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
