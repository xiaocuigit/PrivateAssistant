package com.assistant.receiver;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.assistant.bean.Alarm;
import com.assistant.ui.activity.WakeUpActivity;
import com.assistant.utils.TransformUtils;
import com.orhanobut.logger.Logger;

import java.util.Calendar;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/5
 * <p>
 * 功能描述 :
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {

    private Context context;

    private int lazyLevel;
    private int[] repeater;

    private Alarm alarm;

    /**
     * 当接收到 启动闹钟的广播后，判断闹钟是不是重复的，如果是，则启动重复的闹钟模式。
     * 如果不是，则只响一次。
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        Bundle bundle = intent.getExtras();
        alarm = (Alarm) bundle.getSerializable("alarm");
        if (alarm == null) {
            Logger.d("onReceiver -- alarm is null");
            return;
        }
        lazyLevel = alarm.getLazyLevel();
        repeater = TransformUtils.getIntsDayOfWeek(alarm.getDayOfWeek());

        if (repeater[0] == 0) {
            // 如果该闹钟只响一次
            WakeUpPhone();
            ringAlarm();
        } else {
            // 如果该闹钟时重复闹钟，首先判断今天是周几，然后判断用户设置的闹钟里面有没有设置该天响铃。
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int current = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            for (int i = 0; i < repeater.length; i++) {
                Logger.e("repeater" + i + " = " + repeater[i]);
                if (repeater[i] == current) {
                    WakeUpPhone();
                    ringAlarm();
                    //break;
                }
            }
        }
    }

    /**
     * 开启闹钟
     */
    private void ringAlarm() {
        Intent intent = new Intent(context, WakeUpActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("alarm", alarm);
        intent.putExtras(bundle);
        // 为WakeUpActivity创建一个新栈，并添加进去。
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }


    /**
     * 点亮屏幕，并解锁。
     */
    private void WakeUpPhone() {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.FULL_WAKE_LOCK, "WakeLock");
        wakeLock.acquire();

        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("Lock");
        // 关闭系统锁屏界面
        keyguardLock.disableKeyguard();
        wakeLock.release();
    }

}
