package com.assistant.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TimePicker;

import com.assistant.R;
import com.assistant.bean.Alarm;
import com.assistant.ui.fragment.AlarmFragment;
import com.assistant.utils.ConstUtils;
import com.assistant.utils.DialogUtils;
import com.assistant.utils.KeyBoardUtils;
import com.assistant.utils.TransformUtils;
import com.assistant.view.AddAlarmItemView;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/3
 * <p>
 * 功能描述 :
 */
public class AddAlarmActivity extends BaseActivity implements View.OnClickListener {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.tp_set_alarm_time)
    TimePicker timePicker;
    @Bind(R.id.cb_day_7)
    CheckBox cbDay7;
    @Bind(R.id.cb_day_1)
    CheckBox cbDay1;
    @Bind(R.id.cb_day_2)
    CheckBox cbDay2;
    @Bind(R.id.cb_day_3)
    CheckBox cbDay3;
    @Bind(R.id.cb_day_4)
    CheckBox cbDay4;
    @Bind(R.id.cb_day_5)
    CheckBox cbDay5;
    @Bind(R.id.cb_day_6)
    CheckBox cbDay6;

    @Bind(R.id.aiv_alarm_tag)
    AddAlarmItemView aivAlarmTag;

    @Bind(R.id.aiv_ring)
    AddAlarmItemView aivRing;

    @Bind(R.id.aiv_lazy_level)
    AddAlarmItemView aivLazyLevel;

    private Alarm mAlarm;
    private int operatorType = 0;
    private int mHour = 6;
    private int mMinute = 30;
    private int mLazyLevel = 0;
    private boolean activated = true;
    private String mTag = "闹钟";
    private String mRingName = "everybody";
    private String mRingId = "everybody.mp3";
    private List<CheckBox> mRepeater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        parseIntent(getIntent());

        EventBus.getDefault().registerSticky(this);
    }

    /**
     * 接收来自AlarmFragment发送的事件
     *
     * @param alarm
     */
    public void onEventMainThread(Alarm alarm) {
        mAlarm = alarm;
        initCheckBox();
        initToolbar();

        initView();
        initListener();
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_add_alarm;
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    @OnClick({R.id.aiv_alarm_tag, R.id.aiv_lazy_level, R.id.aiv_ring})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.aiv_alarm_tag:
                showTagDialog();
                break;
            case R.id.aiv_lazy_level:
                showLazyLevelDialog();
                break;
            case R.id.aiv_ring:
                Intent intent = new Intent(this, RingSetActivity.class);
                switch (operatorType) {
                    case ConstUtils.ADD_ALARM:
                        intent.putExtra("currentRingId", "0");
                        break;
                    case ConstUtils.UPDATE_ALARM:
                        // 将当前闹钟的铃音ID发送给RingSetActivity
                        intent.putExtra("currentRingId", mAlarm.getRingResId());
                        break;
                }
                startActivityForResult(intent, ConstUtils.SET_RING_ALARM);

                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case ConstUtils.SET_ALARM_DONE:
                mRingName = data.getStringExtra("SongName");
                if (data.getStringExtra("SongId") != null) {
                    mRingId = data.getStringExtra("SongId");
                }
                aivRing.setDesc(mRingName);
                break;
            case ConstUtils.SET_ALARM_CANCEL:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 当用户按下返回键后，进行判断
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("TAG", "点击");
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showNotSaveAlarmDialog();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 当用户按返回键时弹出对话框提示用户是否保存设置
     */
    private void showNotSaveAlarmDialog() {
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(this);
        builder.setTitle("提示")
                .setMessage(R.string.not_save_alarm_leave_tip)
                .setPositiveButton(R.string.sure, (dialog1, which) -> {
                    saveAlarm();
                })
                .setNegativeButton(R.string.cancel, (dialog, which1) -> {
                    finish();
                });
        builder.show();
    }

    /**
     * 通过对话框来提示用户输入赖床等级
     */
    private void showLazyLevelDialog() {
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(this);
        builder.setTitle(R.string.lazy_level_title);
        String[] item = new String[]{"本宝宝从不赖床！", "稍微拖延个七八分钟啦~"
                , "半个小时准时起床！", "七点的闹钟八点起~", "闹钟是什么东西？！"};

        if (mAlarm != null) {
            mLazyLevel = mAlarm.getLazyLevel();
        }
        builder.setSingleChoiceItems(item, mLazyLevel, (dialog, which) -> {
            mLazyLevel = which;
            aivLazyLevel.setDesc("赖床指数" + which + "级");
            dialog.dismiss();
        });
        builder.show();
    }

    /**
     * 提示用户输入标签
     */
    private void showTagDialog() {

        AlertDialog.Builder builder = DialogUtils.makeDialogBuilderByTheme(this);
        builder.setTitle(R.string.set_alarm_tag);
        View view = View.inflate(this, R.layout.dialog_tag, null);
        AppCompatEditText etTag = (AppCompatEditText) view.findViewById(R.id.et_tag);
        Button btnSure = (Button) view.findViewById(R.id.btn_sure);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);

        KeyBoardUtils.showKeyBoard(this);
        etTag.requestFocus();

        builder.setView(view);

        AlertDialog dialog = builder.create();
        btnSure.setOnClickListener(v1 -> {
            String tagText = etTag.getText().toString();
            if (TextUtils.isEmpty(tagText)) {
                etTag.setError("标签不能为空");
                return;
            }
            mTag = tagText;
            aivAlarmTag.setDesc(mTag);
            KeyBoardUtils.hideKeyBoard(this, etTag);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v1 -> {
            KeyBoardUtils.hideKeyBoard(this, etTag);
            dialog.dismiss();
        });

        dialog.show();
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
                saveAlarm();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getRepeater() {
        String dayRepeater = "";
        if (cbDay1.isChecked()) {
            dayRepeater += "1" + ",";
        }
        if (cbDay2.isChecked()) {
            dayRepeater += "2" + ",";
        }
        if (cbDay3.isChecked()) {
            dayRepeater += "3" + ",";
        }
        if (cbDay4.isChecked()) {
            dayRepeater += "4" + ",";
        }
        if (cbDay5.isChecked()) {
            dayRepeater += "5" + ",";
        }
        if (cbDay6.isChecked()) {
            dayRepeater += "6" + ",";
        }
        if (cbDay7.isChecked()) {
            dayRepeater += "7" + ",";
        }
        if (dayRepeater.equals("")) {
            dayRepeater = "0";
        }
        return dayRepeater;
    }

    /**
     * 保存闹钟
     */
    private void saveAlarm() {
        // 设置重复的日期
        mAlarm.setHour(mHour);
        mAlarm.setMinute(mMinute);
        mAlarm.setSort(mHour * 100 + mMinute);  // 设置排序的参数
        mAlarm.setTag(mTag);
        mAlarm.setRing(mRingName);
        mAlarm.setRingResId(mRingId);
        mAlarm.setLazyLevel(mLazyLevel);
        mAlarm.setDayOfWeek(getRepeater());
        mAlarm.setActivate(activated);          // 设置闹钟的激活状态

        switch (operatorType) {
            case ConstUtils.ADD_ALARM:
                // 保存闹钟
                mAlarm.setUserId(user.getUserId());
                finalDb.saveBindId(mAlarm);
                break;
            case ConstUtils.UPDATE_ALARM:
                // 更新闹钟
                finalDb.update(mAlarm);
                break;
            default:
                break;
        }
        EventBus.getDefault().post(AlarmFragment.AlarmEvent.UPDATE_ALARM);
        finish();
    }

    private void parseIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            operatorType = intent.getIntExtra(ConstUtils.OPERATE_ALARM_TYPE_KEY, 0);
        }
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            mHour = hourOfDay;
            mMinute = minute;
            Logger.d("timepicker hour is " + mHour + "minute is " + mMinute);
        });
        aivLazyLevel.setOnClickListener(this);
        aivAlarmTag.setOnClickListener(this);
        aivRing.setOnClickListener(this);
    }

    /**
     * 初始化界面显示的信息
     */
    private void initView() {
        timePicker.setIs24HourView(true);
        timePicker.setAlpha(0.7f);

        timePicker.setCurrentHour(mHour);
        timePicker.setCurrentMinute(mMinute);
        aivAlarmTag.setDesc(mTag);
        aivLazyLevel.setDesc("赖床指数" + mLazyLevel + "级");
        aivRing.setDesc(mRingName);
    }

    private void initCheckBox() {
        mRepeater = new ArrayList<>();
        mRepeater.add(cbDay1);
        mRepeater.add(cbDay2);
        mRepeater.add(cbDay3);
        mRepeater.add(cbDay4);
        mRepeater.add(cbDay5);
        mRepeater.add(cbDay6);
        mRepeater.add(cbDay7);
    }

    @Override
    protected void initToolbar() {
        super.initToolbar(toolbar);
        switch (operatorType) {
            case ConstUtils.ADD_ALARM:
                // 添加一个新闹钟
                toolbar.setTitle(R.string.new_alarm);
                // 新建一个alarm对象
                mAlarm = new Alarm();
                break;
            case ConstUtils.UPDATE_ALARM:
                // 更新闹钟
                toolbar.setTitle(R.string.edit_alarm);
                initDefaultDate();
                break;
            default:
                break;
        }
    }


    /**
     * 当用户是编辑当前闹钟时，初始化之前的设置到界面上
     */
    private void initDefaultDate() {
        if (mAlarm != null) {

            mHour = mAlarm.getHour();
            mMinute = mAlarm.getMinute();
            mLazyLevel = mAlarm.getLazyLevel();
            mTag = mAlarm.getTag();
            mRingName = mAlarm.getRing();
            mRingId = mAlarm.getRingResId();
            activated = mAlarm.isActivate();

            setMyRepeater();
        }
    }

    /**
     * 初始化 Checkbook
     */
    private void setMyRepeater() {
        int[] days = TransformUtils.getIntsDayOfWeek(mAlarm.getDayOfWeek());
        for (int i = 0; i < days.length; i++) {
            if (days[0] == 0) {
                break;
            } else {
                // 根据用户的设置来初始化重复的天数
                mRepeater.get(days[i] - 1).setChecked(true);
            }
        }
    }

}
