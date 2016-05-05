package com.assistant.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.assistant.App;
import com.assistant.R;
import com.assistant.bean.UnLockTime;
import com.assistant.ui.fragment.PhoneFragment;
import com.assistant.utils.ConstUtils;
import com.orhanobut.logger.Logger;

import de.greenrobot.event.EventBus;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/26
 * <p>
 * 功能描述 : 接收手机解锁的广播
 */
public class UnLockPhoneReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        UnLockTime unLockTime = (UnLockTime) bundle.getSerializable("UnLockTime");
        if (unLockTime == null) {
            return;
        }
        unLockTime.setLockState(ConstUtils.NORMAL_STATE);
        App.getFinalDb().update(unLockTime);
        // 设置手机的状态为正常
        App.setPhoneState(ConstUtils.NORMAL_STATE);
        // 发送一个通知
        EventBus.getDefault().post(PhoneFragment.PhoneFragmentEvent.STOP_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_lock_open_black_24dp))
                .setSmallIcon(R.drawable.ic_lock_open_white_24dp)
                .setContentTitle(context.getResources().getString(R.string.notification_title))
                .setContentText(context.getResources().getString(R.string.notification_content))
                .setTicker(context.getResources().getString(R.string.notification_ticker))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true)
                .setOnlyAlertOnce(true);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(unLockTime.getId(), mBuilder.build());

        Logger.d("手机已经解锁");
    }
}
