package com.assistant.bean;

import net.tsz.afinal.annotation.sqlite.Id;
import net.tsz.afinal.annotation.sqlite.Table;

import java.io.Serializable;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/3
 * <p>
 * 功能描述 :
 */
@Table(name = "alarm")
public class Alarm implements Serializable {
    @Id(column = "id")
    private int id;
    private int hour;
    private int minute;
    private int sort;               // 用来排序的参数
    private int lazyLevel;          // 赖床等级
    private boolean activate;       // 闹钟当前是否打开
    private String tag;             // 标签
    private String ring;            // 闹钟的铃音
    private String ringResId;       // 铃音的资源文件位置
    private String dayOfWeek;       // 闹钟重复的天数

    private String userId;          // 用来区分不同的用户

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getLazyLevel() {
        return lazyLevel;
    }

    public void setLazyLevel(int lazyLevel) {
        this.lazyLevel = lazyLevel;
    }

    public boolean isActivate() {
        return activate;
    }

    public void setActivate(boolean activate) {
        this.activate = activate;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRing() {
        return ring;
    }

    public void setRing(String ring) {
        this.ring = ring;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getRingResId() {
        return ringResId;
    }

    public void setRingResId(String ringResId) {
        this.ringResId = ringResId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
