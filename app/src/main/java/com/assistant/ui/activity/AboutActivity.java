package com.assistant.ui.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.assistant.R;
import com.assistant.utils.ConstUtils;

import butterknife.Bind;
import butterknife.OnClick;

public class AboutActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.version_text)
    TextView versionText;
    @Bind(R.id.project_home_btn)
    Button projectHomeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVersionText();
    }

    private void initVersionText() {
        versionText.setText("v " + getVersion(this));
    }

    private String getVersion(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "1.0.0";
    }

    @Override
    protected void initToolbar() {
        super.initToolbar(toolbar);
        toolbar.setTitle(R.string.about);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_about;
    }

    @OnClick(R.id.project_home_btn)
    public void onClick() {
        startViewAction(ConstUtils.PROJECT_URL);
    }

    private void startViewAction(String uriStr) {
        try {
            Uri uri = Uri.parse(uriStr);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
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
