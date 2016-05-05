package com.assistant.controll;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.orhanobut.logger.Logger;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/26
 * <p>
 * 功能描述 : 电源键按下后的处理逻辑
 */
public class PowerKeyControl {
    private Context mContext;
    private IntentFilter mIntentFilter;
    private OnPowerKeyListener mOnPowerKeyListener;
    private PowerKeyReceiver mPowerKeyScreenOn;
    private PowerKeyReceiver mPowerKeyScreenOff;

    public PowerKeyControl(Context context) {
        this.mContext = context;
    }

    /**
     * 注册对屏幕打开的监听器
     */
    public void initScreenOnListener() {
        // 当按下电源键，屏幕亮后。
        mIntentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        mPowerKeyScreenOn = new PowerKeyReceiver();
        mContext.registerReceiver(mPowerKeyScreenOn, mIntentFilter);
        Logger.d("电源键按下监听");
    }

    /**
     * 注册对屏幕关闭的监听器
     */
    public void initScreenOffListener() {
        // 当按下电源键，屏幕亮后。
        mIntentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        mPowerKeyScreenOff = new PowerKeyReceiver();
        mContext.registerReceiver(mPowerKeyScreenOff, mIntentFilter);
        Logger.d("电源键按下监听");
    }

    /**
     * 关闭所有的监听器
     */
    public void stopListener() {
        if (mPowerKeyScreenOn != null) {
            mContext.unregisterReceiver(mPowerKeyScreenOn);
            Logger.d("停止对屏幕打开的监听");
        }
        if (mPowerKeyScreenOff != null) {
            mContext.unregisterReceiver(mPowerKeyScreenOff);
            Logger.d("停止对屏幕关闭的监听");
        }
    }

    public void setPowerKeyListener(OnPowerKeyListener powerKeyListener) {
        this.mOnPowerKeyListener = powerKeyListener;
    }

    /**
     * 回调接口
     */
    public interface OnPowerKeyListener {
        void onPowerKeyScreenOn();

        void onPowerKeyScreenOff();
    }

    /**
     * 接收来自电源键按下时发出的广播
     */
    class PowerKeyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                // 如果接收到的是屏幕亮的广播
                mOnPowerKeyListener.onPowerKeyScreenOn();
            }
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                mOnPowerKeyListener.onPowerKeyScreenOff();
            }
        }
    }
}
