package com.assistant.controll;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.assistant.App;
import com.assistant.bean.Alarm;
import com.assistant.receiver.AlarmReceiver;
import com.orhanobut.logger.Logger;

import java.util.Calendar;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/4
 * <p>
 * 功能描述 :
 */
public class AlarmClock {

    private Alarm mAlarm;
    private Context mContext;

    public AlarmClock() {
        mContext = App.getContext();
    }

    /**
     * 更加Alarm 对象的 isAlarmOn 字段来控制闹钟的开关
     *
     * @param alarm
     */
    public void turnAlarm(Alarm alarm) {
        if (alarm == null) {
            return;
        }
        mAlarm = alarm;
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(mContext, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("alarm", mAlarm);
        intent.putExtras(bundle);
        intent.setAction("com.assistant.RING_ALARM");

        Logger.e("alarm id is " + alarm.getId());

        PendingIntent pi = PendingIntent.getBroadcast(mContext, alarm.getId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (alarm.isActivate()) {
            startAlarm(alarmManager, pi);
        } else {
            cancelAlarm(alarmManager, pi);
        }
    }

    private void cancelAlarm(AlarmManager alarmManager, PendingIntent pi) {
        alarmManager.cancel(pi);
    }


    private void startAlarm(AlarmManager alarmManager, PendingIntent pi) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, mAlarm.getHour());
        c.set(Calendar.MINUTE, mAlarm.getMinute());
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        if (c.getTimeInMillis() < System.currentTimeMillis()) {
            // 如果设置的闹钟时间早于当前时间
            long dayOfMill = 1000 * 60 * 60 * 24;
            if (Build.VERSION.SDK_INT >= 19) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis() + dayOfMill, pi);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis() + dayOfMill, pi);
            }
        } else {
            if (Build.VERSION.SDK_INT >= 19) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
            }
        }
    }
}
