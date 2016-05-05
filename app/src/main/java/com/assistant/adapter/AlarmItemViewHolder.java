package com.assistant.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/3
 * <p>
 * 功能描述 :
 */
public class AlarmItemViewHolder extends RecyclerView.ViewHolder {
    // 显示闹钟时间
    private TextView mTime;
    // 显示重复次数
    private TextView mRepeater;

    public AlarmItemViewHolder(View itemView) {
        super(itemView);
    }
}
