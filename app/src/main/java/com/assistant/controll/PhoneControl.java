package com.assistant.controll;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.assistant.App;
import com.assistant.bean.TimeItem;
import com.assistant.bean.UnLockTime;
import com.assistant.receiver.UnLockPhoneReceiver;
import com.assistant.utils.ConstUtils;
import com.orhanobut.logger.Logger;

import java.util.Calendar;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/25
 * <p>
 * 功能描述 : 用来控制手机强制锁定。
 */
public class PhoneControl {

    private Context mContext;

    public PhoneControl() {
        this.mContext = App.getContext();
    }

    /**
     * 一旦设置了一个闹钟，如果不取消将会一直存在。
     *
     * @param hour
     * @param minute
     */
    public void forceLockPhone(UnLockTime unLockTime) {
        // 在这里启动一个后台服务，用来检测锁定的时间是否到达。如果手机还处于锁定状态，当用户按下电源键后显示锁定界面
        // 如果锁定时间已经超出，则销毁这个后台服务。手机可以正常使用。
        // 当前的时间加上设置的锁定时间，得到一个未来的时间，然后，当时间到后发送一个通知。
        Logger.d("强制控制手机");
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(mContext, UnLockPhoneReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("UnLockTime", unLockTime);
        intent.putExtras(bundle);
        intent.setAction("com.assistant.UNLOCK_PHONE");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                ConstUtils.PHONE_STATE_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (App.getPhoneState() == ConstUtils.LOCK_STATE) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, unLockTime.getHour());
            calendar.set(Calendar.MINUTE, unLockTime.getMinute());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                long dayOfMill = 1000 * 60 * 60 * 24;
                if (Build.VERSION.SDK_INT >= 19) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + dayOfMill, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + dayOfMill, pendingIntent);
                }
            } else {
                if (Build.VERSION.SDK_INT >= 19) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }

        } else {
            alarmManager.cancel(pendingIntent);
        }
    }
}
