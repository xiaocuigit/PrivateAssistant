package com.assistant.ui.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;

import com.assistant.R;
import com.assistant.bean.UnLockTime;
import com.orhanobut.logger.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

/**
 * 手机处于锁定时，用户按电源键后显示的界面
 */
public class LockPhoneActivity extends BaseActivity {

    @Bind(R.id.tv_display_time)
    TextView tvDisplayTime;
    @Bind(R.id.tv_left_unlock_time)
    TextView tvLeftUnlockTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        initUI();
    }

    @Override
    protected void onDestroy() {
        Logger.d("销毁该activity");
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onEventMainThread(LockPhoneActivityEvent event) {
        switch (event) {
            case DESTROY_ACTIVITY:
                finish();
                break;
        }
    }

    public enum LockPhoneActivityEvent {
        DESTROY_ACTIVITY
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: //禁止返回键
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_lock_phone;
    }

    private void initUI() {
        initCurrentTime();
        initLeftLockTime();
    }

    /**
     * 显示剩余的解锁时间
     */
    private void initLeftLockTime() {
        Calendar calendar = Calendar.getInstance();
        int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
        int nowMinute = calendar.get(Calendar.MINUTE);
        int nowTime = nowHour * 60 + nowMinute;

        List<UnLockTime> list = finalDb.findAll(UnLockTime.class);
        UnLockTime mUnLockTime = list.get(0);
        int unLockHour = mUnLockTime.getHour();
        int unLockMinute = mUnLockTime.getMinute();
        int unLockTime = unLockHour * 60 + unLockMinute;

        int time = unLockTime - nowTime;
        String text1;
        String text2 = "后手机将解锁";
        if (time > 0) {
            if (time == 60) {
                text1 = getString(R.string.display_seted_time_3, 1);
            } else if (time < 60) {
                text1 = getString(R.string.display_seted_time_2, time);
            } else {
                text1 = getString(R.string.display_seted_time_1, time / 60, time % 60);
            }
            tvLeftUnlockTime.setText(text1 + text2);
        } else if (time == 0) {
            tvLeftUnlockTime.setText("手机即将解锁");
        } else {
            int realMinute = 24 * 60 - nowTime + unLockTime;

            if (realMinute == 60) {
                text1 = getString(R.string.display_seted_time_3, 1);
            } else if (realMinute < 60) {
                text1 = getString(R.string.display_seted_time_2, realMinute);
            } else {
                text1 = getString(R.string.display_seted_time_1, realMinute / 60, realMinute % 60);
            }
            tvLeftUnlockTime.setText(text1 + text2);
        }
    }

    /**
     * 每分钟刷新一次界面的时间
     */
    private void initCurrentTime() {
        DateFormat mDateFormat = new SimpleDateFormat("HH : mm");
        Date mDate = new Date();
        tvDisplayTime.setText(mDateFormat.format(mDate));
    }

}
