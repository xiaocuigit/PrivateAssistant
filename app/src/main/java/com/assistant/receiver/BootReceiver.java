package com.assistant.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.assistant.App;
import com.assistant.bean.Alarm;
import com.assistant.bean.UnLockTime;
import com.assistant.controll.AlarmClock;
import com.assistant.controll.PhoneControl;
import com.assistant.ui.activity.LoginActivity;
import com.assistant.ui.fragment.PhoneFragment;
import com.assistant.utils.ConstUtils;
import com.orhanobut.logger.Logger;

import net.tsz.afinal.FinalDb;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/7
 * <p>
 * 功能描述 : 当系统重新启动后，检查所有的闹钟，打开已经激活了的闹钟。
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        checkAndStartAlarm(context);
    }

    private void checkAndStartAlarm(Context context) {
        FinalDb finalDb = App.getFinalDb();

        AlarmClock alarmClock = new AlarmClock();
        PhoneControl phoneControl = new PhoneControl();

        List<Alarm> alarmList = finalDb.findAllByWhere(Alarm.class, null, "sort", false);
        List<UnLockTime> unLockTimes = finalDb.findAllByWhere(UnLockTime.class, null);

        for (Alarm alarm : alarmList) {
            if (alarm.isActivate()) {
                Logger.d("重启后，开启闹钟 ID = " + alarm.getId());
                alarmClock.turnAlarm(alarm);
            }
        }

        for (UnLockTime unLockTime : unLockTimes) {
            Logger.d("UnLockTime -- for 循环");
            if (unLockTime.getLockState() == ConstUtils.LOCK_STATE) {
                Logger.d("重启手机后发下手机的状态应该是锁定的，则重新锁定该手机");
                App.setPhoneState(ConstUtils.LOCK_STATE);
                phoneControl.forceLockPhone(unLockTime);
                // 如果手机是锁定状态的，则开机自启动HomeActivity
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                // 告诉PhoneFragment重新启动服务
                EventBus.getDefault().postSticky(PhoneFragment.PhoneFragmentEvent.RESTART_SERVICE);
                break;
            }
        }
    }
}
