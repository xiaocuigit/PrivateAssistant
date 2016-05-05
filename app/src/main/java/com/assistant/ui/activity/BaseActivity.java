package com.assistant.ui.activity;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;

import com.assistant.App;
import com.assistant.R;
import com.assistant.bean.User;
import com.assistant.utils.PreferenceUtils;
import com.assistant.utils.ThemeUtil;
import com.assistant.utils.ThreadPoolUtils;
import com.assistant.utils.ToolbarUtils;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import net.tsz.afinal.FinalDb;

import butterknife.ButterKnife;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/2
 * <p>
 * 功能描述 :
 */
public abstract class BaseActivity extends RxAppCompatActivity {

    public PreferenceUtils preferenceUtils;
    public ThreadPoolUtils threadPoolUtils;
    public FinalDb finalDb;
    public User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferenceUtils = App.getPreferenceUtils();
        threadPoolUtils = App.getThreadPoolUtils();
        finalDb = App.getFinalDb();
        user = App.getUser();

        initTheme();

        super.onCreate(savedInstanceState);

        setContentView(getLayoutView());

        ButterKnife.bind(this);

        initToolbar();
    }

    protected void initTabLayout(TabLayout tabLayout) {
        tabLayout.setBackgroundColor(getColorPrimary());
    }

    protected abstract
    @LayoutRes
    int getLayoutView();

    private void initTheme() {
        ThemeUtil.Theme theme = ThemeUtil.getCurrentTheme(this);
        ThemeUtil.changeTheme(this, theme);
    }

    protected void initToolbar() {
    }

    protected void initToolbar(Toolbar toolbar) {
        ToolbarUtils.initToolbar(toolbar, this);
    }

    public int getColorPrimary() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    public int getColorById(int res) {
        if (res <= 0)
            throw new IllegalArgumentException("resource id can not be less 0");
        return getResources().getColor(res);
    }
}
