package com.assistant.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.assistant.R;
import com.assistant.bean.TimeItem;
import com.kyleduo.switchbutton.SwitchButton;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/24
 * <p>
 * 功能描述 :
 */
public class TimeItemAdapter extends BaseAdapter {
    private List<TimeItem> timeItems;
    private Context context;

    public TimeItemAdapter(Context context, List<TimeItem> timeItems) {
        this.timeItems = timeItems;
        this.context = context;
    }

    public void setTimeItems(List<TimeItem> timeItems) {
        this.timeItems.clear();
        this.timeItems = timeItems;
    }

    @Override
    public int getCount() {
        return timeItems.size();
    }

    @Override
    public Object getItem(int position) {
        return timeItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (timeItems == null) {
            return null;
        }
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.times_item_layout, null);

            holder.mTime = (TextView) convertView.findViewById(R.id.tv_time_item);
            holder.mTurn = (SwitchButton) convertView.findViewById(R.id.bt_turn_item);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        TimeItem timeItem = timeItems.get(position);
        String text = "锁定 ";
        String text1;
        if (timeItem.getHour() == 0) {
            // 只显示分钟
            text1 = context.getString(R.string.display_seted_time_2, timeItem.getMinute());
        } else if (timeItem.getMinute() == 0) {
            // 只显示小时
            text1 = context.getString(R.string.display_seted_time_3, timeItem.getHour());
        } else {
            // 显示小时和分钟
            text1 = context.getString(R.string.display_seted_time_1, timeItem.getHour(), timeItem.getMinute());
        }
        holder.mTime.setText(text + text1);

        holder.mTurn.setChecked(false);
        holder.mTurn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // 如果用户点击激活按钮，然后弹出一个对话框告诉用户信息。
                holder.mTime.setTextColor(Color.BLACK);
                Integer p = position;
                EventBus.getDefault().postSticky(p);
            } else {
                holder.mTime.setTextColor(Color.GRAY);
            }
        });
        return convertView;
    }

    static class ViewHolder {
        TextView mTime;
        SwitchButton mTurn;
    }
}
