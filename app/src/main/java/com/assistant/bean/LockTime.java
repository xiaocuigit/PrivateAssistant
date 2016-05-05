package com.assistant.bean;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/26
 * <p>
 * 功能描述 :
 */
public class LockTime {
    private int lockHour;       // 锁定的小时数
    private int lockMinute;     // 锁定的分钟数
    private int currentHour;    // 锁定手机时的小时
    private int currentMinute;  // 锁定手机时的分钟

    public int getLockHour() {
        return lockHour;
    }

    public void setLockHour(int lockHour) {
        this.lockHour = lockHour;
    }

    public int getLockMinute() {
        return lockMinute;
    }

    public void setLockMinute(int lockMinute) {
        this.lockMinute = lockMinute;
    }

    public int getCurrentHour() {
        return currentHour;
    }

    public void setCurrentHour(int currentHour) {
        this.currentHour = currentHour;
    }

    public int getCurrentMinute() {
        return currentMinute;
    }

    public void setCurrentMinute(int currentMinute) {
        this.currentMinute = currentMinute;
    }
}
