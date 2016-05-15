package com.assistant.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.assistant.App;
import com.assistant.R;
import com.assistant.bean.Alarm;
import com.assistant.controll.AlarmClock;
import com.assistant.utils.TransformUtils;
import com.kyleduo.switchbutton.SwitchButton;

import net.tsz.afinal.FinalDb;

import java.util.List;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/4
 * <p>
 * 功能描述 :
 */
public class AlarmAdapter extends BaseAdapter {
    private List<Alarm> alarmList;
    private Context mContext;
    private AlarmClock mAlarmClock;
    private FinalDb finalDb;

    public AlarmAdapter(Context context, List<Alarm> list) {
        mContext = context;
        alarmList = list;
        mAlarmClock = new AlarmClock();
        finalDb = App.getFinalDb();
    }

    public List<Alarm> getAlarmList() {
        return alarmList;
    }

    public void setAlarmList(List<Alarm> alarmList) {
        this.alarmList = alarmList;
    }

    @Override
    public int getCount() {
        return alarmList.size();
    }

    @Override
    public Object getItem(int position) {
        return alarmList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (alarmList == null) {
            return null;
        }
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.alarms_item_layout, null);

            holder.mTime = (TextView) convertView.findViewById(R.id.tv_title_item);
            holder.mDesc = (TextView) convertView.findViewById(R.id.tv_desc_item);
            holder.mTurn = (SwitchButton) convertView.findViewById(R.id.bt_turn_item);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Alarm alarm = alarmList.get(position);
        String hour;
        String minute;
        if (alarm.getHour() < 10) {
            hour = "0" + alarm.getHour();
        } else {
            hour = alarm.getHour() + "";
        }
        if (alarm.getMinute() < 10) {
            minute = "0" + alarm.getMinute();
        } else {
            minute = alarm.getMinute() + "";
        }

        holder.mTime.setText(hour + " : " + minute);

        boolean activated = alarm.isActivate();
        updateAlarmItem(holder, activated, alarm);
        holder.mTurn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // 开启闹钟
                alarm.setActivate(true);
                updateAlarmItem(holder, true, alarm);
            } else {
                alarm.setActivate(false);
                updateAlarmItem(holder, false, alarm);
            }
            mAlarmClock.turnAlarm(alarm);
            // 将需要更新的对象发送给Fragment进行更新
            finalDb.update(alarm);
        });
        return convertView;
    }

    /**
     * 根据 isAlarmOn的 状态来初始化的 desc 显示的内容
     *
     * @param holder
     * @param isAlarmOn
     * @param alarm
     */
    private void updateAlarmItem(ViewHolder holder, boolean isAlarmOn, Alarm alarm) {
        holder.mTurn.setChecked(isAlarmOn);
        String desc;
        if (isAlarmOn) {
            String dayOfWeek = alarm.getDayOfWeek();
            if (dayOfWeek.equals("8")) {
                desc = "开启   一次性闹钟";
            } else if (dayOfWeek.equals("1,2,3,4,5,")) {
                desc = "开启   工作日";
            } else if (dayOfWeek.equals("0,1,2,3,4,5,6,")) {
                desc = "开启   每天";
            } else if (dayOfWeek.equals("0,6,")) {
                desc = "开启   周末";
            } else {
                StringBuffer days = transform(dayOfWeek);
                desc = "开启   每周" + days + "重复";
            }
            holder.mDesc.setText(desc);
            holder.mTime.setTextColor(Color.BLACK);
            holder.mDesc.setTextColor(Color.BLACK);
        } else {
            desc = "关闭";
            holder.mDesc.setText(desc);
            holder.mTime.setTextColor(Color.GRAY);
            holder.mDesc.setTextColor(Color.GRAY);
        }
    }

    private StringBuffer transform(String dayOfWeek) {
        String[] days = new String[]{"日", "一", "二", "三", "四", "五", "六"};
        int[] repeater = TransformUtils.getIntsDayOfWeek(dayOfWeek);
        StringBuffer text = new StringBuffer();
        for (int i = 0; i < repeater.length; i++) {
            text.append(days[repeater[i]]);
            text.append(" ");
        }
        return text;
    }

    static class ViewHolder {
        TextView mTime;
        TextView mDesc;
        SwitchButton mTurn;
    }
}
