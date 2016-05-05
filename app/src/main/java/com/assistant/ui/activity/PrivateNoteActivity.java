package com.assistant.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.assistant.App;
import com.assistant.R;
import com.assistant.ui.fragment.NoteFragment;
import com.assistant.utils.ConstUtils;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

public class PrivateNoteActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initToolbar();
        initFragment();
    }

    private void initFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_content);
        if (fragment == null) {
            fragment = NoteFragment.newInstance();
            fm.beginTransaction()
                    .add(R.id.fragment_content, fragment)
                    .commit();
        }
    }

    /**
     * 关闭 PrivateNoteActivity 前的操作
     */
    private void closeActivity() {
        // 当用户按下返回键后，通知主页显示正常的笔记
        App.setNoteType(ConstUtils.NORMAL_NOTE);
        // 刷新下界面显示的笔记
        EventBus.getDefault().post(NoteFragment.NoteEvent.UPDATE_NOTE);
        // 进入主页
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                closeActivity();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                closeActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_private_note;
    }

    @Override
    protected void initToolbar() {
//        ToolbarUtils.initToolbar(toolbar, this);
        super.initToolbar(toolbar);
        toolbar.setTitle(R.string.private_note_title);
    }
}
