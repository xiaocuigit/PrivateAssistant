package com.assistant.ui.activity;

import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.assistant.R;
import com.assistant.adapter.RingAdapter;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/4
 * <p>
 * 功能描述 :
 */
public class CustomRingSetActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.lv_ring)
    ListView lvCustomRing;
    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;

    private RingAdapter ringAdapter;
    private MediaPlayer mediaPlayer;

    private List<String> mCustomRingName;
    private List<String> mCustomRingPath;

    private int currentItem;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            initAdapter();
            showProgressWheel(false);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    private void initData() {
        currentItem = 0;

        mediaPlayer = new MediaPlayer();
        showProgressWheel(true);
        try {
            Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                mCustomRingName = new ArrayList<>();
                mCustomRingPath = new ArrayList<>();

                while (cursor.moveToNext()) {
                    String ringName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String ringPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                    mCustomRingName.add(ringName);
                    mCustomRingPath.add(ringPath);
                }
                cursor.close();
                handler.sendEmptyMessage(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initAdapter() {
        if (mCustomRingName != null) {
            ringAdapter = new RingAdapter(this, mCustomRingName, currentItem);
            lvCustomRing.setAdapter(ringAdapter);
            initListener();
        }
    }

    private void initListener() {
        lvCustomRing.setOnItemClickListener((parent, view, position, id) -> {
            ringTheSong(position);
            currentItem = position;
            ringAdapter.setCurrentItem(position);
            ringAdapter.notifyDataSetChanged();
        });
    }

    /**
     * 播放该位置的音乐
     *
     * @param position
     */
    private void ringTheSong(int position) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        } else {
            mediaPlayer.reset();
        }
        try {
            mediaPlayer.setDataSource(mCustomRingPath.get(position));
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setVolume(1f, 1f);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopTheSong() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }
    }

    @Override
    protected void initToolbar() {
        super.initToolbar(toolbar);
        toolbar.setTitle(R.string.set_alarm_my_ring);
    }

    /**
     * 进度条开关设置
     *
     * @param visible 为true则显示进度条
     */
    private void showProgressWheel(boolean visible) {
        progressWheel.setBarColor(getColorPrimary());
        if (visible) {
            if (!progressWheel.isSpinning()) {
                // 进度条开始旋转
                progressWheel.spin();
            }
        } else {
            progressWheel.postDelayed(() -> {

                if (progressWheel.isSpinning()) {
                    progressWheel.stopSpinning();
                }

            }, 300);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ring, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done:
                Intent intent = new Intent();
                intent.putExtra("RingName", mCustomRingName.get(currentItem));
                intent.putExtra("RingPath", mCustomRingPath.get(currentItem));
                setResult(RESULT_OK, intent);
                stopTheSong();
                finish();
                break;
            case android.R.id.home:
                cancelRing();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // 按下返回键后的操作
                cancelRing();
                break;

        }
        return super.onKeyDown(keyCode, event);
    }

    private void cancelRing() {
        stopTheSong();
        setResult(RESULT_CANCELED, new Intent());
        finish();
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_ring_set;
    }
}
