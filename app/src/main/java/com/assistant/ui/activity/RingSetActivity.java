package com.assistant.ui.activity;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.assistant.R;
import com.assistant.adapter.RingAdapter;
import com.assistant.utils.ConstUtils;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;

import butterknife.Bind;

public class RingSetActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.lv_ring)
    ListView lvRing;
    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;

    private String[] ringName = new String[]{"Everybody", "荆棘鸟", "加勒比海盗", "圣斗士(慎点)",
            "Flower", "Time Travel", "Thank you for", "律动", "Morning", "Echo", "Alarm Clock"};

    private String[] ringId = new String[]{"everybody.mp3", "bird.mp3", "galebi.mp3", "shendoushi.mp3",
            "flower.mp3", "timetravel.mp3", "thankufor.mp3", "mx1.mp3", "mx2.mp3", "echo.mp3", "clock.mp3"};

    private ArrayList<String> ringNameList;
    private ArrayList<String> ringIDList;

    private RingAdapter ringAdapter;
    private MediaPlayer mediaPlayer;

    private String mRingName;
    private String mRingId;

    private int currentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initToolbar();
        initData();
        initAdapter();
        initListener();
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        lvRing.setOnItemClickListener((parent, view, position, id) -> {
            mRingName = ringNameList.get(position);
            mRingId = ringIDList.get(position);
            currentItem = position;
            ringAdapter.setCurrentItem(currentItem);
            ringAdapter.notifyDataSetChanged();
            ringTheSong(position);
        });
    }

    /**
     * 播放该位置的音乐
     *
     * @param position
     */
    private void ringTheSong(int position) {

        AssetFileDescriptor assetFileDescriptor;

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        } else {
            mediaPlayer.reset();
        }
        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            if (position == 0 && !ringNameList.get(position).equals(ringName[0])) {
                mediaPlayer.setDataSource(ringIDList.get(0));
            } else {
                assetFileDescriptor = this.getAssets().openFd(ringIDList.get(position));

                mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(),
                        assetFileDescriptor.getStartOffset(),
                        assetFileDescriptor.getLength());
            }
            mediaPlayer.setVolume(1f, 1f);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void finishActivity() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }
        finish();
    }

    private void stopTheSong() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }
    }

    /**
     * 初始化适配器
     */
    private void initAdapter() {
        showProgressWheel(true);

        for (int i = 0; i < ringName.length; i++) {
            ringNameList.add(ringName[i]);
            ringIDList.add(ringId[i]);
        }
        ringAdapter = new RingAdapter(this, ringNameList, currentItem);
        lvRing.setAdapter(ringAdapter);

        showProgressWheel(false);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        parseIntent();
        mRingName = ringName[0];
        mRingId = ringId[0];
        ringNameList = new ArrayList<>();
        ringIDList = new ArrayList<>();
    }

    /**
     * 解析Intent，获取已经存在的闹钟的铃音ID
     */
    private void parseIntent() {
        String getRingId = getIntent().getStringExtra("currentRingId");
        if (getRingId.equals("0")) {
            // 如果闹钟为新建的
            currentItem = 0;
        } else {
            for (int i = 0; i < ringId.length; i++) {
                if (ringId[i].equals(getRingId)) {
                    currentItem = i;
                    break;
                }
            }
        }
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
    protected int getLayoutView() {
        return R.layout.activity_ring_set;
    }

    @Override
    protected void initToolbar() {
        super.initToolbar(toolbar);
        toolbar.setTitle(R.string.set_alarm_ring);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_set_ring, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_custom_ring:
                stopTheSong();
                startActivityForResult(new Intent(this, CustomRingSetActivity.class), ConstUtils.SET_MY_RING_ALARM);
                break;
            case R.id.action_done_ring:
                saveRing();
                break;
            case android.R.id.home:
                // 不保存设置的铃音
                cancelRing();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // 按下返回键的时候取消设置的铃音
                cancelRing();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            ringNameList.add(0, data.getStringExtra("RingName"));
            ringIDList.add(0, data.getStringExtra("RingPath"));
            currentItem = 0;
            ringAdapter.setCurrentItem(currentItem);
            mRingName = ringNameList.get(0);
            mRingId = ringIDList.get(0);
            ringAdapter.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void cancelRing() {
        setResult(ConstUtils.SET_ALARM_CANCEL, new Intent());
        finishActivity();
    }

    private void saveRing() {
        Intent intent = new Intent();
        // 将用户设置的闹钟铃音的 名字和 资源ID 通过intent传递到目标页面
        intent.putExtra("SongName", mRingName);
        intent.putExtra("SongId", mRingId);
        setResult(ConstUtils.SET_ALARM_DONE, intent);
        finishActivity();
    }
}
