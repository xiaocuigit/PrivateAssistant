package com.assistant.service;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Vibrator;

import com.assistant.App;
import com.assistant.controll.PowerKeyControl;
import com.assistant.receiver.AdminReceiver;
import com.assistant.receiver.PhoneStateReceiver;
import com.assistant.ui.activity.LockPhoneActivity;
import com.assistant.utils.ConstUtils;
import com.orhanobut.logger.Logger;

import de.greenrobot.event.EventBus;

public class LockPhoneService extends Service {

    private PowerKeyControl mPowerKeyControl;
    private DevicePolicyManager mDeviceManager;
    private ComponentName mComponentName;
    private PhoneStateReceiver receiver;
    private Vibrator vibrator;

    public LockPhoneService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        initPhoneStateReceiver();
        initDeviceManager();
        initPowerKeyListener();
        lockScreen();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d("onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        if (receiver != null) {
            // 注销接听电话的监听器
            unregisterReceiver(receiver);
        }
        // 注销电源按钮的监听器
        mPowerKeyControl.stopListener();
    }

    public void onEventMainThread(ServiceEvent event) {
        switch (event) {
            case LOCK_SCREEN:
                App.setRingState(ConstUtils.OFF_RING);
                lockScreen();
                break;
        }
    }

    public enum ServiceEvent {
        LOCK_SCREEN
    }

    /**
     * 注册接听电话的监听器
     */
    private void initPhoneStateReceiver() {
        receiver = new PhoneStateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        registerReceiver(receiver, intentFilter);
    }

    /**
     * 初始化设备管理器
     */
    private void initDeviceManager() {
        mDeviceManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(this, AdminReceiver.class);
    }

    /**
     * 初始化电源按钮监听器
     */
    private void initPowerKeyListener() {
        mPowerKeyControl = new PowerKeyControl(this);
        mPowerKeyControl.initScreenOnListener();
        mPowerKeyControl.initScreenOffListener();
        mPowerKeyControl.setPowerKeyListener(new PowerKeyControl.OnPowerKeyListener() {
            @Override
            public void onPowerKeyScreenOn() {
                // 当电源键按下，屏幕开启后的操作
                Logger.d("********屏幕打开*******");
                if (App.getRingState() == ConstUtils.COMING_RING) {
                    Logger.d("有来电");
                } else {
                    startVibrate();
                    Intent intent = new Intent(LockPhoneService.this, LockPhoneActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    new Thread(() -> {
                        try {
                            Thread.sleep(1000);
                            Logger.d("延时1秒锁屏");
                            // 重新锁屏
                            lockScreen();
                            EventBus.getDefault().post(LockPhoneActivity.LockPhoneActivityEvent.DESTROY_ACTIVITY);

                        } catch (InterruptedException e) {
                            Logger.d("Thread error");
                            e.printStackTrace();
                        }
                    }).start();
                }

            }

            @Override
            public void onPowerKeyScreenOff() {
                stopVibrate();
                Logger.d("屏幕关闭");
                if (App.getPhoneState() == ConstUtils.LOCK_STATE) {
                    EventBus.getDefault().post(LockPhoneActivity.LockPhoneActivityEvent.DESTROY_ACTIVITY);
                }
            }
        });
    }

    /**
     * 锁屏
     */
    private void lockScreen() {
        if (mDeviceManager.isAdminActive(mComponentName)) {
            mDeviceManager.lockNow();
            Logger.d("锁屏");
        }
    }

    /**
     * 控制手机振动
     */
    private void startVibrate() {
        boolean isVibrate = App.getPreferenceUtils().getBooleanParam(ConstUtils.IS_LOCK_PHONE_VIBRATE, true);
        if (isVibrate) {
            // 如果用户设置了该选项则振动
            Logger.d("手机开始振动");
            vibrator = (Vibrator) App.getContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(new long[]{500, 1000}, 0);
        }
    }

    /**
     * 取消手机振动
     */
    private void stopVibrate() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

}
