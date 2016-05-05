package com.assistant.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.assistant.R;

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
public class RingAdapter extends BaseAdapter {

    private List<String> listRing;
    private Context context;
    private int mCurrentItem;

    public RingAdapter(Context context, List<String> list, int mCurrentItem) {
        this.context = context;
        this.mCurrentItem = mCurrentItem;
        listRing = list;
    }

    public void setCurrentItem(int mCurrentItem){
        this.mCurrentItem = mCurrentItem;
    }

    @Override
    public int getCount() {
        return listRing.size();
    }

    @Override
    public Object getItem(int position) {
        return listRing.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.item_ringset, null);
            holder.mRingName = (TextView) convertView.findViewById(R.id.tv_name_ring);
            holder.mRadioButton = (RadioButton) convertView.findViewById(R.id.rb_check_ring);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mRingName.setText(listRing.get(position));
        if (mCurrentItem == position) {
            holder.mRadioButton.setChecked(true);
        } else {
            holder.mRadioButton.setChecked(false);
        }
        return convertView;
    }

    class ViewHolder {
        TextView mRingName;
        RadioButton mRadioButton;
    }

}
