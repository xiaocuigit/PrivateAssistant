package com.assistant.receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.assistant.App;
import com.assistant.service.LockPhoneService;
import com.assistant.utils.ConstUtils;
import com.orhanobut.logger.Logger;

import de.greenrobot.event.EventBus;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/27
 * <p>
 * 功能描述 :
 */
public class PhoneStateReceiver extends BroadcastReceiver {
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.e("PhoneStateReceiver");
        mContext = context;
        App.setRingState(ConstUtils.COMING_RING);
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);


    }

    PhoneStateListener listener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    if (App.getPhoneState() == ConstUtils.LOCK_STATE){
                        // 如果打完电话手机还处于锁定期间，则锁屏
                        EventBus.getDefault().post(LockPhoneService.ServiceEvent.LOCK_SCREEN);
                    }

                    Toast.makeText(mContext, "挂断", Toast.LENGTH_SHORT).show();
                    Logger.d("***挂断***");
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Toast.makeText(mContext, "接听", Toast.LENGTH_SHORT).show();
                    Logger.d("接听");
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Toast.makeText(mContext, "来电", Toast.LENGTH_SHORT).show();
                    Logger.d("***来电***");
                    break;
            }
        }
    };
}
