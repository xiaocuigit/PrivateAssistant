package com.assistant.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.assistant.R;

import java.util.List;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/3/29
 * <p>
 * 功能描述 :
 */
public class ColorsListAdapter extends BaseListAdapter<Integer> {

    private int checkItem;

    public ColorsListAdapter(List<Integer> list, Context context) {
        super(list, context);
    }

    @Override
    protected View bindView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.colors_image_layout, null);
            holder = new ViewHolder();
            holder.imageView1 = (ImageView) convertView.findViewById(R.id.img_1);
            holder.imageView2 = (ImageView) convertView.findViewById(R.id.img_2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.imageView1.setImageResource(list.get(position));
        if (checkItem == position) {
            holder.imageView2.setImageResource(R.drawable.ic_done_white);
        }
        return convertView;
    }

    public void setCheckItem(int checkItem) {
        this.checkItem = checkItem;
    }

    class ViewHolder {
        ImageView imageView1;
        ImageView imageView2;
    }
}
