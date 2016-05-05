package com.assistant.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.assistant.R;
import com.assistant.ui.fragment.SettingFragment;

import butterknife.Bind;

public class SettingActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setSupportActionBar(toolbar);
//        initToolbar();
        init();
    }

    private void init() {
        SettingFragment fragment = SettingFragment.getInstance(this);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_content, fragment)
                .commit();
    }

    @Override
    protected void initToolbar() {
        super.initToolbar(toolbar);
        toolbar.setTitle(R.string.setting);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_setting;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
